package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.ParticleTrail;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;

import java.util.Random;
import java.util.Set;

public class MagmaTower extends Tower {

    private final int infernalTowerPrestige = 1;
    private final int volcanoPrestige = 2;

    public MagmaTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        MagmaCube magma = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
        magma.setSize(2);
        entities.add(magma);
        if (level > 1 && prestige == 0) {
            MagmaCube magma2 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
            magma.addPassenger(magma2);
            magma2.setSize(1);
            entities.add(magma2);
            if (level == 3) {
                MagmaCube magma3 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
                magma2.addPassenger(magma3);
                magma3.setSize(1);
                MagmaCube magma4 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
                magma3.addPassenger(magma4);
                magma4.setSize(1);
                entities.add(magma3);
                entities.add(magma4);
            }
        }
        if (prestige == infernalTowerPrestige) {
            MagmaCube magma2 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
            magma.addPassenger(magma2);
            magma2.setSize(2);
            MagmaCube magma3 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
            magma2.addPassenger(magma3);
            magma3.setSize(2);
            MagmaCube magma4 = (MagmaCube) location.getWorld().spawnEntity(location.clone().add(0.5,1, 0.5), EntityType.MAGMA_CUBE);
            magma3.addPassenger(magma4);
            magma4.setSize(2);
            entities.add(magma2);
            entities.add(magma3);
            entities.add(magma4);
        }
        if (prestige == volcanoPrestige) {
            magma.setSize(3);
        }

        applyStats(TowerType.MAGMA);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        Set<TWMob> inAttackRadius = defaultAttackMobs(mobSet);
        for (TWMob mob : inAttackRadius) {
            boolean success = mob.takeDamage(damage, this, AttackType.AOE);
            if (success) damageDealt+=damage;
            if (prestige == infernalTowerPrestige) {
                mob.applyBurn(5);
            }
            if (prestige == volcanoPrestige) {
                Random random = new Random();
                if (random.nextInt(4) == 0) {
                    mob.applySlowness(0.3F, 3);
                }
            }
        }
        shots++;
        resetCooldown();
    }

    public void animateAttack(TWMob mob) {
        Location source = location.clone();
        Location goal = mob.getEyeLocation();


        ParticleTrail.spawnParticleTrail(source, goal, 1, 5, Particle.SMOKE);
    }

    public void cleanup() {}

    @Override
    protected void setSlownessIndicatorHeight() {
        int height = 3;
        if (level == 3) height = 4;
        if (prestige == infernalTowerPrestige) height = 5;
        slownessIndicatorLocation = location.clone().add(0,height, 0);
    }
}