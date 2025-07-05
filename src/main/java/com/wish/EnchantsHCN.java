package com.wish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

            // Verificar que el item es compatible con el encantamiento
            if (!isItemValidForEnchant(item, enchantName)) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.book-invalid-item")));
                return true;
            }

            // NUEVO: Verificar si el item ya tiene el encantamiento
            if (hasEnchant(item, enchantName)) {
                player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.enchant-already-applied", "&cEste item ya tiene el encantamiento %enchant%")
                                .replace("%enchant%", getConfig().getString("enchants." + enchantName + ".name"))));
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

        // Verificar si el arma tiene el encantamiento Halloweenefy
        if (hasHalloweenefyEnchant(weapon)) {
            int level = getHalloweenefyLevel(weapon);
            int chance = getConfig().getInt("enchants.halloweenefy.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de calabaza
                applyPumpkinEffect(attacker, victim);
            }
        }

        // Verificar si el arma tiene el encantamiento Poison
        if (hasPoisonEnchant(weapon)) {
            int level = getPoisonLevel(weapon);
            int chance = getConfig().getInt("enchants.poison.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de veneno
                applyPoisonEffect(attacker, victim, level);
            }
        }

        // Verificar si el arma tiene el encantamiento Slowness
        if (hasSlownessEnchant(weapon)) {
            int level = getSlownessLevel(weapon);
            int chance = getConfig().getInt("enchants.slowness.levels." + level + ".chance");

            // Verificar si el encantamiento se activa según la probabilidad
            if (random.nextInt(100) < chance) {
                // Aplicar efecto de lentitud
                applySlownessEffect(attacker, victim, level);
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
        EnchantInfo info = getEnchantInfoFromBook(cursor);
        if (info == null) {
            return;
        }

        // Verificar si el item es compatible con el encantamiento
        if (!isItemValidForEnchant(current, info.enchantName)) {
            player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                    getConfig().getString("messages.book-invalid-item")));
            return;
        }

        // NUEVO: Verificar si el item ya tiene el encantamiento
        if (hasEnchant(current, info.enchantName)) {
            player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                    getConfig().getString("messages.enchant-already-applied", "&cEste item ya tiene el encantamiento %enchant%")
                            .replace("%enchant%", getConfig().getString("enchants." + info.enchantName + ".name"))));
            return;
        }

        // La detección de eventos funciona, ahora registremos un mensaje para verificar
        getLogger().info("Detectado uso de libro encantado: " + info.enchantName + " nivel " + info.level);

        // Cancelar el evento original
        event.setCancelled(true);

        // Crear copia del item para aplicar el encantamiento
        ItemStack itemCopy = current.clone();
        applyEnchant(itemCopy, info.enchantName, info.level);

        // Actualizar el item en el inventario (establece el item modificado)
        event.setCurrentItem(itemCopy);

        // Consumir el libro (quitar del cursor)
        player.setItemOnCursor(null);

        // Mensaje al jugador
        String enchantDisplayName = getConfig().getString("enchants." + info.enchantName + ".name");
        player.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.book-applied")
                        .replace("%enchant%", enchantDisplayName)
                        .replace("%level%", String.valueOf(info.level))));
    }

    /**
     * Aplica el efecto de calabaza a un jugador
     */
    private void applyPumpkinEffect(Player attacker, Player victim) {
        // Obtener el casco actual del jugador
        ItemStack helmet = victim.getInventory().getHelmet();
        ItemStack pumpkin = new ItemStack(Material.PUMPKIN, 1);

        // Si el jugador tiene un casco, quitarlo y darle una calabaza
        if (helmet != null && helmet.getType() != Material.AIR) {
            // Guardar el casco en el inventario o tirarlo al suelo
            if (victim.getInventory().firstEmpty() != -1) {
                victim.getInventory().addItem(helmet);
                victim.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.helmet-inventory")));
            } else {
                victim.getWorld().dropItem(victim.getLocation(), helmet);
                victim.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                        getConfig().getString("messages.helmet-dropped")));
            }
        }

        // Colocar la calabaza en la cabeza
        victim.getInventory().setHelmet(pumpkin);

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.pumpkin-placed")
                        .replace("%player%", victim.getName())));

        victim.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.pumpkin-received")
                        .replace("%player%", attacker.getName())));
    }

    /**
     * Aplica el efecto de veneno a un jugador
     */
    private void applyPoisonEffect(Player attacker, Player victim, int level) {
        // Obtener la duración y amplitud del efecto desde la configuración
        int duration = getConfig().getInt("enchants.poison.levels." + level + ".duration", 5);
        int amplifier = getConfig().getInt("enchants.poison.levels." + level + ".amplifier", 0);

        // Aplicar efecto de veneno
        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, amplifier));

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.poison-applied")
                        .replace("%player%", victim.getName())
                        .replace("%duration%", String.valueOf(duration))));

        victim.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.poison-received")
                        .replace("%player%", attacker.getName())
                        .replace("%duration%", String.valueOf(duration))));
    }

    /**
     * Aplica el efecto de lentitud a un jugador
     */
    private void applySlownessEffect(Player attacker, Player victim, int level) {
        // Obtener la duración y amplitud del efecto desde la configuración
        int duration = getConfig().getInt("enchants.slowness.levels." + level + ".duration", 4);
        int amplifier = getConfig().getInt("enchants.slowness.levels." + level + ".amplifier", 0);

        // Aplicar efecto de lentitud
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, amplifier));

        // Mensajes a los jugadores
        attacker.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.slowness-applied")
                        .replace("%player%", victim.getName())
                        .replace("%duration%", String.valueOf(duration))));

        victim.sendMessage(colorize(getConfig().getString("messages.prefix") + " " +
                getConfig().getString("messages.slowness-received")
                        .replace("%player%", attacker.getName())
                        .replace("%duration%", String.valueOf(duration))));
    }

    /**
     * Verifica si un item es compatible con un encantamiento específico
     */
    private boolean isItemValidForEnchant(ItemStack item, String enchantName) {
        if (enchantName.equals("soulbound")) {
            // Verificar si el item está en la lista de items restringidos
            List<String> restrictedItems = getConfig().getStringList("enchants.soulbound.restricted-items");
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
        } else if (enchantName.equals("halloweenefy") || enchantName.equals("poison") || enchantName.equals("slowness")) {
            // Verificar si el item está en la lista de items permitidos
            List<String> allowedItems = getConfig().getStringList("enchants." + enchantName + ".allowed-items");
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
     * Clase interna para almacenar información del encantamiento
     */
    private class EnchantInfo {
        String enchantName;
        int level;

        public EnchantInfo(String enchantName, int level) {
            this.enchantName = enchantName;
            this.level = level;
        }
    }

    /**
     * Obtiene la información del encantamiento de un libro
     */
    private EnchantInfo getEnchantInfoFromBook(ItemStack book) {
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
    private ItemStack createEnchantedBook(String enchantName, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta meta = book.getItemMeta();

        String enchantDisplayName = getConfig().getString("enchants." + enchantName + ".name");

        // Usar mensaje configurable para el título del libro
        String bookTitle = getConfig().getString("messages.book-title", "&b%enchant% %level%")
                .replace("%enchant%", enchantDisplayName)
                .replace("%level%", String.valueOf(level));
        meta.setDisplayName(colorize(bookTitle));

        List<String> lore = new ArrayList<>();

        // Usar mensajes configurables para la información del encantamiento
        String enchantInfo = getConfig().getString("messages.book-enchant-info", "&7Encantamiento: &b%enchant%")
                .replace("%enchant%", enchantDisplayName);
        String levelInfo = getConfig().getString("messages.book-level-info", "&7Nivel: &b%level%")
                .replace("%level%", String.valueOf(level));

        lore.add(colorize(enchantInfo));
        lore.add(colorize(levelInfo));
        lore.add("");

        // Añadir descripción del encantamiento
        String description = getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");
        if (description != null && !description.isEmpty()) {
            lore.add(colorize(description));
            lore.add("");
        }

        // Añadir instrucciones configurables
        String instructions1 = getConfig().getString("messages.book-instructions-1", "&eArrastra este libro sobre un item");
        String instructions2 = getConfig().getString("messages.book-instructions-2", "&epara aplicar el encantamiento");

        lore.add(colorize(instructions1));
        lore.add(colorize(instructions2));

        // Añadir identificador completamente invisible usando formato mágico y color de texto invisible
        // El formato &k hace que el texto sea ilegible y &0 lo hace negro (casi invisible)
        lore.add(colorize("&0&k" + enchantName + ":" + level));

        meta.setLore(lore);
        book.setItemMeta(meta);

        return book;
    }

    private void applyEnchant(ItemStack item, String enchantName, int level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Remover encantamiento existente si ya existe
        removeEnchant(item, enchantName);

        // Agregar el nuevo encantamiento
        List<String> enchantLores = getConfig().getStringList("enchants." + enchantName + ".levels." + level + ".lore");
        String enchantDescription = getConfig().getString("enchants." + enchantName + ".levels." + level + ".description");

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

    private void removeEnchant(ItemStack item, String enchantName) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>();

            // Para cada nivel del encantamiento
            for (int i = 1; i <= getConfig().getInt("enchants." + enchantName + ".max-level", 0); i++) {
                // Obtener todas las líneas de lore para este nivel
                List<String> enchantLores = getConfig().getStringList("enchants." + enchantName + ".levels." + i + ".lore");
                // Obtener la descripción para este nivel
                String enchantDescription = getConfig().getString("enchants." + enchantName + ".levels." + i + ".description", "");

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
     * NUEVO: Método general para verificar si un item tiene un encantamiento específico
     */
    private boolean hasEnchant(ItemStack item, String enchantName) {
        if (enchantName.equals("soulbound")) {
            return hasSoulboundEnchant(item);
        } else if (enchantName.equals("halloweenefy")) {
            return hasHalloweenefyEnchant(item);
        } else if (enchantName.equals("poison")) {
            return hasPoisonEnchant(item);
        } else if (enchantName.equals("slowness")) {
            return hasSlownessEnchant(item);
        }

        // Si es un encantamiento que no conocemos específicamente, hacer una verificación genérica
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants." + enchantName + ".max-level", 0); i++) {
                List<String> enchantLores = getConfig().getStringList("enchants." + enchantName + ".levels." + i + ".lore");
                String enchantDescription = getConfig().getString("enchants." + enchantName + ".levels." + i + ".description", "");

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

    private boolean hasSoulboundEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants.soulbound.max-level"); i++) {
                List<String> enchantLores = getConfig().getStringList("enchants.soulbound.levels." + i + ".lore");
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

    private int getSoulboundLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = getConfig().getInt("enchants.soulbound.max-level"); i >= 1; i--) {
                List<String> enchantLores = getConfig().getStringList("enchants.soulbound.levels." + i + ".lore");
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

    private boolean hasHalloweenefyEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants.halloweenefy.max-level"); i++) {
                List<String> enchantLores = getConfig().getStringList("enchants.halloweenefy.levels." + i + ".lore");
                String enchantDescription = getConfig().getString("enchants.halloweenefy.levels." + i + ".description");

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

    private int getHalloweenefyLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = getConfig().getInt("enchants.halloweenefy.max-level"); i >= 1; i--) {
                List<String> enchantLores = getConfig().getStringList("enchants.halloweenefy.levels." + i + ".lore");
                String enchantDescription = getConfig().getString("enchants.halloweenefy.levels." + i + ".description");

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

    private boolean hasPoisonEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants.poison.max-level"); i++) {
                List<String> enchantLores = getConfig().getStringList("enchants.poison.levels." + i + ".lore");
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

    private int getPoisonLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = getConfig().getInt("enchants.poison.max-level"); i >= 1; i--) {
                List<String> enchantLores = getConfig().getStringList("enchants.poison.levels." + i + ".lore");
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

    private boolean hasSlownessEnchant(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 1; i <= getConfig().getInt("enchants.slowness.max-level"); i++) {
                List<String> enchantLores = getConfig().getStringList("enchants.slowness.levels." + i + ".lore");
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

    private int getSlownessLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = getConfig().getInt("enchants.slowness.max-level"); i >= 1; i--) {
                List<String> enchantLores = getConfig().getStringList("enchants.slowness.levels." + i + ".lore");
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