package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;

public class TPOnHit implements OnHitAbility {

    private final TWMob twMob;
    private final int distance;

    public TPOnHit(int distance, TWMob twMob) {
        this.twMob = twMob;
        this.distance = distance;
    }

    @Override
    public void onHit() {
        twMob.getMobNavigation().skipBlocks(distance);
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.HIT;
    }
}
