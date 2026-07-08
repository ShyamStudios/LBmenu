package me.wethink.lBmenu.action;

import me.wethink.lBmenu.LBmenu;

public class ActionParser {

    public static MenuAction parse(String line, LBmenu plugin) {
        if (line == null || line.isBlank()) {
            return null;
        }
        int endBracket = line.indexOf(']');
        if (endBracket == -1 || !line.startsWith("[")) {
            plugin.getLogger().warning("Malformed action (missing tag): " + line);
            return null;
        }
        String type = line.substring(1, endBracket).trim().toLowerCase();
        String args = line.substring(endBracket + 1).trim();

        switch (type) {
            case "playercommand":
                return new PlayerCommandAction(plugin, args);
            case "consolecommand":
                return new ConsoleCommandAction(plugin, args);
            case "message":
                return new MessageAction(plugin, args);
            case "close":
                return new CloseAction(plugin);
            case "sound":
                return new SoundAction(plugin, args);
            default:
                plugin.getLogger().warning("Unknown action type '" + type + "' in: " + line);
                return null;
        }
    }
}
