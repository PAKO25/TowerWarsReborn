package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TeslaTower extends Tower {

    private Creeper attackingCreeper;
    private final int beamPrestige = 1;
    private final int lightningPrestige = 2;
    private TWMob targetLock;
    private float damageMultiplier = 0;
    private Creeper animationCreeper;
    private int lastRandomAnimationOffsetX = 0;
    private int lastRandomAnimationOffsetZ = 0;
    private ArmorStand armorStand; //kocka

    public TeslaTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        attackingCreeper = (Creeper) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.CREEPER);
        entities.add(attackingCreeper);
        animationCreeper = (Creeper) location.getWorld().spawnEntity(location, EntityType.CREEPER);
        animationCreeper.setAI(false);
        animationCreeper.setInvisible(true);
        entities.add(animationCreeper);

        Creeper creeper4 = null;
        if (level > 1 || prestige != 0) {
            Creeper creeper2 = (Creeper) location.getWorld().spawnEntity(location.clone().add(0, 1, 0.5), EntityType.CREEPER);
            Creeper creeper3 = (Creeper) location.getWorld().spawnEntity(location.clone().add(1, 1, 0.5), EntityType.CREEPER);
            entities.add(creeper2);
            entities.add(creeper3);
            if (level == 3 || prestige != 0) {
                creeper4 = (Creeper) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.CREEPER);
                attackingCreeper.addPassenger(creeper4);
                entities.add(creeper4);
            }
        }


        if (prestige != 0) {
            if (armorStand == null)
                armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, -0.3, 0.5), EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            ItemStack block;
            if (prestige == beamPrestige) {
                block = new ItemStack(Material.GOLD_BLOCK);
            } else {
                block = new ItemStack(Material.DIAMOND_BLOCK);
            }
            EntityEquipment entityEquipment = armorStand.getEquipment();
            entityEquipment.setHelmet(block);
            armorStand.addPassenger(attackingCreeper);
            attackingCreeper.addPassenger(creeper4);
        }
        applyStats(TowerType.TESLA);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        if (targetLock == null || !mobSet.contains(targetLock)) {
            //dobi najdle od konca
            double maxPathLeft = 0;
            TWMob target = null;
            for (TWMob mob : mobSet) {
                double pathLeft = mob.getMobNavigation().getPathLeft();
                if (pathLeft > maxPathLeft) {
                    maxPathLeft = pathLeft;
                    target = mob;
                }
            }
            targetLock = target;
            if (prestige != lightningPrestige) {
                damageMultiplier = 0; //lightning prestige reseta multiplier samo ko ni nobenega v rangu
            }
        }

        boolean success = targetLock.takeDamage((int) (damage * (1 + damageMultiplier)), this, AttackType.NORMAL);
        if (success) damageDealt+=(int) (damage * (1 + damageMultiplier));
        shots++;

        if (prestige == 0) {
            if (damageMultiplier < 2) {
                damageMultiplier = damageMultiplier + 0.5F;
            }
        }
        if (prestige == beamPrestige) {
            targetLock.applySlowness(0.3F, (int) Math.ceil(reload));
            damageMultiplier = damageMultiplier + 0.5F;
        }
        if (prestige == lightningPrestige) {
            if (damageMultiplier < 3) {
                damageMultiplier = damageMultiplier + 0.5F;
            }
        }

        //animacija se požene v isOnCooldown() v Tower.java
        resetCooldown();
    }

    public void animateAttack(TWMob mob) {}

    public void nothingInRange() {
        damageMultiplier = 0; //za lightning prestige
        targetLock = null;
    }

    public void continuouslyAttack() {
        if (targetLock == null || !targetLock.isAlive()) { //reseta
            for (Entity entity : entities) {
                if (entity instanceof Creeper creeper) {
                    creeper.setPowered(false);
                }
            }
            attackingCreeper.setInvisible(false);
            animationCreeper.setInvisible(true);
            animationCreeper.setPowered(false);
            return;
        }
        //dejanski napad
        for (Entity entity : entities) {
            if (entity instanceof Creeper creeper) {
                creeper.setPowered(true);
            }
        }

        attackingCreeper.setInvisible(true);
        attackingCreeper.setPowered(false);
        animationCreeper.setInvisible(false);
        animationCreeper.setPowered(true);

        //pridobi random lokacijo
        Location targetLocation = targetLock.getEyeLocation().clone();
        int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
        int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);
        while (offsetX == lastRandomAnimationOffsetX) {
            offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
        }
        while (offsetZ == lastRandomAnimationOffsetZ) {
            offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);
        }
        lastRandomAnimationOffsetX = offsetX;
        lastRandomAnimationOffsetZ = offsetZ;
        targetLocation.add(offsetX, -0.5, offsetZ);

        //premakne creeprea
        animationCreeper.teleport(targetLocation);
    }

    public void cleanup() {
        animationCreeper.remove();
        if (armorStand != null) {
            armorStand.remove();
        }
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        int height = 3;
        if (level == 3) height = 5;
        if (level == 4) height = 5;
        slownessIndicatorLocation = location.clone().add(0,height, 0);
    }


    public String debugInfo() {
        String animationCreeperStatus = animationCreeper.toString() + "{" +
                "dead=" + animationCreeper.isDead() +
                ", invisible=" + animationCreeper.isInvisible() +
                ", ticking=" + animationCreeper.isTicking() +
                ", empty(!haspassanger)=" + animationCreeper.isEmpty() +
                ", location=" + animationCreeper.getLocation() +
                ", valid=" + animationCreeper.isValid() +
                ", killer=" + animationCreeper.getKiller() +
                ", ticksLived=" + animationCreeper.getTicksLived() +
                ", UUID=" + animationCreeper.getUniqueId() +
                '}';
        String attackingCreeperStatus = attackingCreeper.toString() + "{" +
                "dead=" + attackingCreeper.isDead() +
                ", invisible=" + attackingCreeper.isInvisible() +
                ", ticking=" + attackingCreeper.isTicking() +
                ", empty(!haspassanger)=" + attackingCreeper.isEmpty() +
                ", location=" + attackingCreeper.getLocation() +
                ", valid=" + attackingCreeper.isValid() +
                ", killer=" + attackingCreeper.getKiller() +
                ", ticksLived=" + attackingCreeper.getTicksLived() +
                ", UUID=" + attackingCreeper.getUniqueId() +
                '}';
        String targetLockStatus = targetLock.getCreature().toString() + "{" +
                "dead=" + targetLock.getCreature().isDead() +
                ", invisible=" + targetLock.getCreature().isInvisible() +
                ", ticking=" + targetLock.getCreature().isTicking() +
                ", empty(!haspassanger)=" + targetLock.getCreature().isEmpty() +
                ", location=" + targetLock.getCreature().getLocation() +
                ", valid=" + targetLock.getCreature().isValid() +
                '}';
        return "TeslaTower{" +
                "damageMultiplier="+damageMultiplier+
                ", animationCreeper="+animationCreeperStatus+
                ", attackingCreeper="+attackingCreeperStatus+
                ", targetLock="+targetLockStatus+"}";
    }
}
