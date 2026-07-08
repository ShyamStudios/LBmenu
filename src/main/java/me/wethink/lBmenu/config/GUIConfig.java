package me.wethink.lBmenu.config;

import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.gui.CustomButton;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GUIConfig {

    private final LBmenu plugin;
    private volatile Snapshot snap;

    public GUIConfig(LBmenu plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        snap = new Snapshot(plugin.getConfig(), plugin);
    }

    public int getRows()                 { return snap.rows; }
    public List<CustomButton> getCustomButtons() { return snap.customButtons; }
    public int getSize()                 { return snap.rows * 9; }
    public String getTitleTemplate()     { return snap.titleTemplate; }
    public List<Integer> getSkullSlots() { return snap.skullSlots; }

    public String getSkullName()         { return snap.skullName; }
    public List<String> getSkullLore()   { return snap.skullLore; }

    public boolean isFillerEnabled()     { return snap.fillerEnabled; }
    public Material getFillerMaterial()  { return snap.fillerMaterial; }
    public String getFillerName()        { return snap.fillerName; }

    public int getSlotPrev()             { return snap.slotPrev; }
    public Material getPrevMaterial()    { return snap.prevMaterial; }
    public String getPrevName()          { return snap.prevName; }
    public List<String> getPrevLore()    { return snap.prevLore; }

    public int getSlotNext()             { return snap.slotNext; }
    public Material getNextMaterial()    { return snap.nextMaterial; }
    public String getNextName()          { return snap.nextName; }
    public List<String> getNextLore()    { return snap.nextLore; }

    public int getSlotClose()            { return snap.slotClose; }
    public Material getCloseMaterial()   { return snap.closeMaterial; }
    public String getCloseName()         { return snap.closeName; }
    public List<String> getCloseLore()   { return snap.closeLore; }

    public int getLeaderboardCacheSeconds()   { return snap.leaderboardCacheSeconds; }
    public int getLeaderboardCacheMaxSize()   { return snap.leaderboardCacheMaxSize; }
    public int getLeaderboardRefreshSeconds() { return snap.leaderboardRefreshSeconds; }
    public int getSkinCacheMinutes()          { return snap.skinCacheMinutes; }
    public int getSkinCacheMaxSize()          { return snap.skinCacheMaxSize; }

    private static final class Snapshot {
        final int rows;
        final String titleTemplate;
        final List<Integer> skullSlots;
        final String skullName;
        final List<String> skullLore;
        final boolean fillerEnabled;
        final Material fillerMaterial;
        final String fillerName;
        final int slotPrev;
        final Material prevMaterial;
        final String prevName;
        final List<String> prevLore;
        final int slotNext;
        final Material nextMaterial;
        final String nextName;
        final List<String> nextLore;
        final int slotClose;
        final Material closeMaterial;
        final String closeName;
        final List<String> closeLore;
        final int leaderboardCacheSeconds;
        final int leaderboardCacheMaxSize;
        final int leaderboardRefreshSeconds;
        final int skinCacheMinutes;
        final int skinCacheMaxSize;
        final List<CustomButton> customButtons;

        Snapshot(FileConfiguration cfg, LBmenu plugin) {
            rows          = Math.max(1, Math.min(6, cfg.getInt("gui.rows", 6)));
            titleTemplate = cfg.getString("gui.title", "&6{holder} &7(Page {page}/{total})");
            skullSlots    = List.copyOf(cfg.getIntegerList("gui.skull-slots"));

            skullName = cfg.getString("gui.skull.name", "&e#{rank} &f{player}");
            skullLore = List.copyOf(cfg.getStringList("gui.skull.lore"));

            fillerEnabled  = cfg.getBoolean("gui.filler.enabled", true);
            fillerMaterial = parseMaterial(cfg.getString("gui.filler.material"),
                    Material.GRAY_STAINED_GLASS_PANE, plugin);
            fillerName     = cfg.getString("gui.filler.name", " ");

            slotPrev     = cfg.getInt("gui.prev-page.slot", 45);
            prevMaterial = parseMaterial(cfg.getString("gui.prev-page.material"), Material.ARROW, plugin);
            prevName     = cfg.getString("gui.prev-page.name", "&6« Previous Page");
            prevLore     = List.copyOf(cfg.getStringList("gui.prev-page.lore"));

            slotNext     = cfg.getInt("gui.next-page.slot", 53);
            nextMaterial = parseMaterial(cfg.getString("gui.next-page.material"), Material.ARROW, plugin);
            nextName     = cfg.getString("gui.next-page.name", "&6Next Page »");
            nextLore     = List.copyOf(cfg.getStringList("gui.next-page.lore"));

            slotClose     = cfg.getInt("gui.close.slot", 49);
            closeMaterial = parseMaterial(cfg.getString("gui.close.material"), Material.BARRIER, plugin);
            closeName     = cfg.getString("gui.close.name", "&cClose");
            closeLore     = List.copyOf(cfg.getStringList("gui.close.lore"));

            leaderboardCacheSeconds = Math.max(60, cfg.getInt("cache.leaderboard.ttl-seconds", 300));
            leaderboardCacheMaxSize = Math.max(50, cfg.getInt("cache.leaderboard.max-size", 500));

            int defaultRefresh = Math.max(60, (int) (leaderboardCacheSeconds * 0.8));
            leaderboardRefreshSeconds = Math.min(
                    leaderboardCacheSeconds - 1,
                    Math.max(60, cfg.getInt("cache.leaderboard.refresh-seconds", defaultRefresh)));

            skinCacheMinutes = Math.max(1,   cfg.getInt("cache.skins.ttl-minutes", 30));
            skinCacheMaxSize = Math.max(100, cfg.getInt("cache.skins.max-size", 2000));

            List<CustomButton> buttonsList = new ArrayList<>();
            ConfigurationSection guiButtonsSec = cfg.getConfigurationSection("gui.buttons");
            if (guiButtonsSec != null) {
                for (String key : guiButtonsSec.getKeys(false)) {
                    ConfigurationSection sec = guiButtonsSec.getConfigurationSection(key);
                    if (sec != null) {
                        CustomButton btn = CustomButton.parse(sec, plugin);
                        if (btn != null) buttonsList.add(btn);
                    }
                }
            }
            ConfigurationSection rootButtonsSec = cfg.getConfigurationSection("buttons");
            if (rootButtonsSec != null) {
                for (String key : rootButtonsSec.getKeys(false)) {
                    ConfigurationSection sec = rootButtonsSec.getConfigurationSection(key);
                    if (sec != null) {
                        CustomButton btn = CustomButton.parse(sec, plugin);
                        if (btn != null) buttonsList.add(btn);
                    }
                }
            }
            ConfigurationSection guiSec = cfg.getConfigurationSection("gui");
            if (guiSec != null) {
                for (String key : guiSec.getKeys(false)) {
                    if (key.equalsIgnoreCase("rows") ||
                        key.equalsIgnoreCase("title") ||
                        key.equalsIgnoreCase("skull-slots") ||
                        key.equalsIgnoreCase("skull") ||
                        key.equalsIgnoreCase("filler") ||
                        key.equalsIgnoreCase("prev-page") ||
                        key.equalsIgnoreCase("next-page") ||
                        key.equalsIgnoreCase("close") ||
                        key.equalsIgnoreCase("buttons")) {
                        continue;
                    }
                    ConfigurationSection sec = guiSec.getConfigurationSection(key);
                    if (sec != null && sec.contains("material")) {
                        CustomButton btn = CustomButton.parse(sec, plugin);
                        if (btn != null) buttonsList.add(btn);
                    }
                }
            }
            for (String key : cfg.getKeys(false)) {
                if (key.equalsIgnoreCase("gui") || key.equalsIgnoreCase("cache") || key.equalsIgnoreCase("buttons")) {
                    continue;
                }
                ConfigurationSection sec = cfg.getConfigurationSection(key);
                if (sec != null && sec.contains("material")) {
                    CustomButton btn = CustomButton.parse(sec, plugin);
                    if (btn != null) buttonsList.add(btn);
                }
            }
            customButtons = List.copyOf(buttonsList);
        }

        private static Material parseMaterial(String name, Material fallback, LBmenu plugin) {
            if (name == null) return fallback;
            try {
                Material m = Material.valueOf(name.toUpperCase());
                return m == Material.AIR ? fallback : m;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown material '" + name + "', using " + fallback.name());
                return fallback;
            }
        }
    }
}