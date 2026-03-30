package me.wethink.lBmenu.gui;

import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.config.GUIConfig;
import me.wethink.lBmenu.leaderboard.LeaderboardEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LeaderboardGUI {

    private final GUIConfig cfg;
    private final String holderName;
    private final int totalPages;
    private final int perPage;

    private final ItemStack[]  builtSkulls;
    private final ItemStack    filler;
    private final ItemStack[]  prevItems;
    private final ItemStack[]  nextItems;
    private final ItemStack[]  closeItems;
    private final Component[]  pageTitles;

    public LeaderboardGUI(LBmenu plugin, String holderName, List<LeaderboardEntry> entries) {
        this.cfg        = plugin.getGUIConfig();
        this.holderName = holderName;
        this.perPage    = Math.max(1, cfg.getSkullSlots().size());
        this.totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));

        this.builtSkulls = new ItemStack[entries.size()];

        @SuppressWarnings("unchecked")
        CompletableFuture<ItemStack>[] skullFutures = new CompletableFuture[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            final int idx = i;
            final LeaderboardEntry entry = entries.get(i);

            skullFutures[i] = plugin.getSkinCache()
                    .getSkullTemplateAsync(entry.playerName())
                    .thenApply(template -> {
                        ItemStack skull = applySkullMeta(template.clone(), entry);
                        builtSkulls[idx] = skull;
                        return skull;
                    });
        }

        CompletableFuture.allOf(skullFutures).join();

        this.filler = cfg.isFillerEnabled() ? buildFiller() : null;

        prevItems  = new ItemStack[totalPages + 1];
        nextItems  = new ItemStack[totalPages + 1];
        closeItems = new ItemStack[totalPages + 1];
        pageTitles = new Component[totalPages + 1];

        for (int pg = 1; pg <= totalPages; pg++) {
            prevItems[pg]  = buildNavItem(cfg.getPrevMaterial(),  cfg.getPrevName(),  cfg.getPrevLore(),  pg);
            nextItems[pg]  = buildNavItem(cfg.getNextMaterial(),  cfg.getNextName(),  cfg.getNextLore(),  pg);
            closeItems[pg] = buildNavItem(cfg.getCloseMaterial(), cfg.getCloseName(), cfg.getCloseLore(), pg);
            pageTitles[pg] = legacy(color(cfg.getTitleTemplate()
                    .replace("{holder}", capitalize(holderName))
                    .replace("{page}",   String.valueOf(pg))
                    .replace("{total}",  String.valueOf(totalPages))));
        }
    }

    public void open(Player player, int page) {
        int p = Math.max(1, Math.min(page, totalPages));

        Inventory inv = Bukkit.createInventory(
                new LeaderboardHolder(holderName, p, this),
                cfg.getSize(),
                pageTitles[p]);

        int startIndex      = (p - 1) * perPage;
        List<Integer> slots = cfg.getSkullSlots();
        for (int i = 0; i < slots.size(); i++) {
            int entryIndex = startIndex + i;
            if (entryIndex >= builtSkulls.length) break;
            int slot = slots.get(i);
            if (slot < cfg.getSize()) inv.setItem(slot, builtSkulls[entryIndex]);
        }

        if (filler != null) {
            for (int slot = 0; slot < cfg.getSize(); slot++) {
                if (inv.getItem(slot) == null) inv.setItem(slot, filler);
            }
        }

        inv.setItem(cfg.getSlotClose(), closeItems[p]);
        if (p > 1)          inv.setItem(cfg.getSlotPrev(), prevItems[p]);
        if (p < totalPages) inv.setItem(cfg.getSlotNext(), nextItems[p]);

        player.openInventory(inv);
    }

    private ItemStack applySkullMeta(ItemStack skull, LeaderboardEntry entry) {
        ItemMeta meta = skull.getItemMeta();
        if (meta == null) return skull;

        String name = color(cfg.getSkullName()
                .replace("{rank}",   String.valueOf(entry.rank()))
                .replace("{player}", entry.playerName())
                .replace("{value}",  entry.value())
                .replace("{holder}", capitalize(holderName)));
        meta.displayName(legacy(name).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = cfg.getSkullLore().stream()
                .map(line -> color(line
                        .replace("{rank}",   String.valueOf(entry.rank()))
                        .replace("{player}", entry.playerName())
                        .replace("{value}",  entry.value())
                        .replace("{holder}", capitalize(holderName))))
                .map(line -> legacy(line).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
        meta.lore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildNavItem(org.bukkit.Material mat, String rawName,
                                   List<String> rawLore, int currentPage) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(legacy(color(rawName
                .replace("{page}", String.valueOf(currentPage))
                .replace("{prev}", String.valueOf(currentPage - 1))
                .replace("{next}", String.valueOf(currentPage + 1))
        )).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = rawLore.stream()
                .map(line -> color(line
                        .replace("{page}", String.valueOf(currentPage))
                        .replace("{prev}", String.valueOf(currentPage - 1))
                        .replace("{next}", String.valueOf(currentPage + 1))))
                .map(line -> legacy(line).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildFiller() {
        ItemStack glass = new ItemStack(cfg.getFillerMaterial());
        ItemMeta  meta  = glass.getItemMeta();
        if (meta != null) {
            String name = cfg.getFillerName();
            meta.displayName(name.isBlank()
                    ? Component.empty()
                    : legacy(color(name)).decoration(TextDecoration.ITALIC, false));
            glass.setItemMeta(meta);
        }
        return glass;
    }

    private static String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }

    private static Component legacy(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s == null ? "" : s);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public int getTotalPages() { return totalPages; }

    public static void openAsync(LBmenu plugin, Player player, String holderName) {
        plugin.getFoliaLib().getScheduler().runAsync(task -> {
            List<LeaderboardEntry> entries = plugin.getLeaderboardCache().getOrFetch(holderName);

            if (entries.isEmpty()) {
                plugin.getFoliaLib().getScheduler().runAtEntity(player, syncTask ->
                        player.sendMessage(Component.text(
                                "No data found for leaderboard: " + holderName,
                                NamedTextColor.RED)));
                return;
            }

            LeaderboardGUI gui = new LeaderboardGUI(plugin, holderName, entries);

            plugin.getFoliaLib().getScheduler().runAtEntity(player, syncTask ->
                    gui.open(player, 1));
        });
    }
}