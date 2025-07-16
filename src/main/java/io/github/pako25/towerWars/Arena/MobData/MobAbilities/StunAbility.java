package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Tower.Tower;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StunAbility implements DeathAbility {

    private final int range;
    private final int duration;
    private final TWMob twMob;

    public StunAbility(int range, int duration, TWMob twMob) {
        this.range = range;
        this.duration = duration;
        this.twMob = twMob;
    }

    @Override
    public boolean onDeath(boolean killed) {
        if (killed) {
            List<Tower> towerSet = getTowersInRange(twMob.getTrack().getTowers());
            towerSet.forEach((Tower tower) -> tower.applyStun(duration));
        }
        return false;
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.DEATH;
    }

    private List<Tower> getTowersInRange(Map<Location, Tower> towerMap) {
        List<Tower> towerSet = new ArrayList<>();
        for (Map.Entry<Location, Tower> entry : towerMap.entrySet()) {
            Location location = entry.getKey();
            Tower tower = entry.getValue();

            if (location.distanceSquared(twMob.getLocation()) <= range * range) {
                towerSet.add(tower);
            }
        }
        return towerSet;
    }
}