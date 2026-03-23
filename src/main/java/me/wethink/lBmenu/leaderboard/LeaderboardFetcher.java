package me.wethink.lBmenu.leaderboard;

import java.util.List;

public interface LeaderboardFetcher {
    /**
     * Fetches leaderboard entries for the given board/holder name.
     * <b>Must be called from the main server thread</b> — PlaceholderAPI
     * requires main-thread access.
     *
     * @param boardName  the board/holder key
     * @param maxEntries max entries to fetch (0 = auto-detect up to internal limit)
     */
    List<LeaderboardEntry> fetch(String boardName, int maxEntries);

    String providerName();
}