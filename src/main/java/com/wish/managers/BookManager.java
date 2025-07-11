package com.wish.managers;

import com.wish.EnchantsHCN;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BookManager {
    
    private final EnchantsHCN plugin;
    
    public BookManager(EnchantsHCN plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Clase interna para almacenar información del encantamiento
     */
    public static class EnchantInfo {
        public String enchantName;
        public int level;

        public EnchantInfo(String enchantName, int level) {
            this.enchantName = enchantName;
            this.level = level;
        }
    }
    
    /**
     * Obtiene la información del encantamiento de un libro
     */
    public EnchantInfo getEnchantInfoFromBook(ItemStack book) {
        if (book != null && book.getType() == Material.ENCHANTED_BOOK &&
                book.hasItemMeta() && book.getItemMeta().hasLore()) {

            List<String> lore = book.getItemMeta().getLore();
            for (String line : lore) {
                // Remover códigos de color para la verificación
                String stripped = ChatColor.stripColor(line);
                if (stripped.contains(":")) {
                    String[] parts = stripped.split(":");
                    if (parts.length == 2) {
                        try {
                            int level = Integer.parseInt(parts[1]);
                            return new EnchantInfo(parts[0], level);
                        } catch (NumberFormatException e) {
                            // No es un número válido
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Crea un libro encantado con el encantamiento especificado
     */
    public ItemStack createEnchantedBook(String enchantName, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta meta = book.getItemMeta();

        String enchantDisplayName = plugin.getConfig().getString("enchants." + enchantName + ".name");

        // Usar mensaje configurable para el título del libro
        String bookTitle = plugin.getConfig().getString("messages.book-title", "&b%enchant% %level%")
                .replace("%enchant%", enchantDisplayName)
                .replace("%level%", String.valueOf(level));
        meta.setDisplayName(colorize(bookTitle));

        List<String> lore = new ArrayList<>();

        // Usar mensajes configurables para la información del encantamiento
        String enchantInfo = plugin.getConfig().getString("messages.book-enchant-info", "&7Encantamiento: &b%enchant%")
                .replace("%enchant%", enchantDisplayName);
        String levelInfo = plugin.getConfig().getString("messages.book-level-info", "&7Nivel: &b%level%")
                .replace("%level%", String.valueOf(level));

        lore.add(colorize(enchantInfo));
        lore.add(colorize(levelInfo));
        lore.add("");

        // Añadir descripción del encantamiento
        String description = plugin.getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");
        if (description != null && !description.isEmpty()) {
            lore.add(colorize(description));
            lore.add("");
        }

        // Añadir instrucciones configurables
        String instructions1 = plugin.getConfig().getString("messages.book-instructions-1", "&eArrastra este libro sobre un item");
        String instructions2 = plugin.getConfig().getString("messages.book-instructions-2", "&epara aplicar el encantamiento");

        lore.add(colorize(instructions1));
        lore.add(colorize(instructions2));

        // Añadir identificador completamente invisible usando formato mágico y color de texto invisible
        // El formato &k hace que el texto sea ilegible y &0 lo hace negro (casi invisible)
        lore.add(colorize("&0&k" + enchantName + ":" + level));

        meta.setLore(lore);
        book.setItemMeta(meta);

        return book;
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}