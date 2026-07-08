package me.wethink.lBmenu.gui;

import me.wethink.lBmenu.LBmenu;
import me.wethink.lBmenu.action.MenuAction;
import me.wethink.lBmenu.action.ActionParser;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class CustomButton {

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final List<Integer> slots;
    private final List<MenuAction> actions;

    public CustomButton(Material material, String name, List<String> lore, List<Integer> slots, List<MenuAction> actions) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.slots = slots;
        this.actions = actions;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public List<MenuAction> getActions() {
        return actions;
    }

    public static CustomButton parse(ConfigurationSection sec, LBmenu plugin) {
        if (sec == null) {
            return null;
        }
        String matStr = sec.getString("material");
        if (matStr == null) {
            return null;
        }
        Material material;
        try {
            material = Material.valueOf(matStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown material '" + matStr + "' for button " + sec.getName());
            return null;
        }

        String name = sec.getString("name");
        List<String> lore = sec.getStringList("lore");

        List<Integer> slots = new ArrayList<>();
        if (sec.isList("slots")) {
            slots.addAll(sec.getIntegerList("slots"));
        } else if (sec.isInt("slots")) {
            slots.add(sec.getInt("slots"));
        } else if (sec.isInt("slot")) {
            slots.add(sec.getInt("slot"));
        } else if (sec.isList("slot")) {
            slots.addAll(sec.getIntegerList("slot"));
        }

        List<MenuAction> actions = new ArrayList<>();
        List<String> actionStrings = sec.getStringList("actions");
        for (String actStr : actionStrings) {
            MenuAction act = ActionParser.parse(actStr, plugin);
            if (act != null) {
                actions.add(act);
            }
        }

        return new CustomButton(material, name, lore, slots, actions);
    }
}
