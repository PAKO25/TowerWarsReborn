package io.github.pako25.towerWars;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private TowerWars towerWars;

    @Override
    public void onEnable() {
        towerWars = TowerWars.initialiseInstance(this);
        towerWars.initialise();
    }

    @Override
    public void onDisable() {
        towerWars.cleanup();
        towerWars = null;
    }
}
