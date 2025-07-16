package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.Player.TWPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PickupListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (twPlayer.isInEditor() || twPlayer.isInGame() || twPlayer.isInLobby()) event.setCancelled(true);
    }
}
