package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerSchemas.TeslaTower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DebugStickListener implements Listener {
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.STICK) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Debug stick")) return;

        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (twPlayer == null || !twPlayer.isInGame()) return;
        event.setCancelled(true);

        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 30, entity -> !entity.equals(player));
        if (result == null) {
            player.sendMessage(Component.text("No hits for entities, checking for blocks."));
            checkBlock(twPlayer);
            return;
        }
        Entity target = result.getHitEntity();
        boolean found = false;

        if (target instanceof Mob mob) {
            Map<UUID, TWPlayer> allPlayers = TWPlayer.debugGetTWPlayerMap();
            List<Tower> allTowers = new ArrayList<>();
            for (TWPlayer oneTwPlayer : allPlayers.values()) {
                allTowers.addAll(oneTwPlayer.getTrack().getTowers().values());
            }
            for (Tower tower : allTowers) {
                if (tower.isEntityInTower(mob)) {
                    found = true;
                    player.sendMessage(Component.text("Entity belongs to tower: " + tower));
                    if (tower instanceof TeslaTower teslaTower) {
                        twPlayer.getPlayer().sendMessage(Component.text("Additional debug for tesla tower: " + teslaTower.debugInfo()));
                    }
                }
            }
        }
        if (!found) {
            player.sendMessage(Component.text("Entity doesn't belong to a tower.", NamedTextColor.RED));
            if (target instanceof Mob mob) {
                String mobStatus = mob + "{" +
                        "dead=" + mob.isDead() +
                        ", invisible=" + mob.isInvisible() +
                        ", ticking=" + mob.isTicking() +
                        ", empty(!haspassanger)=" + mob.isEmpty() +
                        ", location=" + mob.getLocation() +
                        ", valid=" + mob.isValid() +
                        '}';
                player.sendMessage(Component.text(mobStatus));
            }
        }
    }

    private void checkBlock(TWPlayer twPlayer) {
        Block targetBlock = twPlayer.getPlayer().getTargetBlockExact(50);
        if (targetBlock == null || targetBlock.getType() != Material.GRASS_BLOCK) return;
        Location targetLocation = targetBlock.getLocation();

        Map<UUID, TWPlayer> allPlayers = TWPlayer.debugGetTWPlayerMap();
        List<Track> allTracks = new ArrayList<>();
        for (TWPlayer oneTwPlayer : allPlayers.values()) {
            if (oneTwPlayer.isInGame()) {
                allTracks.add(oneTwPlayer.getTrack());
            }
        }

        boolean found = false;
        for (Track track : allTracks) {
            if (track.isLocationInsideTrackBounds(targetLocation)) {
                if (track.isBlockOccupiedByTower(targetLocation)) {
                    found = true;
                    Tower tower = track.getTowers().get(targetLocation);
                    twPlayer.getPlayer().sendMessage(Component.text("Block belongs to tower: " + tower.toString()));
                    if (tower instanceof TeslaTower teslaTower) {
                        twPlayer.getPlayer().sendMessage(Component.text("Additional debug for tesla tower: " + teslaTower.debugInfo()));
                    }
                }
            }
        }

        if (!found) {
            twPlayer.getPlayer().sendMessage(Component.text("Block is not occupied by a tower."));
        }
    }
}