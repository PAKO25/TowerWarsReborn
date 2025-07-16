package io.github.pako25.towerWars.Editor;

import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class EditorOptionsInventory implements InventoryHolder {

    private final Inventory inventory;
    private final TWPlayer twPlayer;
    private final JavaPlugin plugin;
    private final ArenaEditor arenaEditor;

    public EditorOptionsInventory(JavaPlugin plugin, TWPlayer twPlayer, ArenaEditor arenaEditor) {
        this.arenaEditor = arenaEditor;
        this.inventory = plugin.getServer().createInventory(this, 45);
        this.twPlayer = twPlayer;
        this.plugin = plugin;
        loadInventory();
    }

    private void loadInventory() {
        //WORLD NAME
        ItemStack worldNameItem = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta worldNameItemMeta = worldNameItem.getItemMeta();
        NamedTextColor worldNameDoneOrNotColor = arenaEditor.getWorldName() == null ? NamedTextColor.RED : NamedTextColor.GREEN;
        worldNameItemMeta.displayName(Component.text("Configure world name", NamedTextColor.WHITE));

        List<Component> worldNameLoreComponents = List.of(
                Component.text(""),
                Component.text("World name: " + arenaEditor.getWorldName(), worldNameDoneOrNotColor),
                Component.text("Left click -> set new at current world")
        );

        worldNameItemMeta.lore(worldNameLoreComponents);
        worldNameItem.setItemMeta(worldNameItemMeta);
        inventory.setItem(11, worldNameItem);

        //TRACK SPAWNS
        ItemStack trackSpawnsItem = new ItemStack(Material.GOLD_BLOCK, 1);
        ItemMeta trackSpawnsItemMeta = trackSpawnsItem.getItemMeta();
        NamedTextColor trackSpawnsDoneOrNotColor = arenaEditor.getTrackSpawns().size() <= arenaEditor.getAvailableColors().size() && arenaEditor.getTrackSpawns().size() > 1 ? NamedTextColor.GREEN : NamedTextColor.RED;
        trackSpawnsItemMeta.displayName(Component.text("Configure track spawns", NamedTextColor.WHITE));

        List<Component> trackSpawnsLoreComponents = new ArrayList<>(List.of(
                Component.text(""),
                Component.text("Configured: " + arenaEditor.getTrackSpawns().size() + "/" + arenaEditor.getAvailableColors().size(), trackSpawnsDoneOrNotColor),
                Component.text("Left click -> get edit stick"),
                Component.text("Right click -> remove last entry"),
                Component.text("")
        ));
        for (Location trackSpawn : arenaEditor.getTrackSpawns()) {
            trackSpawnsLoreComponents.add(Component.text(" - [", NamedTextColor.GRAY).append(Component.text(trackSpawn.getBlockX() + " " + trackSpawn.getBlockY() + " " + trackSpawn.getBlockZ() + "]")));
        }

        trackSpawnsItemMeta.lore(trackSpawnsLoreComponents);
        trackSpawnsItem.setItemMeta(trackSpawnsItemMeta);
        inventory.setItem(13, trackSpawnsItem);

        //TRACK BOUNDS
        ItemStack trackBoundsItem = new ItemStack(Material.OAK_FENCE);
        ItemMeta trackBoundsItemMeta = trackBoundsItem.getItemMeta();
        trackBoundsItemMeta.displayName(Component.text("Configure track bounds"));

        NamedTextColor trackBoundsDoneOrNotColor = arenaEditor.getTrackBoundsLength() == 4 ? NamedTextColor.GREEN : NamedTextColor.RED;
        List<Component> trackBoundsLoreComponents = new ArrayList<>(List.of(
                Component.text(""),
                Component.text("Configured: " + arenaEditor.getTrackBoundsLength() + "/4", trackBoundsDoneOrNotColor),
                Component.text("Left click -> get edit fence"),
                Component.text("Right click -> remove last entry"),
                Component.text(""),
                Component.text("Carefully read instructions!", NamedTextColor.RED),
                Component.text("")
        ));
        Location firstTrackSpawn;
        try {
            firstTrackSpawn = arenaEditor.getTrackSpawns().getFirst().clone();
        } catch (NoSuchElementException e) {
            firstTrackSpawn = null;
        }

        if (firstTrackSpawn == null) {
            trackBoundsLoreComponents.add(Component.text("First set a track spawn!", NamedTextColor.RED));
        } else {
            for (Vector trackBound : arenaEditor.getTrackBounds()) {
                if (trackBound == null) continue;
                Location trackBoundLocation = firstTrackSpawn.add(trackBound);
                trackBoundsLoreComponents.add(Component.text(" - [", NamedTextColor.GRAY).append(Component.text(trackBoundLocation.getBlockX() + " " + trackBoundLocation.getBlockY() + " " + trackBoundLocation.getBlockZ() + "]")));
            }
        }

        trackBoundsItemMeta.lore(trackBoundsLoreComponents);
        trackBoundsItem.setItemMeta(trackBoundsItemMeta);
        inventory.setItem(15, trackBoundsItem);

        //TRACK PATHS
        ItemStack trackPathsItem = new ItemStack(Material.RAIL);
        ItemMeta trackPathsItemMeta = trackPathsItem.getItemMeta();

        trackPathsItemMeta.displayName(Component.text("Configure track paths"));
        boolean pathsConfigurationValid = !arenaEditor.getPaths().isEmpty();
        for (List<Vector> path : arenaEditor.getPaths()) {
            if (path.size() < 3) {
                pathsConfigurationValid = false;
                break;
            }
        }

        List<Component> trackPathsLoreComponents = new ArrayList<>(List.of(
                Component.text(""),
                Component.text("Configured: " + (pathsConfigurationValid ? "YES" : "NO"), pathsConfigurationValid ? NamedTextColor.GREEN : NamedTextColor.RED),
                Component.text("Left click -> get tools"),
                Component.text("Right click -> create new path"),
                Component.text(""),
                Component.text("Carefully read instructions!", NamedTextColor.RED),
                Component.text("")
        ));
        int pathCount = 0;
        for (List<Vector> path : arenaEditor.getPaths()) {
            trackPathsLoreComponents.add(Component.text("Path " + pathCount + ": " + path.size() + " waypoints", NamedTextColor.GRAY));
            pathCount++;
        }

        trackPathsItemMeta.lore(trackPathsLoreComponents);
        trackPathsItem.setItemMeta(trackPathsItemMeta);
        inventory.setItem(28, trackPathsItem);

        //ENABLED
        ItemStack trackEnabledItem = new ItemStack(arenaEditor.isArenaEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta trackEnabledItemMeta = trackBoundsItem.getItemMeta();
        trackEnabledItemMeta.displayName(Component.text("Toggle enabled status"));
        trackEnabledItemMeta.lore(List.of(Component.text("Track is ", NamedTextColor.WHITE).append(Component.text(arenaEditor.isArenaEnabled() ? "Enabled" : "Disabled", arenaEditor.isArenaEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))));
        trackEnabledItem.setItemMeta(trackEnabledItemMeta);
        inventory.setItem(34, trackEnabledItem);

        //LOBBY SPAWN
        ItemStack arenaLobbySpawnItem = new ItemStack(Material.GLASS);
        ItemMeta arenaLobbySpawnItemMeta = arenaLobbySpawnItem.getItemMeta();
        boolean configuredLobbySpawn = arenaEditor.getLobbySpawn() != null;
        arenaLobbySpawnItemMeta.displayName(Component.text("Set pregame lobby spawn"));
        arenaLobbySpawnItemMeta.lore(List.of(
                Component.text("If none is set players won't teleport."),
                Component.text("Left click -> set new"),
                Component.text("Location: " + (configuredLobbySpawn ? arenaEditor.getLobbySpawn().toVector().toString() : "not set"), configuredLobbySpawn ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));
        arenaLobbySpawnItem.setItemMeta(arenaLobbySpawnItemMeta);
        inventory.setItem(30, arenaLobbySpawnItem);

        //TOWER BLOCK
        ItemStack towerBlockItem = new ItemStack(Material.COBBLESTONE);
        ItemMeta towerBlockItemMeta = towerBlockItem.getItemMeta();
        towerBlockItemMeta.displayName(Component.text("Tower place block"));
        boolean towerPlaceMaterialSet = arenaEditor.getTowerPlaceMaterial() != null;
        towerBlockItemMeta.lore(List.of(
                Component.text("Left click -> set new material"),
                Component.text("Currently set: " + (towerPlaceMaterialSet ? arenaEditor.getTowerPlaceMaterial().toString() : "null"), towerPlaceMaterialSet ? NamedTextColor.GREEN : NamedTextColor.RED),
                Component.empty(),
                Component.text("Define the type of block players", NamedTextColor.WHITE),
                Component.text("can place towers on", NamedTextColor.WHITE)
        ));
        towerBlockItem.setItemMeta(towerBlockItemMeta);
        inventory.setItem(32, towerBlockItem);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void itemClick(ItemStack item, boolean isLeftClick, boolean isRightClick) {
        switch (item.getType()) {
            case OAK_SIGN:
                arenaEditor.setWorldName(twPlayer.getPlayer().getWorld().getName());
                break;
            case GOLD_BLOCK:
                if (isRightClick) {
                    if (arenaEditor.getTrackSpawns().isEmpty()) {
                        twPlayer.getPlayer().sendMessage(Component.text("There are no track spawns left!"));
                        return;
                    }
                    Location removed = arenaEditor.removeLastTrackSpawn();
                    twPlayer.getPlayer().sendMessage(Component.text("Removed track spawn " + removed.toVector()));
                }
                if (isLeftClick) {
                    ItemStack trackSpawnStick = new ItemStack(Material.STICK, 1);
                    ItemMeta trackSpawnStickMeta = trackSpawnStick.getItemMeta();
                    trackSpawnStickMeta.displayName(Component.text("Track Spawn Configurator", NamedTextColor.GOLD));
                    trackSpawnStickMeta.lore(List.of(
                            Component.text("Left click to set new track spawn"),
                            Component.text("Right click to remove track spawn")
                    ));
                    trackSpawnStick.setItemMeta(trackSpawnStickMeta);

                    arenaEditor.giveDefaultInventory();
                    twPlayer.getPlayer().getInventory().addItem(trackSpawnStick);
                }
                break;
            case OAK_FENCE:
                if (isRightClick) {
                    if (arenaEditor.getTrackBoundsLength() == 0) {
                        twPlayer.getPlayer().sendMessage("There are no track bounds left!");
                        return;
                    }
                    Vector removed = arenaEditor.removeLastTrackBound();
                    twPlayer.getPlayer().sendMessage(Component.text("Removed track bound " + removed.toString()));
                }
                if (isLeftClick) {
                    try {
                        Location trackSpawn = arenaEditor.getTrackSpawns().getFirst();
                        twPlayer.getPlayer().teleport(trackSpawn.clone().add(0, 2, 0));
                    } catch (NoSuchElementException e) {
                        twPlayer.getPlayer().sendMessage(Component.text("First configure at least one track spawn!", NamedTextColor.RED));
                        return;
                    }
                    ItemStack trackBoundsFence = new ItemStack(Material.OAK_FENCE, 1);
                    ItemMeta trackBoundsFenceMeta = trackBoundsFence.getItemMeta();
                    trackBoundsFenceMeta.displayName(Component.text("Track Bounds Configurator", NamedTextColor.GOLD));
                    trackBoundsFenceMeta.lore(List.of(
                            Component.text("Left click to set new track bound"),
                            Component.text("Right click to remove track bound"),
                            Component.empty(),
                            Component.text("Configure track bounds in relation to the", NamedTextColor.GRAY),
                            Component.text("track spawn you were teleported to!", NamedTextColor.GRAY)
                    ));
                    trackBoundsFence.setItemMeta(trackBoundsFenceMeta);

                    arenaEditor.giveDefaultInventory();
                    twPlayer.getPlayer().getInventory().addItem(trackBoundsFence);
                }
                break;
            case RAIL:
                if (isRightClick) {
                    arenaEditor.getPaths().add(new ArrayList<>());
                }
                if (isLeftClick || (isRightClick && twPlayer.getPlayer().getInventory().contains(Material.RAIL))) {
                    if (isLeftClick) {
                        try {
                            Location trackSpawn = arenaEditor.getTrackSpawns().getFirst();
                            twPlayer.getPlayer().teleport(trackSpawn.clone().add(0, 2, 0));
                        } catch (NoSuchElementException e) {
                            twPlayer.getPlayer().sendMessage(Component.text("First configure at least one track spawn!", NamedTextColor.RED));
                            return;
                        }
                    }
                    ItemStack rail = new ItemStack(Material.RAIL);
                    ItemMeta railMeta = rail.getItemMeta();
                    railMeta.displayName(Component.text("Waypoint configurator", NamedTextColor.GOLD));
                    railMeta.lore(List.of(
                            Component.text("Left click to set a new waypoint"),
                            Component.text("Right click to remove waypoint"),
                            Component.empty(),
                            Component.text("Select the path you are editing", NamedTextColor.GRAY),
                            Component.text("with the path selection tool!", NamedTextColor.GRAY),
                            Component.empty(),
                            Component.text("Configure waypoints in relation to the", NamedTextColor.GRAY),
                            Component.text("track spawn you were teleported to!", NamedTextColor.GRAY)
                    ));
                    rail.setItemMeta(railMeta);

                    ItemStack repeater = new ItemStack(Material.REPEATER);
                    ItemMeta repeaterMeta = repeater.getItemMeta();
                    repeaterMeta.displayName(Component.text("Path selection", NamedTextColor.GOLD));
                    List<Component> repeaterMetaLoreComponents = new ArrayList<>(List.of(
                            Component.text("Left click to change path"),
                            Component.text("Right click to remove selected path")
                    ));
                    int pathCount = 0;
                    for (List<Vector> path : arenaEditor.getPaths()) {
                        NamedTextColor pathColorInLore = pathCount == arenaEditor.getSelectedPathIndex() ? NamedTextColor.YELLOW : NamedTextColor.GRAY;
                        repeaterMetaLoreComponents.add(Component.text("Path " + pathCount + ": " + path.size() + " waypoints", pathColorInLore));
                        pathCount++;
                    }
                    repeaterMeta.lore(repeaterMetaLoreComponents);
                    repeater.setItemMeta(repeaterMeta);

                    arenaEditor.giveDefaultInventory();
                    twPlayer.getPlayer().getInventory().addItem(rail);
                    twPlayer.getPlayer().getInventory().addItem(repeater);
                }
                break;
            case GREEN_WOOL:
                arenaEditor.setEnabled(false);
                break;
            case RED_WOOL:
                arenaEditor.setEnabled(true);
                break;
            case GLASS:
                arenaEditor.setLobbySpawn(twPlayer.getPlayer().getLocation());
                break;
            case COBBLESTONE:
                StringPrompt prompt = new StringPrompt() {
                    @Override
                    public @NotNull String getPromptText(@NotNull ConversationContext context) {
                        return "Enter the name of the material you wish to use as the tower placement block in chat.";
                    }

                    @Override
                    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                        try {
                            if (input == null) throw new IllegalArgumentException();
                            Material newMaterial = Material.matchMaterial(input, false);
                            if (newMaterial == null || !newMaterial.isBlock()) throw new IllegalArgumentException();
                            arenaEditor.setTowerPlaceMaterial(newMaterial);
                            twPlayer.getPlayer().sendMessage("New material set successfully.");
                        } catch (IllegalArgumentException e) {
                            twPlayer.getPlayer().sendMessage(Component.text("The material you provided is invalid."));
                            twPlayer.getPlayer().sendMessage(Component.text("The list of available materials can be found ", NamedTextColor.WHITE).append(Component.text("HERE", NamedTextColor.RED).clickEvent(ClickEvent.openUrl("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")).hoverEvent(HoverEvent.showText(Component.text("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")))));
                        }
                        twPlayer.unfreeze();
                        return END_OF_CONVERSATION;
                    }
                };
                Conversation conversation = new Conversation(plugin, twPlayer.getPlayer(), prompt);
                twPlayer.getPlayer().beginConversation(conversation);
                twPlayer.getPlayer().closeInventory();
                twPlayer.getPlayer().showTitle(Title.title(Component.text("Write material in chat"), Component.empty(), Title.DEFAULT_TIMES));
                twPlayer.freeze();
                break;
        }
        inventory.clear();
        loadInventory();
    }
}