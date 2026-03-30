package me.wethink.lBmenu.leaderboard;

import org.bukkit.Bukkit;

public class FetcherRegistry {

    private static volatile LeaderboardFetcher cached;

    public static LeaderboardFetcher get() {
        if (cached != null) return cached;

        if (Bukkit.getPluginManager().isPluginEnabled("ajLeaderboards")) {
            cached = new AjLeaderboardsFetcher();
        } else if (Bukkit.getPluginManager().isPluginEnabled("Topper")) {
            cached = new TopperFetcher();
        } else {
            cached = new TopperFetcher();
        }

        return cached;
    }

    public static void reset() {
        cached = null;
    }
}
