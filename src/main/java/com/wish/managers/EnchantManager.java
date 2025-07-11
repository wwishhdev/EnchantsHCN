package com.wish.managers;

import com.wish.EnchantsHCN;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnchantManager {
    
    private final EnchantsHCN plugin;
    
    public EnchantManager(EnchantsHCN plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Verifica si un item es compatible con un encantamiento específico
     */
    public boolean isItemValidForEnchant(ItemStack item, String enchantName) {
        if (enchantName.equals("soulbound")) {
            // Verificar si el item está en la lista de items restringidos
            List<String> restrictedItems = plugin.getConfig().getStringList("enchants.soulbound.restricted-items");
            for (String restrictedItem : restrictedItems) {
                if (restrictedItem.contains(":")) {
                    // Formato con metadatos, ej: "322:1"
                    String[] parts = restrictedItem.split(":");
                    int id = Integer.parseInt(parts[0]);
                    short data = Short.parseShort(parts[1]);

                    if (item.getTypeId() == id && item.getDurability() == data) {
                        return false;
                    }
                } else if (restrictedItem.matches("\\d+")) {
                    // Formato numérico, ej: "322"
                    int id = Integer.parseInt(restrictedItem);
                    if (item.getTypeId() == id) {
                        return false;
                    }
                } else {
                    // Formato Material, ej: "POTION"
                    try {
                        Material material = Material.valueOf(restrictedItem);
                        if (item.getType() == material) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignorar materiales inválidos
                    }
                }
            }
            return true;
        } else if (enchantName.equals("halloweenefy") || enchantName.equals("poison") || enchantName.equals("slowness") || enchantName.equals("iceaspect")) {
            // Verificar si el item está en la lista de items permitidos
            List<String> allowedItems = plugin.getConfig().getStringList("enchants." + enchantName + ".allowed-items");
            for (String allowedItem : allowedItems) {
                try {
                    Material material = Material.valueOf(allowedItem);
                    if (item.getType() == material) {
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorar materiales inválidos
                }
            }
            return false;
        }
        return true;
    }
    
    /**
     * Aplica un encantamiento a un item
     */
    public void applyEnchant(ItemStack item, String enchantName, int level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Remover encantamiento existente si ya existe
        removeEnchant(item, enchantName);

        // Agregar el nuevo encantamiento
        List<String> enchantLores = plugin.getConfig().getStringList("enchants." + enchantName + ".levels." + level + ".lore");
        String enchantDescription = plugin.getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");

        // Añadir cada línea de lore del encantamiento
        for (String loreLine : enchantLores) {
            lore.add(colorize(loreLine));
        }

        // Añadir descripción si existe
        if (enchantDescription != null && !enchantDescription.isEmpty()) {
            lore.add(colorize(enchantDescription));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * Remueve un encantamiento de un item
     */
    public void removeEnchant(ItemStack item, String enchantName) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>();

            // Para cada nivel del encantamiento
            for (int i = 1; i <= plugin.getConfig().getInt("enchants." + enchantName + ".max-level", 0); i++) {
                // Obtener todas las líneas de lore para este nivel
                List<String> enchantLores = plugin.getConfig().getStringList("enchants." + enchantName + ".levels." + i + ".lore");
                // Obtener la descripción para este nivel
                String enchantDescription = plugin.getConfig().getString("enchants." + enchantName + ".levels." + i + ".description", "");

                // Colorizar cada línea para poder comparar correctamente
                List<String> colorizedLores = new ArrayList<>();
                for (String loreLine : enchantLores) {
                    colorizedLores.add(colorize(loreLine));
                }
                String colorizedDescription = colorize(enchantDescription);

                // Eliminar líneas que coincidan con las del encantamiento
                for (String line : lore) {
                    // Si la línea no está en el lore del encantamiento y no es la descripción, mantenerla
                    if (!colorizedLores.contains(line) && !line.equals(colorizedDescription)) {
                        newLore.add(line);
                    }
                }

                // Si hemos removido algún lore, actualizar la lista para la siguiente iteración
                if (lore.size() != newLore.size()) {
                    lore = new ArrayList<>(newLore);
                    newLore.clear();
                }
            }

            // Si al final tenemos una lista, usarla como el nuevo lore
            if (!lore.isEmpty()) {
                meta.setLore(lore);
                item.setItemMeta(meta);
            } else {
                // Si no hay lore, eliminar completamente el lore
                meta.setLore(null);
                item.setItemMeta(meta);
            }
        }
    }
    
    /**
     * Método general para verificar si un item tiene un encantamiento específico
     */
    public boolean hasEnchant(ItemStack item, String enchantName) {
        if (enchantName.equals("soulbound")) {
            return hasSoulboundEnchant(item);
        } else if (enchantName.equals("halloweenefy")) {
            return hasHalloweenefyEnchant(item);
        } else if (enchantName.equals("poison")) {
            return hasPoisonEnchant(item);
        } else if (enchantName.equals("slowness")) {
            return hasSlownessEnchant(item);
        } else if (enchantName.equals("iceaspect")) {
            return hasIceAspectEnchant(item);
        }

        // Si es un encantamiento que no conocemos específicamente, hacer una verificación genérica
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants." + enchantName + ".max-level", 0); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants." + enchantName + ".levels." + i + ".lore");
                String enchantDescription = plugin.getConfig().getString("enchants." + enchantName + ".levels." + i + ".description", "");

                // Verificar si alguna línea del lore coincide
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }

                // Verificar si la descripción coincide
                if (enchantDescription != null && !enchantDescription.isEmpty()) {
                    String colorized = colorize(enchantDescription);
                    if (lore.contains(colorized)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // Métodos específicos para cada encantamiento
    public boolean hasSoulboundEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants.soulbound.max-level"); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.soulbound.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getSoulboundLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = plugin.getConfig().getInt("enchants.soulbound.max-level"); i >= 1; i--) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.soulbound.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return i;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public boolean hasHalloweenefyEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants.halloweenefy.max-level"); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.halloweenefy.levels." + i + ".lore");
                String enchantDescription = plugin.getConfig().getString("enchants.halloweenefy.levels." + i + ".description");

                // Verificar si alguna línea del lore coincide
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }

                // Verificar si la descripción coincide
                if (enchantDescription != null && !enchantDescription.isEmpty()) {
                    String colorized = colorize(enchantDescription);
                    if (lore.contains(colorized)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getHalloweenefyLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = plugin.getConfig().getInt("enchants.halloweenefy.max-level"); i >= 1; i--) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.halloweenefy.levels." + i + ".lore");
                String enchantDescription = plugin.getConfig().getString("enchants.halloweenefy.levels." + i + ".description");

                // Verificar si alguna línea del lore coincide
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return i;
                        }
                    }
                }

                // Verificar si la descripción coincide
                if (enchantDescription != null && !enchantDescription.isEmpty()) {
                    String colorized = colorize(enchantDescription);
                    if (lore.contains(colorized)) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    public boolean hasPoisonEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants.poison.max-level"); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.poison.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getPoisonLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = plugin.getConfig().getInt("enchants.poison.max-level"); i >= 1; i--) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.poison.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return i;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public boolean hasSlownessEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants.slowness.max-level"); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.slowness.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getSlownessLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = plugin.getConfig().getInt("enchants.slowness.max-level"); i >= 1; i--) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.slowness.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return i;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public boolean hasIceAspectEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= plugin.getConfig().getInt("enchants.iceaspect.max-level"); i++) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.iceaspect.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getIceAspectLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = plugin.getConfig().getInt("enchants.iceaspect.max-level"); i >= 1; i--) {
                List<String> enchantLores = plugin.getConfig().getStringList("enchants.iceaspect.levels." + i + ".lore");
                for (String loreLine : enchantLores) {
                    if (loreLine != null && !loreLine.isEmpty()) {
                        String colorized = colorize(loreLine);
                        if (lore.contains(colorized)) {
                            return i;
                        }
                    }
                }
            }
        }
        return 0;
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}