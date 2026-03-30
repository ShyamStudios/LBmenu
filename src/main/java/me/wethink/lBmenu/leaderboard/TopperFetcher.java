package me.wethink.lBmenu.leaderboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Fetches leaderboard data from Topper via PlaceholderAPI.
 *
 * Topper exposes:
 * %topper_<holder>;top_name;<rank>%  → player name at rank
 * %topper_<holder>;top_value;<rank>% → value at rank
 *
 * Must be called from the main server thread — PlaceholderAPI requires it.
 */
public class TopperFetcher implements LeaderboardFetcher {

    private static final int MAX_SCAN = 200;

    // Pre-compiled once at class load — avoids Pattern.compile() on every isBlankOrEmpty call.
    private static final Pattern ALL_DASHES = Pattern.compile("-+");

    // Cached after first resolution — getOfflinePlayer(name) does a blocking
    // filesystem lookup the first time for unknown names.
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
            // Single setPlaceholders call per rank instead of two.
            // \0 is used as separator — it can never appear in a player name or numeric value.
            String combined = PlaceholderAPI.setPlaceholders(d,
                    "%topper_" + holderName + ";top_name;" + rank + "%\0"
                            + "%topper_" + holderName + ";top_value;" + rank + "%");

            int sep = combined.indexOf('\0');
            if (sep == -1) break;

            String name  = combined.substring(0, sep);
            String value = combined.substring(sep + 1);

            // ajLeaderboards/Topper returns the raw placeholder when no data exists.
            if (isBlankOrEmpty(name) || name.startsWith("%topper_")) break;
            if (isBlankOrEmpty(value) || value.startsWith("%topper_")) continue;

            entries.add(new LeaderboardEntry(rank, name, value));
        }
        return entries;
    }

    /**
     * Returns true if the string is null, blank, all dashes, "N/A", or "none".
     * Uses a pre-compiled Pattern to avoid per-call regex compilation.
     */
    static boolean isBlankOrEmpty(String s) {
        if (s == null || s.isBlank()) return true;
        String t = s.trim();
        return t.equalsIgnoreCase("N/A")
                || t.equalsIgnoreCase("none")
                || ALL_DASHES.matcher(t).matches();
    }
}