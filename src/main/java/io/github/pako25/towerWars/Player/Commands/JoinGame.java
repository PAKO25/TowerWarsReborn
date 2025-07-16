package io.github.pako25.towerWars.Player.Commands;

import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;

import java.util.List;

public class JoinGame implements SubcommandHandler {
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

        if (twPlayer.isInGame() || twPlayer.isInLobby()) {
            twPlayer.getPlayer().sendMessage(Component.text("You are already in a game!"));
            return;
        }

        if (args.length != 1) {
            twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars join <arena>"));
            return;
        }
        String arena = args[0];
        GameManager queue = GameManager.getInstance();

        if (!queue.arenaExists(arena)) {
            twPlayer.getPlayer().sendMessage(Component.text("Arena doesn't exist."));
            return;
        }

        if (!GameManager.getInstance().isArenaEnabled(arena)) {
            twPlayer.getPlayer().sendMessage(Component.text("The arena is disabled."));
            return;
        }

        if (!queue.isArenaFree(arena)) {
            twPlayer.getPlayer().sendMessage(Component.text("The arena is occupied."));
            return;
        }

        queue.joinQueue(twPlayer, arena);
    }

    @Override
    public List<String> onTabComplete(TWPlayer twPlayer, String[] args) {
        if (!twPlayer.getPlayer().hasPermission("towerwars.play")) return List.of();
        if (args.length > 1) return List.of();
        if (twPlayer.isInEditor() || twPlayer.isInGame() || twPlayer.isInLobby()) return List.of();
        return GameManager.getInstance().getAvailableArenas();
    }
}