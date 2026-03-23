package me.wethink.lBmenu.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.leaderboard.FetcherRegistry;
import me.wethink.lBmenu.leaderboard.LeaderboardEntry;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Cache for leaderboard data.
 *
 * <p>Key behaviours:
 * <ul>
 *   <li>First open triggers a blocking main-thread fetch (cold start only).</li>
 *   <li>A background task refreshes known boards on the configured
 *       {@code cache.leaderboard.refresh-seconds} interval (default 240 s)
 *       so the cache is never cold on subsequent opens.</li>
 *   <li>All PlaceholderAPI calls are dispatched to the main thread; async
 *       callers block on a CompletableFuture with a 5-second timeout.</li>
 * </ul>
 */
public class LeaderboardCache {

    private final LBmenu plugin;
    private final Cache<String, List<LeaderboardEntry>> cache;

    // Boards opened at least once — the background task keeps these warm.
    private final Set<String> knownBoards = ConcurrentHashMap.newKeySet();

    private BukkitTask refreshTask;

    public LeaderboardCache(LBmenu plugin) {
        this.plugin = plugin;
        int ttl     = plugin.getGUIConfig().getLeaderboardCacheSeconds();
        int maxSize = plugin.getGUIConfig().getLeaderboardCacheMaxSize();
        int refresh = plugin.getGUIConfig().getLeaderboardRefreshSeconds();

        cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl, TimeUnit.SECONDS)
                .maximumSize(maxSize)
                .build();

        startRefreshTask(refresh);

        plugin.getLogger().info("[LeaderboardCache] TTL=" + ttl + "s  refresh=" + refresh + "s");
    }

    private void startRefreshTask(int refreshSeconds) {
        long refreshTicks = refreshSeconds * 20L;
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::refreshAll, refreshTicks, refreshTicks);
    }

    /**
     * Background refresh — called async.
     * Dispatches each known board to the main thread (non-blocking fire-and-forget).
     */
    private void refreshAll() {
        for (String board : knownBoards) {
            fetchOnMainThread(board).thenAccept(entries -> {
                if (!entries.isEmpty()) cache.put(board, entries);
            }).exceptionally(ex -> {
                plugin.getLogger().warning(
                        "[LeaderboardCache] Background refresh failed for '"
                                + board + "': " + ex.getMessage());
                return null;
            });
        }
    }

    /**
     * Returns cached entries, or fetches them (cold start only).
     * Safe to call from any thread.
     */
    public List<LeaderboardEntry> getOrFetch(String holderName) {
        String key = normalize(holderName);
        knownBoards.add(key);

        List<LeaderboardEntry> cached = cache.getIfPresent(key);
        if (cached != null) return cached; // hot path — zero main-thread cost

        // Cold start: block until the main-thread fetch completes.
        List<LeaderboardEntry> fetched = fetchOnMainThreadBlocking(holderName);
        if (!fetched.isEmpty()) cache.put(key, fetched);
        return fetched;
    }

    /**
     * Non-blocking dispatch to main thread.
     * Used by the background refresh task.
     */
    private CompletableFuture<List<LeaderboardEntry>> fetchOnMainThread(String holderName) {
        CompletableFuture<List<LeaderboardEntry>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(List.copyOf(FetcherRegistry.get().fetch(holderName, 0)));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Blocking version for cold-start only.
     * If already on the main thread, fetches directly without dispatching.
     */
    private List<LeaderboardEntry> fetchOnMainThreadBlocking(String holderName) {
        if (plugin.getServer().isPrimaryThread()) {
            return List.copyOf(FetcherRegistry.get().fetch(holderName, 0));
        }
        try {
            return fetchOnMainThread(holderName).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().warning("[LeaderboardCache] Fetch interrupted for: " + holderName);
            return List.of();
        } catch (TimeoutException e) {
            plugin.getLogger().warning("[LeaderboardCache] Fetch timed out (5 s) for: " + holderName);
            return List.of();
        } catch (ExecutionException e) {
            plugin.getLogger().warning(
                    "[LeaderboardCache] Fetch failed for: " + holderName + " — " + e.getCause());
            return List.of();
        }
    }

    /** Cancels the background refresh task. Call before rebuilding or on disable. */
    public void stopRefreshTask() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
        knownBoards.clear();
    }

    public void invalidate(String holderName) {
        String key = normalize(holderName);
        cache.invalidate(key);
        knownBoards.remove(key);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
}