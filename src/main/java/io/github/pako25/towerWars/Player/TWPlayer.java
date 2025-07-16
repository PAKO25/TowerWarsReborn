package io.github.pako25.towerWars.Player;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.Editor.ArenaEditor;
import io.github.pako25.towerWars.Editor.EditorOptionsInventory;
import io.github.pako25.towerWars.GameManagment.Game;
import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.GameManagment.PlayerStats;
import io.github.pako25.towerWars.Player.Inventories.PlaceTowerInventory;
import io.github.pako25.towerWars.Player.Inventories.SummonMobInventory;
import io.github.pako25.towerWars.Player.Inventories.UpgradeTowerInventory;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class TWPlayer {
    private static final Map<UUID, TWPlayer> TWPlayerMap = new HashMap<>();

    private PlaceTowerInventory placeTowerInventory;
    private SummonMobInventory summonMobInventory;
    private ArenaEditor activeArenaEditor;
    private final Player player;
    private final JavaPlugin plugin;
    private Track track;
    private Game game;
    private Location placeTowerClickLocation;
    private Sidebar sidebar;
    private boolean inGame = false;
    private boolean inEditor = false;
    private boolean inLobby = false;
    private Location locationBeforeGame;
    private BossBar bossBar;

    private int coin = 0;
    private int income = 0;
    private int stock = 30;

    private TWPlayer(Player player, JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public static TWPlayer newTWPlayer(Player player, JavaPlugin plugin) {
        TWPlayer TWPlayer = new TWPlayer(player, plugin);
        TWPlayerMap.put(player.getUniqueId(), TWPlayer);
        PlayerStats.getStats(player.getUniqueId());
        return TWPlayer;
    }

    public static Collection<TWPlayer> getTWPlayerCollection() {
        return TWPlayerMap.values();
    }

    public static void removePlayer(UUID uuid) {
        TWPlayerMap.remove(uuid);
    }

    public static TWPlayer getTWPlayer(UUID uuid) {
        return TWPlayerMap.get(uuid);
    }

    public Player getPlayer() {
        return player;
    }

    public void openSummonMobInventory() {
        if (!inGame) return;
        if (summonMobInventory == null) {
            summonMobInventory = new SummonMobInventory(plugin, this);
        }
        player.openInventory(summonMobInventory.getInventory());
    }

    public void clickOnBlock(Location targetBlock) {
        if (!inGame) return;
        if (track.isBlockOccupiedByTower(targetBlock)) {
            openTowerMenu(targetBlock);
        } else {
            openPlaceTowerInventory(targetBlock);
        }
    }

    public void openPlaceTowerInventory(Location targetBlock) {
        if (!inGame) return;
        boolean onTrack = track.isLocationInsideTrackBounds(targetBlock);
        if (!onTrack) return;

        placeTowerClickLocation = targetBlock;
        if (placeTowerInventory == null) {
            placeTowerInventory = new PlaceTowerInventory(plugin, this);
        }
        placeTowerInventory.loadInventory();
        player.openInventory(placeTowerInventory.getInventory());
    }

    public void openTowerMenu(Location location) {
        if (!inGame) return;
        player.openInventory(new UpgradeTowerInventory(plugin, this, track.getTowers().get(location), location).getInventory());
    }

    public Track getTrack() {
        return track;
    }

    public boolean summonMob(MobType mobType, int cost, int income) {
        if (cost > coin) {
            return false;
        }
        stock--;
        coin = coin - cost;
        if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseGold_spent(cost);
        if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseMobs_sent();
        increaseIncome(income);
        updateSidebar(false);
        track.getMobStates().getMobState(mobType).incrementSummon();
        game.sendMonstersFrom(track.getUUID(), mobType, this);
        return true;
    }

    public boolean placeTower(TowerType towerType, int level, int prestige, Component towerName) {
        int cost = Tower.getTowerBuyCost(towerType, level, prestige);
        if (!track.hasSpaceLeft()) {
            player.sendMessage(Component.text("You have already placed the maximum amount of towers!", NamedTextColor.RED));
            return false;
        }
        boolean success = buyForCoin(cost);
        if (success) {
            if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseTowers_placed();
            track.placeTower(towerType, placeTowerClickLocation, level, prestige);
            player.sendMessage(
                    Component.text("You placed the following tower: ", NamedTextColor.GREEN)
                            .append(towerName)
                            .append(Component.text(" for ", NamedTextColor.GREEN))
                            .append(Component.text(cost, NamedTextColor.GOLD))
                            .append(Component.text(" gold ", NamedTextColor.GREEN))
                            .append(Component.text("(" + track.getTowers().size() + "/" + track.getMaxTowers() + ")", NamedTextColor.GRAY))
            );
        }
        return success;
    }

    public void gameStart(Track track, Game game) {
        this.track = track;
        this.game = game;
        this.sidebar = new Sidebar(game, this);
        inGame = true;
        inLobby = false;

        coin = 75;
        income = 5;

        clearBossBar();
        player.sendMessage(Component.text("Game started"));
        player.teleport(track.getTrackSpawn().clone().add(0, 2, 0));
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        player.setInvulnerable(true);
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setHealth(20);

        ItemStack placeTowerItem = new ItemStack(Material.ARMOR_STAND, 1);
        ItemMeta placeTowerItemMeta = placeTowerItem.getItemMeta();
        placeTowerItemMeta.displayName(Component.text("Place tower"));
        placeTowerItem.setItemMeta(placeTowerItemMeta);

        ItemStack summonMobItem = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta summonMobItemMeta = summonMobItem.getItemMeta();
        summonMobItemMeta.displayName(Component.text("Summon mob"));
        summonMobItem.setItemMeta(summonMobItemMeta);

        ItemStack upgradeTowerItem = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta upgradeTowerItemMeta = upgradeTowerItem.getItemMeta();
        upgradeTowerItemMeta.displayName(Component.text("Upgrade tower"));
        upgradeTowerItem.setItemMeta(upgradeTowerItemMeta);

        player.getInventory().addItem(placeTowerItem);
        player.getInventory().addItem(summonMobItem);
        player.getInventory().addItem(upgradeTowerItem);
    }

    public void gameEnd(boolean lost) {
        inGame = false;
        track = null;
        game = null;
        try {
            sidebar.delete();
        } catch (Exception e) {
            System.out.println("Sidebar couldn't be deleted.");
        }
        sidebar = null;
        player.getInventory().clear();
        player.closeInventory();
        player.setAllowFlight(false);
        player.setInvulnerable(false);

        if (locationBeforeGame != null) {
            player.teleport(locationBeforeGame.clone().add(0, 2, 0));
        }

        if (lost) {
            if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseGames_lost();
            player.showTitle(Title.title(Component.text("You lost", NamedTextColor.RED), Component.text(""), Title.DEFAULT_TIMES));
        } else {
            if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseGames_won();
            player.showTitle(Title.title(Component.text("You won!", NamedTextColor.GOLD), Component.text(""), Title.DEFAULT_TIMES));
        }

        if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).saveToDatabase();
    }

    public void openArenaEditorInventory(ArenaEditor arenaEditor) {
        player.openInventory(new EditorOptionsInventory(plugin, this, arenaEditor).getInventory());
    }

    public void leaveServer() {
        clearBossBar();
        if (inGame) {
            GameManager.getInstance().leaveQueue(this);
            inGame = false;
            track = null;
            game = null;
            sidebar = null;
        }
        if (inEditor) {
            ArenaEditor.closeInstanceByPlayer(this, true);
        }
        if (inLobby) {
            GameManager.getInstance().leaveQueue(this);
        }
        PlayerStats.closePlayerInstance(player.getUniqueId());
    }

    public void updateSidebar(boolean tracksChanged) {
        if (sidebar == null) return;
        sidebar.updateSidebar(coin, income, game.getIncomeTimer(), game.getGameTimer(), tracksChanged);
    }

    public void clearBossBar() {
        if (bossBar != null) {
            player.hideBossBar(bossBar);
            bossBar = null;
        }
    }

    public void setBossBar(BossBar bossBar) {
        if (this.bossBar != null) clearBossBar();
        this.bossBar = bossBar;
        player.showBossBar(bossBar);
    }

    public void recieveIncome() {
        player.sendMessage(Component.text("You got ", NamedTextColor.YELLOW).append(Component.text("+" + income, NamedTextColor.GOLD)).append(Component.text(" gold from passive income!", NamedTextColor.YELLOW)));
        coin += income;
    }

    public void increaseCoin(int amount) {
        coin += amount;
    }

    public void increaseIncome(int amount) {
        income += amount;
    }

    public boolean buyForCoin(int amount) {
        if (amount <= coin) {
            coin = coin - amount;
            if (PlayerStats.trackingEnabled) PlayerStats.getStats(player.getUniqueId()).increaseGold_spent(amount);
            return true;
        }
        player.sendMessage(Component.text("Not enough gold!", NamedTextColor.RED));
        return false;
    }

    public void increaseStock() {
        int amount = (game.getTickCounter() / 1200) + 1;
        if (stock < 30) stock += Math.min(amount, 5);
        if (stock > 30) stock = 30;
        if (summonMobInventory != null && !summonMobInventory.getInventory().getViewers().isEmpty())
            summonMobInventory.loadInventory();
    }

    public void freeze() {
        player.setInvulnerable(true);
        List<PotionEffect> effects = List.of(
                new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 255, false, false, false),
                new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1, false, false, false),
                new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 255, false, false, false)
        );
        player.addPotionEffects(effects);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void unfreeze() {
        player.setInvulnerable(false);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        if (player.getPreviousGameMode() != null)
            player.setGameMode(player.getPreviousGameMode());
    }

    public int getIncome() {
        return income;
    }

    public int getStock() {
        return stock;
    }

    public boolean isInGame() {
        return inGame;
    }

    public int getCoin() {
        return coin;
    }

    public boolean isInEditor() {
        return inEditor;
    }

    public void setInEditor(boolean inEditor) {
        this.inEditor = inEditor;
    }

    public ArenaEditor getActiveArenaEditor() {
        return activeArenaEditor;
    }

    public void setActiveArenaEditor(ArenaEditor arenaEditor) {
        activeArenaEditor = arenaEditor;
    }

    public static Map<UUID, TWPlayer> debugGetTWPlayerMap() {
        return TWPlayerMap;
    }

    public Location getLocationBeforeGame() {
        return locationBeforeGame;
    }

    public void setLocationBeforeGame(Location locationBeforeGame) {
        this.locationBeforeGame = locationBeforeGame;
    }

    public void setInLobby(boolean inLobby) {
        this.inLobby = inLobby;
    }

    public boolean isInLobby() {
        return inLobby;
    }

    public Game getGame() {
        return game;
    }

    public BossBar getBossBar() {
        return bossBar;
    }
}