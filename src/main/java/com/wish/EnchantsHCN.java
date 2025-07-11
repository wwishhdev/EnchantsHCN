package com.wish;

import com.wish.managers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantsHCN extends JavaPlugin {

    private EnchantManager enchantManager;
    private EffectManager effectManager;
    private BookManager bookManager;
    private CommandManager commandManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Inicializar managers
        enchantManager = new EnchantManager(this);
        effectManager = new EffectManager(this);
        bookManager = new BookManager(this);
        commandManager = new CommandManager(this, enchantManager, bookManager);
        eventManager = new EventManager(this, enchantManager, effectManager, bookManager);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(eventManager, this);

        // Registrar comandos
        getCommand("enchantshcn").setExecutor(commandManager);
        getCommand("enchantshcnreload").setExecutor(commandManager);
        getCommand("enchantshcnbook").setExecutor(commandManager);

        getLogger().info("EnchantsHCN ha sido habilitado! by wwishhdev <3");
    }

    @Override
    public void onDisable() {
        // Limpiar todas las cápsulas de hielo activas
        if (effectManager != null) {
            effectManager.cleanupAllIceCapsules();
        }
        getLogger().info("EnchantsHCN ha sido deshabilitado! by wwishhdev <3");
    }

    // Getters para acceder a los managers desde otras clases si es necesario
    public EnchantManager getEnchantManager() {
        return enchantManager;
    }
    
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    public BookManager getBookManager() {
        return bookManager;
    }
}