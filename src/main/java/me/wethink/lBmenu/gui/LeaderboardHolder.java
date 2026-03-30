package me.wethink.lBmenu.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class LeaderboardHolder implements InventoryHolder {

    private final String holderName;
    private final int currentPage;
    private final LeaderboardGUI gui;

    public LeaderboardHolder(String holderName, int currentPage, LeaderboardGUI gui) {
        this.holderName  = holderName;
        this.currentPage = currentPage;
        this.gui         = gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("Use Bukkit.createInventory with this holder");
    }

    public String getHolderName()  { return holderName; }
    public int getCurrentPage()    { return currentPage; }
    public LeaderboardGUI getGui() { return gui; }
}
