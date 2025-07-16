package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Tower.Tower;
import org.bukkit.Location;

import java.util.*;

public class SlowAbility implements TickAbility {

    private final TWMob twMob;
    private final int range;
    private final float amplifier;
    private final int duration;

    public SlowAbility(TWMob twMob, int range, float amplifier, int duration) {
        this.twMob = twMob;
        this.range = range;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    @Override
    public void onTick() {
        List<Tower> towerSet = getTowersInRange(twMob.getTrack().getTowers());
        towerSet.forEach((Tower tower) -> tower.applySlowness(duration, amplifier));
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.TICK;
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
