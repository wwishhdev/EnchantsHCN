package com.wish.hooks;

import com.wish.EnchantsHCN;
import me.jose.advancedclans.AdvancedClans;
import me.jose.advancedclans.api.AdvancedClansApi;
import me.jose.advancedclans.objects.Clan;
import me.jose.advancedclans.objects.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClanHook {
    
    private final EnchantsHCN plugin;
    private AdvancedClansApi clansApi;
    private boolean enabled;
    private boolean preventClanDamage;
    private boolean preventAllyDamage;
    
    public ClanHook(EnchantsHCN plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("advancedclans.enabled", true);
        this.preventClanDamage = plugin.getConfig().getBoolean("advancedclans.prevent-clan-damage", true);
        this.preventAllyDamage = plugin.getConfig().getBoolean("advancedclans.prevent-ally-damage", true);
        
        if (enabled && isAdvancedClansAvailable()) {
            try {
                this.clansApi = Bukkit.getServicesManager().getRegistration(AdvancedClansApi.class).getProvider();
                plugin.getLogger().info("Hook con AdvancedClans activado exitosamente!");
            } catch (Exception e) {
                plugin.getLogger().warning("Error al conectar con AdvancedClans: " + e.getMessage());
                this.enabled = false;
            }
        } else {
            this.enabled = false;
            if (!isAdvancedClansAvailable()) {
                plugin.getLogger().info("AdvancedClans no encontrado, hook desactivado.");
            }
        }
    }
    
    /**
     * Verifica si AdvancedClans está disponible
     */
    private boolean isAdvancedClansAvailable() {
        return Bukkit.getPluginManager().getPlugin("AdvancedClans") != null;
    }
    
    /**
     * Verifica si el hook está habilitado
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Verifica si se debe prevenir el daño entre miembros del clan
     */
    public boolean shouldPreventClanDamage() {
        return preventClanDamage;
    }
    
    /**
     * Verifica si se debe prevenir el daño entre aliados
     */
    public boolean shouldPreventAllyDamage() {
        return preventAllyDamage;
    }
    
    /**
     * Verifica si dos jugadores están en el mismo clan
     */
    public boolean areInSameClan(Player attacker, Player victim) {
        if (!enabled || clansApi == null) {
            return false;
        }
        
        try {
            ClanPlayer attackerClan = clansApi.getClanPlayer(attacker);
            ClanPlayer victimClan = clansApi.getClanPlayer(victim);
            
            if (attackerClan == null || victimClan == null) {
                return false;
            }
            
            if (!attackerClan.hasClan() || !victimClan.hasClan()) {
                return false;
            }
            
            return attackerClan.getClan().getId() == victimClan.getClan().getId();
        } catch (Exception e) {
            plugin.getLogger().warning("Error al verificar clanes: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si dos jugadores son aliados
     * NOTA: Implementación temporal - la API de AdvancedClans no expone métodos de alianza públicamente
     */
    public boolean areAllies(Player attacker, Player victim) {
        if (!enabled || clansApi == null) {
            return false;
        }
        
        try {
            ClanPlayer attackerClan = clansApi.getClanPlayer(attacker);
            ClanPlayer victimClan = clansApi.getClanPlayer(victim);
            
            if (attackerClan == null || victimClan == null) {
                return false;
            }
            
            if (!attackerClan.hasClan() || !victimClan.hasClan()) {
                return false;
            }
            
            // TODO: Implementar verificación de aliados cuando la API lo permita
            // Por ahora retornamos false ya que no tenemos acceso a los métodos de alianza
            // La funcionalidad de prevenir daño entre clanes sigue funcionando
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error al verificar aliados: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si se debe aplicar un efecto entre dos jugadores
     * Retorna true si se debe aplicar, false si se debe prevenir
     */
    public boolean shouldApplyEffect(Player attacker, Player victim) {
        if (!enabled) {
            return true; // Si el hook está desactivado, aplicar normalmente
        }
        
        // Verificar si están en el mismo clan y se debe prevenir
        if (preventClanDamage && areInSameClan(attacker, victim)) {
            return false;
        }
        
        // Verificar si son aliados y se debe prevenir
        if (preventAllyDamage && areAllies(attacker, victim)) {
            return false;
        }
        
        return true; // Aplicar el efecto normalmente
    }
    
    /**
     * Recarga la configuración del hook
     */
    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("advancedclans.enabled", true);
        this.preventClanDamage = plugin.getConfig().getBoolean("advancedclans.prevent-clan-damage", true);
        this.preventAllyDamage = plugin.getConfig().getBoolean("advancedclans.prevent-ally-damage", true);
        
        if (enabled && isAdvancedClansAvailable() && clansApi == null) {
            try {
                this.clansApi = Bukkit.getServicesManager().getRegistration(AdvancedClansApi.class).getProvider();
                plugin.getLogger().info("Hook con AdvancedClans reactivado!");
            } catch (Exception e) {
                plugin.getLogger().warning("Error al reconectar con AdvancedClans: " + e.getMessage());
                this.enabled = false;
            }
        }
    }
}