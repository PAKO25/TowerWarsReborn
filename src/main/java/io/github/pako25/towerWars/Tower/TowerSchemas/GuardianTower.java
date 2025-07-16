package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.Set;

public class GuardianTower extends Tower {

    private final int mindControlPrestige = 1;
    private final int fatiguePrestige = 2;
    private Guardian attackingGuardian;
    private ArmorStand tempTarget;
    private ArmorStand armorStand; //kocka

    public GuardianTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {

        Guardian guardian = (Guardian) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.GUARDIAN);
        entities.add(guardian);
        attackingGuardian = guardian;
        guardian.setAI(false);
        if (level > 1 || prestige != 0) {
            Guardian guardian2 = (Guardian) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.GUARDIAN);
            guardian.addPassenger(guardian2);
            entities.add(guardian2);
            guardian2.setAI(false);
            if (level > 2 || prestige != 0) {
                Guardian guardian3 = (Guardian) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.GUARDIAN);
                guardian2.addPassenger(guardian3);
                entities.add(guardian3);
                guardian3.setAI(false);
            }
        }

        if (prestige != 0) {
            if (armorStand == null)
                armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, -0.3, 0.5), EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            ItemStack block;
            if (prestige == mindControlPrestige) {
                block = new ItemStack(Material.GOLD_BLOCK);
            } else {
                block = new ItemStack(Material.DIAMOND_BLOCK);
            }
            EntityEquipment entityEquipment = armorStand.getEquipment();
            entityEquipment.setHelmet(block);
            armorStand.addPassenger(guardian);
        }


        applyStats(TowerType.GUARDIAN);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        TWMob targetMob = getClosestToExit(mobSet);

        Location target = targetMob.getEyeLocation();
        Location source = attackingGuardian.getEyeLocation();
        Set<TWMob> allMobs = track.getActiveMobs();

        //damaga mobe znotraj laserja, pravtako pridobi oddaljenost najbolj oddaljenega
        double longestSquaredDistance = 0;
        TWMob animationTarget = null;
        for (TWMob twMob : allMobs) {
            if (isInsideLaser(source, target, twMob.getEyeLocation(), 2)) {
                boolean success = twMob.takeDamage(damage, this, AttackType.LASER);
                if (success) damageDealt += damage;
                double distanceSquared = twMob.getEyeLocation().distanceSquared(attackingGuardian.getEyeLocation());
                if (distanceSquared > longestSquaredDistance) {
                    longestSquaredDistance = distanceSquared;
                    animationTarget = twMob;
                }
            }
        }
        shots++;

        assert animationTarget != null;
        animateAttack(animationTarget, longestSquaredDistance);

        if (prestige == fatiguePrestige) {
            mobSet.iterator().next().disableSpecialAbility(5);
        }
        if (prestige == mindControlPrestige) {
            if (new Random().nextInt(3) == 0) {
                mobSet.iterator().next().getMobNavigation().walkBackwards(3);
            }
        }

        resetCooldown();
    }

    private void animateAttack(TWMob targetMob, double distanceSquared) {
        Vector source = attackingGuardian.getEyeLocation().toVector();
        Vector direction = targetMob.getEyeLocation().toVector();

        direction.subtract(source);
        double directionLenSquared = direction.lengthSquared();

        double s = Math.sqrt(distanceSquared / directionLenSquared);

        Vector scaledVector = direction.clone().multiply(s);
        Vector goal = source.clone().add(scaledVector);

        tempTarget = (ArmorStand) location.getWorld().spawnEntity(goal.toLocation(location.getWorld()), EntityType.ARMOR_STAND);
        tempTarget.setGravity(false);
        tempTarget.setVisible(false);
        tempTarget.setInvulnerable(true);

        attackingGuardian.setLastDamage(0);
        attackingGuardian.setTarget(tempTarget);
        attackingGuardian.setLaser(true);
    }

    public void animateAttack(TWMob mob) {
    }

    public void cleanup() {
        if (tempTarget != null) {
            tempTarget.remove();
        }
        if (armorStand != null) {
            armorStand.remove();
        }
    }

    private boolean isInsideLaser(Location source, Location target, Location testLocation, float diameter) {
        // target - source
        double vx = target.x() - source.x();
        double vy = target.y() - source.y();
        double vz = target.z() - source.z();

        // test - source
        double wx = testLocation.x() - source.x();
        double wy = testLocation.y() - source.y();
        double wz = testLocation.z() - source.z();

        // dot product - je spredaj al zadaj?
        double D = vx * wx + vy * wy + vz * wz;
        if (D < 0) return false;

        // cross product
        double cx = vy * wz - vz * wy;
        double cy = vz * wx - vx * wz;
        double cz = vx * wy - vy * wx;

        // ||v×w||^2
        double crossNormSq = cx * cx + cy * cy + cz * cz;

        // ||v||^2
        double vNormSq = vx * vx + vy * vy + vz * vz;

        // Distance = sqrt( ||v×w||^2 / ||v||^2 )
        double distanceSquared = crossNormSq / vNormSq;
        return distanceSquared < diameter * diameter;
    }

    public void resetTargeting() {
        if (tempTarget == null) return;
        attackingGuardian.setLaser(false);
        tempTarget.remove();
        tempTarget = null;
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        int height = level + 1;
        slownessIndicatorLocation = location.clone().add(0, height, 0);
    }
}