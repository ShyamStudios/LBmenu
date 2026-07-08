package me.wethink.lBmenu.gui;

import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.action.MenuAction;
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

        if (slot >= 0 && slot < plugin.getGUIConfig().getSize()) {
            if (slot == slotClose) {
                player.closeInventory();
                plugin.getLeaderboardCache().invalidate(holder.getHolderName());
                LeaderboardGUI.openAsync(plugin, player, holder.getHolderName());
            } else if (slot == slotPrev && page > 1) {
                gui.open(player, page - 1);
            } else if (slot == slotNext && page < gui.getTotalPages()) {
                gui.open(player, page + 1);
            } else {
                for (CustomButton btn : plugin.getGUIConfig().getCustomButtons()) {
                    if (btn.getSlots().contains(slot)) {
                        for (MenuAction action : btn.getActions()) {
                            action.execute(player);
                        }
                        break;
                    }
                }
            }
        }
    }
}