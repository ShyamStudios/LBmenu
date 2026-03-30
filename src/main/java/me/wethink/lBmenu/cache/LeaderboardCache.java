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

/**
 * High-performance leaderboard cache backed by Caffeine's {@link AsyncLoadingCache}.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li><b>AsyncLoadingCache</b> — automatic deduplication of concurrent fetches for
 *       the same key (cache stampede protection). If 50 players open the same board
 *       simultaneously, only <em>one</em> PAPI fetch is dispatched.</li>
 *   <li><b>refreshAfterWrite</b> — after the refresh interval, the <em>next</em> read
 *       triggers an async reload while instantly returning the stale value. Players
 *       never wait for a fetch on a warm cache. No manual BukkitTask needed.</li>
 *   <li><b>expireAfterWrite</b> — hard eviction boundary. Entries are fully removed
 *       after this TTL, forcing a fresh fetch on the next access.</li>
 *   <li><b>Scheduler.systemScheduler()</b> — prompts Caffeine to clean up expired
 *       entries eagerly rather than waiting for the next read/write to trigger
 *       maintenance. Reduces stale memory footprint.</li>
 *   <li><b>Main-thread dispatch</b> — PlaceholderAPI requires main-thread access.
 *       The async loader dispatches a task to the main thread and completes the
 *       {@link CompletableFuture} there.</li>
 * </ul>
 */
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

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns a {@link CompletableFuture} of leaderboard entries for the given board.
     *
     * <p>If the entry is cached and fresh, the future completes immediately (zero cost).
     * If the entry is cached but stale, the stale value is returned instantly while a
     * background refresh is triggered. If the entry is absent, a new fetch is dispatched
     * to the main thread and the future completes when it finishes.
     *
     * <p>Concurrent calls for the same key are automatically coalesced — only one
     * fetch is dispatched regardless of how many callers are waiting.
     */
    public CompletableFuture<List<LeaderboardEntry>> getOrFetchAsync(String holderName) {
        return cache.get(normalize(holderName));
    }

    /**
     * Blocking variant for callers that cannot work with futures.
     * Times out after 5 seconds to avoid hanging the server thread.
     */
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

    /**
     * Evicts all entries. Call on reload or disable.
     */
    public void invalidateAll() {
        cache.synchronous().invalidateAll();
    }

    /**
     * Evicts a single board.
     */
    public void invalidate(String holderName) {
        cache.synchronous().invalidate(normalize(holderName));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Dispatches a PAPI fetch to the main server thread and completes the future there.
     * If already on the main thread (e.g. during a synchronous cache load triggered by
     * a command), executes directly to avoid deadlocking.
     */
    private CompletableFuture<List<LeaderboardEntry>> loadFromMainThread(String key) {
        if (Bukkit.isPrimaryThread()) {
            return CompletableFuture.completedFuture(doFetch(key));
        }

        CompletableFuture<List<LeaderboardEntry>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
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