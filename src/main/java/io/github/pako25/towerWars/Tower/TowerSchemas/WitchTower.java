package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.ParticleTrail;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;

public class WitchTower extends Tower {

    public WitchTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        entities.add((Mob) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.WITCH));

        applyStats(TowerType.WITCH);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        Set<TWMob> inAttackRadius = null;
        if (level == 1) {
            inAttackRadius = defaultAttackMobs(mobSet);
        }
        if (level == 2) {
            //najprej napade tiste, ki še nimajo weaknessa
            Set<TWMob> mobsWithoutWeakness = mobSet.stream()
                    .filter(mob -> !mob.hasWeakness())
                    .collect(Collectors.toSet());
            if (mobsWithoutWeakness.isEmpty()) {
                inAttackRadius = defaultAttackMobs(mobSet);
            } else {
                inAttackRadius = defaultAttackMobs(mobsWithoutWeakness);
            }
        }
        assert inAttackRadius != null;
        for (TWMob mob : inAttackRadius) {
            boolean success = mob.takeDamage(damage, this, AttackType.MAGIC);
            if (success) damageDealt += damage;
            if (level == 1) {
                mob.applyWeakness(0.4F, 10);
            }
            if (level == 2) {
                mob.applyWeakness(0.8F, 10);
            }
        }
        shots++;
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

        ParticleTrail.spawnParticleTrail(sourceLocation, targetLocation, 0.8, 5, Particle.ENCHANT);
    }

    public void cleanup() {
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        slownessIndicatorLocation = location.clone().add(0, 4, 0);
    }
}
