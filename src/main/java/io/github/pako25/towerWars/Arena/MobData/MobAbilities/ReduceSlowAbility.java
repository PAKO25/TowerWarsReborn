package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

public class ReduceSlowAbility implements MobAbility {

    private final float amplifier;

    public ReduceSlowAbility(float amplifier) {
        this.amplifier = amplifier;
    }

    public float getAmplifier() {
        return amplifier;
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.REDUCEDSLOW;
    }
}
