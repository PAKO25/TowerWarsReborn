package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

public class NoKillBonusesAbility implements MobAbility {
    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.NOKILLBONUSES;
    }
}