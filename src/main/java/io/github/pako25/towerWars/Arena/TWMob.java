package io.github.pako25.towerWars.Arena;

import io.github.pako25.towerWars.Arena.MobData.MobAbilities.*;
import io.github.pako25.towerWars.Arena.MobData.MobBuilder;
import io.github.pako25.towerWars.Arena.MobData.MobNavigation;
import io.github.pako25.towerWars.Arena.MobData.MobState;
import io.github.pako25.towerWars.GameManagment.PlayerStats;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerSchemas.AttackType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TWMob {
    private Mob creature;
    private Track track;
    private Location trackSpawn;
    private ArrayList<Vector> path = new ArrayList<>();
    private final TWPlayer summonerTWPlayer;
    private final MobNavigation mobNavigation;
    private final List<MobAbility> abilities = new ArrayList<>();
    private Mob navigatableMob;
    private final MobType mobType;

    private double speed;
    private int income;
    private int health;
    private int maxHealth;
    private int cost;
    private int burnTimer = 0;
    private int weaknessTimer = 0;
    private float weaknessAmplifier = 1;
    private boolean alive = true;
    private int noSpecialAbilityTimer = 0;
    private int slownessTimer = 0;

    public TWMob(Track track, ArrayList<Vector> path, Location trackSpawn, MobType mobType, TWPlayer summonerTWPlayer, Mob creature) {
        this.track = track;
        this.path = path;
        this.trackSpawn = trackSpawn;
        this.summonerTWPlayer = summonerTWPlayer;
        this.creature = creature;
        this.navigatableMob = creature;
        this.mobNavigation = new MobNavigation(path, track.getTrackSpawn(), this);;
        this.mobType = mobType;
        applyAttributes(mobType);
        updateHealthDisplay();
        Bukkit.getScheduler().runTaskLater(track.getPlugin(), mobNavigation::startNavigation, 3L);
    }

    private void applyAttributes(MobType mobType) {
        //applya atribute od tistega kaj je summonal moba
        MobState mobState = summonerTWPlayer.getTrack().getMobStates().getMobState(mobType);
        this.cost = mobState.getCost(summonerTWPlayer.getIncome());
        this.health = mobState.getHealth();
        maxHealth = this.health;
        this.income = mobState.getIncome(summonerTWPlayer.getIncome());
        this.speed = mobState.getSpeed();
    }

    public synchronized boolean takeDamage(int damage, Tower tower, AttackType attackType) {
        if (attackType == AttackType.AOE) {
            AOEDodge aoeDodge = (AOEDodge) getAbilityByType(AbilityTypes.AOEDODGE);
            if (aoeDodge != null && aoeDodge.dodge()) return false;
        }
        OnHitAbility onHitAbility = (OnHitAbility) getAbilityByType(AbilityTypes.HIT);
        if (onHitAbility != null) onHitAbility.onHit();

        creature.playHurtAnimation(1);
        health = (int) (health - getAbsorbedDamageFromAbsorbers(damage) * weaknessAmplifier);
        if (health <= 0) {
            tower.increaseKillCount();
            despawn(true);
            tower.correctDamageDealt(Math.abs(health));
        }
        updateHealthDisplay();
        return true;
    }

    private int getAbsorbedDamageFromAbsorbers(int damage) {
        //loopa skozi bližnje mobe, za vsakega dobi absorbiran damage in ga odšteje
        //preskoči če ima sam absorber ability.
        DamageAbsorber damageAbsorber = (DamageAbsorber) getAbilityByType(AbilityTypes.ABSORBER);
        if (damageAbsorber != null) return damage;

        Set<TWMob> mobSet = track.getMobsInRange(getLocation(), 5);
        for (TWMob closeMob : mobSet) {
            damage -= closeMob.absorbDamage(damage);
        }
        return damage;
    }

    public void updateHealthDisplay() {
        creature.customName(Component.text(summonerTWPlayer.getPlayer().getName() + " ", summonerTWPlayer.getTrack().getColor()).append(Component.text(health, NamedTextColor.WHITE)).append(Component.text(" ❤", NamedTextColor.RED)));
        creature.setCustomNameVisible(true);
    }

    public void applySlowness(float amplifier, int duration) {
        slownessTimer = duration;
        ReduceSlowAbility reduceSlowAbility = (ReduceSlowAbility) getAbilityByType(AbilityTypes.REDUCEDSLOW);
        if (reduceSlowAbility != null) {
            float antiAmplifier = reduceSlowAbility.getAmplifier();
            navigatableMob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MobBuilder.BaseSpeed / ((1 + amplifier) / (1 + antiAmplifier)));
        } else {
            navigatableMob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MobBuilder.BaseSpeed / (1 + amplifier));
        }
    }

    public void applyWeakness(float percent, int seconds) {
        if (seconds > weaknessTimer) {
            weaknessTimer = seconds;
        }
        weaknessAmplifier = 1 + percent;
    }

    //teče vsake 0.25 sekunde
    public void tick(boolean fullsecond) {
        if (!alive) return;

        if (health <= 0) {
            alive = false;
            despawn(true);
        }

        //preveri če je na koncu
        if (mobNavigation.getNavigation() != null && mobNavigation.getNavigation().isDone() && !isHodiNazaj()) {
            track.loseLive(this);
            boolean despawnSuccessfull = despawn(false); //nič ga ne ubije, samo do konca je prišo
            if (despawnSuccessfull) {
                alive = false;
            }
        }

        if (fullsecond) tickEffects();
    }

    //teče vsako sekundo
    public void tickEffects() {
        if (burnTimer > 0) {
            burnTick();
            burnTimer = burnTimer - 1;
        }
        if (weaknessTimer <= 0) {
            weaknessAmplifier = 1;
        }
        if (noSpecialAbilityTimer > 0) {
            noSpecialAbilityTimer = noSpecialAbilityTimer - 1;
        }
        if (slownessTimer > 0) {
            slownessTimer -= 1;
        } else {
            navigatableMob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MobBuilder.BaseSpeed);
        }
        if (mobNavigation.getHodiNazajTimer() > 0) {
            mobNavigation.decreaseHodiNazajTimer();
        } else {
            if (mobNavigation.isHodiNazaj()) {
                mobNavigation.idiNaprej();
                mobNavigation.setHodiNazaj(false);
            }
        }
        if (track.getGame().getTickCounter() % 12 == 0) {
            TickAbility tickAbility = (TickAbility) getAbilityByType(AbilityTypes.TICK);
            if (tickAbility != null) {
                tickAbility.onTick();
            }
        }
    }

    public void applyBurn(int seconds) {
        if (seconds > burnTimer) {
            burnTimer = seconds;
        }
    }

    public void burnTick() {
        health = (int) (health*0.95);
        updateHealthDisplay();
    }

    public boolean despawn(boolean killed) {
        if (!alive) return false;

        boolean cancelDespawn = false;
        DeathAbility deathAbility = (DeathAbility) getAbilityByType(AbilityTypes.DEATH);
        if (deathAbility != null) cancelDespawn = deathAbility.onDeath(killed);
        if (cancelDespawn) return false;

        if (navigatableMob != creature) navigatableMob.remove();
        creature.remove();
        alive = false;

        if (killed) {
            giveKillBonuses();
        }

        return true;
    }

    private void giveKillBonuses() {
        if (PlayerStats.trackingEnabled) PlayerStats.getStats(track.getTwPlayer().getPlayer().getUniqueId()).increaseMob_kills();

        NoKillBonusesAbility noKillBonusesAbility = (NoKillBonusesAbility) getAbilityByType(AbilityTypes.NOKILLBONUSES);
        if (noKillBonusesAbility != null) return;

        int killGold = (int) Math.round(cost * 0.17);
        int killIncome = (int) Math.round(cost * 0.02);
        ArmorStand killDisplay = (ArmorStand) creature.getWorld().spawnEntity(creature.getLocation(), EntityType.ARMOR_STAND);
        Component textComponent = Component.text("+" + killGold + " gold ", NamedTextColor.GOLD);
        if (killIncome > 0) {
            textComponent = textComponent
                    .append(Component.text("(", NamedTextColor.GRAY))
                    .append(Component.text("+" + killIncome + " income", NamedTextColor.AQUA))
                    .append(Component.text(")", NamedTextColor.GRAY));
        }

        killDisplay.customName(textComponent);
        killDisplay.setCustomNameVisible(true);
        killDisplay.setGravity(false);
        killDisplay.setInvisible(true);
        killDisplay.setInvulnerable(true);
        track.getTwPlayer().increaseCoin(killGold);
        track.getTwPlayer().increaseIncome(killIncome);
        track.getMobKillDisplays().add(killDisplay);
    }

    private MobAbility getAbilityByType(AbilityTypes abilityType) {
        if (noSpecialAbilityTimer > 0) return null;
        for (MobAbility ability : abilities) {
            if (ability.isAbilityType(abilityType)) {
                return ability;
            }
        }
        return null;
    }

    public void heal(float factor) {
        TickAbility tickAbility = (TickAbility) getAbilityByType(AbilityTypes.TICK);
        if (tickAbility instanceof HealAbility) return; //se ne heala če ma sam heal ability

        health += (int) (maxHealth * factor);
        if (health > maxHealth) health = maxHealth;
        updateHealthDisplay();
    }

    public int absorbDamage(int damage) {
        //preveri če je absorber -> v tem primeru uporabi ability
        DamageAbsorber damageAbsorber = (DamageAbsorber) getAbilityByType(AbilityTypes.ABSORBER);
        if (damageAbsorber != null) {
            int absorbedDamage = damageAbsorber.absorbDamage(damage);
            if (absorbedDamage > health) {
                despawn(true);
                return health;
            }
            health -= absorbedDamage;
            updateHealthDisplay();
            return absorbedDamage;
        }
        return 0;
    }

    public Location getLocation() {
        return creature.getLocation();
    }
    public Location getEyeLocation() {
        return creature.getEyeLocation();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean hasWeakness() {
        return weaknessTimer > 0;
    }

    public void disableSpecialAbility(int duration) {
        noSpecialAbilityTimer = duration;
    }

    public Mob getCreature() {
        return creature;
    }

    public boolean isHodiNazaj() {
        return mobNavigation.isHodiNazaj();
    }

    public MobNavigation getMobNavigation() {
        return mobNavigation;
    }

    public double getSpeed() {
        return speed;
    }

    public List<MobAbility> getAbilities() {
        return abilities;
    }
    public Track getTrack() {
        return track;
    }

    public ArrayList<Vector> getPath() {
        return path;
    }

    public MobType getMobType() {
        return mobType;
    }
    public void setNavigatableMob(Mob mob) {
        navigatableMob = mob;
    }
    public Mob getNavigatableCreature() {
        return navigatableMob;
    }

    public TWPlayer getSummonerTWPlayer() {
        return summonerTWPlayer;
    }

    public void gameEnd() {
        mobNavigation.getNavigation().stop();
        if (navigatableMob != creature) navigatableMob.remove();
        creature.remove();
    }
}