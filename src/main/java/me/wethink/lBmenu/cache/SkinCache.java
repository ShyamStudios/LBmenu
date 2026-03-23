package me.wethink.lBmenu.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.wethink.lBmenu.LBmenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SkinCache {

    private final LBmenu plugin;
    private final Cache<String, OfflinePlayer> playerCache;
    private final Cache<String, ItemStack> skullTemplateCache;

    public SkinCache(LBmenu plugin) {
        this.plugin = plugin;
        int ttlMinutes = plugin.getGUIConfig().getSkinCacheMinutes();
        int maxSize    = plugin.getGUIConfig().getSkinCacheMaxSize();

        playerCache = Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build();

        skullTemplateCache = Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build();
    }

    /**
     * Returns the cached skull template (not cloned).
     * Callers that need to mutate the item must clone it themselves.
     * Called from async thread during GUI pre-build — safe because
     * resolveOfflinePlayer avoids main-thread-only APIs.
     */
    public ItemStack getSkullTemplate(String playerRef) {
        String key = normalizeKey(playerRef);
        return skullTemplateCache.get(key, k -> buildSkullTemplate(playerRef));
    }

    private ItemStack buildSkullTemplate(String playerRef) {
        OfflinePlayer target = playerCache.get(
                normalizeKey(playerRef), k -> resolveOfflinePlayer(playerRef));

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

        // Only reached for players who have never joined — does a blocking
        // filesystem/DB lookup. Log so you know when this is happening.
        plugin.getLogger().warning(
                "[SkinCache] Blocking lookup for unknown player: '" + trimmed +
                        "' — store UUIDs in your leaderboard source to avoid this.");
        return Bukkit.getOfflinePlayer(trimmed);
    }

    private static String normalizeKey(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }

    public void invalidateAll() {
        playerCache.invalidateAll();
        skullTemplateCache.invalidateAll();
    }
}