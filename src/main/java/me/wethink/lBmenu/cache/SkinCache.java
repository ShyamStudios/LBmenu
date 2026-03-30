package me.wethink.lBmenu.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import me.wethink.lBmenu.LBmenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * High-performance player skull cache backed by Caffeine's {@link AsyncLoadingCache}.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li><b>Single cache</b> — the previous implementation used two separate caches
 *       (one for OfflinePlayer resolution, one for skull ItemStacks). This consolidates
 *       them into a single {@code AsyncLoadingCache<String, ItemStack>}, halving memory
 *       overhead, TTL tracking, and eviction bookkeeping.</li>
 *   <li><b>expireAfterAccess</b> — skins are read-heavy and almost never change
 *       mid-session. Using {@code expireAfterAccess} instead of {@code expireAfterWrite}
 *       keeps frequently-used skulls warm and only evicts truly idle entries.</li>
 *   <li><b>AsyncLoadingCache</b> — concurrent requests for the same player's skull
 *       are automatically coalesced. If 10 GUI builds request the same skull
 *       simultaneously, only <em>one</em> build is performed.</li>
 *   <li><b>Scheduler.systemScheduler()</b> — prompt cleanup of expired entries
 *       instead of waiting for the next cache operation.</li>
 * </ul>
 */
public class SkinCache {

    private final LBmenu plugin;
    private final AsyncLoadingCache<String, ItemStack> skullCache;

    public SkinCache(LBmenu plugin) {
        this.plugin = plugin;
        int ttlMinutes = plugin.getGUIConfig().getSkinCacheMinutes();
        int maxSize    = plugin.getGUIConfig().getSkinCacheMaxSize();

        skullCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(ttlMinutes, TimeUnit.MINUTES)
                .scheduler(Scheduler.systemScheduler())
                .buildAsync((key, executor) ->
                        CompletableFuture.supplyAsync(() -> buildSkullTemplate(key), executor));

        plugin.getLogger().info("[SkinCache] TTL=" + ttlMinutes + "min (expireAfterAccess)"
                + "  maxSize=" + maxSize + "  (single AsyncLoadingCache)");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns a {@link CompletableFuture} of the cached skull template for the given player.
     *
     * <p>If the skull is already cached, the future completes immediately.
     * Concurrent requests for the same player are automatically coalesced by Caffeine —
     * only one skull build is ever in-flight per player key.
     *
     * <p><b>Important:</b> The returned ItemStack is the cached template. Callers that
     * need to mutate it (set display name, lore, etc.) <b>must clone</b> it first.
     */
    public CompletableFuture<ItemStack> getSkullTemplateAsync(String playerRef) {
        return skullCache.get(normalizeKey(playerRef));
    }

    /**
     * Blocking variant — returns the cached skull template directly.
     * Falls back to a plain PLAYER_HEAD if the async build fails or times out.
     */
    public ItemStack getSkullTemplate(String playerRef) {
        try {
            return getSkullTemplateAsync(playerRef).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().warning("[SkinCache] Skull build failed for '"
                    + playerRef + "': " + e.getMessage());
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    /**
     * Evicts all cached skulls. Call on reload or disable.
     */
    public void invalidateAll() {
        skullCache.synchronous().invalidateAll();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Builds a skull ItemStack for the given player reference (name or UUID).
     * Called on Caffeine's executor thread — safe for CPU-bound work.
     */
    private ItemStack buildSkullTemplate(String playerRef) {
        OfflinePlayer target = resolveOfflinePlayer(playerRef);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null && target != null) {
            meta.setOwningPlayer(target);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    /**
     * Resolves a player reference to an {@link OfflinePlayer}.
     * Tries fast paths first (UUID parse → online exact → server cache) before
     * falling back to a blocking name-based lookup.
     */
    private OfflinePlayer resolveOfflinePlayer(String playerRef) {
        if (playerRef == null || playerRef.isBlank()) return null;
        String trimmed = playerRef.trim();

        // Fast path 1: UUID string
        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(trimmed));
        } catch (IllegalArgumentException ignored) {}

        // Fast path 2: currently online
        Player online = Bukkit.getPlayerExact(trimmed);
        if (online != null) return online;

        // Fast path 3: server's usercache
        OfflinePlayer serverCached = Bukkit.getOfflinePlayerIfCached(trimmed);
        if (serverCached != null) return serverCached;

        // Slow path: blocking filesystem/DB lookup
        // This only happens for players who have never joined the server.
        plugin.getLogger().warning("[SkinCache] Blocking lookup for unknown player: '"
                + trimmed + "' — store UUIDs in your leaderboard source to avoid this.");
        return Bukkit.getOfflinePlayer(trimmed);
    }

    private static String normalizeKey(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }
}