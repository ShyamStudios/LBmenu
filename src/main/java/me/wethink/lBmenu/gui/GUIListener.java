package me.wethink.lBmenu.gui;

import me.wethink.lBmenu.LBmenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final LBmenu plugin;

    public GUIListener(LBmenu plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof LeaderboardHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        int page = holder.getCurrentPage();
        LeaderboardGUI gui = holder.getGui();

        int slotClose = plugin.getGUIConfig().getSlotClose();
        int slotPrev  = plugin.getGUIConfig().getSlotPrev();
        int slotNext  = plugin.getGUIConfig().getSlotNext();

        if (slot == slotClose) {
            // Refresh: close current inventory and re-open with fresh data.
            // invalidate() forces the AsyncLoadingCache to fetch new data on next access.
            player.closeInventory();
            plugin.getLeaderboardCache().invalidate(holder.getHolderName());
            LeaderboardGUI.openAsync(plugin, player, holder.getHolderName());
        } else if (slot == slotPrev && page > 1) {
            // gui already has pre-built skulls — open() is just inventory population,
            // so it's safe and fast to call directly on the server thread here.
            gui.open(player, page - 1);
        } else if (slot == slotNext && page < gui.getTotalPages()) {
            gui.open(player, page + 1);
        }
    }
}