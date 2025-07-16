package io.github.pako25.towerWars.Tower.TowerSchemas;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VillagerTower extends Tower {

    private final Set<Tower> buffedTowers = new HashSet<>();
    private int lastTotalTowersOnTrack = 1;

    public VillagerTower(Location location, int level, int prestige, Track track) {
        super(location, level, prestige, track);
    }

    public void spawn() {
        Villager villager = (Villager) location.getWorld().spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.VILLAGER);
        entities.add(villager);
        villager.setAI(false);
        applyStats(TowerType.VILLAGER);
    }

    private Set<Tower> getTowersInRange(Map<Location, Tower> towerMap) {
        Set<Tower> towerSet = new HashSet<>();
        for (Map.Entry<Location, Tower> entry : towerMap.entrySet()) {
            Location location = entry.getKey();
            Tower tower = entry.getValue();

            if (location.equals(this.location)) continue;

            if (location.distanceSquared(this.location) <= range * range) {
                towerSet.add(tower);
            }
        }
        return towerSet;
    }

    public void buffTowers() {
        if (lastTotalTowersOnTrack == track.getTowers().size()) return;
        lastTotalTowersOnTrack = track.getTowers().size();
        Set<Tower> towersInRange = getTowersInRange(track.getTowers());
        for (Tower tower : towersInRange) {
            if (tower instanceof VillagerTower) continue; //ne buffa ostalih villagerjov
            if (buffedTowers.add(tower)) { //samo če je novi tower -> doda tower, returna true če ga še ni bilo
                tower.applyDebuffProtection(this);
                if (level == 2) {
                    tower.applyReloadBoost(0.1F);
                }
            }
        }
    }

    @Override
    public void attackMobs(Set<TWMob> mobSet) {
    }

    public void removeFromProtection(Tower tower) {
        buffedTowers.remove(tower);
    }

    public void animateAttack(TWMob mob) {
    }

    public void cleanup() {
        for (Tower tower : buffedTowers) {
            tower.removeVillagerBoosts(this);
        }
        buffedTowers.clear();
    }

    @Override
    protected void setSlownessIndicatorHeight() {
        slownessIndicatorLocation = location.clone().add(0, 3, 0);
    }
}
