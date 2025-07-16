package io.github.pako25.towerWars.Player.Inventories;

import io.github.pako25.towerWars.GameManagment.PlayerStats;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsInventory implements InventoryHolder {

    private final Inventory inventory;
    private final UUID uuid;

    public StatsInventory(TWPlayer twPlayer) {
        this.inventory = TowerWars.getPlugin().getServer().createInventory(this, 45);
        this.uuid = twPlayer.getPlayer().getUniqueId();
        loadInventory();
    }

    public void loadInventory() {
        PlayerStats stats = PlayerStats.getStats(uuid);

        // games won
        ItemStack won = new ItemStack(Material.GREEN_WOOL);
        ItemMeta wonMeta = won.getItemMeta();
        wonMeta.displayName(Component.text("Games Won", NamedTextColor.GREEN));
        List<Pair<String, Integer>> winsLeaderBoard = PlayerStats.getGames_wonLeaderBoard();
        ArrayList<Component> wonLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getGames_won(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < winsLeaderBoard.size(); i++) {
            Pair<String, Integer> record = winsLeaderBoard.get(i);
            wonLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        wonMeta.lore(wonLore);
        won.setItemMeta(wonMeta);
        inventory.setItem(11, won);

        // Games Lost
        ItemStack lost = new ItemStack(Material.RED_WOOL);
        ItemMeta lostMeta = lost.getItemMeta();
        lostMeta.displayName(Component.text("Games Lost", NamedTextColor.RED));
        List<Pair<String, Integer>> lostLeaderBoard = PlayerStats.getGames_lostLeaderBoard();
        ArrayList<Component> lostLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getGames_lost(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < lostLeaderBoard.size(); i++) {
            Pair<String, Integer> record = lostLeaderBoard.get(i);
            lostLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        lostMeta.lore(lostLore);
        lost.setItemMeta(lostMeta);
        inventory.setItem(13, lost);

        // Mob Kills
        ItemStack kills = new ItemStack(Material.IRON_SWORD);
        ItemMeta killsMeta = kills.getItemMeta();
        killsMeta.displayName(Component.text("Mob Kills", NamedTextColor.DARK_GREEN));
        List<Pair<String, Integer>> killsLeaderBoard = PlayerStats.getMob_killsLeaderBoard();
        ArrayList<Component> killsLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getMob_kills(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < killsLeaderBoard.size(); i++) {
            Pair<String, Integer> record = killsLeaderBoard.get(i);
            killsLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        killsMeta.lore(killsLore);
        kills.setItemMeta(killsMeta);
        inventory.setItem(15, kills);

        // Towers Placed
        ItemStack towers = new ItemStack(Material.OAK_FENCE);
        ItemMeta towersMeta = towers.getItemMeta();
        towersMeta.displayName(Component.text("Towers Placed", NamedTextColor.BLUE));
        List<Pair<String, Integer>> towersLeaderBoard = PlayerStats.getTowers_placedLeaderBoard();
        ArrayList<Component> towersLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getTowers_placed(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < towersLeaderBoard.size(); i++) {
            Pair<String, Integer> record = towersLeaderBoard.get(i);
            towersLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        towersMeta.lore(towersLore);
        towers.setItemMeta(towersMeta);
        inventory.setItem(29, towers);

        // Gold Spent
        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldMeta = gold.getItemMeta();
        goldMeta.displayName(Component.text("Gold Spent", NamedTextColor.GOLD));
        List<Pair<String, Integer>> goldLeaderBoard = PlayerStats.getGold_spentLeaderBoard();
        ArrayList<Component> goldLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getGold_spent(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < goldLeaderBoard.size(); i++) {
            Pair<String, Integer> record = goldLeaderBoard.get(i);
            goldLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        goldMeta.lore(goldLore);
        gold.setItemMeta(goldMeta);
        inventory.setItem(31, gold);

        // Mobs Sent
        ItemStack sent = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta sentMeta = sent.getItemMeta();
        sentMeta.displayName(Component.text("Mobs Sent", NamedTextColor.DARK_PURPLE));
        List<Pair<String, Integer>> sentLeaderBoard = PlayerStats.getMobs_sentLeaderBoard();
        ArrayList<Component> sentLore = new ArrayList<>(List.of(
                Component.text("You: " + stats.getMobs_sent(), NamedTextColor.WHITE),
                Component.empty()
        ));
        for (int i = 0; i < sentLeaderBoard.size(); i++) {
            Pair<String, Integer> record = sentLeaderBoard.get(i);
            sentLore.add(Component.text((i + 1) + ". " + record.getLeft() + " (" + record.getRight() + ")", NamedTextColor.GRAY));
        }
        sentMeta.lore(sentLore);
        sent.setItemMeta(sentMeta);
        inventory.setItem(33, sent);

    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}