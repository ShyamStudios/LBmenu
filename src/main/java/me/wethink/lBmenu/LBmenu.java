package me.wethink.lBmenu;

import com.tcoded.folialib.FoliaLib;
import me.wethink.lBmenu.cache.LeaderboardCache;
import me.wethink.lBmenu.cache.SkinCache;
import me.wethink.lBmenu.command.LBMenuCommand;
import me.wethink.lBmenu.config.GUIConfig;
import me.wethink.lBmenu.gui.GUIListener;
import me.wethink.lBmenu.leaderboard.FetcherRegistry;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class LBmenu extends JavaPlugin {

    private static LBmenu instance;
    private FoliaLib foliaLib;
    private SkinCache skinCache;
    private LeaderboardCache leaderboardCache;
    private GUIConfig guiConfig;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        instance         = this;
        foliaLib         = new FoliaLib(this);
        guiConfig        = new GUIConfig(this);
        skinCache        = new SkinCache(this);
        leaderboardCache = new LeaderboardCache(this);

        // Commands & listeners
        LBMenuCommand cmd = new LBMenuCommand(this);
        getCommand("lbmenu").setExecutor(cmd);
        getCommand("lbmenu").setTabCompleter(cmd);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // bStats
        int pluginId = 30375;
        new Metrics(this, pluginId);

        // Startup banner
        printStartupBanner(start);
    }

    @Override
    public void onDisable() {
        if (leaderboardCache != null) leaderboardCache.stopRefreshTask();
        if (skinCache != null)        skinCache.invalidateAll();
        if (leaderboardCache != null) leaderboardCache.invalidateAll();

        send("&cPlugin disabled.");
    }

    public static LBmenu getInstance()           { return instance; }
    public FoliaLib getFoliaLib()                 { return foliaLib; }
    public SkinCache getSkinCache()               { return skinCache; }
    public LeaderboardCache getLeaderboardCache() { return leaderboardCache; }
    public GUIConfig getGUIConfig()               { return guiConfig; }

    /**
     * Rebuilds both caches and re-detects the active leaderboard plugin.
     */
    public void rebuildCaches() {
        if (leaderboardCache != null) leaderboardCache.stopRefreshTask();
        if (skinCache != null)        skinCache.invalidateAll();
        if (leaderboardCache != null) leaderboardCache.invalidateAll();

        FetcherRegistry.reset();

        skinCache        = new SkinCache(this);
        leaderboardCache = new LeaderboardCache(this);
    }

    // ===============================
    // 🔥 STARTUP BANNER
    // ===============================

    private void printStartupBanner(long startTime) {
        String name = getDescription().getName();
        String version = getDescription().getVersion();

        String authors = String.join(", ", getDescription().getAuthors());
        String website = getDescription().getWebsite() != null
                ? getDescription().getWebsite()
                : "Not specified";

        send("");
        send("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        send("      &b&l" + name + " &7v" + version);
        send("");
        send("  &7Author(s): &f" + (authors.isEmpty() ? "Unknown" : authors));
        send("  &7bStats: &aEnabled");
        send("  &7Server: &f" + getServer().getName());
        send("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        log("&7Initializing Folia scheduler...", "&a✔");
        log("&7Loading GUI configuration...", "&a✔");
        log("&7Initializing skin cache...", "&a✔");
        log("&7Initializing leaderboard cache...", "&a✔");
        log("&7Registering commands...", "&a✔");
        log("&7Registering listeners...", "&a✔");

        long time = System.currentTimeMillis() - startTime;

        send("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        send("  &a✔ Plugin enabled successfully");
        send("  &7Load Time: &f" + time + "ms");
        send("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        send("");
    }

    private void log(String text, String status) {
        send("  " + text + " " + status);
    }

    private void send(String msg) {
        getServer().getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&8[&bLBmenu&8] " + msg
                )
        );
    }
}