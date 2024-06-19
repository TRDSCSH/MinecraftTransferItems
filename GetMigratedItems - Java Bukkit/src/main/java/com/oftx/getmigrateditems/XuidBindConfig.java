package com.oftx.getmigrateditems;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class XuidBindConfig {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public XuidBindConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    // 创建配置文件
    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "xuid_bind.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
                saveConfig();  // 初次创建文件后保存空配置
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建配置文件: " + configFile.getName());
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    // 获取玩家 UUID 对应的 XUID
    public String getXuidByPlayerUuid(UUID playerUuid) {
        return config.getString(playerUuid.toString());
    }

    // 设置玩家 UUID 对应的 XUID
    public void setXuidWithPlayerUuid(UUID playerUuid, String xuid) {
        config.set(playerUuid.toString(), xuid);
        saveConfig();
    }

    // 保存配置文件
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + configFile.getName());
            e.printStackTrace();
        }
    }

    // 重新加载配置文件
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "xuid_bind.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
