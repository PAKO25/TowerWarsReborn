package io.github.pako25.towerWars.Player.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class EndermanTeleportListener implements Listener {

    private static Set<UUID> uuidSet = new HashSet<>();
    private static EndermanTeleportListener endermanTeleportListener;

    private EndermanTeleportListener() {}

    public static EndermanTeleportListener getListener() {
        if (endermanTeleportListener == null) {
            endermanTeleportListener = new EndermanTeleportListener();
        }
        return endermanTeleportListener;
    }

    public void addEntityUUID(UUID uuid) {
        uuidSet.add(uuid);
    }

    public void removeEntityUUID(UUID uuid) {
        uuidSet.remove(uuid);
    }

    @EventHandler
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (uuidSet.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
