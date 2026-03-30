package me.wethink.lBmenu.leaderboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TopperFetcher implements LeaderboardFetcher {

    private static final int MAX_SCAN = 200;
    private static final Pattern ALL_DASHES = Pattern.compile("-+");
    private static volatile OfflinePlayer dummy;

    private static OfflinePlayer getDummy() {
        if (dummy != null) return dummy;
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached("__dummy__");
        dummy = (cached != null) ? cached : Bukkit.getOfflinePlayer("__dummy__");
        return dummy;
    }

    @Override
    public String providerName() { return "Topper"; }

    @Override
    public List<LeaderboardEntry> fetch(String holderName, int maxEntries) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        int limit = (maxEntries <= 0) ? MAX_SCAN : Math.min(maxEntries, MAX_SCAN);
        OfflinePlayer d = getDummy();

        for (int rank = 1; rank <= limit; rank++) {
            String combined = PlaceholderAPI.setPlaceholders(d,
                    "%topper_" + holderName + ";top_name;" + rank + "%\0"
                            + "%topper_" + holderName + ";top_value;" + rank + "%");

            int sep = combined.indexOf('\0');
            if (sep == -1) break;

            String name  = combined.substring(0, sep);
            String value = combined.substring(sep + 1);

            if (isBlankOrEmpty(name) || name.startsWith("%topper_")) break;
            if (isBlankOrEmpty(value) || value.startsWith("%topper_")) continue;

            entries.add(new LeaderboardEntry(rank, name, value));
        }
        return entries;
    }

    static boolean isBlankOrEmpty(String s) {
        if (s == null || s.isBlank()) return true;
        String t = s.trim();
        return t.equalsIgnoreCase("N/A")
                || t.equalsIgnoreCase("none")
                || ALL_DASHES.matcher(t).matches();
    }
}