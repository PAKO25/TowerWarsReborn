package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Tower.Tower;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlindAbility implements TickAbility {

    private final int range;
    private final float amplifier;
    private final TWMob twMob;
    private final int duration;

    public BlindAbility(int range, float amplifier, int duration, TWMob twMob) {
        this.range = range;
        this.amplifier = amplifier;
        this.twMob = twMob;
        this.duration = duration;
    }

    @Override
    public void onTick() {
        List<Tower> towers = getTowersInRange(twMob.getTrack().getTowers());
        towers.forEach((Tower tower) -> tower.applyBlindness(duration, amplifier));
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
