package io.github.pako25.towerWars.Arena.MobData.MobAbilities;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Arena.TWMob;
import io.github.pako25.towerWars.Arena.Track;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class SplitAbility implements DeathAbility {

    private final TWMob twMob;
    private final Track track;

    public SplitAbility(TWMob twMob) {
        this.twMob = twMob;
        track = twMob.getTrack();
    }

    @Override
    public boolean onDeath(boolean killed) {
        if (killed) {
            int nextWaypointIndex = twMob.getMobNavigation().getNavigation().getPath().getNextNodeIndex();
            track.summonMob(MobType.MINI_ZOMBIE, getNewTrimmedPath(nextWaypointIndex), twMob.getSummonerTWPlayer());
            track.summonMob(MobType.MINI_ZOMBIE, getNewTrimmedPath(nextWaypointIndex), twMob.getSummonerTWPlayer());
            track.summonMob(MobType.MINI_ZOMBIE, getNewTrimmedPath(nextWaypointIndex), twMob.getSummonerTWPlayer());
        }
        return false;
    }

    private ArrayList<Vector> getNewTrimmedPath(int nextWaypointIndex) {
        ArrayList<Vector> path = track.getRandomPath();
        ArrayList<Vector> trimmedPath = new ArrayList<>();

        trimmedPath.add(twMob.getLocation().clone().subtract(track.getTrackSpawn()).toVector()); //spawn
        for (int i = nextWaypointIndex + 1; i < path.size(); i++) {
            trimmedPath.add(path.get(i));
        }

        return trimmedPath;
    }

    @Override
    public boolean isAbilityType(AbilityTypes abilityType) {
        return abilityType == AbilityTypes.DEATH;
    }
}
