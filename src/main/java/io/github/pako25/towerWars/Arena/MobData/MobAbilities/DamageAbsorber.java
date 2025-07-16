package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.TWMob;

public class DamageAbsorber implements MobAbility {

    private final TWMob twMob;
    private final float part;

    public DamageAbsorber(float part, TWMob twMob) {
        this.twMob = twMob;
        this.part = part;
    }

    public int absorbDamage(int damage) {
        return (int) (damage * part);
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.ABSORBER;
    }
}
