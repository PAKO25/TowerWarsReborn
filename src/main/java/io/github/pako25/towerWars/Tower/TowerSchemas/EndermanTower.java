package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Player.Listeners.EndermanTeleportListener;
import io.github.pako25.towerWars.Tower.ParticleTrail;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.Set;

public class EndermanTower extends Tower {

    public EndermanTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        Mob enderman = (Mob) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.ENDERMAN);
        entities.add(enderman);
        EndermanTeleportListener.getListener().addEntityUUID(enderman.getUniqueId());
        applyStats(TowerType.ENDERMAN);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        TWMob target = getClosestToExit(mobSet);
        if (target.isHodiNazaj()) {
            return; //mob hodi nazaj, naslednji tick ponovi napad na naslednjega
        }
        target.getMobNavigation().teleportBack();
        if (level == 2) {
            boolean success = target.takeDamage((int) (target.getHealth() * 0.1), this, AttackType.MAGIC);
            if (success) damageDealt += (int) (target.getHealth() * 0.1);
        }
        shots++;
        animateAttack(target);
        resetCooldown();
    }

    public void animateAttack(TWMob mob) {
        Location targetLocation = mob.getEyeLocation();
        Mob creature = entities.iterator().next();
        Location sourceLocation = creature.getEyeLocation();
        Vector dir = targetLocation.toVector().subtract(sourceLocation.toVector()).normalize();

        // compute yaw/pitch (Bukkit uses degrees)
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ()));
        float pitch = (float) Math.toDegrees(Math.asin(dir.getY()));

        // apply rotation
        creature.setBodyYaw(yaw);
        creature.setRotation(yaw, pitch);

        ParticleTrail.spawnParticleTrail(sourceLocation, targetLocation, 0.8, 5, Particle.PORTAL);
    }

    public void cleanup() {
        EndermanTeleportListener.getListener().removeEntityUUID(entities.iterator().next().getUniqueId());
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        slownessIndicatorLocation = location.clone().add(0, 4, 0);
    }
}
