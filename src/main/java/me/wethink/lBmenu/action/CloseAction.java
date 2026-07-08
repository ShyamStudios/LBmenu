package me.wethink.lBmenu.action;

import me.wethink.lBmenu.LBmenu;
import org.bukkit.entity.Player;

public class CloseAction implements MenuAction {

    private final LBmenu plugin;

    public CloseAction(LBmenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player) {
        plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> player.closeInventory());
    }
}
