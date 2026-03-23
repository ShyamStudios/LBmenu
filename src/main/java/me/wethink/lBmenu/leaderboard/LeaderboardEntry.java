package me.wethink.lBmenu.leaderboard;

/**
 * Represents a single leaderboard row: rank, player name, and value string.
 */
public record LeaderboardEntry(int rank, String playerName, String value) {}
