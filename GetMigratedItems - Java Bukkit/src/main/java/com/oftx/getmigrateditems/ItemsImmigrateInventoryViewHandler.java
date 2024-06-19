package com.oftx.getmigrateditems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemsImmigrateInventoryViewHandler {
    public static PlayerItemDataConfig playerItemDataConfig;

    static void clickEventHandler(Player player, ItemStack clickedItem, int slot, InventoryView view) throws IOException {
        if (clickedItem != null) {
            final String uuid = player.getUniqueId().toString();
            if (slot == 36) { // 左下角
                final String xuid = playerItemDataConfig.getPlayerXuid(player.getUniqueId().toString());
                sendViewToPlayer(player, xuid, playerItemDataConfig.getCurrentPageNumber(uuid) - 1);
            } else if (slot == 44) { // 右下角
                final String xuid = playerItemDataConfig.getPlayerXuid(player.getUniqueId().toString());
                sendViewToPlayer(player, xuid, playerItemDataConfig.getCurrentPageNumber(uuid) + 1);
            } else {
                final String materialID = clickedItem.getType().toString();
                final int maxStackSize = clickedItem.getMaxStackSize();
                final int currentItemCount = playerItemDataConfig.getItemCountInConfig(playerItemDataConfig.getPlayerItemDataFileByXuid(playerItemDataConfig.getPlayerXuid(player.getUniqueId().toString())), materialID);
                final int giveCount = Math.min(currentItemCount, maxStackSize);

                ItemStack newItem = clickedItem.clone();
                newItem.setAmount(giveCount);

                // 获取物品的 ItemMeta
                ItemMeta meta = newItem.getItemMeta();
                if (meta != null) {
                    // 移除物品的 Lore
                    meta.setLore(null); // 或者 meta.setLore(new ArrayList<>());
                    newItem.setItemMeta(meta);
                }

                HashMap<Integer, ItemStack> result = player.getInventory().addItem(newItem);

                if (result.isEmpty()) {
                    final String xuid = playerItemDataConfig.getPlayerXuid(player.getUniqueId().toString());
                    final File file = playerItemDataConfig.getConfigFileByXuid(xuid);
                    final int newCount = currentItemCount - giveCount;
                    playerItemDataConfig.setItemCountInConfig(file, materialID, newCount);
                    clickedItem.setAmount(newCount > 0 ? 1 : 0);
                    setItemLoreInInventory(view, clickedItem, player);
                }
            }
        }
    }

    static private void setItemLoreInInventory(InventoryView view, ItemStack itemStack, Player player) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("数量:" + playerItemDataConfig.getItemCountInConfig(playerItemDataConfig.getPlayerItemDataFileByXuid(playerItemDataConfig.getPlayerXuid(player.getUniqueId().toString())), itemStack.getType().toString()));
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
        }
    }

    static void closeEventHandler(Player p) {
        final String uuid = p.getUniqueId().toString();
        playerItemDataConfig.popFromCurrentItemsData(uuid);
    }

    static void sendViewToPlayer(Player player, String xuid, int page) {
        YamlConfiguration playerItemDataFile = playerItemDataConfig.getPlayerItemDataFileByXuid(xuid);
        if (playerItemDataFile != null) {
            final List<Map.Entry<String, Object>> itemsData = playerItemDataConfig.getPlayerItemData(playerItemDataFile);
            if (itemsData != null) {
                final String uuid = player.getUniqueId().toString();
                final List<Map.Entry<String, Object>> itemsOnTheFirstPage = playerItemDataConfig.getPlayerItemDataOnPage(uuid, itemsData, page);
                final String playerName = playerItemDataConfig.getPlayerNameInConfig(playerItemDataFile);
                final String playerUuid = player.getUniqueId().toString();

                playerItemDataConfig.pushToCurrentItemsData(playerUuid, itemsData);
                playerItemDataConfig.pushToPlayerXuidMap(playerUuid, xuid);

                final int currentPage = playerItemDataConfig.getCurrentPageNumber(uuid);
                Inventory inv = Bukkit.createInventory(null, 9 * 5, "物品迁移 - " + playerName + " - Page " + currentPage);
                for (final Map.Entry<String, Object> entry : itemsOnTheFirstPage) {
                    ItemStack item = new ItemStack(Material.valueOf(entry.getKey()), 1);
                    ItemMeta meta = item.getItemMeta();

                    if (meta != null) {
                        List<String> lore = new ArrayList<>();
                        lore.add("数量:" + entry.getValue()); // 你可以在这里添加更多的lore内容
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }

                    inv.addItem(item);
                }

                // 创建 "上一页" 和 "下一页" 的箭头
                ItemStack previousPageArrow = createArrow(ChatColor.GREEN + "上一页");
                ItemStack nextPageArrow = createArrow(ChatColor.GREEN + "下一页");

                // 将箭头放置在左下角和右下角
                inv.setItem(36, previousPageArrow); // 左下角
                inv.setItem(44, nextPageArrow); // 右下角

                player.openInventory(inv);
            } else {
                player.sendMessage("你的迁移物品空空如也");
//                player.sendMessage("XUID 为 " + xuid + " 无数据");
            }
        } else {
            player.sendMessage("不存在 XUID 为 " + xuid + " 的数据");
        }
    }

    private static ItemStack createArrow(String name) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.addEnchant(Enchantment.FLAME, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            arrow.setItemMeta(meta);
        }
        return arrow;
    }
}
