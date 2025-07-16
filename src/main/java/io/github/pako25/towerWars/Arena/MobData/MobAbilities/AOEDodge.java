package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

public class AOEDodge implements MobAbility {

    private final float chance;

    public AOEDodge(float chance) {
        this.chance = chance;
    }

    public boolean dodge() {
        return Math.random() < chance;
    }

    public boolean isAbilityType(AbilityTypes abilityType) {
        return  abilityType == AbilityTypes.AOEDODGE;
    }
}