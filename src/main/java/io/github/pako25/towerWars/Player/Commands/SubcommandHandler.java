package io.github.pako25.towerWars.Player.Commands;

import io.github.pako25.towerWars.Player.TWPlayer;

import java.util.List;

public interface SubcommandHandler {
    public void onCommand(TWPlayer twPlayer, String[] args);
    public List<String> onTabComplete(TWPlayer twPlayer, String[] args);
}