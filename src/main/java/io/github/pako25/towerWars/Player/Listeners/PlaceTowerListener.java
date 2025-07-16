package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.GameManagment.Game;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
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
import org.bukkit.util.RayTraceResult;

import java.util.Collection;

public class PlaceTowerListener implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.ARMOR_STAND) return;
        event.setCancelled(true);
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (!twPlayer.isInGame()) return;

        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 30, entity -> !entity.equals(player));
        if (result != null) {
            Entity target = result.getHitEntity();
            boolean found = false;
            if (target instanceof Mob mob) {
                Collection<Tower> towers = twPlayer.getTrack().getTowers().values();
                for (Tower tower : towers) {
                    if (tower.isEntityInTower(mob)) {
                        found = true;
                        twPlayer.openTowerMenu(tower.getLocation());
                    }
                }
            }
            if (found) return;
        }

        Game game = twPlayer.getGame();
        Material towerPlaceMaterial = game.getTowerPlaceMaterial();

        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null || targetBlock.getType() != towerPlaceMaterial) return;
        twPlayer.clickOnBlock(targetBlock.getLocation());
    }
}
