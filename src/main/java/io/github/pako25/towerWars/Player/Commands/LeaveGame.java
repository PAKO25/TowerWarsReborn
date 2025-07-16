package io.github.pako25.towerWars.Player.Commands;

import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;

import java.util.List;

public class LeaveGame implements SubcommandHandler {
    @Override
    public void onCommand(TWPlayer twPlayer, String[] args) {
        if (!twPlayer.getPlayer().hasPermission("towerwars.play")) {
            twPlayer.getPlayer().sendMessage(Component.text("No permission."));
            return;
        }

        if (twPlayer.isInEditor()) {
            twPlayer.getPlayer().sendMessage(Component.text("You are in arena configuration mode. Quit the mode and try again."));
            return;
        }
        if (!twPlayer.isInGame() && !twPlayer.isInLobby()) {
            twPlayer.getPlayer().sendMessage("You are not in a game!");
            return;
        }

        if (args.length != 0) {
            twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars leave"));
            return;
        }
        GameManager gameManager = GameManager.getInstance();
        gameManager.leaveQueue(twPlayer);
    }

    @Override
    public List<String> onTabComplete(TWPlayer twPlayer, String[] args) {
        return List.of();
    }
}
