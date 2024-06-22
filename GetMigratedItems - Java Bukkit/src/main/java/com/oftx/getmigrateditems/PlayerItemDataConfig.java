package com.oftx.getmigrateditems;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.oftx.getmigrateditems.ItemsImmigrateInventoryViewHandler.playerItemDataConfig;

public class PlayerItemDataConfig {
    private final JavaPlugin plugin;
    private final Map<String, List<Map.Entry<String, Object>>> currentItemsDataInPlayerInv = new HashMap<>();
    private final Map<String, Integer> currentPage = new HashMap<>();
    private final Map<String, String> playerXuidMap = new HashMap<>();

    PlayerItemDataConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void pushToCurrentItemsData(String playerUuid, List<Map.Entry<String, Object>> itemsData) {
        currentItemsDataInPlayerInv.put(playerUuid, itemsData);
    }

    public void popFromCurrentItemsData(String playerUuid) {
        currentItemsDataInPlayerInv.remove(playerUuid);
    }

    public List<Map.Entry<String, Object>> getCurrentItemData(String playerUuid) {
        return currentItemsDataInPlayerInv.get(playerUuid);
    }

    public void pushToCurrentPage(String playerUuid, int page) {
        currentPage.put(playerUuid, page);
    }

    public void popFromCurrentPage(String playerUuid) {
        currentPage.remove(playerUuid);
    }

    public int getCurrentPageNumber(String playerUuid) {
        try {
            final int page = currentPage.get(playerUuid);
            return page <= 0 ? 1 : page;
        } catch (NullPointerException e) {
            return 1;
        }
    }

    public void pushToPlayerXuidMap(String playerUuid, String xuid) {
        playerXuidMap.put(playerUuid, xuid);
    }

    public void popFromPlayerXuidMap(String playerUuid) {
        playerXuidMap.remove(playerUuid);
    }

    public String getPlayerXuid(String playerUuid) {
        return playerXuidMap.get(playerUuid);
    }

    public List<Map.Entry<String, Object>> getCurrentItemsData(String playerUuid) {
        return currentItemsDataInPlayerInv.get(playerUuid);
    }

    public int getItemCountInConfig(YamlConfiguration config, String MaterialID) {
        return config.getInt(MaterialID);
    }

    public void setItemCountInConfig(File file, String MaterialID, int count) throws IOException {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (count > 0) {
            config.set(MaterialID, count);
        } else {
            config.set(MaterialID, null);
        }
        config.save(file);
    }

    public File getConfigFileByXuid(String xuid) {
        final String folderName = "data";
        File configFile = new File(plugin.getDataFolder() + File.separator + folderName + File.separator + xuid + ".yml");
        if (!configFile.exists()) {
            return null;
        } else {
            return configFile;
        }
    }

    public YamlConfiguration getPlayerItemDataFileByXuid(String playerXuid) {
        YamlConfiguration result;
        final String folderName = "data";
        File playerItemDataFile = new File(plugin.getDataFolder() + File.separator + folderName + File.separator + playerXuid + ".yml");
        if (!playerItemDataFile.exists()) {
            result = null;
        } else {
            result = YamlConfiguration.loadConfiguration(playerItemDataFile);
        }
        return result;
    }

    public String getPlayerNameInConfig(FileConfiguration playerItemDataFile) {
        return playerItemDataFile.getString("player");
    }

    public List<Map.Entry<String, Object>> getPlayerItemData(FileConfiguration playerItemDataFile) {
        List<Map.Entry<String, Object>> result;
        if (playerItemDataFile == null) {
            result = null;
        } else {
            result = new ArrayList<>(playerItemDataFile.getValues(true).entrySet());
            result.removeIf(entry -> entry.getKey().equalsIgnoreCase("player"));
        }
        return result;
    }

    static public int getLastPageNumber(int count, int itemCountPerPage) {
        if (count <= 0) return 1;
        if (itemCountPerPage <= 0) return 1;
        return ((count - 1) / itemCountPerPage) + 1;
    }

    public List<Map.Entry<String, Object>> getPlayerItemDataOnPage(String playerUuid, List<Map.Entry<String, Object>> itemsData, int page, int itemCountPerPage) {
        if (itemsData == null || itemsData.isEmpty()) return itemsData;
        if (itemCountPerPage <= 0 || itemCountPerPage > 6 * 9) itemCountPerPage = 9;
        if (page <= 0) page = 1;
        final int allItemCount = itemsData.size();
        int lastPage = getLastPageNumber(allItemCount, itemCountPerPage);
        if (page > lastPage) page = lastPage;
        playerItemDataConfig.pushToCurrentPage(playerUuid, page);


        int startIndex = (page - 1) * itemCountPerPage;
        int endIndex = startIndex + itemCountPerPage;
        if (endIndex >= allItemCount) endIndex = allItemCount;
        return itemsData.subList(startIndex, endIndex);
    }

    public List<Map.Entry<String, Object>> getPlayerItemDataOnPage(String playerUuid, List<Map.Entry<String, Object>> itemsData, int page) {
        return getPlayerItemDataOnPage(playerUuid, itemsData, page, 4 * 9);
    }
}
