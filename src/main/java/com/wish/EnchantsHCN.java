package com.wish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
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
        getCommand("enchantshcnbook").setExecutor(this);

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

        // Comando para crear libro encantado
        if (cmd.getName().equalsIgnoreCase("enchantshcnbook")) {
            if (!sender.hasPermission("enchantshcn.book")) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-permission")));
                return true;
            }

            // Verificar argumentos
            if (args.length < 1) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cUso: /enchantshcnbook <enchant> [nivel] [jugador]"));
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
                    sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cEl nivel debe ser un número"));
                    return true;
                }
            }

            // Si se especifica jugador
            if (args.length > 2) {
                targetPlayer = Bukkit.getPlayer(args[2]);
                if (targetPlayer == null) {
                    sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cJugador no encontrado"));
                    return true;
                }
            } else if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " &cDebes especificar un jugador"));
                return true;
            }

            // Verificar que el encantamiento existe
            if (!getConfig().contains("enchants." + enchantName)) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.no-valid-enchant")));
                return true;
            }

            // Verificar que el nivel es válido
            int maxLevel = getConfig().getInt("enchants." + enchantName + ".max-level");
            if (level < 1 || level > maxLevel) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") +
                        " &cEl nivel debe estar entre 1 y " + maxLevel));
                return true;
            }

            // Crear el libro encantado
            ItemStack book = createEnchantedBook(enchantName, level);

            // Dar el libro al jugador
            if (targetPlayer.getInventory().firstEmpty() == -1) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.book-inventory-full")));
                return true;
            }

            targetPlayer.getInventory().addItem(book);

            // Mensaje de éxito
            String enchantDisplayName = getConfig().getString("enchants." + enchantName + ".name");
            targetPlayer.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                    getConfig().getString("messages.book-given")
                            .replace("%enchant%", enchantDisplayName)
                            .replace("%level%", String.valueOf(level))));

            if (sender != targetPlayer) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix") +
                        " &aHas dado un libro de encantamiento " + enchantDisplayName + " " + level +
                        " a " + targetPlayer.getName()));
            }

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Verificar si el jugador está haciendo clic en su propio inventario
        if (event.getClickedInventory() == null ||
                (event.getClickedInventory().getType() != InventoryType.PLAYER &&
                        event.getClickedInventory().getType() != InventoryType.CRAFTING)) {
            return;
        }

        // Verificar si el jugador está arrastrando un libro encantado sobre un item
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || current == null ||
                cursor.getType() != Material.ENCHANTED_BOOK ||
                current.getType() == Material.AIR) {
            return;
        }

        // Verificar si el libro es un libro de encantamiento del plugin
        String enchantName = getEnchantFromBook(cursor);
        if (enchantName == null) {
            return;
        }

        int level = getEnchantLevelFromBook(cursor, enchantName);
        if (level <= 0) {
            return;
        }

        // Cancelar el evento para evitar que el libro se coloque en el slot
        event.setCancelled(true);

        // Aplicar el encantamiento al item
        ItemStack itemCopy = current.clone();
        applyEnchant(itemCopy, enchantName, level);

        // Actualizar el item en el inventario
        event.setCurrentItem(itemCopy);

        // Quitar el libro del cursor (consumirlo)
        player.setItemOnCursor(null);

        // Mensaje de éxito
        String enchantDisplayName = getConfig().getString("enchants." + enchantName + ".name");
        player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.book-applied")
                        .replace("%enchant%", enchantDisplayName)
                        .replace("%level%", String.valueOf(level))));
    }

    /**
     * Crea un libro encantado con el encantamiento especificado
     */
    private ItemStack createEnchantedBook(String enchantName, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta meta = book.getItemMeta();

        String enchantDisplayName = getConfig().getString("enchants." + enchantName + ".name");
        meta.setDisplayName(colorize("&b" + enchantDisplayName + " " + level));

        List<String> lore = new ArrayList<>();
        lore.add(colorize("&7Encantamiento: &b" + enchantDisplayName));
        lore.add(colorize("&7Nivel: &b" + level));
        lore.add("");

        // Añadir descripción del encantamiento
        String description = getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");
        if (description != null && !description.isEmpty()) {
            lore.add(colorize(description));
            lore.add("");
        }

        // Añadir instrucciones
        lore.add(colorize("&eArrastra este libro sobre un item"));
        lore.add(colorize("&epara aplicar el encantamiento"));

        // Añadir un identificador oculto para el plugin
        lore.add(enchantName + ":" + level);

        meta.setLore(lore);
        book.setItemMeta(meta);

        return book;
    }

    /**
     * Obtiene el nombre del encantamiento de un libro
     */
    private String getEnchantFromBook(ItemStack book) {
        if (book != null && book.getType() == Material.ENCHANTED_BOOK &&
                book.hasItemMeta() && book.getItemMeta().hasLore()) {

            List<String> lore = book.getItemMeta().getLore();
            for (String line : lore) {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        return parts[0];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Obtiene el nivel del encantamiento de un libro
     */
    private int getEnchantLevelFromBook(ItemStack book, String enchantName) {
        if (book != null && book.getType() == Material.ENCHANTED_BOOK &&
                book.hasItemMeta() && book.getItemMeta().hasLore()) {

            List<String> lore = book.getItemMeta().getLore();
            for (String line : lore) {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2 && parts[0].equals(enchantName)) {
                        try {
                            return Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                }
            }
        }
        return 0;
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