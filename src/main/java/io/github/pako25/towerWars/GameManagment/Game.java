package io.github.pako25.towerWars.GameManagment;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Main;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Game {
    private final List<Track> trackList = new ArrayList<>();
    private final JavaPlugin plugin;
    private final String arenaName;
    private Material towerPlaceMaterial;

    private BukkitTask gameTicker;

    private final int incomeTimeout = 6;
    private final int maxTime = 60 * 60;
    private int incomeTimer = 5;
    private int gameTimer = maxTime;
    private int tickCounter = 1;

    public Game(List<TWPlayer> twPlayers, String arenaName) {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.arenaName = arenaName;
        startGame(twPlayers);
    }

    public void startGame(List<TWPlayer> twPlayers) {
        try {
            initialiseTracks(twPlayers);
            gameTicker = (new BukkitRunnable() {
                @Override
                public void run() {
                    tickGame();
                }
            }).runTaskTimer(plugin, 0L, 5L);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe(e.getMessage());
            plugin.getLogger().info("The game can not proceed!");
        }
    }

    private void tickGame() {
        tickCounter++;

        if (tickCounter % 4 == 0) { //vsako sekundo
            gameTimer--;
            incomeTimer--;
        }
        if (incomeTimer == 0) { //doda income
            incomeTimer = incomeTimeout;
            trackList.forEach(Track::giveIncome);
        }
        if (tickCounter % 600 == 0) {
            trackList.forEach(Track::powerCreep);
        }

        trackList.forEach(Track::tickTrack);

        if (tickCounter >= maxTime * 4) {
            staleMate();
        }
    }

    public void initialiseTracks(List<TWPlayer> twPlayers) throws IllegalArgumentException {
        FileConfiguration config = CustomConfig.getFileConfiguration(arenaName);

        String towerPlaceMaterialRaw = config.getString("towerPlaceMaterial");
        if (towerPlaceMaterialRaw != null) {
            towerPlaceMaterial = Material.matchMaterial(towerPlaceMaterialRaw, false);
        }
        if (towerPlaceMaterial == null) {
            throw new IllegalArgumentException("Tower place material field has an invalid value!");
        }

        String worldName = config.getString("worldName");
        if (worldName == null) throw new IllegalArgumentException("Missing worldName field in " + arenaName + ".yml");

        List<Location> trackSpawns = new ArrayList<>();
        List<?> spawnsRaw = config.getList("trackSpawns");
        if (spawnsRaw == null)
            throw new IllegalArgumentException("Missing 'trackSpawns' section in " + arenaName + ".yml");

        for (Object spawnObj : spawnsRaw) {
            if (!(spawnObj instanceof List<?> coords) || coords.size() != 3) {
                throw new IllegalArgumentException("Invalid trackSpawn entry, skipping: " + spawnObj);
            }
            World world = plugin.getServer().getWorld(worldName);
            trackSpawns.add(new Location(world, (int) coords.get(0), (int) coords.get(1), (int) coords.get(2)));
        }

        if (trackSpawns.size() > GameManager.getInstance().getAllColors().size()) {
            throw new IllegalArgumentException("You can't have more than " + GameManager.getInstance().getAllColors().size() + " track spawns!");
        }
        if (twPlayers.size() > trackSpawns.size()) {
            throw new IllegalArgumentException("You can't have more players than track spawns!");
        }

        Iterator<NamedTextColor> colorIterator = GameManager.getInstance().getAllColors().iterator();
        Iterator<Location> locationIterator = trackSpawns.iterator();
        for (TWPlayer twPlayer : twPlayers) {
            Track track = new Track(locationIterator.next(), plugin, twPlayer, this, arenaName, colorIterator.next());
            trackList.add(track);
        }

        trackList.forEach(Track::gameStart);
    }

    public void sendMonstersFrom(UUID trackUUID, MobType mobType, TWPlayer summoner) {
        for (Track track : trackList) {
            if (!track.getUUID().equals(trackUUID) && track.isAlive()) {
                track.summonMob(mobType, summoner);
            }
        }
    }

    public void giveLiveToOthers(UUID trackUUID, NamedTextColor fromColor, String causeName) {
        for (Track track : trackList) {
            if (track.getUUID().equals(trackUUID) && track.isAlive()) {
                track.gainLive(fromColor, causeName);
            }
            //track.gainLive(); //ODSTRANI
        }
        trackList.forEach(Track::updateSidebar);
    }

    public void trackDied(Track deadTrack) {
        NamedTextColor deadColor = deadTrack.getColor();
        String deadColorText = deadColor.toString();
        if (deadColorText.equals("GOLD")) deadColorText = "ORANGE";

        Iterator<Track> iterator = trackList.iterator();
        while (iterator.hasNext()) {
            Track track = iterator.next();
            if (track.equals(deadTrack)) {
                iterator.remove();
            } else {
                track.getTwPlayer().getPlayer().sendMessage(Component.text(deadColorText, deadColor).append(Component.text(" has lost!", NamedTextColor.RED)));
            }
        }
        if (trackList.size() == 1 && tickCounter < maxTime * 4) {
            gameEnd();
        }
    }

    private void gameEnd() {
        trackList.getFirst().closeTrack();
        cleanup();
    }

    private void staleMate() {
        trackList.forEach((Track track) -> track.setLives(0));
        trackList.forEach(Track::closeTrack);
        cleanup();
    }

    public void cancelGame() {
        staleMate();
    }

    private void cleanup() {
        gameTicker.cancel();
        trackList.clear();
        GameManager.getInstance().gameEnd(arenaName);
    }

    public int getIncomeTimer() {
        return incomeTimer;
    }

    public int getGameTimer() {
        return gameTimer;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public Material getTowerPlaceMaterial() {
        return towerPlaceMaterial;
    }
}
