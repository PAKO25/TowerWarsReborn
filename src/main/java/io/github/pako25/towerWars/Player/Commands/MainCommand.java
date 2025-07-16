package io.github.pako25.towerWars.Player.Commands;

import io.github.pako25.towerWars.GameManagment.GameManager;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements TabExecutor {

    private JoinGame joinGameHandler = new JoinGame();
    private LeaveGame leaveGameHandler = new LeaveGame();
    private ArenaCommand arenaCommandHandler = new ArenaCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /towerwars <join|leave|forcestart|arena>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());

        switch (subcommand) {
            case "join":
                joinGameHandler.onCommand(twPlayer, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "leave":
                leaveGameHandler.onCommand(twPlayer, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "arena":
                arenaCommandHandler.onCommand(twPlayer,  Arrays.copyOfRange(args, 1, args.length));
                break;

            case "forcestart":
                if (!twPlayer.getPlayer().hasPermission("towerwars.forcestart")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return true;
                }
                if (twPlayer.isInEditor()) {
                    twPlayer.getPlayer().sendMessage(Component.text("You are in arena configuration mode. Quit the mode and try again."));
                    return true;
                }
                if (twPlayer.isInGame()) {
                    twPlayer.getPlayer().sendMessage(Component.text("The game is ongoing."));
                    return true;
                }
                if (args.length != 1) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars forcestart"));
                    return true;
                }
                GameManager.getInstance().forceStart(twPlayer);
                break;

            case "debug":
                if (!twPlayer.getPlayer().hasPermission("towerwars.debug")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return true;
                }
                if (args.length != 1) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars debug"));
                    return true;
                }
                ItemStack debugItem = new ItemStack(Material.STICK, 1);
                ItemMeta debugItemMeta = debugItem.getItemMeta();
                debugItemMeta.displayName(Component.text("Debug stick"));
                debugItem.setItemMeta(debugItemMeta);
                player.getInventory().addItem(debugItem);
                break;

            case "increaseincome":
                if (!twPlayer.getPlayer().hasPermission("towerwars.debug")) {
                    twPlayer.getPlayer().sendMessage(Component.text("No permission."));
                    return true;
                }
                if (args.length != 2) {
                    twPlayer.getPlayer().sendMessage(Component.text("Invalid arguments. Use /towerwars increaseincome <amount>"));
                    return true;
                }
                if (!twPlayer.isInGame()) {
                    twPlayer.getPlayer().sendMessage(Component.text("You need to be in a game!"));
                    return true;
                }
                String incomeString = args[1];
                int income = Integer.parseInt(incomeString);
                twPlayer.increaseIncome(income);
                break;

            case "help":
                player.sendMessage(Component.text("Use /towerwars join <arena> to join a game"));
                player.sendMessage(Component.text("Use /towerwars leave to leave a game"));
                player.sendMessage(Component.text("You can check available arenas using /towerwars arena list"));
                player.sendMessage(Component.text("If you can not perform any of those actions ask the server administrator to give you towerwars.play and towerwars.list permissions."));
                break;

            default:
                player.sendMessage("Unknown subcommand: " + subcommand + ". Try /towerwars help");
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return List.of();
        TWPlayer twPlayer = TWPlayer.getTWPlayer(player.getUniqueId());

        return switch (args.length > 0 ? args[0] : "") {
            case "join" -> joinGameHandler.onTabComplete(twPlayer, Arrays.copyOfRange(args, 1, args.length));
            case "arena" -> arenaCommandHandler.onTabComplete(twPlayer, Arrays.copyOfRange(args, 1, args.length));
            default -> {
                List<String> availableSubcommands = new ArrayList<>(List.of("help"));
                if (player.hasPermission("towerwars.play")) availableSubcommands.addAll(List.of("join", "leave"));
                if (player.hasPermission("towerwars.forcestart")) availableSubcommands.add("forcestart");
                if (player.hasPermission("towerwars.debug")) availableSubcommands.addAll(List.of("debug", "increaseincome"));
                if (player.hasPermission("towerwars.configure") || player.hasPermission("towerwars.list")) availableSubcommands.add("arena");
                yield availableSubcommands.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
        };
    }
}