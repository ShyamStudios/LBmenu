package me.wethink.lBmenu.action;

import me.wethink.lBmenu.LBmenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundAction implements MenuAction {

    private final LBmenu plugin;
    private final String soundStr;

    public SoundAction(LBmenu plugin, String soundStr) {
        this.plugin = plugin;
        this.soundStr = soundStr;
    }

    @Override
    public void execute(Player player) {
        String[] parts = soundStr.split(" ");
        if (parts.length == 0 || parts[0].isBlank()) {
            return;
        }
        String soundName = parts[0];
        float volume = 1.0f;
        float pitch = 1.0f;
        if (parts.length > 1) {
            try {
                volume = Float.parseFloat(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        if (parts.length > 2) {
            try {
                pitch = Float.parseFloat(parts[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        String lookup = soundName.toUpperCase().replace(".", "_");
        Sound bukkitSound = null;
        try {
            bukkitSound = Sound.valueOf(lookup);
        } catch (IllegalArgumentException ignored) {
        }

        final float finalVolume = volume;
        final float finalPitch = pitch;
        final Sound finalBukkitSound = bukkitSound;
        plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
            if (finalBukkitSound != null) {
                player.playSound(player.getLocation(), finalBukkitSound, finalVolume, finalPitch);
            } else {
                player.playSound(player.getLocation(), soundName.toLowerCase(), finalVolume, finalPitch);
            }
        });
    }
}
