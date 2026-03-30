package me.wethink.lBmenu.leaderboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class AjLeaderboardsFetcher implements LeaderboardFetcher {

    private static final int MAX_SCAN = 200;
    private static volatile OfflinePlayer dummy;

    private static OfflinePlayer getDummy() {
        if (dummy != null) return dummy;
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached("__dummy__");
        dummy = (cached != null) ? cached : Bukkit.getOfflinePlayer("__dummy__");
        return dummy;
    }

    @Override
    public String providerName() { return "ajLeaderboards"; }

    @Override
    public List<LeaderboardEntry> fetch(String boardName, int maxEntries) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        int limit = (maxEntries <= 0) ? MAX_SCAN : Math.min(maxEntries, MAX_SCAN);
        OfflinePlayer d = getDummy();

        for (int rank = 1; rank <= limit; rank++) {
            String combined = PlaceholderAPI.setPlaceholders(d,
                    "%ajlb_lb_" + boardName + "_" + rank + "_alltime_name%\0"
                            + "%ajlb_lb_" + boardName + "_" + rank + "_alltime_value%");

            int sep = combined.indexOf('\0');
            if (sep == -1) break;

            String name  = combined.substring(0, sep);
            String value = combined.substring(sep + 1);

            if (TopperFetcher.isBlankOrEmpty(name) || name.startsWith("%ajlb_")) break;
            if (TopperFetcher.isBlankOrEmpty(value) || value.startsWith("%ajlb_")) continue;

            entries.add(new LeaderboardEntry(rank, name, value));
        }
        return entries;
    }
}