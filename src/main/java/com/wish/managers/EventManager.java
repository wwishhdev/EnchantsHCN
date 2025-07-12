package com.wish.managers;

import com.wish.EnchantsHCN;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EventManager implements Listener {
    
    private final EnchantsHCN plugin;
    private final EnchantManager enchantManager;
    private final EffectManager effectManager;
    private final BookManager bookManager;
    private final Random random = new Random();
    
    public EventManager(EnchantsHCN plugin, EnchantManager enchantManager, EffectManager effectManager, BookManager bookManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.effectManager = effectManager;
        this.bookManager = bookManager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> keepItems = new ArrayList<>();

        // Revisar todos los items que dropea el jugador
        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (enchantManager.hasSoulboundEnchant(item)) {
                int level = enchantManager.getSoulboundLevel(item);
                int chance = plugin.getConfig().getInt("enchants.soulbound.levels." + level + ".chance");

                // Verificar si el encantamiento se activa según la probabilidad
                if (random.nextInt(100) < chance) {
                    // Crear una copia del item sin el encantamiento SoulBound
                    ItemStack itemCopy = item.clone();
                    enchantManager.removeEnchant(itemCopy, "soulbound");

                    // El item (sin SoulBound) se guarda
                    keepItems.add(itemCopy);

                    // Eliminarlo de los drops
                    iterator.remove();
                }
            }
        }

        if (!keepItems.isEmpty()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (ItemStack item : keepItems) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
                player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " &aAlgunos de tus items fueron protegidos por SoulBound"));
            }, 5L);
        }
    }
    
    // Cambiamos la prioridad a HIGH para que se ejecute después de WorldGuard
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Verificar si el evento ya ha sido cancelado por otro plugin (como WorldGuard en zonas no-PvP)
        if (event.isCancelled()) {
            return; // Si el evento está cancelado, no aplicamos ningún efecto
        }

        // Verificar si el atacante es un jugador
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        // Verificar si el afectado es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // Verificar si el atacante tiene un item en la mano
        ItemStack weapon = attacker.getItemInHand();
        if (weapon == null || weapon.getType() == Material.AIR) {
            return;
        }

        // Verificar si se debe aplicar efectos (clanes/aliados)
        // Esta verificación se hace aquí para evitar repetir en cada efecto
        boolean shouldApplyEffects = plugin.getClanHook().shouldApplyEffect(attacker, victim);

        // Verificar si el arma tiene el encantamiento Halloweenefy
        if (enchantManager.hasHalloweenefyEnchant(weapon) && shouldApplyEffects) {
            int level = enchantManager.getHalloweenefyLevel(weapon);
            int chance = plugin.getConfig().getInt("enchants.halloweenefy.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de calabaza
                effectManager.applyPumpkinEffect(attacker, victim);
            }
        }

        // Verificar si el arma tiene el encantamiento Poison
        if (enchantManager.hasPoisonEnchant(weapon) && shouldApplyEffects) {
            int level = enchantManager.getPoisonLevel(weapon);
            int chance = plugin.getConfig().getInt("enchants.poison.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de veneno
                effectManager.applyPoisonEffect(attacker, victim, level);
            }
        }

        // Verificar si el arma tiene el encantamiento Slowness
        if (enchantManager.hasSlownessEnchant(weapon) && shouldApplyEffects) {
            int level = enchantManager.getSlownessLevel(weapon);
            int chance = plugin.getConfig().getInt("enchants.slowness.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de lentitud
                effectManager.applySlownessEffect(attacker, victim, level);
            }
        }

        // Verificar si el arma tiene el encantamiento Ice Aspect
        if (enchantManager.hasIceAspectEnchant(weapon) && shouldApplyEffects) {
            int level = enchantManager.getIceAspectLevel(weapon);
            int chance = plugin.getConfig().getInt("enchants.iceaspect.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de cápsula de hielo
                effectManager.applyIceEffect(attacker, victim, level);
            }
        }
    }
    
    // Usamos prioridad HIGHEST para que nuestro evento se ejecute después de otros eventos
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || current == null) {
            return;
        }

        // Verificar si se está usando un libro encantado
        if (cursor.getType() != Material.ENCHANTED_BOOK || current.getType() == Material.AIR) {
            return;
        }

        // Obtener información del encantamiento del libro
        BookManager.EnchantInfo info = bookManager.getEnchantInfoFromBook(cursor);
        if (info == null) {
            return;
        }

        // Verificar si el item es compatible con el encantamiento
        if (!enchantManager.isItemValidForEnchant(current, info.enchantName)) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.book-invalid-item")));
            return;
        }

        // Verificar si el item ya tiene el encantamiento
        if (enchantManager.hasEnchant(current, info.enchantName)) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                    plugin.getConfig().getString("messages.enchant-already-applied", "&cEste item ya tiene el encantamiento %enchant%")
                            .replace("%enchant%", plugin.getConfig().getString("enchants." + info.enchantName + ".name"))));
            return;
        }

        // La detección de eventos funciona, ahora registremos un mensaje para verificar
        plugin.getLogger().info("Detectado uso de libro encantado: " + info.enchantName + " nivel " + info.level);

        // Cancelar el evento original
        event.setCancelled(true);

        // Crear copia del item para aplicar el encantamiento
        ItemStack itemCopy = current.clone();
        enchantManager.applyEnchant(itemCopy, info.enchantName, info.level);

        // Actualizar el item en el inventario (establece el item modificado)
        event.setCurrentItem(itemCopy);

        // Consumir el libro (quitar del cursor)
        player.setItemOnCursor(null);

        // Mensaje al jugador
        String enchantDisplayName = plugin.getConfig().getString("enchants." + info.enchantName + ".name");
        player.sendMessage(colorize(plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.book-applied")
                        .replace("%enchant%", enchantDisplayName)
                        .replace("%level%", String.valueOf(info.level))));
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}