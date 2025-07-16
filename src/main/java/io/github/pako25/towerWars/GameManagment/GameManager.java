package io.github.pako25.towerWars.GameManagment;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Main;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GameManager {
    private static GameManager gameManager;

    private final List<NamedTextColor> allColors;
    private final Map<String, Game> gameMap = new HashMap<>();
    private final List<String> allArenas;
    private final Map<String, GameQueue> queue = new HashMap<>();
    private final JavaPlugin plugin;

    private GameManager() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        FileConfiguration config = CustomConfig.getFileConfiguration("config");
        allArenas = config.getStringList("arenas");
        for (String arenaName : allArenas) {
            CustomConfig.setup(arenaName);
        }
        allColors = List.of(NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.YELLOW, NamedTextColor.GOLD, NamedTextColor.AQUA);
    }

    public void joinQueue(TWPlayer twPlayer, String arenaName) {
        if (!(arenaExists(arenaName) && isArenaFree(arenaName))) {
            twPlayer.getPlayer().sendMessage(Component.text("This arena is unavailable."));
            return;
        }

        if (isPlayerInQueue(twPlayer)) {
            twPlayer.getPlayer().sendMessage(Component.text("You are already in a queue!"));
            return;
        }

        if (!queue.containsKey(arenaName)) {
            //ustvari novi element v queue (arenaname, player)
            queue.put(arenaName, new GameQueue(arenaName, twPlayer));
        } else {
            //doda playera v corresponding queue element
            queue.get(arenaName).addPlayer(twPlayer);
            if (queue.get(arenaName).isFull()) {
                startGame(arenaName);
                queue.remove(arenaName);
            }
        }
        SignManager.getInstance().updateSigns();
    }

    public void leaveQueue(TWPlayer twPlayer) {
        if (twPlayer.isInGame()) {
            twPlayer.getTrack().setLives(0);
            twPlayer.getTrack().closeTrack();
            twPlayer.getPlayer().sendMessage(Component.text("You left the game and therefore forfeited the battle."));
            return;
        }

        Iterator<Map.Entry<String, GameQueue>> iterator = queue.entrySet().iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Map.Entry<String, GameQueue> entry = iterator.next();
            boolean wasStartable = entry.getValue().isStartable();
            if (entry.getValue().removePlayer(twPlayer)) removed = true;

            if (!entry.getValue().isStartable() && wasStartable) entry.getValue().cancelCountdown(true);
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        if (!removed) twPlayer.getPlayer().sendMessage(Component.text("You are not in a queue!"));
    }

    private void startGame(String arenaName) {
        try {
            Game game = new Game(queue.get(arenaName).getPlayers(), arenaName);
            gameMap.put(arenaName, game);
            queue.get(arenaName).cancelCountdown(false);
            SignManager.getInstance().updateSigns();
        } catch (Exception e) {
            plugin.getLogger().severe("Could not initialise a new game inside " + arenaName + " for the following reason: ");
            plugin.getLogger().severe(e.getMessage());
            queue.get(arenaName).getPlayers().forEach(twPlayer -> twPlayer.getPlayer().sendMessage(Component.text("Server error occurred, try another arena.", NamedTextColor.RED)));
            queue.get(arenaName).cancelCountdown(false);
            queue.remove(arenaName);
        }
    }

    public void forceStart(TWPlayer twPlayer) {
        if (!isPlayerInQueue(twPlayer)) {
            twPlayer.getPlayer().sendMessage(Component.text("You are not in a queue!"));
            return;
        }

        Iterator<GameQueue> iterator = queue.values().iterator();
        while (iterator.hasNext()) {
            GameQueue q = iterator.next();
            if (q.containsPlayer(twPlayer) ) {
                if (q.isStartable()) {
                    startGame(q.getArenaName());
                    iterator.remove();
                } else {
                    twPlayer.getPlayer().sendMessage(Component.text("Not enough players to force start!"));
                }
                break;
            }
        }
    }
    protected void startGameByCountdown(String arenaName) {
        startGame(arenaName);
        queue.remove(arenaName);
    }

    private boolean isPlayerInQueue(TWPlayer twPlayer) {
        for (GameQueue q : queue.values()) {
            if (q.containsPlayer(twPlayer)) return true;
        }
        return false;
    }

    public void gameEnd(String arenaName) {
        gameMap.remove(arenaName);
        SignManager.getInstance().updateSigns();
    }

    public boolean isArenaFree(String arenaName) {
        return !gameMap.containsKey(arenaName);
    }

    public List<NamedTextColor> getAllColors() {
        return allColors;
    }

    public boolean arenaExists(String arenaName) {
        return allArenas.contains(arenaName);
    }

    public static GameManager getInstance() {
        if (gameManager == null) {
            gameManager = new GameManager();
        }
        return gameManager;
    }

    public void cancelAllGames() {
        for (Game game : gameMap.values()) {
            game.cancelGame();
        }
        queue.clear();
        gameMap.clear();
        SignManager.getInstance().updateSigns();
    }
    public List<String> getAllArenas() {
        return allArenas;
    }
    public void addArenaToAllArenas(String arenaName) {
        allArenas.add(arenaName);

        CustomConfig mainConfig = CustomConfig.getCustomConfig("config");
        mainConfig.getCustomFile().set("arenas", allArenas);
        mainConfig.save();
    }
    public List<String> getAvailableArenas() {
        List<String> availableArenas = new ArrayList<>();
        for (String arenaName : allArenas) {
            if (!isArenaFree(arenaName)) continue;

            FileConfiguration cfg = CustomConfig.getFileConfiguration(arenaName);
            boolean enabled = cfg.getBoolean("enabled");
            if (enabled) availableArenas.add(arenaName);
        }
        return availableArenas;
    }
    public int getPeopleQueuedForArena(String arenaName) {
        if (queue.containsKey(arenaName)) {
            return queue.get(arenaName).getPlayers().size();
        }
        return 0;
    }
    public boolean isArenaEnabled(String arenaName) {
        if (!allArenas.contains(arenaName)) return false;
        FileConfiguration cfg = CustomConfig.getFileConfiguration(arenaName);
        return cfg.getBoolean("enabled");
    }
}