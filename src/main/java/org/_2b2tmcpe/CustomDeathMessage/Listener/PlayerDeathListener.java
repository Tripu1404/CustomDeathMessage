package org._2b2tmcpe.CustomDeathMessage.Listener;

import org._2b2tmcpe.CustomDeathMessage.Main;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
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
        String message = "";
        Player player = event.getEntity();
        String playerName = player.getName();

        EntityDamageEvent ev = player.getLastDamageCause();
        DamageCause cause = ev.getCause();

        plugin.getLogger().debug("Death cause: " + cause.name());

        if (ev instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();

            // Auto-muerte por ataque propio
            if (damager.equals(player)) {
                message = this.convertConfigTags(conf.getString("SUICIDE"), playerName);
            }
            // Ataque por jugador con arma
            else if (damager instanceof Player && cause != DamageCause.PROJECTILE) {
                String weaponName = ((Player) damager).getInventory().getItemInHand().getName();
                message = this.convertConfigTags(conf.getString("KILL_BY_WEAPON"), playerName, damager.getName(), weaponName);
            }
            // Ataque por entidad (mob)
            else if (cause == DamageCause.ENTITY_ATTACK && !(damager instanceof Player)) {
                message = this.convertConfigTags(conf.getString("MOB_ATTACK"), playerName, damager.getName());
            }
            // Proyectiles (cualquier tipo de Entity)
            else if (cause == DamageCause.PROJECTILE) {
                message = this.convertConfigTags(conf.getString("PROJECTILE"), playerName, damager.getName());
            }
            // Explosión de bloque o Ender Crystal
            else if (cause == DamageCause.ENTITY_EXPLOSION) {
                String template = conf.getString("ENTITY_EXPLOSION");
                if (damager.getName().equalsIgnoreCase("EnderCrystal")) {
                    template = conf.getString("ENDER_CRYSTAL");
                }
                message = this.convertConfigTags(template, playerName, damager.getName());
            }
            // Lightning
            else if (cause == DamageCause.LIGHTNING) {
                message = this.convertConfigTags(conf.getString("LIGHTNING"), playerName, damager.getName());
            }
        } else {
            // Otros tipos de muerte
            message = getDeathMessage(cause, playerName);
        }

        event.setDeathMessage(TextFormat.RED + message);
    }

    public String getDeathMessage(DamageCause cause, String playerName) {
        String msg;
        switch (cause) {
            case SUFFOCATION: msg = conf.getString("SUFFOCATION"); break;
            case FALL: msg = conf.getString("FALL"); break;
            case FIRE: msg = conf.getString("FIRE"); break;
            case FIRE_TICK: msg = conf.getString("FIRE_TICK"); break;
            case LAVA: msg = conf.getString("LAVA"); break;
            case DROWNING: msg = conf.getString("DROWNING"); break;
            case BLOCK_EXPLOSION: msg = conf.getString("BLOCK_EXPLOSION"); break;
            case VOID: msg = conf.getString("VOID"); break;
            case SUICIDE: msg = conf.getString("SUICIDE"); break;
            case MAGIC: msg = conf.getString("MAGIC"); break;
            default: msg = conf.getString("CUSTOM"); break;
        }
        return convertConfigTags(msg, playerName);
    }

    // Sobrecargas para reemplazar tags
    public String convertConfigTags(String msg, String playerName) {
        return msg.replace("<Player>", playerName);
    }

    public String convertConfigTags(String msg, String playerName, String attackerName) {
        msg = msg.replace("<Player>", playerName);
        msg = msg.replace("<Attacker>", attackerName);
        return msg;
    }

    public String convertConfigTags(String msg, String playerName, String attackerName, String weaponName) {
        msg = msg.replace("<Player>", playerName);
        msg = msg.replace("<Attacker>", attackerName);
        msg = msg.replace("<WeaponName>", weaponName);
        return msg;
    }
}
