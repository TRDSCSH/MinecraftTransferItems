package com.oftx.getmigrateditems;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.enchantments.Enchantment;

import java.util.UUID;

public class WuPinCommand implements CommandExecutor {
    private final PlayerItemDataConfig playerItemDataConfig;
    private final XuidBindConfig config;

    public WuPinCommand(JavaPlugin plugin) {
        playerItemDataConfig = new PlayerItemDataConfig(plugin);
        config = new XuidBindConfig(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length == 0) {
                return false;
            }
            final UUID uuid = player.getUniqueId();
            if (strings[0].equalsIgnoreCase("open")) {
                final String xuid = config.getXuidByPlayerUuid(uuid);
                if (xuid == null) {
                    player.sendMessage("§c你尚未绑定基岩版XUID\n§r请使用 §6/wupin bind <xuid>§r 命令来绑定你的XUID");
                    return true;
                }
                ItemsImmigrateInventoryViewHandler.sendViewToPlayer(player, xuid, 1);
            } else if (strings[0].equalsIgnoreCase("bind")) {
                final String xuid = strings[1];
                config.setXuidWithPlayerUuid(uuid, xuid);
                player.sendMessage("§a绑定成功:" + xuid);
                return true;
            }

            return true;
        } else {
            commandSender.sendMessage("This command can only be executed by a player");
            return false;
        }
    }

    private ItemStack createArrow(String name) {
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
