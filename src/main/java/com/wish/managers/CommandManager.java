package com.wish.managers;

import com.wish.EnchantsHCN;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandManager implements CommandExecutor {
    
    private final EnchantsHCN plugin;
    private final EnchantManager enchantManager;
    private final BookManager bookManager;
    
    public CommandManager(EnchantsHCN plugin, EnchantManager enchantManager, BookManager bookManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.bookManager = bookManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Comando de recarga
        if (cmd.getName().equalsIgnoreCase("enchantshcnreload")) {
            return handleReloadCommand(sender);
        }
        
        // Comando para crear libro encantado
        if (cmd.getName().equalsIgnoreCase("enchantshcnbook")) {
            return handleBookCommand(sender, args);
        }
        
        // Comando para aplicar encantamiento
        if (cmd.getName().equalsIgnoreCase("enchantshcn")) {
            return handleEnchantCommand(sender, args);
        }
        
        return false;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("enchantshcn.reload")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        plugin.reloadConfig();
        
        // Recargar configuración del ClanHook
        plugin.getClanHook().reload();
        
        sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.reload")));
        return true;
    }
    
    private boolean handleBookCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("enchantshcn.book")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Verificar argumentos
        if (args.length < 1) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cUso: /enchantshcnbook <enchant> [nivel] [jugador]"));
            return true;
        }

        String enchantName = args[0].toLowerCase();
        int level = 1;
        Player targetPlayer = null;

        // Si se especifica nivel
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cEl nivel debe ser un número"));
                return true;
            }
        }

        // Si se especifica jugador
        if (args.length > 2) {
            targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cJugador no encontrado"));
                return true;
            }
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        } else {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cDebes especificar un jugador"));
            return true;
        }

        // Verificar que el encantamiento existe
        if (!plugin.getConfig().contains("enchants." + enchantName)) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-valid-enchant")));
            return true;
        }

        // Verificar que el nivel es válido
        int maxLevel = plugin.getConfig().getInt("enchants." + enchantName + ".max-level");
        if (level < 1 || level > maxLevel) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") +
                    " &cEl nivel debe estar entre 1 y " + maxLevel));
            return true;
        }

        // Crear el libro encantado
        ItemStack book = bookManager.createEnchantedBook(enchantName, level);

        // Dar el libro al jugador
        if (targetPlayer.getInventory().firstEmpty() == -1) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.book-inventory-full")));
            return true;
        }

        targetPlayer.getInventory().addItem(book);

        // Mensaje de éxito
        String enchantDisplayName = plugin.getConfig().getString("enchants." + enchantName + ".name");
        targetPlayer.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.book-given")
                        .replace("%enchant%", enchantDisplayName)
                        .replace("%level%", String.valueOf(level))));

        if (sender != targetPlayer) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") +
                    " &aHas dado un libro de encantamiento " + enchantDisplayName + " " + level +
                    " a " + targetPlayer.getName()));
        }

        return true;
    }
    
    private boolean handleEnchantCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores");
            return true;
        }

        Player player = (Player) sender;

        // Verificar permisos
        if (!player.hasPermission("enchantshcn.use")) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Verificar argumentos
        if (args.length < 1) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cUso: /enchantshcn <enchant> [nivel]"));
            return true;
        }

        String enchantName = args[0].toLowerCase();
        int level = 1;

        // Si se especifica nivel
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &cEl nivel debe ser un número"));
                return true;
            }
        }

        // Verificar que el encantamiento existe
        if (!plugin.getConfig().contains("enchants." + enchantName)) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-valid-enchant")));
            return true;
        }

        // Verificar que el nivel es válido
        int maxLevel = plugin.getConfig().getInt("enchants." + enchantName + ".max-level");
        if (level < 1 || level > maxLevel) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") +
                    " &cEl nivel debe estar entre 1 y " + maxLevel));
            return true;
        }

        // Verificar que el jugador tiene un item en la mano
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.no-item")));
            return true;
        }

        // Verificar que el item es compatible con el encantamiento
        if (!enchantManager.isItemValidForEnchant(item, enchantName)) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.book-invalid-item")));
            return true;
        }

        // Verificar si el item ya tiene el encantamiento
        if (enchantManager.hasEnchant(item, enchantName)) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.enchant-already-applied", "&cEste item ya tiene el encantamiento %enchant%")
                            .replace("%enchant%", plugin.getConfig().getString("enchants." + enchantName + ".name"))));
            return true;
        }

        // Aplicar encantamiento
        enchantManager.applyEnchant(item, enchantName, level);
        player.setItemInHand(item);

        // Mensaje de éxito
        String enchantDisplayName = plugin.getConfig().getString("enchants." + enchantName + ".name");
        player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.enchant-applied")
                        .replace("%enchant%", enchantDisplayName)
                        .replace("%level%", String.valueOf(level))));

        return true;
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}