package me.wethink.lBmenu.leaderboard;

import java.util.List;

public interface LeaderboardFetcher {

    List<LeaderboardEntry> fetch(String boardName, int maxEntries);

    String providerName();
}