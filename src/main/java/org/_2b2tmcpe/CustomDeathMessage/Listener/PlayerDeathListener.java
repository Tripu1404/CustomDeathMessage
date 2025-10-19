package org._2b2tmcpe.CustomDeathMessage.Listener;

import org._2b2tmcpe.CustomDeathMessage.Main;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public class PlayerDeathListener implements Listener {

    private final Main plugin;
    private final Config conf;
    private final Map<String, Entity> lastDamager = new HashMap<>();

    public PlayerDeathListener(Main plugin) {
        this.plugin = plugin;
        this.conf = plugin.getConfig();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            Entity victim = event.getEntity();
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (victim instanceof Player) {
                lastDamager.put(victim.getName(), damager);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent lastCause = player.getLastDamageCause();
        String playerName = player.getName();
        String message = conf.getString("CUSTOM");

        Entity damager = lastDamager.get(playerName);
        String attackerName = (damager != null) ? damager.getName() : null;

        if (lastCause == null) {
            event.setDeathMessage(TextFormat.RED + convert(message, playerName));
            return;
        }

        EntityDamageEvent.DamageCause cause = lastCause.getCause();

        switch (cause) {
            case FALL:
                if (attackerName != null && damager instanceof Player && damager != player) {
                    message = conf.getString("FALL_BY_PLAYER");
                    message = convert(message, playerName, attackerName);
                } else {
                    message = conf.getString("FALL");
                    message = convert(message, playerName);
                }
                break;

            case FIRE:
            case FIRE_TICK:
            case LAVA:
                if (attackerName != null && damager instanceof Player && damager != player) {
                    message = conf.getString("FIRE_TICK_BY_PLAYER");
                    message = convert(message, playerName, attackerName);
                } else {
                    message = conf.getString(cause.name());
                    message = convert(message, playerName);
                }
                break;

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (damager instanceof EntityEndCrystal && attackerName != null) {
                    message = conf.getString("ENDER_CRYSTAL");
                    message = convert(message, playerName, attackerName);
                } else if (attackerName != null) {
                    message = conf.getString("BLOCK_EXPLOSION_BY_PLAYER");
                    message = convert(message, playerName, attackerName);
                } else {
                    message = conf.getString("BLOCK_EXPLOSION");
                    message = convert(message, playerName);
                }
                break;

            case MAGIC:
                if (attackerName != null) {
                    message = conf.getString("MAGIC_BY_ENTITY");
                    message = convert(message, playerName, attackerName);
                } else {
                    message = conf.getString("MAGIC");
                    message = convert(message, playerName);
                }
                break;

            default:
                message = conf.getString("CUSTOM");
                message = convert(message, playerName);
                break;
        }

        event.setDeathMessage(TextFormat.RED + message);
        lastDamager.remove(playerName);
    }

    private String convert(String msg, String player) {
        return msg.replace("<Player>", player);
    }

    private String convert(String msg, String player, String attacker) {
        return msg.replace("<Player>", player).replace("<Attacker>", attacker);
    }
}
