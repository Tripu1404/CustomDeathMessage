package org._2b2tmcpe.CustomDeathMessage.Listener;

import org._2b2tmcpe.CustomDeathMessage.Main;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.Projectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class PlayerDeathListener implements Listener {

    private Main plugin;
    private Config conf;

    public PlayerDeathListener(Main plugin) {
        this.plugin = plugin;
        this.conf = plugin.getConfig();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        String playerName = event.getEntity().getName();
        EntityDamageEvent ev = event.getEntity().getLastDamageCause();
        DamageCause cause = ev != null ? ev.getCause() : null;

        String message = "";
        Entity damager = null;
        boolean hasAttacker = false;

        // Detectar si hubo entidad atacante
        if (ev instanceof EntityDamageByEntityEvent) {
            damager = ((EntityDamageByEntityEvent) ev).getDamager();
            hasAttacker = damager != null && !(damager.equals(event.getEntity()));
        }

        switch (cause) {
            case FALL:
                if (hasAttacker && damager instanceof Player) {
                    message = convertConfigTags(conf.getString("FALL_BY_PLAYER"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("FALL"), playerName);
                }
                break;
            case FIRE_TICK:
                if (hasAttacker && damager instanceof Player) {
                    message = convertConfigTags(conf.getString("FIRE_TICK_BY_PLAYER"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("FIRE_TICK"), playerName);
                }
                break;
            case FIRE:
                message = convertConfigTags(conf.getString("FIRE"), playerName);
                break;
            case LAVA:
                message = convertConfigTags(conf.getString("LAVA"), playerName);
                break;
            case DROWNING:
                message = convertConfigTags(conf.getString("DROWNING"), playerName);
                break;
            case SUFFOCATION:
                message = convertConfigTags(conf.getString("SUFFOCATION"), playerName);
                break;
            case VOID:
                message = convertConfigTags(conf.getString("VOID"), playerName);
                break;
            case SUICIDE:
                message = convertConfigTags(conf.getString("SUICIDE"), playerName);
                break;
            case BLOCK_EXPLOSION:
                if (hasAttacker && damager instanceof Player) {
                    message = convertConfigTags(conf.getString("BLOCK_EXPLOSION_BY_PLAYER"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("BLOCK_EXPLOSION"), playerName);
                }
                break;
            case ENTITY_EXPLOSION:
                if (hasAttacker) {
                    if (damager.getName().contains("EnderCrystal")) {
                        message = convertConfigTags(conf.getString("ENTITY_EXPLOSION_ENDER_CRYSTAL"), playerName, damager.getName());
                    } else {
                        message = convertConfigTags(conf.getString("ENTITY_EXPLOSION"), playerName, damager.getName());
                    }
                } else {
                    message = convertConfigTags(conf.getString("ENTITY_EXPLOSION"), playerName);
                }
                break;
            case ENTITY_ATTACK:
                if (damager instanceof Player) {
                    String itemName = ((Player) damager).getInventory().getItemInHand().getName();
                    message = convertConfigTags(conf.getString("KILL_BY_WEAPON"), playerName, damager.getName(), itemName);
                } else if (hasAttacker) {
                    message = convertConfigTags(conf.getString("MOB_ATTACK"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("MOB_ATTACK"), playerName);
                }
                break;
            case PROJECTILE:
                if (hasAttacker) {
                    message = convertConfigTags(conf.getString("PROJECTILE"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("PROJECTILE"), playerName);
                }
                break;
            case MAGIC:
                if (hasAttacker) {
                    message = convertConfigTags(conf.getString("MAGIC_BY_ENTITY"), playerName, damager.getName());
                } else {
                    message = convertConfigTags(conf.getString("MAGIC"), playerName);
                }
                break;
            default:
                message = convertConfigTags(conf.getString("CUSTOM"), playerName);
                break;
        }

        event.setDeathMessage(TextFormat.RED + message);
    }

    // Sobrecargas de convertConfigTags
    public String convertConfigTags(String deathMessage, String playerName, String attacker, String weaponName) {
        return deathMessage.replace("<Player>", playerName)
                           .replace("<Attacker>", attacker)
                           .replace("<WeaponName>", weaponName);
    }

    public String convertConfigTags(String deathMessage, String playerName, String attacker) {
        return deathMessage.replace("<Player>", playerName)
                           .replace("<Attacker>", attacker);
    }

    public String convertConfigTags(String deathMessage, String playerName) {
        return deathMessage.replace("<Player>", playerName);
    }
}
