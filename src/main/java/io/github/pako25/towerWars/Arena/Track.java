package io.github.pako25.towerWars.Arena;

import io.github.pako25.towerWars.Arena.MobData.MobBuilder;
import io.github.pako25.towerWars.Arena.MobData.MobStates;
import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.GameManagment.Game;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
import io.github.pako25.towerWars.Tower.TowerSchemas.GuardianTower;
import io.github.pako25.towerWars.Tower.TowerSchemas.TeslaTower;
import io.github.pako25.towerWars.Tower.TowerSchemas.VillagerTower;
import io.github.pako25.towerWars.Tower.TowerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;


public class Track {
    private final int maxTowers = 60;
    private final Location trackSpawn;
    private final Map<Location, Tower> placedTowers = new HashMap<>();
    private final ArrayList<ArrayList<Vector>> paths = new ArrayList<>();
    private final Set<TWMob> activeMobs = new HashSet<>();
    private final MobQueue mobQueue = new MobQueue();
    private final List<ArmorStand> mobKillDisplays = new ArrayList<>();
    private final MobStates mobStates = new MobStates();
    private final Plugin plugin;
    private final TWPlayer twPlayer;
    private final Game game;
    private final UUID uuid = UUID.randomUUID();
    private final Vector[] trackBounds = new Vector[4];
    private final String arenaName;
    private final NamedTextColor color;
    private int lives = 20;
    private boolean alive = true;

    public Track(Location trackSpawn, Plugin plugin, TWPlayer twPlayer, Game game, String arenaName, NamedTextColor color) {
        this.trackSpawn = trackSpawn;
        this.plugin = plugin;
        this.twPlayer = twPlayer;
        this.arenaName = arenaName;
        this.game = game;
        this.color = color;
        loadBounds();
        loadPaths();
    }

    public void gameStart() {
        twPlayer.gameStart(this, game);
    }

    private void loadBounds() {
        FileConfiguration config = CustomConfig.getFileConfiguration(arenaName);
        List<?> boundsRaw = config.getList("trackBounds");
        if (boundsRaw == null) throw new IllegalStateException("Missing 'trackBounds' section in arena config.");

        for (int i = 0; i < boundsRaw.size(); i++) {
            if (!(boundsRaw.get(i) instanceof List<?> coords) || coords.size() != 3) {
                plugin.getLogger().warning("Invalid trackBounds entry, skipping: " + boundsRaw.get(i));
                continue;
            }
            trackBounds[i] = (new Vector((int) coords.get(0), (int) coords.get(1), (int) coords.get(2)));
        }
    }

    private void loadPaths() {
        FileConfiguration config = CustomConfig.getFileConfiguration(arenaName);
        List<?> rawPaths = config.getList("paths");
        if (rawPaths == null) throw new IllegalStateException("Missing 'paths' section in arena.yml");

        for (Object rawPathObj : rawPaths) {
            if (!(rawPathObj instanceof List<?> rawPathList)) {
                plugin.getLogger().warning("Invalid path entry, skipping: " + rawPathObj);
                continue;
            }

            ArrayList<Vector> path = new ArrayList<>();
            for (Object coordObj : rawPathList) {
                if (!(coordObj instanceof List<?> coordList) || coordList.size() != 3) {
                    plugin.getLogger().warning("  Invalid coordinate, skipping: " + coordObj);
                    continue;
                }

                path.add(new Vector((int) coordList.get(0), (int) coordList.get(1), (int) coordList.get(2)));
            }

            if (!path.isEmpty()) {
                paths.add(path);
            }
        }
    }

    public void summonMob(MobType mobType, TWPlayer summoner) {
        Random random = new Random();
        int index = random.nextInt(paths.size());
        if (mobType == MobType.GHAST)
            index = (paths.size() / 2) + 1; //vse arena imajo liho število poti, drugače je pač zamaknjen
        ArrayList<Vector> path = paths.get(index);

        TWMob mob = MobBuilder.buildMob(this, path, mobType, summoner);
        mobQueue.add(mob);
    }

