package me.wethink.lBmenu.leaderboard;

import org.bukkit.Bukkit;

/**
 * Resolves the correct LeaderboardFetcher at runtime based on which
 * leaderboard plugin is installed.
 *
 * Priority: ajLeaderboards > Topper
 */
public class FetcherRegistry {

    private static LeaderboardFetcher cached;

    /** Returns the active fetcher, detecting once and caching the result. */
    public static LeaderboardFetcher get() {
        if (cached != null) return cached;

        if (Bukkit.getPluginManager().isPluginEnabled("ajLeaderboards")) {
            cached = new AjLeaderboardsFetcher();
        } else if (Bukkit.getPluginManager().isPluginEnabled("Topper")) {
            cached = new TopperFetcher();
        } else {
            // Fall back to Topper-style PAPI probing — works for any PAPI-based leaderboard
            cached = new TopperFetcher();
        }

        return cached;
    }

    /** Call on reload so the next fetch re-detects the active plugin. */
    public static void reset() {
        cached = null;
    }
}
