package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class UpgradeTowerListener implements Listener {
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.EXPERIENCE_BOTTLE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Upgrade tower")) return;

        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (twPlayer == null || !twPlayer.isInGame()) return;
        event.setCancelled(true);

        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 30, entity -> entity != player);
        if (result == null) return;
        Entity target = result.getHitEntity();

        if (target instanceof Mob mob) {
            for (Tower tower : twPlayer.getTrack().getTowers().values()) {
                if (tower.isEntityInTower(mob)) {
                    tower.upgradeFromDistance();
                }
            }
        }
    }
}