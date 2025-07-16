package io.github.pako25.towerWars.Editor;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Main;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class ArenaEditor {
    private static final List<ArenaEditor> EditorList = new ArrayList<>();
    private final CustomConfig config;
    private final List<NamedTextColor> availableColors;
    private final Vector[] trackBounds = new Vector[4];
    private final ArrayList<ArrayList<Vector>> paths = new ArrayList<ArrayList<Vector>>();
    private final List<Location> trackSpawns = new ArrayList<>();
    private final String arenaName;
    private final TWPlayer twPlayer;
    private final JavaPlugin plugin;
    private String worldName;
    private boolean enabled;
    private static ItemStack instructionsBook;
    private int selectedPathIndex = 0;
    private Location lobbySpawn;
    private Material towerPlaceMaterial;

    public static void newInstance(String arenaName, TWPlayer twPlayer) {
        for (ArenaEditor arenaEditor : EditorList) {
            if (arenaEditor.getArenaName().equals(arenaName)) {
                twPlayer.getPlayer().sendMessage(Component.text("Someone is already configuring this arena."));
                return;
            }
        }
        EditorList.add(new ArenaEditor(arenaName, twPlayer));
    }

    public static void closeInstanceByPlayer(TWPlayer twPlayer, boolean save) {
        Iterator<ArenaEditor> iterator = EditorList.iterator();
        while (iterator.hasNext()) {
            ArenaEditor arenaEditor = iterator.next();
            if (arenaEditor.isPlayerEditor(twPlayer.getPlayer().getUniqueId())) {
                if (save) {
                    arenaEditor.saveConfig();
                    twPlayer.getPlayer().sendMessage(Component.text("Configuration saved."));
                } else {
                    twPlayer.getPlayer().sendMessage(Component.text("Discarded changes."));
                }
                iterator.remove();
            }
        }

        twPlayer.setInEditor(false);
        twPlayer.setActiveArenaEditor(null);
        twPlayer.getPlayer().getInventory().clear();
        twPlayer.getPlayer().sendMessage(Component.text("Quit editing mode."));
    }

    public static void closeAllEditors() {
        for (ArenaEditor arenaEditor : EditorList) {
            arenaEditor.saveConfig();
        }
        EditorList.clear();
    }

    private ArenaEditor(String arenaName, TWPlayer twPlayer) {
        this.arenaName = arenaName;
        this.twPlayer = twPlayer;
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.availableColors = GameManager.getInstance().getAllColors();

        List<String> allArenas = GameManager.getInstance().getAllArenas();
        if (allArenas.contains(arenaName)) {
            twPlayer.getPlayer().sendMessage(Component.text("The arena already exists. You are editing an already existing arena."));
            config = CustomConfig.getCustomConfig(arenaName);
            config.reload();
            loadExistingArena();
        } else {
            twPlayer.getPlayer().sendMessage(Component.text("Creating a new arena '" + arenaName + "'"));
            CustomConfig.setup(arenaName);
            config = CustomConfig.getCustomConfig(arenaName);
            GameManager.getInstance().addArenaToAllArenas(arenaName);
        }

        enterEditingMode();
        showEditorOptions();
    }

    public void showEditorOptions() {
        twPlayer.openArenaEditorInventory(this);
        //editor options inventory -> prikaže kaj je že in kaj še ni konfigurirano + opcija za savanje
        //ob izbiri opcije ti cleara inventory in da prave iteme + item za editor options
        //pol še samo naredim pravo logiko za vsak itrem posebej (tak ga lahko ma samo ko edita pravo stvar)
        //mogoče še dodam kocke na katere lahko placaš towere v konfiguracijo
        //++pazi da ne more sam zapret inventorija!
    }

    private void enterEditingMode() {
        twPlayer.setInEditor(true);
        twPlayer.setActiveArenaEditor(this);
        if (!trackSpawns.isEmpty()) {
            twPlayer.getPlayer().teleport(trackSpawns.getFirst().clone().add(0, 1, 0));
        }
        twPlayer.getPlayer().setGameMode(GameMode.CREATIVE);
        giveDefaultInventory();
    }

    private void loadExistingArena() {
        //ENABLED
        enabled = false; //config.getCustomFile().getBoolean("enabled");

        //ARENA LOBBY SPAWN
        List<?> locationRaw = config.getCustomFile().getList("lobbyLocation");
        if (locationRaw == null || locationRaw.size() != 3) {
            lobbySpawn = null;
        } else {
            lobbySpawn = new Location(twPlayer.getPlayer().getWorld(), (int) locationRaw.get(0), (int) locationRaw.get(1), (int) locationRaw.get(2));
        }

        //WORLD NAME
        worldName = config.getCustomFile().getString("worldName");

        //SPAWNS
        List<?> spawnsRaw = config.getCustomFile().getList("trackSpawns");
        if (spawnsRaw != null) {
            for (Object spawnObj : spawnsRaw) {
                if (!(spawnObj instanceof List<?> coords) || coords.size() != 3) {
                    plugin.getLogger().warning("Invalid trackSpawn entry, removing: " + spawnObj);
                    continue;
                }
                World world = twPlayer.getPlayer().getWorld();
                trackSpawns.add(new Location(world, (int) coords.get(0), (int) coords.get(1), (int) coords.get(2)));
            }

            if (trackSpawns.size() > availableColors.size()) {
                plugin.getLogger().warning("You can't have more than " + availableColors.size() + " track spawns! Some will be removed!");
                return;
            }
        }

        //BOUNDS
        List<?> boundsRaw = config.getCustomFile().getList("trackBounds");
        if (boundsRaw != null) {
            for (int i = 0; i < boundsRaw.size(); i++) {
                if (!(boundsRaw.get(i) instanceof List<?> coords) || coords.size() != 3) {
                    plugin.getLogger().warning("Invalid trackBounds entry, removing: " + boundsRaw.get(i));
                    continue;
                }
                trackBounds[i] = (new Vector((int) coords.get(0), (int) coords.get(1), (int) coords.get(2)));
            }
        }

        //PATHS
        List<?> rawPaths = config.getCustomFile().getList("paths");
        if (rawPaths != null) {
            for (Object rawPathObj : rawPaths) {
                if (!(rawPathObj instanceof List<?> rawPathList)) {
                    plugin.getLogger().warning("Invalid path entry, skipping: " + rawPathObj);
                    continue;
                }

                ArrayList<Vector> path = new ArrayList<>();
                for (Object coordObj : rawPathList) {
                    if (!(coordObj instanceof List<?> coordList) || coordList.size() != 3) {
                        plugin.getLogger().warning("Invalid coordinate, skipping: " + coordObj);
                        continue;
                    }

                    path.add(new Vector((int) coordList.get(0), (int) coordList.get(1), (int) coordList.get(2)));
                }

                if (!path.isEmpty()) {
                    paths.add(path);
                }
            }
        }

        //TOWER PLACE MATERIAL
        String towerPlaceMaterialRaw = config.getCustomFile().getString("towerPlaceMaterial");
        if (towerPlaceMaterialRaw != null) {
            towerPlaceMaterial = Material.matchMaterial(towerPlaceMaterialRaw, false);
            if (towerPlaceMaterial == null) towerPlaceMaterial = Material.matchMaterial(towerPlaceMaterialRaw, true);
        }
    }

    private void saveConfig() {
        FileConfiguration cfg = config.getCustomFile();
        cfg.set("worldName", worldName);

        List<List<Integer>> trackSpawnsFormattedList = new ArrayList<>();
        for (Location trackSpawn : trackSpawns) {
            trackSpawnsFormattedList.add(List.of(trackSpawn.getBlockX(), trackSpawn.getBlockY(), trackSpawn.getBlockZ()));
        }
        cfg.set("trackSpawns", trackSpawnsFormattedList);

        List<List<Integer>> trackBoundsFormattedList = new ArrayList<>();
        for (Vector trackBound : trackBounds) {
            if (trackBound == null) continue;
            trackBoundsFormattedList.add(List.of(trackBound.getBlockX(), trackBound.getBlockY(), trackBound.getBlockZ()));
        }
        cfg.set("trackBounds", trackBoundsFormattedList);

        List<List<List<Integer>>> pathsFormattedList = new ArrayList<>();
        for (ArrayList<Vector> path : paths) {
            List<List<Integer>> pathFormattedList = new ArrayList<>();
            for (Vector waypoint : path) {
                pathFormattedList.add(List.of(waypoint.getBlockX(), waypoint.getBlockY(), waypoint.getBlockZ()));
            }
            pathsFormattedList.add(pathFormattedList);
        }
        cfg.set("paths", pathsFormattedList);

        if (lobbySpawn == null) {
            cfg.set("lobbyLocation", null);
        } else {
            List<Integer> lobbySpawnList = new ArrayList<>(List.of(lobbySpawn.getBlockX(), lobbySpawn.getBlockY(), lobbySpawn.getBlockZ()));
            cfg.set("lobbyLocation", lobbySpawnList);
        }

        cfg.set("towerPlaceMaterial", towerPlaceMaterial.name());

        cfg.set("enabled", enabled);

        config.save();
    }

    public void giveDefaultInventory() {
        twPlayer.getPlayer().getInventory().clear();

        ItemStack optionsItem = new ItemStack(Material.COMPASS, 1);
        ItemMeta optionsItemMeta = optionsItem.getItemMeta();
        optionsItemMeta.displayName(Component.text("Editor options", NamedTextColor.RED));
        optionsItem.setItemMeta(optionsItemMeta);

        ItemStack saveItem = new ItemStack(Material.CLOCK);
        ItemMeta saveItemMeta = saveItem.getItemMeta();
        saveItemMeta.displayName(Component.text("EXIT", NamedTextColor.RED));
        saveItemMeta.lore(List.of(
                Component.text("Left click -> ", NamedTextColor.WHITE).append(Component.text("SAVE", NamedTextColor.GREEN)),
                Component.text("Sneak + right click -> ", NamedTextColor.WHITE).append(Component.text("DISCARD", NamedTextColor.RED))
        ));
        saveItem.setItemMeta(saveItemMeta);

        twPlayer.getPlayer().getInventory().setItem(7, optionsItem);
        twPlayer.getPlayer().getInventory().setItem(6, instructionsBook);
        twPlayer.getPlayer().getInventory().setItem(8, saveItem);
    }

    public Location removeLastTrackSpawn() {
        return trackSpawns.removeLast();
    }

    public void addNewTrackSpawn(Location location) {
        if (isWorldInvalid(location.getWorld().getName())) return;
        if (trackSpawns.contains(location)) {
            twPlayer.getPlayer().sendMessage(Component.text("This location is a track spawn already!", NamedTextColor.YELLOW));
            return;
        }
        trackSpawns.add(location);
        twPlayer.getPlayer().sendMessage(Component.text("Added a new track spawn: " + location.toVector()));
    }

    public Location removeTrackSpawn(Location location) {
        Iterator<Location> iterator = trackSpawns.iterator();
        while (iterator.hasNext()) {
            Location trackSpawn = iterator.next();
            if (trackSpawn.equals(location)) {
                iterator.remove();
                return trackSpawn;
            }
        }
        return null;
    }

    public void addNewTrackBound(Location location) {
        if (isWorldInvalid(location.getWorld().getName())) return;
        try {
            Location trackSpawn = trackSpawns.getFirst();
            Vector trackBound = location.clone().subtract(trackSpawn).toVector();
            for (int i = 0; i < trackBounds.length; i++) {
                if (trackBounds[i] != null) {
                    if (trackBounds[i].equals(trackBound)) {
                        twPlayer.getPlayer().sendMessage(Component.text("This location is already a track bound!"));
                        return;
                    }
                    continue;
                }
                trackBounds[i] = trackBound;
                break;
            }
        } catch (NoSuchElementException e) {
            twPlayer.getPlayer().sendMessage(Component.text("Configure a track spawn first!"));
        }
    }

    public Vector removeLastTrackBound() {
        for (int i = trackBounds.length - 1; i > -1; i--) {
            if (trackBounds[i] != null) {
                Vector removed = trackBounds[i];
                trackBounds[i] = null;
                return removed;
            }
        }
        return null;
    }

    public Vector removeTrackBound(Location location) {
        try {
            Location trackSpawn = trackSpawns.getFirst();
            Vector trackBound = location.clone().subtract(trackSpawn).toVector();
            for (int i = 0; i < trackBounds.length; i++) {
                if (trackBounds[i] == null) continue;
                if (trackBounds[i].equals(trackBound)) {
                    trackBounds[i] = null;
                    return trackBound;
                }
            }
        } catch (NoSuchElementException e) {
            twPlayer.getPlayer().sendMessage(Component.text("Configure a track spawn first!"));
        }
        return null;
    }

    public void removeSelectedPath() {
        if (paths.isEmpty()) {
            twPlayer.getPlayer().sendMessage(Component.text("No paths left!"));
            return;
        }

        try {
            int pathSize = paths.get(selectedPathIndex).size();
            paths.remove(selectedPathIndex);
            twPlayer.getPlayer().sendMessage(Component.text("Removed path with " + pathSize + " waypoints."));
        } catch (IndexOutOfBoundsException e) {
            selectedPathIndex = 0;
        }

        selectedPathIndex--;
        if (selectedPathIndex < 0) selectedPathIndex = 0;
    }

    public void addNewWaypoint(Location location) {
        if (isWorldInvalid(location.getWorld().getName())) return;
        if (paths.isEmpty()) {
            twPlayer.getPlayer().sendMessage(Component.text("First make (and select) a path!", NamedTextColor.YELLOW));
            return;
        }
        try {
            Location trackSpawn = trackSpawns.getFirst();
            Vector waypoint = location.clone().subtract(trackSpawn).toVector();
            if (paths.get(selectedPathIndex).contains(waypoint)) {
                twPlayer.getPlayer().sendMessage(Component.text("This is already a waypoint."));
                return;
            }
            paths.get(selectedPathIndex).add(waypoint);
            twPlayer.getPlayer().sendMessage(Component.text("Added a new waypoint."));
        } catch (NoSuchElementException e) {
            twPlayer.getPlayer().sendMessage(Component.text("Configure a track spawn first!"));
        } catch (IndexOutOfBoundsException e) {
            twPlayer.getPlayer().sendMessage(Component.text("An error occurred. IndexOutOfBounds in addNewWaypoint. Contact the developer."));
        }
    }

    public void removeWaypoint(Location location) {
        if (paths.isEmpty()) return;
        if (isWorldInvalid(location.getWorld().getName())) return;
        try {
            Location trackSpawn = trackSpawns.getFirst();
            Vector waypoint = location.clone().subtract(trackSpawn).toVector();
            boolean existed = paths.get(selectedPathIndex).remove(waypoint);
            if (existed)
                twPlayer.getPlayer().sendMessage(Component.text("Removed waypoint: " + waypoint));
        } catch (NoSuchElementException e) {
            twPlayer.getPlayer().sendMessage(Component.text("Configure a track spawn first!"));
        } catch (IndexOutOfBoundsException e) {
            twPlayer.getPlayer().sendMessage(Component.text("An error occurred. IndexOutOfBounds in removeNewWaypoint. Contact the developer."));
        }
    }

    private boolean isWorldInvalid(String worldName) {
        if (this.worldName == null) {
            twPlayer.getPlayer().sendMessage(Component.text("First set the world name!", NamedTextColor.RED));
            return true;
        }
        if (!this.worldName.equals(worldName)) {
            twPlayer.getPlayer().sendMessage(Component.text("The locations world doesn't match with the worldName you set! (" + this.worldName + ")", NamedTextColor.RED));
            return true;
        }
        return false;
    }

    public void changeSelectedPathIndex() {
        if (paths.isEmpty()) {
            selectedPathIndex = 0;
            return;
        }
        if (selectedPathIndex == paths.size() - 1) {
            selectedPathIndex = 0;
        } else {
            selectedPathIndex++;
        }
        twPlayer.getPlayer().sendMessage(Component.text("Selected path " + selectedPathIndex + " with " + paths.get(selectedPathIndex).size() + " waypoints."));
    }

    private boolean verifyConfigurationValidity() {
        if (worldName == null) return false;
        if (towerPlaceMaterial == null) return false;
        if (plugin.getServer().getWorld(worldName) == null) {
            twPlayer.getPlayer().sendMessage(Component.text("The world you provided doesn't exist.", NamedTextColor.YELLOW));
            return false;
        }
        if (trackSpawns.size() > availableColors.size() || trackSpawns.size() < 2) return false;
        if (getTrackBoundsLength() != 4) return false;
        if (paths.isEmpty()) return false;
        for (Location location : trackSpawns) {
            if (isWorldInvalid(location.getWorld().getName())) return false;
        }
        for (Vector v1 : trackBounds) {
            int inlineCounter = 0;
            for (Vector v2 : trackBounds) {
                if (!v1.equals(v2)) {
                    if (areVectorsDifferentInOneDimension(v1, v2)) inlineCounter++;
                }
            }
            if (inlineCounter != 2) {
                twPlayer.getPlayer().sendMessage(Component.text("Track bounds don't form a rectangle!", NamedTextColor.YELLOW));
                return false;
            }
        }
        for (List<Vector> path : paths) {
            if (path.size() < 3) {
                twPlayer.getPlayer().sendMessage(Component.text("Each path must consist of at least 3 waypoints.", NamedTextColor.YELLOW));
                return false;
            }
            for (int i = 1; i < path.size() - 1; i++) {
                if (!areVectorsDifferentInOneDimension(path.get(i), path.get(i - 1)) || !areVectorsDifferentInOneDimension(path.get(i), path.get(i + 1))) {
                    twPlayer.getPlayer().sendMessage(Component.text("Consecutive path waypoints can differ in at most one coordinate.", NamedTextColor.YELLOW));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPlayerEditor(UUID uuid) {
        return uuid.equals(twPlayer.getPlayer().getUniqueId());
    }

    public List<NamedTextColor> getAvailableColors() {
        return availableColors;
    }

    public Vector[] getTrackBounds() {
        return trackBounds;
    }

    public ArrayList<ArrayList<Vector>> getPaths() {
        return paths;
    }

    public List<Location> getTrackSpawns() {
        return trackSpawns;
    }

    public int getSelectedPathIndex() {
        return selectedPathIndex;
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public Material getTowerPlaceMaterial() {
        return towerPlaceMaterial;
    }

    public void setTowerPlaceMaterial(Material material) {
        towerPlaceMaterial = material;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public boolean isArenaEnabled() {
        return enabled;
    }

    private boolean areVectorsDifferentInOneDimension(Vector v1, Vector v2) {
        //excluding Y
        int differentDimensions = 0;
        if (v1.getX() != v2.getX()) differentDimensions++;
        if (v1.getZ() != v2.getZ()) differentDimensions++;
        return differentDimensions == 1;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            enabled = verifyConfigurationValidity();
            if (!enabled) {
                twPlayer.getPlayer().sendMessage(Component.text("Your configuration is invalid.", NamedTextColor.RED));
            }
        }
        this.enabled = enabled;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public int getTrackBoundsLength() {
        int counter = 0;
        for (Vector bound : trackBounds) {
            if (bound != null) counter++;
        }
        return counter;
    }

    public static void generateInstructionsBook() {
        if (instructionsBook != null) return;
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.author(Component.text("TowerWars"));
        bookMeta.title(Component.text("Instructions"));
        bookMeta.addPages(
                Component.text("Arena world", NamedTextColor.RED).appendNewline().appendNewline().append(Component.text("The arena world is the world in which all tracks will be located. You cannot set track spawns, bounds or path waypoints in any other world.", NamedTextColor.BLACK)),
                Component.text("Track spawns", NamedTextColor.RED).appendNewline().appendNewline().append(Component.text("Each track spawn belongs to a specific track. A track has its own bounds and path waypoints. All tracks are identical in their bounds, path configurations and ORIENTATION but can differ elsewhere.", NamedTextColor.GRAY, TextDecoration.ITALIC)).appendNewline().append(Component.text("             continue->", NamedTextColor.BLACK)),
                Component.text("Track spawns are 'anchors' for each track. They are the only absolute location that is stored, all bounds and path waypoints are vectors and therefore relative to their corresponding track spawns. This allows easier moving and configuring of tracks.", NamedTextColor.BLACK),
                Component.text("Track bounds", NamedTextColor.RED).appendNewline().appendNewline().append(Component.text("Track bounds are 2-dimensional 'corners' of a track. The Y coordinate is ignored, therefore tracks can not be stacked on top of one another. Towers can only be placed INSIDE track bounds.", NamedTextColor.BLACK)),
                Component.text("The bounds must form a rectangle. They need to be set only for one track spawn and will be automatically configured for all other tracks. The same applies to path waypoints.", NamedTextColor.BLACK).appendNewline().append(Component.text("(read page 3)", NamedTextColor.BLACK)),
                Component.text("Paths", NamedTextColor.RED).appendNewline().appendNewline().append(Component.text("Paths are made up of waypoints that tell the mobs where to go. Mobs can only move in a straight line. There can be multiple paths in a single track", NamedTextColor.BLACK)),
                Component.text("Path waypoints", NamedTextColor.RED).appendNewline().appendNewline().append(Component.text("Waypoints need to be set in the correct order (start to end). Between two neighbouring waypoints at most one coordinate direction can differ. They need to lay on a plane (singular Y).", NamedTextColor.BLACK))
        );
        book.setItemMeta(bookMeta);
        instructionsBook = book;
    }
}