    public void summonMob(MobType mobType, ArrayList<Vector> customPath, TWPlayer summoner) {
        TWMob mob = MobBuilder.buildMob(this, customPath, mobType, summoner);
        mobQueue.add(mob);
    }

    private void tickMobs() {
        synchronized (activeMobs) {
            Iterator<TWMob> iterator = activeMobs.iterator();
            boolean fullSecond = game.getTickCounter() % 4 == 0;

            while (iterator.hasNext()) {
                TWMob mob = iterator.next();
                mob.tick(fullSecond);

                //preveri za despawn
                if (!mob.isAlive()) iterator.remove();
            }
        }
    }

    void loseLive(TWMob cause) {
        lives--;
        if (cause.getMobType() == MobType.DEATH_RIDER) {
            lives--;
        } else {
            game.giveLiveToOthers(cause.getSummonerTWPlayer().getTrack().getUUID(), color, cause.getMobType().name());

            NamedTextColor toColor = cause.getSummonerTWPlayer().getTrack().getColor();
            String toColorText = toColor.toString().toUpperCase();
            if (toColorText.equals("GOLD")) toColorText = "ORANGE";
            twPlayer.getPlayer().sendMessage(
                    Component.text("You gave 1", NamedTextColor.GREEN)
                            .append(Component.text("❤", NamedTextColor.RED))
                            .append(Component.text(" to ", NamedTextColor.GREEN))
                            .append(Component.text(toColorText, toColor))
            );
        }
        if (lives < 1) {
            closeTrack();
        }
    }

