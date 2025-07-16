package io.github.pako25.towerWars.GameManagment;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class SignManager implements Listener {

    private static SignManager signManager;
    private final List<Location> managedSigns = new ArrayList<>();

    public static SignManager getInstance() {
        if (signManager == null) signManager = new SignManager();
        return signManager;
    }

    private SignManager() {
        FileConfiguration cfg = CustomConfig.getFileConfiguration("managedSigns");

        try {
            List<Location> signLocations = new ArrayList<>();
            List<?> locationsRaw = cfg.getList("locations");
            if (locationsRaw == null) return;

            for (Object locationRaw : locationsRaw) {
                if (!(locationRaw instanceof List<?> locationData) || locationData.size() != 4) {
                    throw new IllegalArgumentException("Invalid entries in managedSigns.yml");
                }
                World world = TowerWars.getPlugin().getServer().getWorld((String) locationData.get(0));
                signLocations.add(new Location(world, (int) locationData.get(1), (int) locationData.get(2), (int) locationData.get(3)));
            }
            for (Location location : signLocations) {
                if (location.getBlock().getState() instanceof Sign) {
                    managedSigns.add(location);
                } else {
                    TowerWars.getPlugin().getLogger().warning("A sign seems to have been removed at: " + location.toString());
                }
            }
        } catch (Exception e) {
            TowerWars.getPlugin().getLogger().severe("An critical error occurred in SignManager when parsing data. Please fix or delete managedSigns.yml");
            TowerWars.getPlugin().getServer().shutdown();
        }
        updateSigns();
        saveManagedSigns();
    }

    public void saveManagedSigns() {
        List<List<Object>> saveList = new ArrayList<>();
        for (Location location : managedSigns) {
            saveList.add(List.of(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        CustomConfig config = CustomConfig.getCustomConfig("managedSigns");
        config.getCustomFile().set("locations", saveList);
        config.save();
    }

    public void updateSigns() {
        ArrayList<Location> markedForRemoval = new ArrayList<>();
        for (Location location : managedSigns) {
            boolean success = updateSign(location);
            if (!success) markedForRemoval.add(location);
        }
        if (!markedForRemoval.isEmpty()) {
            managedSigns.removeAll(markedForRemoval);
            for (Location location : markedForRemoval) {
                if (location.getBlock().getType() != Material.AIR) location.getBlock().setType(Material.AIR);
            }
            saveManagedSigns();
        }
    }

    private boolean updateSign(Location location) {
        if (!(location.getBlock().getState() instanceof Sign sign)) return false;
        if (!sign.isPlaced()) return false;
        String arenaName = PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(2));
        if (!GameManager.getInstance().arenaExists(arenaName)) return false;

        if (!GameManager.getInstance().isArenaEnabled(arenaName)) {
            sign.getSide(Side.FRONT).line(3, Component.text("DISABLED", NamedTextColor.RED));
            sign.update(true);
            return true;
        }

        if (!GameManager.getInstance().isArenaFree(arenaName)) {
            sign.getSide(Side.FRONT).line(3, Component.text("FULL", NamedTextColor.RED));
            sign.update(true);
            return true;
        }

        List<?> spawnsRaw = CustomConfig.getFileConfiguration(arenaName).getList("trackSpawns");
        if (spawnsRaw == null) return false;
        int maxPlayers = spawnsRaw.size();
        int queuedPeople = GameManager.getInstance().getPeopleQueuedForArena(arenaName);
        sign.getSide(Side.FRONT).line(3, Component.text(queuedPeople + " / " + maxPlayers));
        sign.update(true);
        return true;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (managedSigns.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Component.text("This sign is protected by towerwars!"));
            return;
        }

        if (e.line(0) == null || e.line(1) == null) return;
        if (!PlainTextComponentSerializer.plainText().serialize(e.line(0)).equalsIgnoreCase("[TOWERWARS]")) return;

        if (!e.getPlayer().hasPermission("towerwars.placesign")) return;

        String arenaName = PlainTextComponentSerializer.plainText().serialize(e.line(1));
        if (!GameManager.getInstance().arenaExists(arenaName)) {
            e.getPlayer().sendMessage(Component.text("The arena \"" +  arenaName + "\" doesn't exist."));
            return;
        }

        e.line(0, Component.text("[TOWERWARS]", NamedTextColor.BLACK));
        e.line(1, Component.empty());
        e.line(2, Component.text(arenaName, NamedTextColor.WHITE));
        managedSigns.add(e.getBlock().getLocation());
        saveManagedSigns();
        Bukkit.getScheduler().runTaskLater(TowerWars.getPlugin(), () -> updateSign(e.getBlock().getLocation()), 1L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Block clickedBlock = e.getClickedBlock();
        if (!managedSigns.contains(clickedBlock.getLocation())) return;
        if (e.getAction().isLeftClick()) {
            if (e.getPlayer().hasPermission("towerwars.placesign")) {
                managedSigns.remove(clickedBlock.getLocation());
                saveManagedSigns();
            } else {
                e.setCancelled(true);
            }
            return;
        }
        if (!e.getAction().isRightClick()) return;
        if (!(clickedBlock.getState() instanceof Sign sign)) {
            managedSigns.remove(clickedBlock.getLocation());
            return;
        }

        e.setCancelled(true);

        if (!e.getPlayer().hasPermission("towerwars.usesign")) {
            e.getPlayer().sendMessage(Component.text("No permission! If you believe this is a mistake ask a server administrator to grant you towerwars.usesign!"));
            return;
        }

        TWPlayer twPlayer = TWPlayer.getTWPlayer(e.getPlayer().getUniqueId());
        String arenaName = PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(2));
        if (!GameManager.getInstance().isArenaFree(arenaName)) {
            e.getPlayer().sendMessage(Component.text("The arena is full!"));
            return;
        }
        if (GameManager.getInstance().isArenaEnabled(arenaName)) {
            GameManager.getInstance().joinQueue(twPlayer, arenaName);
        } else {
            e.getPlayer().sendMessage(Component.text("This arena is disabled!"));
        }
    }
}