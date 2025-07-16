package io.github.pako25.towerWars.Arena.MobData;

import com.destroystokyo.paper.entity.ai.MobGoals;
import io.github.pako25.towerWars.Arena.AntiFire;
import io.github.pako25.towerWars.Arena.MobData.MobAbilities.*;
import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Player.TWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobBuilder {

    public static float BaseSpeed = 0.15F;

    public static TWMob buildMob(Track track, ArrayList<Vector> path, MobType mobType, TWPlayer summoner) {
        EntityType entityType = track.getMobStates().getMobState(mobType).getEntityType();
        Mob creature = (Mob) track.getTrackSpawn().getWorld().spawnEntity(track.getTrackSpawn().clone().add(path.getFirst()), entityType);
        applyAttributes(mobType, creature);

        TWMob mob = new TWMob(track, path, track.getTrackSpawn(), mobType, summoner, creature);

        if (mobType == MobType.GHAST || mobType == MobType.SQUID || mobType == MobType.RABBIT) {
            applyCustomNavigatableMob(creature, mob, track, path);
        }

        applyAbilities(mobType, mob, summoner.getTrack().getMobStates().getMobState(mobType));

        return mob;
    }

    private static void applyCustomNavigatableMob(Mob mob, TWMob twMob, Track track, ArrayList<Vector> path) {
        Mob navigatable = (Mob) track.getTrackSpawn().getWorld().spawnEntity(track.getTrackSpawn().clone().add(path.getFirst()), EntityType.SILVERFISH);
        navigatable.addPassenger(mob);
        navigatable.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(BaseSpeed);
        navigatable.setInvulnerable(true);
        navigatable.setInvisible(true);
        navigatable.setCollidable(false);
        MobGoals mobGoals = Bukkit.getMobGoals();
        mobGoals.removeAllGoals(navigatable);
        twMob.setNavigatableMob(navigatable);
    }

    public static void applyAttributes(MobType mobType, Mob creature) {
        if (creature instanceof Ageable ageable) {
            ageable.setAdult();
            if (creature instanceof Breedable breedable) {
                breedable.setAgeLock(true);
            }
        }

        switch (mobType) {
            case LEATHER_ZOMBIE:
                creature.getEquipment().setHelmet (new ItemStack(Material.LEATHER_HELMET));
                creature.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                creature.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                creature.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                break;

            case GOLD_ZOMBIE:
                creature.getEquipment().setHelmet ( new ItemStack(Material.GOLDEN_HELMET));
                creature.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
                creature.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
                creature.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
                break;

            case DIAMOND_ZOMBIE:
                creature.getEquipment().setHelmet (new ItemStack(Material.DIAMOND_HELMET));
                creature.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                creature.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                creature.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                break;

            case CHARGED_CREEPER:
                ((Creeper) creature).setPowered(true);
                break;

            case RAINBOW_SHEEP:
                Sheep sheep = (Sheep) creature;
                DyeColor[] colors = DyeColor.values();
                sheep.setColor(colors[new Random().nextInt(colors.length)]);
                break;

            case MINI_ZOMBIE:
                if (creature instanceof Ageable ageable) {
                    ageable.setBaby();
                }
        }

        creature.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(BaseSpeed);
        AntiFire.getListener().add(creature);
        creature.setAggressive(false);
        creature.setCollidable(false);
        creature.setInvulnerable(true);
        MobGoals mobGoals = Bukkit.getMobGoals();
        mobGoals.removeAllGoals(creature);
    }

    public static void applyAbilities(MobType mobType, TWMob twMob, MobState mobState) {
        List<MobAbility> abilities = twMob.getAbilities();
        switch (mobType) {
            case WOLF -> {
                abilities.add(new AOEDodge(0.33F));
            }
            case RABBIT -> {
                abilities.add(new AOEDodge(0.5F));
            }
            case WILD_CAT -> {
                abilities.add(new AOEDodge(1F));
            }
            case PRIEST -> {
                abilities.add(new HealAbility(0.2F, 5, twMob));
            }
            case HIGH_PRIEST -> {
                abilities.add(new HealAbility(0.4F * (1 + mobState.getSummoningBonusHealingFactor()), 5, twMob));
            }
            case ENDERMITE -> {
                abilities.add(new TPOnHit((int) (5 * (1 + mobState.getSummoningBonusTpDistanceFactor())), twMob));
            }
            case BLACK_SPIDER -> {
                abilities.add(new SlowAbility(twMob, 5, 0.2F, 2));
            }
            case SPIDER_JOCKEY -> {
                abilities.add(new SlowAbility(twMob, 5, 0.4F * (1 + mobState.getSummoningBonusSlowFactor()), 2));
            }
            case CREEPER -> {
                abilities.add(new StunAbility(5, 2, twMob));
            }
            case CHARGED_CREEPER -> {
                abilities.add(new StunAbility((int) (8 * (1 + mobState.getSummoningBonusStunRange())), 2, twMob));
            }
            case SQUID -> {
                abilities.add(new BlindAbility(5, 0.33F * (1 + mobState.getSummoningBonusBlindFactor()), 5, twMob));
            }
            case LEATHER_ZOMBIE -> {
                abilities.add(new DamageAbsorber(0.25F, twMob));
            }
            case GOLD_ZOMBIE -> {
                abilities.add(new DamageAbsorber(0.6F, twMob));
            }
            case DIAMOND_ZOMBIE -> {
                abilities.add(new DamageAbsorber(0.8F, twMob));
            }
            case WILD_HORSE -> {
                abilities.add(new ReduceSlowAbility(0.5F));
            }
            case ZOMBIE -> {
                abilities.add(new SplitAbility(twMob));
            }
            case DEATH_RIDER -> {
                abilities.add(new RespawnAbility(twMob));
                abilities.add(new NoKillBonusesAbility());
            }
            case GHAST -> {
                abilities.add(new NoKillBonusesAbility());
                abilities.add(new DamageAbsorber(1F, twMob));
            }
        }
    }
}