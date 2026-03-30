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

    public CompletableFuture<ItemStack> getSkullTemplateAsync(String playerRef) {
        return skullCache.get(normalizeKey(playerRef));
    }

    public ItemStack getSkullTemplate(String playerRef) {
        try {
            return getSkullTemplateAsync(playerRef).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().warning("[SkinCache] Skull build failed for '"
                    + playerRef + "': " + e.getMessage());
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    public void invalidateAll() {
        skullCache.synchronous().invalidateAll();
    }

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

    private OfflinePlayer resolveOfflinePlayer(String playerRef) {
        if (playerRef == null || playerRef.isBlank()) return null;
        String trimmed = playerRef.trim();

        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(trimmed));
        } catch (IllegalArgumentException ignored) {}

        Player online = Bukkit.getPlayerExact(trimmed);
        if (online != null) return online;

        OfflinePlayer serverCached = Bukkit.getOfflinePlayerIfCached(trimmed);
        if (serverCached != null) return serverCached;

        plugin.getLogger().warning("[SkinCache] Blocking lookup for unknown player: '"
                + trimmed + "' — store UUIDs in your leaderboard source to avoid this.");
        return Bukkit.getOfflinePlayer(trimmed);
    }

    private static String normalizeKey(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }
}