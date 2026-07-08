package me.wethink.lBmenu.action;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wethink.lBmenu.LBmenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerCommandAction implements MenuAction {

    private final LBmenu plugin;
    private final String command;

    public PlayerCommandAction(LBmenu plugin, String command) {
        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public void execute(Player player) {
        String cmd = command;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            cmd = PlaceholderAPI.setPlaceholders(player, cmd);
        }
        cmd = cmd.replace("{player}", player.getName());
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        String finalCmd = cmd;
        plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> player.performCommand(finalCmd));
    }
}
