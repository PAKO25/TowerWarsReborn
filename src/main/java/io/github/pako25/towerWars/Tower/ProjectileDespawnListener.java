package io.github.pako25.towerWars.Tower;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProjectileDespawnListener implements Listener {

    private final Set<UUID> entitiesUUIDSet = new HashSet<>();
    private static ProjectileDespawnListener projectileDespawnListener;

    private ProjectileDespawnListener() {
    }

    public static ProjectileDespawnListener getInstance() {
        if (projectileDespawnListener == null) {
            projectileDespawnListener = new ProjectileDespawnListener();
        }
        return projectileDespawnListener;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (entitiesUUIDSet.contains(arrow.getUniqueId())) {
                arrow.remove();
                entitiesUUIDSet.remove(arrow.getUniqueId());
            }
        }
    }

    public void addEntityUUID(UUID uuid) {
        entitiesUUIDSet.add(uuid);
    }
}
