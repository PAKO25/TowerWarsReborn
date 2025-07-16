package io.github.pako25.towerWars.Player.Commands;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Editor.ArenaEditor;
import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ArenaCommand implements SubcommandHandler {
    @Override
    public void onCommand(TWPlayer twPlayer, String[] args) {

        if (args.length == 0) {
            twPlayer.getPlayer().sendMessage("Usage: /towerwars arena <subcommand>");
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "list":
                if (!twPlayer.getPlayer().hasPermission("towerwars.list")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return;
                }
                if (args.length != 1) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars arena list"));
                    return;
                }
                List<String> allArenas = GameManager.getInstance().getAllArenas();
                List<Component> statuses = new ArrayList<>();
                for (String arenaName : allArenas) {
                    FileConfiguration cfg = CustomConfig.getFileConfiguration(arenaName);
                    boolean enabled = cfg.getBoolean("enabled");
                    boolean available = GameManager.getInstance().isArenaFree(arenaName);
                    NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
                    statuses.add(Component.text(arenaName, NamedTextColor.WHITE).append(Component.text("(", NamedTextColor.GRAY)).append(Component.text(enabled ? "ENABLED" : "DISABLED", color)).append(Component.text(") (", NamedTextColor.GRAY)).append(Component.text(available ? "FREE" : "OCCUPIED", available ? NamedTextColor.GREEN : NamedTextColor.RED)).append(Component.text(")", NamedTextColor.GRAY)));
                    if (!allArenas.getLast().equals(arenaName)) statuses.add(Component.newline());
                }
                twPlayer.getPlayer().sendMessage(Component.text("Loaded arenas: ").appendNewline().append(statuses));
                break;

            case "configure":
                if (!twPlayer.getPlayer().hasPermission("towerwars.configure")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return;
                }
                if (twPlayer.isInGame() || twPlayer.isInLobby()) {
                    twPlayer.getPlayer().sendMessage(Component.text("Can not open the arena editor while you are in a game."));
                    return;
                }
                if (twPlayer.isInEditor()) {
                    twPlayer.getPlayer().sendMessage(Component.text("You are already in editing mode. Save the current configuration first."));
                    return;
                }
                if (args.length != 2) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars arena configure <arena>"));
                    return;
                }
                ArenaEditor.newInstance(args[1], twPlayer);
                break;

            case "saveconfiguration":
                if (!twPlayer.getPlayer().hasPermission("towerwars.configure")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return;
                }
                if (args.length != 1) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars arena list"));
                    return;
                }
                if (!twPlayer.isInEditor()) {
                    twPlayer.getPlayer().sendMessage(Component.text("You are not in editing mode."));
                    return;
                }
                ArenaEditor.closeInstanceByPlayer(twPlayer, true);
                break;

            case "discardconfiguration":
                if (!twPlayer.getPlayer().hasPermission("towerwars.configure")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return;
                }
                if (args.length != 1) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars arena list"));
                    return;
                }
                if (!twPlayer.isInEditor()) {
                    twPlayer.getPlayer().sendMessage(Component.text("You are not in editing mode."));
                    return;
                }
                ArenaEditor.closeInstanceByPlayer(twPlayer, false);
                break;

            default:
                twPlayer.getPlayer().sendMessage("Unknown subcommand: " + subcommand);
                break;
        }

    }

    @Override
    public List<String> onTabComplete(TWPlayer twPlayer, String[] args) {

        if (args.length == 1) {
            List<String> availableSubcommands = new ArrayList<>();
            if (twPlayer.getPlayer().hasPermission("towerwars.configure")) {
                availableSubcommands.addAll(List.of("configure", "saveconfiguration", "discardconfiguration"));
            }
            if (twPlayer.getPlayer().hasPermission("towerwars.list")) {
                availableSubcommands.add("list");
            }
            return availableSubcommands.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length != 0 && args[0].equals("configure") && twPlayer.getPlayer().hasPermission("towerwars.configure"))
            return GameManager.getInstance().getAllArenas().stream().filter(arenaName -> GameManager.getInstance().isArenaFree(arenaName) && arenaName.toLowerCase().startsWith(args[1].toLowerCase())).toList();

        return List.of();
    }
}