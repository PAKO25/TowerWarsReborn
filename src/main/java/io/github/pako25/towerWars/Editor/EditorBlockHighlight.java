package io.github.pako25.towerWars.Editor;

import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.NoSuchElementException;

public class EditorBlockHighlight extends BukkitRunnable {

    public static EditorBlockHighlight EditorBlockHighlightFactory() {
        EditorBlockHighlight i = new EditorBlockHighlight();
        i.start();
        return i;
    }

    @Override
    public void run() {
        for (TWPlayer twPlayer : TWPlayer.getTWPlayerCollection()) {
            if (!twPlayer.isInEditor()) continue;

            Player player = twPlayer.getPlayer();
            ArenaEditor arenaEditor = twPlayer.getActiveArenaEditor();

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.STICK) {
                for (Location trackSpawn : arenaEditor.getTrackSpawns()) {
                    spawnHighlightParticles(trackSpawn.getBlock(), player, Particle.HAPPY_VILLAGER);
                }
            }
            if (item.getType() == Material.OAK_FENCE) {
                try {
                    Location trackSpawn = arenaEditor.getTrackSpawns().getFirst();
                    spawnHighlightParticles(trackSpawn.getBlock(), player, Particle.FALLING_HONEY);
                    for (Vector trackBound : arenaEditor.getTrackBounds()) {
                        if (trackBound == null) continue;
                        spawnHighlightParticles(trackSpawn.clone().add(trackBound).getBlock(), player, Particle.HAPPY_VILLAGER);
                    }
                } catch (NoSuchElementException ignored) {
                }
            }
            if (item.getType() == Material.REPEATER || item.getType() == Material.RAIL) {
                if (arenaEditor.getTrackSpawns().isEmpty()) return;
                Location trackSpawn = arenaEditor.getTrackSpawns().getFirst();
                spawnHighlightParticles(trackSpawn.getBlock(), player, Particle.FALLING_HONEY);

                if (arenaEditor.getPaths().isEmpty()) return;
                List<Vector> path = arenaEditor.getPaths().get(arenaEditor.getSelectedPathIndex());

                if (path.size() == 1) {
                    spawnHighlightParticles(trackSpawn.clone().add(path.getFirst()).getBlock(), player, Particle.WHITE_SMOKE);
                } else {
                    for (int i = 0; i < path.size(); i++) {
                        if (i == 0) {
                            spawnHighlightParticles(trackSpawn.clone().add(path.get(i)).getBlock(), player, Particle.WHITE_SMOKE);
                        } else if (i == path.size() - 1) {
                            spawnHighlightParticles(trackSpawn.clone().add(path.get(i)).getBlock(), player, Particle.SMOKE);
                        } else {
                            spawnHighlightParticles(trackSpawn.clone().add(path.get(i)).getBlock(), player, Particle.HAPPY_VILLAGER);
                        }
                    }
                }
            }
        }
    }

    private void spawnHighlightParticles(Block block, Player player, Particle particle) {
        player.spawnParticle(
                particle,
                block.getLocation().add(0.5, 1.0, 0.5),
                10,   // count
                0.3, 0.3, 0.3, // offset XYZ
                0.05  // speed
        );
    }

    public void start() {
        // Schedule task every 4 ticks
        this.runTaskTimer(TowerWars.getPlugin(), 0L, 8L);
    }

    public void stop() {
        this.cancel();
    }
}

