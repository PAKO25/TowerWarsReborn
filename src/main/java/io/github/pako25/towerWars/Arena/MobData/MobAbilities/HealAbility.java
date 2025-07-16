package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;

import java.util.Set;

public class HealAbility implements  TickAbility {

    private final float factor;
    private final int range;
    private final TWMob twMob;

    public HealAbility(float factor, int range, TWMob twMob) {
        this.factor = factor;
        this.twMob = twMob;
        this.range = range;
    }

    @Override
    public void onTick() {
        Set<TWMob> mobsInRange = twMob.getTrack().getMobsInRange(twMob.getLocation(), range);
        mobsInRange.forEach((TWMob mob) -> mob.heal(factor));
        //heala samo druge, implementirano v mob.heal
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.TICK;
    }
}
