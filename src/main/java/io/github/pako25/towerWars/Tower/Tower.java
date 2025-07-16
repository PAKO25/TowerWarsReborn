package io.github.pako25.towerWars.Tower;

import com.destroystokyo.paper.entity.ai.MobGoals;
import io.github.pako25.towerWars.Arena.AntiFire;
import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Tower.TowerSchemas.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Tower {

    protected int level = 1;
    protected int maxLevel = 3;
    protected int prestige = 0;
    protected Location location;
    protected Set<Mob> entities = new HashSet<>();
    protected final Track track;
    protected Set<VillagerTower> villagerProtectors = new HashSet<>();

    protected int damage;
    protected double reload;
    protected int range;
    protected int splash;
    protected int cost;
    protected double cooldown = 0;
    protected boolean immuneToDebuffs = false;
    protected float reloadBoost = 1;
    private TowerType towerType;
    private float slownessTimer = 0;
    private float slownessAmplifier = 1;
    private float stunTimer = 0;
    private float blindnessTimer = 0;
    private float blindnessAmplifier = 1F;
    protected Location slownessIndicatorLocation;
    private boolean isUpgradableTextShown = false;
    private ArmorStand upgradableText;

    protected int damageDealt = 0;
    protected int shots = 0;
    protected int kills = 0;

    public Tower(Location location, int level, int prestige, Track track) {
        this.location = location;
        this.level = level;
        this.prestige = prestige;
        this.track = track;
        spawn();
        setSlownessIndicatorHeight();
        initiateUpgradableText();
    }

    abstract public void spawn();
    public abstract void attackMobs(Set<TWMob> mobSet);
    public abstract void animateAttack(TWMob mob);
    public abstract void cleanup();
    protected abstract void setSlownessIndicatorHeight();


    public void applyStats(TowerType towerType) {
        this.towerType = towerType;
        FileConfiguration cfg = CustomConfig.getFileConfiguration("towerConfig");
        // basic/advanced
        String type = cfg.getString(towerType.name() + ".type");
        if (type.equals("support")) maxLevel = 2;

        entities.forEach(mob -> {
            AntiFire.getListener().add(mob);
            mob.setInvulnerable(true);
            mob.setCollidable(false);
            MobGoals mobGoals = Bukkit.getMobGoals();
            mobGoals.removeAllGoals(mob);
        });

        if (prestige == 0) {
            damage = cfg.getInt(towerType.name() + ".levels." + level + ".damage");
            reload = cfg.getDouble(towerType.name() + ".levels." + level + ".reload");
            range = cfg.getInt(towerType.name() + ".levels." + level + ".range");
            splash = cfg.getInt(towerType.name() + ".levels." + level + ".splash");
            cost = cfg.getInt(towerType.name() + ".levels." + level + ".cost");
        } else {
            damage = cfg.getInt(towerType.name() + ".prestiges." + prestige + ".damage");
            reload = cfg.getDouble(towerType.name() + ".prestiges." + prestige + ".reload");
            range = cfg.getInt(towerType.name() + ".prestiges." + prestige + ".range");
            splash = cfg.getInt(towerType.name() + ".prestiges." + prestige + ".splash");
            cost = cfg.getInt(towerType.name() + ".prestiges." + prestige + ".cost");
        }
    }

    public void upgrade() {
        if (level < maxLevel) {
            level++;
            entities.forEach(Entity::remove);
            spawn();
            isUpgradableTextShown = false;
            upgradableText.setCustomNameVisible(false);
        }
    }

    public void prestige(int PrestigeType) {
        if (level == maxLevel) {
            level = 4;
            prestige = PrestigeType;
            entities.forEach(Entity::remove);
            spawn();
            isUpgradableTextShown = false;
            upgradableText.setCustomNameVisible(false);
        }
    }

    public static int getTowerBuyCost(TowerType towerType, int level, int prestige) {
        FileConfiguration cfg = CustomConfig.getFileConfiguration("towerConfig");
        if (prestige == 0) {
            return cfg.getInt(towerType.name() + ".levels." + level + ".cost");
        } else {
            return cfg.getInt(towerType.name() + ".prestiges." + prestige + ".cost");
        }
    }

    public static Tower summonTower(Location location, TowerType towerType, int level, int prestige, Track track) {
        switch (towerType) {
            case ARCHER -> {
                return new ArcherTower(location, level, prestige, track);
            }
            case SLIME -> {
                return new SlimeTower(location, level, prestige, track);
            }
            case MAGMA -> {
                return new MagmaTower(location, level, prestige, track);
            }
            case WITCH -> {
                return new WitchTower(location, level, prestige, track);
            }
            case ENDERMAN -> {
                return new EndermanTower(location, level, prestige, track);
            }
            case VILLAGER -> {
                return new VillagerTower(location, level, prestige, track);
            }
            case GUARDIAN -> {
                return new GuardianTower(location, level, prestige, track);
            }
            case TESLA -> {
                return new TeslaTower(location, level, prestige, track);
            }
        }
        return null;
    }

    public boolean isOnCooldown() {
        if (track.getGame().getTickCounter() % 4 == 0) {
            showUpgradableText();
        }
        if (stunTimer > 0) {
            stunTimer-=0.25F;
            showStunEffectParticles();
            return true;
        }
        if (this instanceof TeslaTower teslaTower) {
            teslaTower.continuouslyAttack();
        }
        if (slownessTimer > 0) {
            slownessTimer -= 0.25F;
        } else {
            slownessAmplifier = 1F;
            hideSlownessIndicator();
        }
        if (cooldown > 0) {
            cooldown = cooldown - (0.25 / slownessAmplifier);
            return true;
        }
        if (blindnessTimer > 0) {
            blindnessTimer -= 0.25F;
            showBlindnessEffectParticles();
            if (Math.random() < blindnessAmplifier) {
                resetCooldown();
                return true;
            }
        }
        return false;
    }

    protected void resetCooldown() {
        cooldown = reload/reloadBoost;
    }

    private void showSlownessIndicator() {
        slownessIndicatorLocation.getBlock().setType(Material.COBWEB);
    }
    private void hideSlownessIndicator() {
        slownessIndicatorLocation.getBlock().setType(Material.AIR);
    }

    protected TWMob getClosestToExit(Set<TWMob> mobSet) {
        double minPathLeft = 0;
        boolean first = true;
        TWMob target = mobSet.iterator().next();
        for (TWMob mob : mobSet) {
            double pathLeft = mob.getMobNavigation().getPathLeft();
            if ((pathLeft < minPathLeft || first) && pathLeft != 0) {
                minPathLeft = pathLeft;
                target = mob;
                first = false;
            }
        }
        return target;
    }

    public Set<TWMob> defaultAttackMobs(Set<TWMob> mobSet) {
        Set<TWMob> inAttackRadius = null;

        //dobi prvega
        TWMob target = getClosestToExit(mobSet);

        //preveri splash
        if (splash > 0) {
            assert target != null;
            inAttackRadius = track.getMobsInRange(target.getLocation(), splash);
        } else {
            inAttackRadius = Collections.singleton(target);
        }

        animateAttack(target);
        return inAttackRadius;
    }

    public void sell() {
        isUpgradableTextShown = false;
        upgradableText.remove();
        hideSlownessIndicator();
        entities.forEach(mob -> {
            AntiFire.getListener().remove(mob);
            mob.remove();
        });
        cleanup();
        for (VillagerTower villagerTower : villagerProtectors) {
            villagerTower.removeFromProtection(this);
        }
        track.cleanupSoldTower(this);
    }

    public void applyDebuffProtection(VillagerTower villagerProtector) {
        blindnessTimer = 0;
        slownessTimer = 0;
        stunTimer = 0;
        villagerProtectors.add(villagerProtector);
        immuneToDebuffs = true;
    }

    public void applyReloadBoost(float percent) {
        reloadBoost = 1 + percent;
    }

    public void removeVillagerBoosts(VillagerTower villagerTower) {
        villagerProtectors.remove(villagerTower);
        if (villagerProtectors.isEmpty()) {
            reloadBoost = 1;
            immuneToDebuffs = false;
        }
    }

    public void showRange() {
        int count = 100;

        double angleIncrement = (2 * Math.PI) / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleIncrement;

            double spawnX = location.x() + Math.cos(angle) * range;
            double spawnY = location.y() + 3;
            double spawnZ = location.z() + Math.sin(angle) * range;

            track.getTwPlayer().getPlayer().spawnParticle(Particle.HAPPY_VILLAGER, spawnX, spawnY, spawnZ, 3, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void showStunEffectParticles() {
        float x = slownessIndicatorLocation.getBlockX() + 0.5F;
        float y = slownessIndicatorLocation.getBlockY() + 0.5F;
        float z = slownessIndicatorLocation.getBlockZ() + 0.5F;
        location.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, x, y, z, 3, 0.2, 0.1, 0.2, 0.0);
    }

    private void showBlindnessEffectParticles() {
        float x = slownessIndicatorLocation.getBlockX() + 0.5F;
        float y = slownessIndicatorLocation.getBlockY() + 0.5F;
        float z = slownessIndicatorLocation.getBlockZ() + 0.5F;
        location.getWorld().spawnParticle(Particle.EFFECT, x, y, z, 3, 0.2, 0.1, 0.2, 0.0);
    }

    public void upgradeFromDistance() {
        if (level < maxLevel) {
            int cost = Tower.getTowerBuyCost(towerType, level + 1, 0) - Tower.getTowerBuyCost(towerType, level, 0);
            boolean success = track.getTwPlayer().buyForCoin(cost);
            if (success) {
                upgrade();
            }
        } else {
            if (maxLevel == 3) {
                track.getTwPlayer().openTowerMenu(location);
            }
        }
    }

    private void showUpgradableText() {
        if ((maxLevel == 2 && level == 2) || level == 4) {
            isUpgradableTextShown = false;
            upgradableText.setCustomNameVisible(false);
            return;
        }

        int cost = 0;
        if (level < maxLevel) {
            cost = Tower.getTowerBuyCost(towerType, level + 1, 0) - Tower.getTowerBuyCost(towerType, level, 0);
        } else {
            if (maxLevel == 3) {
                int cost1 = Tower.getTowerBuyCost(towerType, level, 1) - Tower.getTowerBuyCost(towerType, level, 0);
                int cost2 = Tower.getTowerBuyCost(towerType, level, 2) - Tower.getTowerBuyCost(towerType, level, 0);
                cost = Math.min(cost1, cost2);
            }
        }

        if (cost <= track.getTwPlayer().getCoin()) {
            if (!isUpgradableTextShown) {
                isUpgradableTextShown = true;
                upgradableText.setCustomNameVisible(true);
            }
        } else {
            if (isUpgradableTextShown) {
                isUpgradableTextShown = false;
                upgradableText.setCustomNameVisible(false);
            }
        }
    }

    private void initiateUpgradableText() {
        upgradableText = (ArmorStand) location.getWorld().spawnEntity(slownessIndicatorLocation.clone().add(0.5, -2, 0.5), EntityType.ARMOR_STAND);
        upgradableText.setInvulnerable(true);
        upgradableText.setInvisible(true);
        upgradableText.setGravity(false);
        upgradableText.setCustomNameVisible(false);
        upgradableText.customName(Component.text("✜", NamedTextColor.GREEN).append(Component.text(" UPGRADE AVAILABLE ", NamedTextColor.WHITE)).append(Component.text("✜", NamedTextColor.GREEN)));
    }

    public TowerType getTowerType() {
        return towerType;
    }

    public int getLevel() {
        return level;
    }

    public int getPrestige() {
        return prestige;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public int getKills() {
        return kills;
    }

    public int getShots() {
        return shots;
    }
    public int getDamage() {
        return damage;
    }

    public double getReload() {
        return reload;
    }

    public int getSplash() {
        return splash;
    }
    public int getRange() {
        return range;
    }

    public Location getLocation() {
        return location;
    }
    public void correctDamageDealt(int correction) {
        damageDealt = damageDealt - correction;
    }

    public int getCost() {
        return cost;
    }
    public void increaseKillCount() {
        kills++;
    }
    public void applySlowness(int duration, float amplifier) {
        if (immuneToDebuffs) return;
        slownessAmplifier = 1 + amplifier;
        slownessTimer = duration;
        showSlownessIndicator();
    }
    public void applyStun(int duration) {
        if (immuneToDebuffs) return;
        stunTimer = duration;
    }
    public void applyBlindness(int duration, float amplifier) {
        if (immuneToDebuffs) return;
        blindnessTimer = duration;
        blindnessAmplifier = amplifier;
    }
    public boolean isEntityInTower(Mob mob) {
        return entities.contains(mob);
    }



    @Override
    public String toString() {
        StringBuilder entitiesStatus = new StringBuilder();
        for (Mob mob : entities) {
            entitiesStatus.append(mob.toString() + "{" +
                    "dead=" + mob.isDead() +
                    ", invisible=" + mob.isInvisible() +
                    ", ticking=" + mob.isTicking() +
                    ", empty(!haspassanger)=" + mob.isEmpty() +
                    ", location=" + mob.getLocation() +
                    ", valid=" + mob.isValid() +
                    ", killer=" + mob.getKiller() +
                    ", ticksLived=" + mob.getTicksLived() +
                    ", UUID=" + mob.getUniqueId() +
                    '}');
        }
        String upgradableTextStatus = upgradableText.toString() + "{" +
                "dead=" + upgradableText.isDead() +
                ", invisible=" + upgradableText.isInvisible() +
                ", ticking=" + upgradableText.isTicking() +
                ", empty(!haspassanger)=" + upgradableText.isEmpty() +
                ", location=" + upgradableText.getLocation() +
                ", valid=" + upgradableText.isValid() +
                '}';
        return "Tower{" +
                "level=" + level +
                ", location=" + location +
                ", entities=" + entities.toString() +
                ", villagerProtectors=" + villagerProtectors +
                ", twPlayerName=" + track.getTwPlayer().getPlayer().getName() +
                ", prestige=" + prestige +
                ", cooldown=" + cooldown +
                ", immuneToDebuffs=" + immuneToDebuffs +
                ", reloadBoost=" + reloadBoost +
                ", towerType=" + towerType +
                ", slownessTimer=" + slownessTimer +
                ", slownessAmplifier=" + slownessAmplifier +
                ", stunTimer=" + stunTimer +
                ", blindnessTimer=" + blindnessTimer +
                ", blindnessAmplifier=" + blindnessAmplifier +
                ", isUpgradableTextShown=" + isUpgradableTextShown +
                "\n /!\\ ENTITY STATUSES /!\\ \n" +
                ", entititiesStatus=" + entitiesStatus +
                ", upgradableText=" + upgradableTextStatus +
                '}';
    }
}