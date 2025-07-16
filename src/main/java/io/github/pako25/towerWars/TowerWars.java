package io.github.pako25.towerWars;

import io.github.pako25.towerWars.Arena.AntiFire;
import io.github.pako25.towerWars.Editor.ArenaEditor;
import io.github.pako25.towerWars.Editor.EditorBlockHighlight;
import io.github.pako25.towerWars.Editor.EditorClickListener;
import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.GameManagment.PlayerStats;
import io.github.pako25.towerWars.GameManagment.SignManager;
import io.github.pako25.towerWars.Player.Commands.MainCommand;
import io.github.pako25.towerWars.Player.HighlightBlock;
import io.github.pako25.towerWars.Player.Listeners.*;
import io.github.pako25.towerWars.Tower.ProjectileDespawnListener;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

public class TowerWars {
    private static TowerWars towerWars;
    private final JavaPlugin plugin;

    private HighlightBlock highlightBlock;
    private EditorBlockHighlight editorBlockHighlight;

    public static TowerWars initialiseInstance(JavaPlugin plugin) {
        towerWars = new TowerWars(plugin);
        return towerWars;
    }

    public static JavaPlugin getPlugin() {
        return towerWars.getPluginLocal();
    }

    private TowerWars(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialise() {
        registerCommands();
        initialiseConfigs();
        registerEvents();
        instantiateSingletons();
        try {
            PlayerStats.initialiseDatabseConnection();
        } catch (Exception e) {
            plugin.getLogger().severe("Can not initialise database connection! Stats won't be tracked.");
            PlayerStats.trackingEnabled = false;
        }
    }

    private void instantiateSingletons() {
        highlightBlock = HighlightBlock.HighlightBlockFactory();
        editorBlockHighlight = EditorBlockHighlight.EditorBlockHighlightFactory();
        GameManager.getInstance();
        ArenaEditor.generateInstructionsBook();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(new PlaceTowerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SummonMobListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(AntiFire.getListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UpgradeTowerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DropItemListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HungerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DebugStickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EditorClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LobbyItemClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PickupListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(SignManager.getInstance(), plugin);
        plugin.getServer().getPluginManager().registerEvents(ProjectileDespawnListener.getInstance(), plugin);
        plugin.getServer().getPluginManager().registerEvents(EndermanTeleportListener.getListener(), plugin);
    }

    private void registerCommands() {
        plugin.getCommand("towerwars").setExecutor(new MainCommand());
    }

    private void initialiseConfigs() {
        plugin.saveDefaultConfig();
        CustomConfig.setup("mobConfig");
        CustomConfig.setup("towerConfig");
        CustomConfig.setup("config");
        CustomConfig.setup("managedSigns");
    }

    private JavaPlugin getPluginLocal() {
        return plugin;
    }

    public void cleanup() {
        if (highlightBlock != null) {
            highlightBlock.stop();
        }
        if (editorBlockHighlight != null) {
            editorBlockHighlight.stop();
        }
        GameManager.getInstance().cancelAllGames();
        SignManager.getInstance().saveManagedSigns();
        ArenaEditor.closeAllEditors();
        PlayerStats.closeServer();
    }
}