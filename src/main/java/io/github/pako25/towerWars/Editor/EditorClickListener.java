package io.github.pako25.towerWars.Editor;

import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class EditorClickListener implements Listener {
    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (!twPlayer.isInEditor()) return;
        ArenaEditor arenaEditor = twPlayer.getActiveArenaEditor();
        event.setCancelled(true);

        switch (item.getType()) {
            case STICK:
                if (event.getClickedBlock() == null) return;
                if (arenaEditor.getTrackSpawns().size() >= arenaEditor.getAvailableColors().size()) {
                    player.sendMessage(Component.text("You have already set all track spawns!", NamedTextColor.YELLOW));
                    arenaEditor.giveDefaultInventory();
                    arenaEditor.showEditorOptions();
                    return;
                }
                arenaEditor.addNewTrackSpawn(event.getClickedBlock().getLocation());
                break;
            case OAK_FENCE:
                if (event.getClickedBlock() == null) return;
                if (arenaEditor.getTrackBoundsLength() >= 4) {
                    player.sendMessage(Component.text("You have already set all track bounds!", NamedTextColor.YELLOW));
                    arenaEditor.giveDefaultInventory();
                    arenaEditor.showEditorOptions();
                    return;
                }
                arenaEditor.addNewTrackBound(event.getClickedBlock().getLocation());
                break;
            case REPEATER:
                arenaEditor.changeSelectedPathIndex();
                remakeRepeater(item, arenaEditor);
                break;
            case RAIL:
                if (event.getClickedBlock() == null) return;
                arenaEditor.addNewWaypoint(event.getClickedBlock().getLocation().clone().add(0, 1, 0));
                remakeRepeater(twPlayer.getPlayer().getInventory().getItem(1), arenaEditor);
                break;
            case CLOCK:
                ArenaEditor.closeInstanceByPlayer(twPlayer, true);
                break;
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());
        if (!twPlayer.isInEditor()) return;
        ArenaEditor arenaEditor = twPlayer.getActiveArenaEditor();
        event.setCancelled(true);

        switch (item.getType()) {
            case COMPASS:
                arenaEditor.showEditorOptions();
                break;
            case STICK:
                if (event.getClickedBlock() == null) return;
                Location removedTrackSpawn = arenaEditor.removeTrackSpawn(event.getClickedBlock().getLocation());
                if (removedTrackSpawn != null) {
                    twPlayer.getPlayer().sendMessage(Component.text("Removed track spawn:" + removedTrackSpawn.toVector()));
                }
                break;
            case WRITTEN_BOOK:
                event.setCancelled(false);
                break;
            case OAK_FENCE:
                if (event.getClickedBlock() == null) return;
                Vector removedTrackBound = arenaEditor.removeTrackBound(event.getClickedBlock().getLocation());
                if (removedTrackBound != null) {
                    twPlayer.getPlayer().sendMessage(Component.text("Removed track bound:" + removedTrackBound));
                }
                break;
            case REPEATER:
                arenaEditor.removeSelectedPath();
                remakeRepeater(item, arenaEditor);
                break;
            case RAIL:
                if (event.getClickedBlock() == null) return;
                arenaEditor.removeWaypoint(event.getClickedBlock().getLocation().clone().add(0, 1, 0));
                remakeRepeater(twPlayer.getPlayer().getInventory().getItem(1), arenaEditor);
                break;
            case CLOCK:
                if (!player.isSneaking()) {
                    player.sendMessage(Component.text("To discard changes right click while sneaking.", NamedTextColor.YELLOW));
                    return;
                }
                ArenaEditor.closeInstanceByPlayer(twPlayer, false);
                break;
        }
    }

    private void remakeRepeater(ItemStack repeater, ArenaEditor arenaEditor) {
        if (repeater == null) return;
        ItemMeta repeaterMeta = repeater.getItemMeta();
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
    }
}