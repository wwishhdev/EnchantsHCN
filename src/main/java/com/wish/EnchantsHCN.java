package com.wish;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EnchantsHCN extends JavaPlugin implements Listener {

    private Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("enchantshcn").setExecutor(this);
        getCommand("enchantshcnreload").setExecutor(this);

        getLogger().info("EnchantsHCN ha sido habilitado! by wwishhdev <3");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnchantsHCN ha sido deshabilitado! by wwishhdev <3");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Comando de recarga
        if (cmd.getName().equalsIgnoreCase("enchantshcnreload")) {
            if (!sender.hasPermission("enchantshcn.reload")) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-permission")));
                return true;
            }
            reloadConfig();
            sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                    getConfig().getString("messages.reload")));
            return true;
        }

        // Comando para aplicar encantamiento
        if (cmd.getName().equalsIgnoreCase("enchantshcn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Este comando solo puede ser usado por jugadores");
                return true;
            }

            Player player = (Player) sender;

            // Verificar permisos
            if (!player.hasPermission("enchantshcn.use")) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-permission")));
                return true;
            }

            // Verificar argumentos
            if (args.length < 1) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cUso: /enchantshcn <enchant> [nivel]"));
                return true;
            }

            String enchantName = args[0].toLowerCase();
            int level = 1;

            // Si se especifica nivel
            if (args.length > 1) {
                try {
                    level = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cEl nivel debe ser un número"));
                    return true;
                }
            }

            // Verificar que el encantamiento existe
            if (!getConfig().contains("enchants." + enchantName)) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-valid-enchant")));
                return true;
            }

            // Verificar que el nivel es válido
            int maxLevel = getConfig().getInt("enchants." + enchantName + ".max-level");
            if (level < 1 || level > maxLevel) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") +
                        " &cEl nivel debe estar entre 1 y " + maxLevel));
                return true;
            }

            // Verificar que el jugador tiene un item en la mano
            ItemStack item = player.getItemInHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-item")));
                return true;
            }

            // Aplicar encantamiento
            applyEnchant(item, enchantName, level);
            player.setItemInHand(item);

            // Mensaje de éxito
            String enchantDisplayName = getConfig().getString("enchants." + enchantName + ".name");
            player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                    getConfig().getString("messages.enchant-applied")
                            .replace("%enchant%", enchantDisplayName)
                            .replace("%level%", String.valueOf(level))));

            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> keepItems = new ArrayList<>();

        // Revisar todos los items que dropea el jugador
        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (hasSoulboundEnchant(item)) {
                int level = getSoulboundLevel(item);
                int chance = getConfig().getInt("enchants.soulbound.levels." + level + ".chance");

                // Verificar si el encantamiento se activa según la probabilidad
                if (random.nextInt(100) < chance) {
                    // Crear una copia del item sin el encantamiento SoulBound
                    ItemStack itemCopy = item.clone();
                    removeEnchant(itemCopy, "soulbound");

                    // El item (sin SoulBound) se guarda
                    keepItems.add(itemCopy);

                    // Eliminarlo de los drops
                    iterator.remove();
                }
            }
        }

        if (!keepItems.isEmpty()) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                for (ItemStack item : keepItems) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " &aAlgunos de tus items fueron protegidos por SoulBound"));
            }, 5L);
        }
    }

    private void applyEnchant(ItemStack item, String enchantName, int level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Remover encantamiento existente si ya existe
        removeEnchant(item, enchantName);

        // Agregar el nuevo encantamiento
        String enchantLore = colorize(getConfig().getString("enchants." + enchantName + ".levels." + level + ".lore"));
        String enchantDescription = getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");

        lore.add(enchantLore);
        if (enchantDescription != null && !enchantDescription.isEmpty()) {
            lore.add(colorize(enchantDescription));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void removeEnchant(ItemStack item, String enchantName) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>();

            for (String line : lore) {
                boolean isEnchantmentLine = false;

                for (int i = 1; i <= getConfig().getInt("enchants." + enchantName + ".max-level", 0); i++) {
                    String enchantLore = colorize(getConfig().getString("enchants." + enchantName + ".levels." + i + ".lore", ""));
                    String enchantDescription = colorize(getConfig().getString("enchants." + enchantName + ".levels." + i + ".description", ""));

                    if (line.equals(enchantLore) || line.equals(enchantDescription)) {
                        isEnchantmentLine = true;
                        break;
                    }
                }

                if (!isEnchantmentLine) {
                    newLore.add(line);
                }
            }

            meta.setLore(newLore);
            item.setItemMeta(meta);
        }
    }

    private boolean hasSoulboundEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants.soulbound.max-level"); i++) {
                String enchantLore = colorize(getConfig().getString("enchants.soulbound.levels." + i + ".lore"));
                if (lore.contains(enchantLore)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getSoulboundLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = getConfig().getInt("enchants.soulbound.max-level"); i >= 1; i--) {
                String enchantLore = colorize(getConfig().getString("enchants.soulbound.levels." + i + ".lore"));
                if (lore.contains(enchantLore)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}