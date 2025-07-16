package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.ProjectileDespawnListener;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;

public class ArcherTower extends Tower {

    private final int sniperPrestige = 1;
    private final int machineGunPrestige = 2;

    public ArcherTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        entities.add((Mob) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.SKELETON));
        entities.forEach(creature -> {
            if (level > 1) {
                EntityEquipment equipment = creature.getEquipment();
                ItemStack chestplate = null, leggings = null, boots = null;

                if (level == 2) {
                    chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                    leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                    boots = new ItemStack(Material.LEATHER_BOOTS);
                }
                if (level == 3) {
                    chestplate = new ItemStack(Material.IRON_CHESTPLATE);
                    leggings = new ItemStack(Material.IRON_LEGGINGS);
                    boots = new ItemStack(Material.IRON_BOOTS);
                }
                if (prestige == sniperPrestige && level == 4) {
                    chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                    LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
                    chestplateMeta.setColor(Color.BLACK);
                    chestplate.setItemMeta(chestplateMeta);
                    leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                    LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
                    leggingsMeta.setColor(Color.BLACK);
                    leggings.setItemMeta(leggingsMeta);
                    boots = new ItemStack(Material.LEATHER_BOOTS);
                    LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                    bootsMeta.setColor(Color.BLACK);
                    boots.setItemMeta(bootsMeta);
                }
                if (prestige == machineGunPrestige && level == 4) {
                    chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                    LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
                    chestplateMeta.setColor(Color.YELLOW);
                    chestplate.setItemMeta(chestplateMeta);
                    leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                    LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
                    leggingsMeta.setColor(Color.YELLOW);
                    leggings.setItemMeta(leggingsMeta);
                    boots = new ItemStack(Material.LEATHER_BOOTS);
                    LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                    bootsMeta.setColor(Color.YELLOW);
                    boots.setItemMeta(bootsMeta);
                }
                equipment.setChestplate(chestplate);
                equipment.setLeggings(leggings);
                equipment.setBoots(boots);
            }
        });
        applyStats(TowerType.ARCHER);
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
        Set<TWMob> inAttackRadius = null;
        if (prestige == sniperPrestige) {
            Set<TWMob> mobsAboveHalfHP = mobSet.stream()
                    .filter(mob -> mob.getHealth() > mob.getMaxHealth() / 2.0)
                    .collect(Collectors.toSet());
            if (mobsAboveHalfHP.isEmpty()) {
                inAttackRadius = defaultAttackMobs(mobSet);
            } else {
                inAttackRadius = defaultAttackMobs(mobsAboveHalfHP);
            }
        } else {
            inAttackRadius = defaultAttackMobs(mobSet);
        }
        for (TWMob mob : inAttackRadius) {
            boolean success = mob.takeDamage(damage, this, AttackType.NORMAL);
            if (success) damageDealt += damage;
            shots++;
        }
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

        // 3) Launch arrow
        double distance = targetLocation.distance(sourceLocation);
        Vector velocity = dir.clone().multiply(2 + 0.12 * distance);
        velocity.setY(velocity.getY() + 0.008 * distance);
        Arrow arrow = creature.launchProjectile(Arrow.class, velocity);
        arrow.setShooter(creature);
        ProjectileDespawnListener.getInstance().addEntityUUID(arrow.getUniqueId());
    }

    public void cleanup() {
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        slownessIndicatorLocation = location.clone().add(0, 3, 0);
    }
}