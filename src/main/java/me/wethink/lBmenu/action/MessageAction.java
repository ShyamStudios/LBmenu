package me.wethink.lBmenu.action;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wethink.lBmenu.LBmenu;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageAction implements MenuAction {

    private final LBmenu plugin;
    private final String message;

    public MessageAction(LBmenu plugin, String message) {
        this.plugin = plugin;
        this.message = message;
    }

    @Override
    public void execute(Player player) {
        String msg = message;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(player, msg);
        }
        msg = msg.replace("{player}", player.getName());
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(msg.replace("&", "§")));
    }
}
