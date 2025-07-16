package io.github.pako25.towerWars.GameManagment;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Main;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class GameQueue {
    private final int maxPlayers;
    private final int minPlayers = 2;
    private final int startWaitTime = 11;

    private final String arenaName;
    private final ArrayList<TWPlayer> players = new ArrayList<>();
    private Location lobbyLocation;
    private BukkitTask countdownTimer;
    private int countdown = 0;

    public GameQueue(String arenaName, TWPlayer player) {
        this.arenaName = arenaName;

        FileConfiguration cfg = CustomConfig.getFileConfiguration(arenaName);
        List<?> locationRaw = cfg.getList("lobbyLocation");
        String worldName = cfg.getString("worldName");

        if (locationRaw == null || locationRaw.size() != 3 || worldName == null) {
            lobbyLocation = null;
        } else {
            try {
                lobbyLocation = new Location(TowerWars.getPlugin().getServer().getWorld(worldName), (int) locationRaw.get(0), (int) locationRaw.get(1), (int) locationRaw.get(2));
            } catch (Exception e) {
                TowerWars.getPlugin().getLogger().severe("Error in parsing world location.");
                player.getPlayer().sendMessage(Component.text("An error occurred.", NamedTextColor.RED));
            }
        }

        List<?> spawnsRaw = cfg.getList("trackSpawns");
        if (spawnsRaw == null)
            throw new IllegalArgumentException("Missing 'trackSpawns' section in " + arenaName + ".yml");
        maxPlayers = spawnsRaw.size();

        addPlayer(player);
    }

    private void startCountdown() {
        if (countdownTimer != null && !countdownTimer.isCancelled()) return;

        countdown = startWaitTime;
        countdownTimer = (new BukkitRunnable() {
            @Override
            public void run() {
                if (!isStartable()) {
                    cancel();
                    return;
                }
                countdown--;
                updateBossBars();

                if (countdown == 30 || countdown == 10 || countdown == 5 || countdown == 4 || countdown == 3 || countdown == 2 || countdown == 1)
                    sendCountDownMessage();

                if (countdown < 1) {
                    cancel();
                    GameManager.getInstance().startGameByCountdown(arenaName);
                }
            }
        }).runTaskTimer(JavaPlugin.getProvidingPlugin(Main.class), 20L, 20L);
        for (TWPlayer twPlayer : players) {
            twPlayer.getPlayer().sendMessage(Component.text("Game start countdown started. 60 seconds left."));
        }
    }

    private void sendCountDownMessage() {
        for (TWPlayer twPlayer : players) {
            twPlayer.getPlayer().sendMessage(Component.text("The game is starting in " + countdown + " seconds."));
        }
    }

    public boolean removePlayer(TWPlayer twPlayer) {
        if (containsPlayer(twPlayer)) {
            players.remove(twPlayer);
            twPlayer.setInLobby(false);
            twPlayer.clearBossBar();
            twPlayer.getPlayer().getInventory().clear();
            if (twPlayer.getLocationBeforeGame() != null)
                twPlayer.getPlayer().teleport(twPlayer.getLocationBeforeGame().clone().add(0, 2, 0));
            twPlayer.getPlayer().sendMessage(Component.text("You left the queue."));
            for (TWPlayer p1 : players) {
                if (!p1.equals(twPlayer)) {
                    p1.getPlayer().sendMessage(twPlayer.getPlayer().displayName().append(Component.text(" left the queue (" + players.size() + "/" + maxPlayers + ")")));
                }
            }
            updateBossBars();
            SignManager.getInstance().updateSigns();
            return true;
        }
        return false;
    }

    public boolean containsPlayer(TWPlayer twPlayer) {
        return players.contains(twPlayer);
    }

    public boolean isFull() {
        return players.size() == maxPlayers;
    }

    public void addPlayer(TWPlayer twPlayer) {
        if (isFull()) return;
        players.add(twPlayer);
        sendPlayerJoinMessage(twPlayer);

        twPlayer.setLocationBeforeGame(twPlayer.getPlayer().getLocation());
        if (lobbyLocation != null) {
            twPlayer.getPlayer().teleport(lobbyLocation.clone().add(0, 2, 0));
        }
        twPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
        twPlayer.setInLobby(true);
        giveLobbyItems(twPlayer);

        if (isStartable()) {
            startCountdown();
        }
        setBossBar(twPlayer);
    }

    private void giveLobbyItems(TWPlayer twPlayer) {
        ItemStack leaveItem = new ItemStack(Material.CLOCK);
        ItemMeta leaveItemMeta = leaveItem.getItemMeta();
        leaveItemMeta.displayName(Component.text("Leave", NamedTextColor.RED));
        leaveItemMeta.lore(List.of(Component.text("Right click -> leave")));
        leaveItem.setItemMeta(leaveItemMeta);
        twPlayer.getPlayer().getInventory().setItem(8, leaveItem);

        ItemStack statsItem = new ItemStack(Material.DARK_OAK_SIGN);
        ItemMeta statsItemMeta = statsItem.getItemMeta();
        statsItemMeta.displayName(Component.text("Stats", NamedTextColor.YELLOW));
        statsItemMeta.lore(List.of(Component.text("Right click -> show stats")));
        statsItem.setItemMeta(statsItemMeta);
        twPlayer.getPlayer().getInventory().setItem(7, statsItem);
    }

    public String getArenaName() {
        return arenaName;
    }

    public ArrayList<TWPlayer> getPlayers() {
        return players;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    private void sendPlayerJoinMessage(TWPlayer twPlayer) {
        twPlayer.getPlayer().sendMessage(Component.text("You joined the queue (" + players.size() + "/" + maxPlayers + ")"));
        for (TWPlayer p1 : players) {
            if (!p1.equals(twPlayer)) {
                p1.getPlayer().sendMessage(twPlayer.getPlayer().displayName().append(Component.text(" joined the queue (" + players.size() + "/" + maxPlayers + ")")));
            }
        }
    }

    public boolean isStartable() {
        return players.size() >= minPlayers;
    }

    public void cancelCountdown(boolean announce) {
        if (countdownTimer != null && !countdownTimer.isCancelled()) {
            countdownTimer.cancel();
        }
        if (announce) {
            for (TWPlayer twPlayer : players) {
                twPlayer.getPlayer().sendMessage(Component.text("Game start countdown canceled."));
            }
            updateBossBars();
        }
    }

    private void setBossBar(TWPlayer twPlayer) {
        float progress = 1;
        if (countdownTimer != null && !countdownTimer.isCancelled()) progress = (float) countdown / startWaitTime;
        BossBar bossBar = BossBar.bossBar(Component.text(arenaName + "   ", NamedTextColor.RED).append(Component.text("Players: " + players.size() + "/" + maxPlayers, NamedTextColor.WHITE)).append(Component.text((progress != 1 ? "   Starting in: " + countdown : ""), NamedTextColor.YELLOW)), progress, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);

        twPlayer.setBossBar(bossBar);
    }

    private void updateBossBars() {
        float progress = 1;
        if (countdownTimer != null && !countdownTimer.isCancelled()) progress = (float) countdown / startWaitTime;

        for (TWPlayer twPlayer : players) {
            if (twPlayer.getBossBar() == null) {
                setBossBar(twPlayer);
                continue;
            }

            BossBar bossBar = twPlayer.getBossBar();
            bossBar.name(Component.text(arenaName + "   ", NamedTextColor.RED).append(Component.text("Players: " + players.size() + "/" + maxPlayers, NamedTextColor.WHITE)).append(Component.text((progress != 1 ? "   Starting in: " + countdown : ""), NamedTextColor.YELLOW)));
            bossBar.progress(progress);
        }
    }
}
