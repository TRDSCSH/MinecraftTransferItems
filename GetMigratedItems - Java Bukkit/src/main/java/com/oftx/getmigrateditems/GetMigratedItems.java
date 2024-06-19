package com.oftx.getmigrateditems;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GetMigratedItems extends JavaPlugin {
    static JavaPlugin plugin;

    @Override
    public void onEnable() { // 豪赤（好吃 [翻译自中文（中国）]）
        Objects.requireNonNull(Bukkit.getPluginCommand("wupin")).setExecutor(new WuPinCommand(this));
        ItemsImmigrateInventoryViewHandler.playerItemDataConfig = new PlayerItemDataConfig(this);
        getServer().getPluginManager().registerEvents(new ClickInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseEventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
