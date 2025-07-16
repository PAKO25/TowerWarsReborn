package io.github.pako25.towerWars.Player;

import io.github.pako25.towerWars.Player.Inventories.PlaceTowerInventory;
import io.github.pako25.towerWars.TowerWars;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HighlightBlock extends BukkitRunnable {

    public static HighlightBlock HighlightBlockFactory() {
        HighlightBlock i = new HighlightBlock();
        i.start();
        return i;
    }

    @Override
    public void run() {
        for (TWPlayer twPlayer : TWPlayer.getTWPlayerCollection()) {
            if (isHoldingPlaceTowerItem(twPlayer.getPlayer()) && twPlayer.isInGame()) {
                Block targetBlock = twPlayer.getPlayer().getTargetBlockExact(30);
                if (targetBlock != null) {
                    if (targetBlock.getType() != twPlayer.getGame().getTowerPlaceMaterial()) return;
                    if (twPlayer.getTrack().isLocationInsideTrackBounds(targetBlock.getLocation())) {
                        spawnHighlightParticles(targetBlock, twPlayer.getPlayer());
                    }
                }
            }
        }
    }

    private boolean isHoldingPlaceTowerItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.getType() == Material.ARMOR_STAND;
    }

    private void spawnHighlightParticles(Block block, Player player) {
        player.spawnParticle(
                Particle.CRIT,
                block.getLocation().add(0.5, 1.0, 0.5),
                10,   // count
                0.3, 0.3, 0.3, // offset XYZ
                0.05  // speed
        );
    }

    public void start() {
        // Schedule task every 4 ticks
        this.runTaskTimer(TowerWars.getPlugin(), 0L, 4L);
    }

    public void stop() {
        this.cancel();
    }
}

