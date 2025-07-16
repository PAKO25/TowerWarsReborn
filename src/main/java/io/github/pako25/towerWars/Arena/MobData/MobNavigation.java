package io.github.pako25.towerWars.Arena.MobData;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Arena.TWMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class MobNavigation {
    private PathNavigation navigation;
    private final ArrayList<Vector> path;
    private final Location trackSpawn;
    private final TWMob twMob;
    private boolean hodiNazaj = false;
    private double lastPathLeft = 0;
    private int hodiNazajTimer = 0;

    public MobNavigation(ArrayList<Vector> path, Location trackSpawn, TWMob twMob) {
        this.path = path;
        this.trackSpawn = trackSpawn;
        this.twMob = twMob;
    }

    public void startNavigation() {
        navigation = ((PathfinderMob) ((CraftLivingEntity) twMob.getNavigatableCreature()).getHandle()).getNavigation();
        navigation.setCanFloat(true);
        Path navigationPath = null;
        for (int i = 1; i < path.size(); i++) {
            Vector waypoint = path.get(i);
            Location goal = trackSpawn.clone().add(waypoint).add(0.5, 0, 0.5);
            if (i == 1) {
                navigationPath = navigation.createPath(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), 0);
                assert navigationPath != null;
                navigationPath.nodes.clear();
            }
            navigationPath.nodes.add(new Node(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ()));
        }

        navigation.moveTo(navigationPath, Math.sqrt(twMob.getSpeed()));
    }

    public double getPathLeft() {
        if (navigation == null) return 0;
        if (hodiNazaj) return lastPathLeft;
        List<Node> nodeList = navigation.getPath().nodes;
        Node nextNode = navigation.getPath().getNextNode();
        double pathLeft = customVectorDistance(twMob.getLocation().toVector(), nodeToVector(nextNode));

        ListIterator<Node> it = nodeList.listIterator();
        while (it.hasNext()) {
            Node node = it.next();
            if (node.x == nextNode.x && node.z == nextNode.z) break;
        }
        Node previousNode = nextNode;
        int i = 1;
        while (it.hasNext()) {
            i++;
            Node currentNode = it.next();
            pathLeft = pathLeft + customVectorDistance(nodeToVector(previousNode), nodeToVector(currentNode));
            previousNode = currentNode;
        }
        lastPathLeft = pathLeft;
        return pathLeft;
    }

    private Vector nodeToVector(Node node) {
        return new Vector(node.x, node.y, node.z);
    }

    private double customVectorDistance(Vector v1, Vector v2) {
        double dx = Math.abs(v1.getX() - v2.getX());
        double dz = Math.abs(v1.getZ() - v2.getZ());
        return dx + dz;
    }

    public void teleportBack() {
        twMob.getNavigatableCreature().teleport(trackSpawn.clone().add(path.getFirst()));
        if (twMob.getMobType() == MobType.GHAST || twMob.getMobType() == MobType.SQUID || twMob.getMobType() == MobType.RABBIT) {
            twMob.getNavigatableCreature().removePassenger(twMob.getCreature());
            twMob.getCreature().teleport(trackSpawn.clone().add(path.getFirst()));
            twMob.getNavigatableCreature().teleport(trackSpawn.clone().add(path.getFirst()));
            twMob.getNavigatableCreature().addPassenger(twMob.getCreature());
        }
        navigation.stop();
        startNavigation();
    }

    public void walkBackwards(int duration) {
        if (hodiNazaj) {
            hodiNazajTimer = duration;
            return;
        }

        Node nextNode = navigation.getPath().getNextNode();
        List<Node> previousNodes = new ArrayList<>();
        for (Vector waypoint : path) {
            Location goal = trackSpawn.clone().add(waypoint).add(0.5, 0, 0.5);
            Node node = new Node(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
            if (node.equals(nextNode)) break;
            previousNodes.add(node);
        }
        Collections.reverse(previousNodes);
        Location spawn = trackSpawn.clone().add(path.getFirst());
        previousNodes.add(new Node(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ())); //doda spawn če ga obrne čisto na začetku
        navigation.stop();

        //novi path za nazaj
        Path backwardsPath = navigation.createPath(nextNode.x, nextNode.y, nextNode.z, 0);
        backwardsPath.nodes.clear();
        backwardsPath.nodes.addAll(previousNodes);
        navigation.moveTo(backwardsPath, Math.sqrt(twMob.getSpeed()));

        hodiNazajTimer = duration;
        hodiNazaj = true;
    }

    public void idiNaprej() {
        if (navigation.getPath().getNextNodeIndex() == navigation.getPath().nodes.size()) {
            startNavigation();
            return;
        }

        Node nextNode = navigation.getPath().getNextNode();
        navigation.stop();
        //novi path za naprej
        Path forwardPath = navigation.createPath(nextNode.x, nextNode.y, nextNode.z, 0);
        forwardPath.nodes.clear();
        boolean reachedNextNode = false;
        for (Vector waypoint : path) {
            Location goal = trackSpawn.clone().add(waypoint).add(0.5, 0, 0.5);
            Node node = new Node(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
            if (reachedNextNode) {
                forwardPath.nodes.add(node);
            }
            if (node.equals(nextNode)) {
                reachedNextNode = true;
            }
        }
        navigation.moveTo(forwardPath, Math.sqrt(twMob.getSpeed()));
    }

    public void skipBlocks(int amount) {
        Location mobLocation = twMob.getLocation().clone();
        Node nextNode = navigation.getPath().getNextNode();

        Vector mobLocationVector = mobLocation.toVector();
        Vector lastWaypoint = mobLocation.toVector();
        Vector nextWaypoint = nodeToVector(nextNode);
        double distance = customVectorDistance(lastWaypoint, nextWaypoint);

        if (distance >= amount) {
            Vector endVector = nextWaypoint.clone().subtract(lastWaypoint).normalize().multiply(amount);
            Location newPosition = mobLocationVector.clone().add(endVector).toLocation(mobLocation.getWorld());
            twMob.getCreature().teleport(newPosition);
            return;
        }

        Vector endVector = nextWaypoint.clone().subtract(lastWaypoint);

        int i = path.indexOf(nextWaypoint.clone().subtract(trackSpawn.toVector())) + 1;

        while (distance < amount && i < path.size()) {
            lastWaypoint = nextWaypoint;
            nextWaypoint = trackSpawn.clone().toVector().add(path.get(i));
            double segmentLength = customVectorDistance(lastWaypoint, nextWaypoint);
            distance += segmentLength;
            if (distance >= amount) {
                endVector.add(nextWaypoint.clone().subtract(lastWaypoint).normalize().multiply(segmentLength - (distance - amount)));
            } else {
                endVector.add(nextWaypoint.clone().subtract(lastWaypoint));
            }
            i++;
        }

        Location newPosition = mobLocationVector.clone().add(endVector).toLocation(mobLocation.getWorld());
        newPosition.setY(mobLocation.getY());
        navigation.stop();
        twMob.getCreature().teleport(newPosition);

        Path forwardPath = navigation.createPath(nextNode.x, nextNode.y, nextNode.z, 0);
        forwardPath.nodes.clear();
        boolean reachedNextNode = false;
        forwardPath.nodes.add(new Node(nextWaypoint.getBlockX(), nextWaypoint.getBlockY(), nextWaypoint.getBlockZ()));
        for (Vector waypoint : path) {
            Vector absoluteVector = trackSpawn.clone().toVector().add(waypoint);
            if (reachedNextNode) {
                forwardPath.nodes.add(new Node(absoluteVector.getBlockX(), absoluteVector.getBlockY(), absoluteVector.getBlockZ()));
            }
            if (absoluteVector.equals(nextWaypoint)) {
                reachedNextNode = true;
            }
        }
        navigation.moveTo(forwardPath, Math.sqrt(twMob.getSpeed()));
    }

    public PathNavigation getNavigation() {
        return navigation;
    }

    public boolean isHodiNazaj() {
        return hodiNazaj;
    }

    public double getLastPathLeft() {
        return lastPathLeft;
    }
    public void setHodiNazaj(boolean hodiNazaj) {
        this.hodiNazaj = hodiNazaj;
    }

    public int getHodiNazajTimer() {
        return hodiNazajTimer;
    }

    public void decreaseHodiNazajTimer() {
        hodiNazajTimer--;
    }
}