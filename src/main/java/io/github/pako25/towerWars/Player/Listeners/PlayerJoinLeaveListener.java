package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TWPlayer.newTWPlayer(event.getPlayer(), TowerWars.getPlugin());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TWPlayer.getTWPlayer(event.getPlayer().getUniqueId()).leaveServer();
        TWPlayer.removePlayer(event.getPlayer().getUniqueId());
    }
}
