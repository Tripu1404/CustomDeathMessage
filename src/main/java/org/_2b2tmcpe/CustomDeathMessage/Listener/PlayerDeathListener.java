package org._2b2tmcpe.CustomDeathMessage.Listener;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDeathListener implements Listener {
    private final Plugin plugin;
    private final Config config;
    // Map para almacenar el último damager conocido por nombre de jugador
    private final Map<String, Entity> lastDamager = new ConcurrentHashMap<>();

    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Guarda el último damager cuando una entidad daña a otra.
     * Esto permite atribuir muertes indirectas (ej. caída después de empujar, ahogo tras empuje, magia indirecta, etc.).
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
        Entity victim = edbe.getEntity();
        Entity damager = edbe.getDamager();

        if (victim instanceof Player) {
            // Guardar el último damager para el jugador
            lastDamager.put(victim.getName(), damager);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        String victimName = victim.getName();

        EntityDamageEvent lastCause = victim.getLastDamageCause();
        DamageCause cause = (lastCause != null) ? lastCause.getCause() : null;

        // Obtener damager preferentemente del evento directo, si existe; si no, del map lastDamager
        Entity damagerFromEvent = null;
        if (lastCause instanceof EntityDamageByEntityEvent) {
            damagerFromEvent = ((EntityDamageByEntityEvent) lastCause).getDamager();
        }
        Entity damager = (damagerFromEvent != null) ? damagerFromEvent : lastDamager.get(victimName);

        String attackerName = (damager != null) ? damager.getName() : null;
        String weaponName = null;
        if (damager instanceof Player) {
            Player p = (Player) damager;
            if (p.getInventory().getItemInHand() != null) {
                weaponName = p.getInventory().getItemInHand().getName();
            }
        }

        String message = config.getString("CUSTOM", "<Player> died without reason");

        // Si no hay causa conocida, usar CUSTOM y limpiar lastDamager
        if (cause == null) {
            message = replacePlayer(message, victimName);
            event.setDeathMessage(TextFormat.RED + message);
            lastDamager.remove(victimName);
            return;
        }

        // Manejo por causa
        switch (cause) {
            case FALL:
                if (damager instanceof Player && !damager.equals(victim)) {
                    // Si existe variante específica (FALL_BY_PLAYER), úsala; si no, usa FALL con Attacker
                    if (config.exists("FALL_BY_PLAYER")) {
                        message = config.getString("FALL_BY_PLAYER");
                        message = replacePlayerAttacker(message, victimName, attackerName);
                    } else {
                        message = config.getString("FALL", "<Player> fell due to <Attacker>");
                        message = replacePlayerAttacker(message, victimName, attackerName);
                    }
                } else {
                    message = config.getString("FALL", "<Player> jumped off the roof");
                    message = replacePlayer(message, victimName);
                }
                break;

            case FIRE_TICK:
            case FIRE:
            case LAVA:
                // Si hay un damager conocido (ej. el que prendió fuego), usar variante by player
                if (damager instanceof Player && !damager.equals(victim) && config.exists("FIRE_TICK_BY_PLAYER")) {
                    message = config.getString("FIRE_TICK_BY_PLAYER");
                    message = replacePlayerAttacker(message, victimName, attackerName);
                } else {
                    // Prioriza la clave exacta (FIRE_TICK o FIRE o LAVA)
                    message = config.getString(cause.name(), "<Player> burned");
                    message = replacePlayer(message, victimName);
                }
                break;

            case DROWNING:
                // Si alguien empujó al jugador al agua previamente (lastDamager es Player), mostrar variante
                if (damager instanceof Player && !damager.equals(victim) && config.exists("DROWNING_BY_PLAYER")) {
                    message = config.getString("DROWNING_BY_PLAYER");
                    message = replacePlayerAttacker(message, victimName, attackerName);
                } else {
                    message = config.getString("DROWNING", "<Player> drowned");
                    message = replacePlayer(message, victimName);
                }
                break;

            case BLOCK_EXPLOSION:
                if (damager instanceof Player && config.exists("BLOCK_EXPLOSION_BY_PLAYER")) {
                    message = config.getString("BLOCK_EXPLOSION_BY_PLAYER");
                    message = replacePlayerAttacker(message, victimName, attackerName);
                } else {
                    message = config.getString("BLOCK_EXPLOSION", "<Player> blew up");
                    message = replacePlayer(message, victimName);
                }
                break;

            case ENTITY_EXPLOSION:
                // End Crystal: intentamos resolver el actor que golpeó/detonó el cristal
                if (damager instanceof EntityEndCrystal || (damager != null && damager.getName().toLowerCase().contains("endercrystal")) || (damager != null && damager.getName().equalsIgnoreCase("Ender Crystal"))) {
                    String realAttacker = attackerName;
                    // Si el EndCrystal fue dañado por otro entity justo antes, intentamos obtenerlo
                    if (damager.getLastDamageCause() instanceof EntityDamageByEntityEvent lastHit) {
                        Entity possible = lastHit.getDamager();
                        if (possible instanceof Player) {
                            realAttacker = possible.getName();
                        }
                    }
                    if (config.exists("ENTITY_EXPLOSION_ENDER_CRYSTAL")) {
                        message = config.getString("ENTITY_EXPLOSION_ENDER_CRYSTAL");
                        message = replacePlayerAttacker(message, victimName, (realAttacker != null ? realAttacker : "Unknown"));
                    } else {
                        message = config.getString("ENTITY_EXPLOSION", "<Player> was blown up by <Attacker>'s Ender Crystal");
                        message = replacePlayerAttacker(message, victimName, (realAttacker != null ? realAttacker : "Unknown"));
                    }
                } else if (damager instanceof Player && config.exists("ENTITY_EXPLOSION")) {
                    message = config.getString("ENTITY_EXPLOSION");
                    message = replacePlayerAttacker(message, victimName, attackerName);
                } else {
                    message = config.getString("ENTITY_EXPLOSION", "<Player> was blown up");
                    message = replacePlayer(message, victimName);
                }
                break;

            case MAGIC:
                // MAGIC a menudo no es un EntityDamageByEntityEvent — usar lastDamager si existe
                if (damager != null && damager instanceof Player && config.exists("MAGIC_BY_ENTITY")) {
                    message = config.getString("MAGIC_BY_ENTITY");
                    message = replacePlayerAttacker(message, victimName, attackerName);
                } else if (config.exists("MAGIC")) {
                    message = config.getString("MAGIC");
                    message = replacePlayer(message, victimName);
                } else {
                    // fallback
                    message = config.getString("CUSTOM", "<Player> died without reason");
                    message = replacePlayer(message, victimName);
                }
                break;

            case ENTITY_ATTACK:
                if (damager instanceof Player) {
                    message = config.getString("KILL_BY_WEAPON", "<Player> got killed by <Attacker> using <WeaponName>");
                    message = replacePlayerAttackerWeapon(message, victimName, attackerName, weaponName);
                } else if (damager != null) {
                    message = config.getString("MOB_ATTACK", "<Player> got killed by <Attacker>");
                    message = replacePlayerAttacker(message, victimName, attackerName != null ? attackerName : damager.getName());
                } else {
                    message = config.getString("MOB_ATTACK", "<Player> got killed by mob");
                    message = replacePlayer(message, victimName);
                }
                break;

            case PROJECTILE:
                if (damager != null) {
                    message = config.getString("PROJECTILE", "<Player> got shot by <Attacker>");
                    message = replacePlayerAttacker(message, victimName, attackerName != null ? attackerName : damager.getName());
                } else {
                    message = config.getString("PROJECTILE", "<Player> got shot");
                    message = replacePlayer(message, victimName);
                }
                break;

            default:
                // Otros: VOID, SUFFOCATION, SUICIDE, etc. Usar clave directa si existe.
                if (config.exists(cause.name())) {
                    message = config.getString(cause.name());
                    message = replacePlayer(message, victimName);
                } else {
                    message = config.getString("CUSTOM", "<Player> died without reason");
                    message = replacePlayer(message, victimName);
                }
                break;
        }

        // Poner en rojo (coherente con versiones previas)
        event.setDeathMessage(TextFormat.RED + message);

        // Limpiar estado del último atacante para este jugador (evita retener data antigua)
        lastDamager.remove(victimName);
    }

    // Helpers para reemplazar tags de forma segura
    private String replacePlayer(String template, String player) {
        if (template == null) return player + " died";
        return template.replace("<Player>", player);
    }

    private String replacePlayerAttacker(String template, String player, String attacker) {
        if (template == null) template = "<Player> was killed by <Attacker>";
        attacker = (attacker != null) ? attacker : "Unknown";
        return template.replace("<Player>", player).replace("<Attacker>", attacker);
    }

    private String replacePlayerAttackerWeapon(String template, String player, String attacker, String weapon) {
        if (template == null) template = "<Player> was killed by <Attacker> using <WeaponName>";
        attacker = (attacker != null) ? attacker : "Unknown";
        weapon = (weapon != null) ? weapon : "bare hands";
        return template.replace("<Player>", player).replace("<Attacker>", attacker).replace("<WeaponName>", weapon);
    }
}
