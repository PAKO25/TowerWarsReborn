package io.github.pako25.towerWars.Arena;

import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiFire implements Listener {

    private final Set<UUID> protectedMobs = new HashSet<>();

    private AntiFire() {
    }

    private static AntiFire listener;

    public static AntiFire getListener() {
        if (listener == null) {
            listener = new AntiFire();
        }
        return listener;
    }

    public void add(Mob mob) {
        protectedMobs.add(mob.getUniqueId());
    }

    public void remove(Mob mob) {
        protectedMobs.remove(mob.getUniqueId());
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (protectedMobs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}