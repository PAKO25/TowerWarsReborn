package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Player.Inventories.StatsInventory;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyItemClickListener implements Listener {
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (!twPlayer.isInLobby()) return;

        if (item.getType() == Material.CLOCK) {
            GameManager.getInstance().leaveQueue(twPlayer);
        }
        if (item.getType() == Material.DARK_OAK_SIGN) {
            player.openInventory(new StatsInventory(twPlayer).getInventory());
        }
    }
}