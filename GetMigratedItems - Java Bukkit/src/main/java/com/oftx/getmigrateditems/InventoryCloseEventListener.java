package com.oftx.getmigrateditems;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;

public class InventoryCloseEventListener implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        InventoryView view = e.getView();
        String invTitle = view.getOriginalTitle();
        Location loc = e.getInventory().getLocation();
        if (invTitle.startsWith("物品迁移") && loc == null) {
            ItemsImmigrateInventoryViewHandler.closeEventHandler(p);
        }
    }
}
