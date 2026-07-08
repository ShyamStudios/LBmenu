package me.wethink.lBmenu.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.leaderboard.FetcherRegistry;
import me.wethink.lBmenu.leaderboard.LeaderboardEntry;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LeaderboardCache {

    private final LBmenu plugin;
    private final AsyncLoadingCache<String, List<LeaderboardEntry>> cache;

    public LeaderboardCache(LBmenu plugin) {
        this.plugin = plugin;
        int ttl     = plugin.getGUIConfig().getLeaderboardCacheSeconds();
        int maxSize = plugin.getGUIConfig().getLeaderboardCacheMaxSize();
        int refresh = plugin.getGUIConfig().getLeaderboardRefreshSeconds();

        cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl, TimeUnit.SECONDS)
                .refreshAfterWrite(refresh, TimeUnit.SECONDS)
                .scheduler(Scheduler.systemScheduler())
                .buildAsync((key, executor) -> loadFromMainThread(key));

        plugin.getLogger().info("[LeaderboardCache] TTL=" + ttl + "s  refresh=" + refresh
                + "s  maxSize=" + maxSize + "  (AsyncLoadingCache + refreshAfterWrite)");
    }

    public CompletableFuture<List<LeaderboardEntry>> getOrFetchAsync(String holderName) {
        return cache.get(normalize(holderName));
    }

    public List<LeaderboardEntry> getOrFetch(String holderName) {
        try {
            return getOrFetchAsync(holderName).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().warning("[LeaderboardCache] Fetch interrupted for: " + holderName);
            return List.of();
        } catch (Exception e) {
            plugin.getLogger().warning("[LeaderboardCache] Fetch failed for: " + holderName
                    + " — " + e.getMessage());
            return List.of();
        }
    }

    public void invalidateAll() {
        cache.synchronous().invalidateAll();
    }

    public void invalidate(String holderName) {
        cache.synchronous().invalidate(normalize(holderName));
    }

    private CompletableFuture<List<LeaderboardEntry>> loadFromMainThread(String key) {
        if (Bukkit.isPrimaryThread()) {
            return CompletableFuture.completedFuture(doFetch(key));
        }

        CompletableFuture<List<LeaderboardEntry>> future = new CompletableFuture<>();
        plugin.getFoliaLib().getScheduler().runNextTick(task -> {
            try {
                future.complete(doFetch(key));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private List<LeaderboardEntry> doFetch(String key) {
        List<LeaderboardEntry> entries = FetcherRegistry.get().fetch(key, 0);
        return entries.isEmpty() ? List.of() : List.copyOf(entries);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
}