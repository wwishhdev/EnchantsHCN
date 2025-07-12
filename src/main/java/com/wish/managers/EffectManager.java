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
    private Map<UUID, Material> iceCapsuleMaterials = new HashMap<>();
    private Map<UUID, Byte> iceCapsuleDataValues = new HashMap<>();
    
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

        // Crear la cápsula de hielo centrada entre ambos jugadores
        createIceCapsule(attacker, victim, duration);

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
     * Crea un cubo de hielo alrededor de ambos jugadores
     */
    private void createIceCapsule(Player attacker, Player victim, int duration) {
        // Calcular la posición central entre ambos jugadores
        org.bukkit.Location attackerLoc = attacker.getLocation();
        org.bukkit.Location victimLoc = victim.getLocation();
        
        // Obtener el punto medio entre ambos jugadores
        double centerX = (attackerLoc.getX() + victimLoc.getX()) / 2.0;
        double centerZ = (attackerLoc.getZ() + victimLoc.getZ()) / 2.0;
        double centerY = Math.min(attackerLoc.getY(), victimLoc.getY()); // Usar la Y más baja
        
        // Crear la ubicación central en el suelo
        org.bukkit.Location center = new org.bukkit.Location(attackerLoc.getWorld(), 
                Math.floor(centerX) + 0.5, Math.floor(centerY), Math.floor(centerZ) + 0.5);
        List<org.bukkit.Location> iceBlocks = new ArrayList<>();
        
        // Obtener configuración del cubo
        int size = plugin.getConfig().getInt("enchants.iceaspect.cube.size", 6);
        boolean hollow = plugin.getConfig().getBoolean("enchants.iceaspect.cube.hollow", true);
        boolean replaceAirOnly = plugin.getConfig().getBoolean("enchants.iceaspect.cube.replace-air-only", true);
        
        // Obtener el material del cubo desde la configuración
        String materialName = plugin.getConfig().getString("enchants.iceaspect.cube.material", "STAINED_GLASS:3");
        Material cubeMaterial = Material.STAINED_GLASS;
        byte dataValue = 3; // Light blue por defecto
        
        // Parsear material y data value si está especificado
        if (materialName.contains(":")) {
            String[] parts = materialName.split(":");
            try {
                cubeMaterial = Material.valueOf(parts[0].toUpperCase());
                dataValue = Byte.parseByte(parts[1]);
            } catch (IllegalArgumentException e) {
                cubeMaterial = Material.STAINED_GLASS;
                dataValue = 3;
                plugin.getLogger().warning("Material inválido en configuración: " + materialName + ". Usando STAINED_GLASS:3 por defecto.");
            }
        } else {
            try {
                cubeMaterial = Material.valueOf(materialName.toUpperCase());
                dataValue = 0; // Sin data value para materiales simples
            } catch (IllegalArgumentException e) {
                cubeMaterial = Material.STAINED_GLASS;
                dataValue = 3;
                plugin.getLogger().warning("Material inválido en configuración: " + materialName + ". Usando STAINED_GLASS:3 por defecto.");
            }
        }
        
        // Calcular el rango del cubo (size/2 en cada dirección)
        int halfSize = size / 2;
        
        // Crear el cubo de hielo
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = 0; y <= size - 1; y++) { // Altura del cubo
                for (int z = -halfSize; z <= halfSize; z++) {
                    boolean shouldPlace = false;
                    
                    if (hollow) {
                        // Solo crear bloques en las paredes del cubo (cubo hueco)
                        // Paredes laterales, suelo y techo
                        if (x == -halfSize || x == halfSize || // Paredes X
                            z == -halfSize || z == halfSize || // Paredes Z
                            y == 0 || y == size - 1) {         // Suelo y techo
                            shouldPlace = true;
                        }
                    } else {
                        // Crear un cubo sólido
                        shouldPlace = true;
                    }
                    
                    if (shouldPlace) {
                        org.bukkit.Location blockLoc = center.clone().add(x, y, z);
                        
                        boolean canReplace = false;
                        if (replaceAirOnly) {
                            // Solo reemplazar bloques de aire
                            canReplace = blockLoc.getBlock().getType() == Material.AIR;
                        } else {
                            // Reemplazar aire o bloques no sólidos
                            canReplace = blockLoc.getBlock().getType() == Material.AIR || 
                                       !blockLoc.getBlock().getType().isSolid();
                        }
                        
                        if (canReplace) {
                            // Guardar el bloque original
                            iceBlocks.add(blockLoc.clone());
                            
                            // Colocar el material configurado con data value
                            blockLoc.getBlock().setType(cubeMaterial);
                            if (dataValue > 0) {
                                blockLoc.getBlock().setData(dataValue);
                            }
                        }
                    }
                }
            }
        }
        
        // Guardar los bloques de hielo para ambos jugadores
        iceCapsules.put(attacker.getUniqueId(), iceBlocks);
        iceCapsules.put(victim.getUniqueId(), iceBlocks);
        
        // Guardar el material y data value usado para ambos jugadores
        String materialData = cubeMaterial.name() + ":" + dataValue;
        iceCapsuleMaterials.put(attacker.getUniqueId(), Material.valueOf(materialData.split(":")[0]));
        iceCapsuleMaterials.put(victim.getUniqueId(), Material.valueOf(materialData.split(":")[0]));
        
        // Guardar data values por separado
        iceCapsuleDataValues.put(attacker.getUniqueId(), dataValue);
        iceCapsuleDataValues.put(victim.getUniqueId(), dataValue);
        
        // Programar la eliminación del cubo
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            removeIceCapsule(attacker.getUniqueId());
            removeIceCapsule(victim.getUniqueId());
        }, duration * 20L); // Convertir segundos a ticks
    }
    
    /**
     * Elimina la cápsula de hielo de un jugador
     */
    public void removeIceCapsule(UUID playerUUID) {
        List<org.bukkit.Location> iceBlocks = iceCapsules.get(playerUUID);
        Material capsuleMaterial = iceCapsuleMaterials.get(playerUUID);
        Byte capsuleDataValue = iceCapsuleDataValues.get(playerUUID);
        
        if (iceBlocks != null) {
            for (org.bukkit.Location loc : iceBlocks) {
                // Restaurar el bloque original (aire) si es el material y data value del cubo
                if (capsuleMaterial != null && loc.getBlock().getType() == capsuleMaterial) {
                    // Verificar data value si es necesario
                    if (capsuleDataValue != null && capsuleDataValue > 0) {
                        if (loc.getBlock().getData() == capsuleDataValue) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    } else {
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
            iceCapsules.remove(playerUUID);
            iceCapsuleMaterials.remove(playerUUID);
            iceCapsuleDataValues.remove(playerUUID);
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