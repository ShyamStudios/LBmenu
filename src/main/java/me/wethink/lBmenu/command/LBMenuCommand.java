package me.wethink.lBmenu.command;

import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.gui.LeaderboardGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LBMenuCommand implements CommandExecutor, TabCompleter {

    private final LBmenu plugin;

    public LBMenuCommand(LBmenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lbmenu.reload")) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return true;
            }
            plugin.getGUIConfig().reload();
            plugin.rebuildCaches();
            me.wethink.lBmenu.leaderboard.FetcherRegistry.reset();
            sender.sendMessage(Component.text("LBmenu config reloaded.", NamedTextColor.GREEN));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the GUI. Use /lbmenu reload for console.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text(
                    "Usage: /lbmenu <placeholder>  e.g. /lbmenu kills", NamedTextColor.YELLOW));
            return true;
        }

        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            player.sendMessage(Component.text("PlaceholderAPI is not installed!", NamedTextColor.RED));
            return true;
        }

        String holderName = args[0].toLowerCase();
        player.sendMessage(Component.text("Loading leaderboard: " + holderName + "...", NamedTextColor.GRAY));
        LeaderboardGUI.openAsync(plugin, player, holderName);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "kills", "deaths", "playtime");
        }
        return List.of();
    }
}
