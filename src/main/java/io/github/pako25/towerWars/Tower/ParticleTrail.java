package io.github.pako25.towerWars.Tower;

import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleTrail {
    public static void spawnParticleTrail(Location source, Location goal, double spacing, int count, Particle particle) {
        double distance = source.distance(goal);
        int points = (int) (distance / spacing);

        for (int step = 0; step < points; step++) {
            double t = (double) step / points;
            double x = source.getX() + t * (goal.getX() - source.getX());
            double y = source.getY() + t * (goal.getY() - source.getY());
            double z = source.getZ() + t * (goal.getZ() - source.getZ());

            Location point = new Location(source.getWorld(), x, y, z);
            source.getWorld().spawnParticle(particle, point, count, 0, 0, 0, 0);
        }
    }
}