    public boolean isLocationInsideTrackBounds(Location location) {
        int x1 = trackSpawn.clone().add(trackBounds[0]).getBlockX();
        int x2 = trackSpawn.clone().add(trackBounds[1]).getBlockX();
        int x3 = trackSpawn.clone().add(trackBounds[2]).getBlockX();
        int x4 = trackSpawn.clone().add(trackBounds[3]).getBlockX();
        int z1 = trackSpawn.clone().add(trackBounds[0]).getBlockZ();
        int z2 = trackSpawn.clone().add(trackBounds[1]).getBlockZ();
        int z3 = trackSpawn.clone().add(trackBounds[2]).getBlockZ();
        int z4 = trackSpawn.clone().add(trackBounds[3]).getBlockZ();
        int checkX = location.getBlockX();
        int checkZ = location.getBlockZ();
        int minX = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        int maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4));
        int minZ = Math.min(Math.min(z1, z2), Math.min(z3, z4));
        int maxZ = Math.max(Math.max(z1, z2), Math.max(z3, z4));

        return checkX >= minX && checkX <= maxX && checkZ >= minZ && checkZ <= maxZ;
    }

    public void placeTower(TowerType towerType, Location location, int level, int prestige) {
        if (!alive) {
            twPlayer.getPlayer().sendMessage(Component.text("You are dead."));
            return;
        }
        Tower tower = Tower.summonTower(location, towerType, level, prestige, this);
        if (tower != null) {
            placedTowers.put(location, tower);
        }
    }

    public void tickTrack() {
        if (!alive) return;
        synchronized (activeMobs) {
            activeMobs.addAll(mobQueue.tick());
        }
        tickMobs();
        boolean noMobs = activeMobs.isEmpty();
        for (Map.Entry<Location, Tower> entry : placedTowers.entrySet()) {
            Location location = entry.getKey();
            Tower tower = entry.getValue();
            if (tower instanceof VillagerTower villagerTower) {
                villagerTower.buffTowers();
                continue;
            }
            if (tower instanceof GuardianTower guardianTower) {
                guardianTower.resetTargeting();
            }
            if (tower.isOnCooldown() || noMobs) continue;
            Set<TWMob> mobSet = getMobsInRange(location, tower.getRange());
            if (tower instanceof TeslaTower teslaTower && mobSet.isEmpty()) teslaTower.nothingInRange();
            if (mobSet.isEmpty()) continue;
            tower.attackMobs(mobSet);
        }
        if (game.getTickCounter() % 4 == 0) {
            twPlayer.increaseStock();
            twPlayer.updateSidebar(false);
        }

        Iterator<ArmorStand> iterator = mobKillDisplays.iterator();
        while (iterator.hasNext()) {
            ArmorStand killDisplay = iterator.next();
            if (killDisplay.getTicksLived() > 20) {
                killDisplay.remove();
                iterator.remove();
            }
        }
    }

    public Set<TWMob> getMobsInRange(Location location, int range) {
        Set<TWMob> mobSet = new HashSet<>();
        for (TWMob mob : activeMobs) {
            if (mob.getLocation().distanceSquared(location) <= range * range) {
                mobSet.add(mob);
            }
        }
        return mobSet;
    }

    public boolean isBlockOccupiedByTower(Location targetBlock) {
        return placedTowers.containsKey(targetBlock);
    }

    public void cleanupSoldTower(Tower tower) {
        twPlayer.increaseCoin(tower.getCost() / 2);
        placedTowers.remove(tower.getLocation());
    }

    public void closeTrack() {
        boolean lost = lives < 1;
        twPlayer.gameEnd(lost);
        if (lost) game.trackDied(this);
        alive = false;

        synchronized (activeMobs) {
            activeMobs.forEach(TWMob::gameEnd);
            activeMobs.clear();
        }
        mobKillDisplays.forEach(ArmorStand::remove);
        mobKillDisplays.clear();

        List<Tower> snapshot = new ArrayList<>(placedTowers.values());
        for (Tower tower : snapshot) {
            tower.sell();
        }
        placedTowers.clear();
    }

    public void powerCreep() {
        mobStates.multiplyPowerCreepMultiplyer(0.13F);
        twPlayer.getPlayer().sendMessage(Component.text("Monsters are getting stronger", NamedTextColor.DARK_RED));
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public UUID getUUID() {
        return uuid;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Map<Location, Tower> getTowers() {
        return placedTowers;
    }

    public Set<TWMob> getActiveMobs() {
        return activeMobs;
    }

    public List<ArmorStand> getMobKillDisplays() {
        return mobKillDisplays;
    }

    public int getLives() {
        return lives;
    }

    public Location getTrackSpawn() {
        return trackSpawn;
    }

    public void giveIncome() {
        twPlayer.recieveIncome();
    }

    public TWPlayer getTwPlayer() {
        return twPlayer;
    }

    public MobStates getMobStates() {
        return mobStates;
    }

    public Game getGame() {
        return game;
    }

    public boolean isAlive() {
        return alive;
    }

    public void gainLive(NamedTextColor fromColor, String causeName) {
        String colorName = fromColor.toString().toUpperCase();
        if (colorName.equals("GOLD")) colorName = "ORANGE";
        twPlayer.getPlayer().sendMessage(
                Component.text("You stole 1", NamedTextColor.GREEN)
                        .append(Component.text("❤", NamedTextColor.RED))
                        .append(Component.text(" from ", NamedTextColor.GREEN))
                        .append(Component.text(colorName, fromColor))
                        .append(Component.text(" (", NamedTextColor.GRAY))
                        .append(Component.text(causeName, NamedTextColor.YELLOW))
                        .append(Component.text(")", NamedTextColor.GRAY))
        );
        lives++;
    }

    public ArrayList<Vector> getRandomPath() {
        Random random = new Random();
        int index = random.nextInt(paths.size());
        ArrayList<Vector> path = paths.get(index);
        return path;
    }

    public boolean hasSpaceLeft() {
        return placedTowers.size() < maxTowers;
    }

    public void updateSidebar() {
        twPlayer.updateSidebar(true);
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxTowers() {
        return maxTowers;
    }
}
