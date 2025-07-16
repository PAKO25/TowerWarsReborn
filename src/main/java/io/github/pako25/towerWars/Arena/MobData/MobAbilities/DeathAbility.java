package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

public interface DeathAbility extends MobAbility {
    public boolean onDeath(boolean killed);
}
