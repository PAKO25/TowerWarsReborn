package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;

public class RespawnAbility implements DeathAbility {

    private final TWMob twMob;

    public RespawnAbility(TWMob twMob) {
        this.twMob = twMob;
    }

    @Override
    public boolean onDeath(boolean killed) {
        if (!killed) {
            twMob.getMobNavigation().teleportBack();
            return true;
        }
        return false;
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.DEATH;
    }
}
