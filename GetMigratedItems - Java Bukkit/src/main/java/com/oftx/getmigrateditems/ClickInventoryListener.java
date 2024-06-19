package com.oftx.getmigrateditems;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ClickInventoryListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();
        int slot = event.getSlot();

        if (clickedInventory == null) {
            return; // 如果点击的库存为null，则直接返回
        }

        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) {
            return; // 确保点击的是玩家
        }

        String title = view.getOriginalTitle();
        InventoryAction action = event.getAction();

        // 确认物品栏标题以“物品迁移”开头
        if (title.startsWith("物品迁移")) {
            // 检查玩家是否点击了自己的物品栏
            if (clickedInventory.equals(player.getInventory())) {
                event.setCancelled(true);
            } else {
                ItemStack clickedItem = event.getCurrentItem();

                // 处理物品迁移逻辑
                ItemsImmigrateInventoryViewHandler.clickEventHandler(player, clickedItem, slot, view);
                event.setCancelled(true);
            }
        }
    }
}
