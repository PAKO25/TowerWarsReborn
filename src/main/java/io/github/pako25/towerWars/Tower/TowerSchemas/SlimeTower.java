package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.ParticleTrail;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;

import java.util.Set;

public class SlimeTower extends Tower {

    private final int stickyTowerPrestige = 1;
    private final int bigSlimePrestige = 2;

    public SlimeTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        Slime slime = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
        slime.setSize(2);
        entities.add(slime);
        if (level > 1 && prestige == 0) {
            Slime slime2 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
            slime.addPassenger(slime2);
            slime2.setSize(1);
            entities.add(slime2);
            if (level == 3) {
                Slime slime3 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
                slime2.addPassenger(slime3);
                slime3.setSize(1);
                Slime slime4 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
                slime3.addPassenger(slime4);
                slime4.setSize(1);
                entities.add(slime3);
                entities.add(slime4);
            }
        }
        if (prestige == stickyTowerPrestige) {
            Slime slime2 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
            slime.addPassenger(slime2);
            slime2.setSize(2);
            Slime slime3 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
            slime2.addPassenger(slime3);
            slime3.setSize(2);
            Slime slime4 = (Slime) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SLIME);
            slime3.addPassenger(slime4);
            slime4.setSize(2);
            entities.add(slime2);
            entities.add(slime3);
            entities.add(slime4);
        }
        if (prestige == bigSlimePrestige) {
            slime.setSize(3);
        }

        applyStats(TowerType.SLIME);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        int duration = 4;
        float amplifier = 0.25F;
        if (level == 2) amplifier = 0.3F;
        if (level == 3) amplifier = 0.4F;
        if (prestige == stickyTowerPrestige) {
            amplifier = 0.9F;
        }
        if (prestige == bigSlimePrestige) {
            amplifier = 0.6F;
        }

        Set<TWMob> inAttackRadius = defaultAttackMobs(mobSet);
        for (TWMob mob : inAttackRadius) {
            boolean success = mob.takeDamage(damage, this, AttackType.AOE);
            if (success) damageDealt += damage;
            mob.applySlowness(amplifier, duration);
        }
        shots++;
        resetCooldown();
    }

    public void animateAttack(TWMob mob) {
        Location source = location.clone();
        Location goal = mob.getEyeLocation();


        ParticleTrail.spawnParticleTrail(source, goal, 1, 5, Particle.WHITE_SMOKE);
    }

    public void cleanup() {
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        int height = 3;
        if (level == 3) height = 4;
        if (prestige == stickyTowerPrestige) height = 5;
        slownessIndicatorLocation = location.clone().add(0, height, 0);
    }
}