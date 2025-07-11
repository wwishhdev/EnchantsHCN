package com.wish.managers;

import com.wish.EnchantsHCN;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EffectManager {
    
    private final EnchantsHCN plugin;
    private Map<UUID, List<org.bukkit.Location>> iceCapsules = new HashMap<>();
    
    public EffectManager(EnchantsHCN plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Aplica el efecto de calabaza a un jugador
     */
    public void applyPumpkinEffect(Player attacker, Player victim) {
        // Obtener el casco actual del jugador
        ItemStack helmet = victim.getInventory().getHelmet();
        ItemStack pumpkin = new ItemStack(Material.PUMPKIN, 1);

        // Si el jugador tiene un casco, quitarlo y darle una calabaza
        if (helmet != null && helmet.getType() != Material.AIR) {
            // Guardar el casco en el inventario o tirarlo al suelo
            if (victim.getInventory().firstEmpty() != -1) {
                victim.getInventory().addItem(helmet);
                victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                        plugin.getConfig().getString("messages.helmet-inventory")));
            } else {
                victim.getWorld().dropItem(victim.getLocation(), helmet);
                victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                        plugin.getConfig().getString("messages.helmet-dropped")));
            }
        }

        // Colocar la calabaza en la cabeza
        victim.getInventory().setHelmet(pumpkin);

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.pumpkin-placed")
                        .replace("%player%", victim.getName())));

        victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.pumpkin-received")
                        .replace("%player%", attacker.getName())));
    }
    
    /**
     * Aplica el efecto de veneno a un jugador
     */
    public void applyPoisonEffect(Player attacker, Player victim, int level) {
        // Obtener la duración y amplitud del efecto desde la configuración
        int duration = plugin.getConfig().getInt("enchants.poison.levels." + level + ".duration", 5);
        int amplifier = plugin.getConfig().getInt("enchants.poison.levels." + level + ".amplifier", 0);

        // Aplicar efecto de veneno
        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, amplifier));

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.poison-applied")
                        .replace("%player%", victim.getName())
                        .replace("%duration%", String.valueOf(duration))));

        victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.poison-received")
                        .replace("%player%", attacker.getName())
                        .replace("%duration%", String.valueOf(duration))));
    }
    
    /**
     * Aplica el efecto de lentitud a un jugador
     */
    public void applySlownessEffect(Player attacker, Player victim, int level) {
        // Obtener la duración y amplitud del efecto desde la configuración
        int duration = plugin.getConfig().getInt("enchants.slowness.levels." + level + ".duration", 4);
        int amplifier = plugin.getConfig().getInt("enchants.slowness.levels." + level + ".amplifier", 0);

        // Aplicar efecto de lentitud
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, amplifier));

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.slowness-applied")
                        .replace("%player%", victim.getName())
                        .replace("%duration%", String.valueOf(duration))));

        victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.slowness-received")
                        .replace("%player%", attacker.getName())
                        .replace("%duration%", String.valueOf(duration))));
    }
    
    /**
     * Aplica el efecto de cápsula de hielo a un jugador
     */
    public void applyIceEffect(Player attacker, Player victim, int level) {
        // Obtener la duración del efecto desde la configuración
        int duration = plugin.getConfig().getInt("enchants.iceaspect.levels." + level + ".duration", 5);

        // Crear la cápsula de hielo
        createIceCapsule(victim, duration);

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.ice-applied")
                        .replace("%player%", victim.getName())
                        .replace("%duration%", String.valueOf(duration))));

        victim.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.ice-received")
                        .replace("%player%", attacker.getName())
                        .replace("%duration%", String.valueOf(duration))));
    }
    
    /**
     * Crea una cápsula de hielo alrededor del jugador
     */
    private void createIceCapsule(Player player, int duration) {
        org.bukkit.Location center = player.getLocation();
        List<org.bukkit.Location> iceBlocks = new ArrayList<>();
        
        // Radio de la cápsula esférica
        double radius = 2.5;
        
        // Crear una cápsula de hielo esférica hueca más grande
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = -3; z <= 3; z++) {
                    // Calcular la distancia desde el centro
                    double distance = Math.sqrt(x * x + (y - 2) * (y - 2) + z * z);
                    
                    // Solo crear bloques en el borde de la esfera (cápsula hueca)
                    if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                        // No crear bloques en el suelo central para que los jugadores puedan estar de pie
                        if (!(y == 0 && Math.abs(x) <= 1 && Math.abs(z) <= 1)) {
                            org.bukkit.Location blockLoc = center.clone().add(x, y, z);
                            
                            // Solo reemplazar bloques de aire o bloques no sólidos
                            if (blockLoc.getBlock().getType() == Material.AIR || 
                                !blockLoc.getBlock().getType().isSolid()) {
                                
                                // Guardar el bloque original
                                iceBlocks.add(blockLoc.clone());
                                
                                // Colocar hielo
                                blockLoc.getBlock().setType(Material.ICE);
                            }
                        }
                    }
                }
            }
        }
        
        // Guardar los bloques de hielo para este jugador
        iceCapsules.put(player.getUniqueId(), iceBlocks);
        
        // Programar la eliminación de la cápsula
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            removeIceCapsule(player.getUniqueId());
        }, duration * 20L); // Convertir segundos a ticks
    }
    
    /**
     * Elimina la cápsula de hielo de un jugador
     */
    public void removeIceCapsule(UUID playerUUID) {
        List<org.bukkit.Location> iceBlocks = iceCapsules.get(playerUUID);
        if (iceBlocks != null) {
            for (org.bukkit.Location loc : iceBlocks) {
                // Restaurar el bloque original (aire)
                if (loc.getBlock().getType() == Material.ICE) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
            iceCapsules.remove(playerUUID);
        }
    }
    
    /**
     * Limpia todas las cápsulas de hielo activas
     */
    public void cleanupAllIceCapsules() {
        for (UUID playerUUID : new ArrayList<>(iceCapsules.keySet())) {
            removeIceCapsule(playerUUID);
        }
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}