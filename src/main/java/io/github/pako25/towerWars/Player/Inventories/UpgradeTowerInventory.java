package io.github.pako25.towerWars.Player.Inventories;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.Tower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpgradeTowerInventory implements InventoryHolder {

    private final Inventory inventory;
    private final TWPlayer twPlayer;
    private final FileConfiguration cfg;
    private final JavaPlugin plugin;
    private final Tower tower;
    private final Location location;

    public UpgradeTowerInventory(JavaPlugin plugin, TWPlayer twPlayer, Tower tower, Location location) {
        this.inventory = plugin.getServer().createInventory(this, 27);
        this.twPlayer = twPlayer;
        this.plugin = plugin;
        this.tower = tower;
        this.location = location;
        cfg = CustomConfig.getFileConfiguration("towerConfig");
        loadInventory();
    }

    private void loadInventory() {
        String key = tower.getTowerType().name();
        String type = cfg.getString(key + ".type");
        int maxLevel = 3;
        if (type.equals("support")) maxLevel = 2;
        int level = tower.getLevel();
        int prestige = tower.getPrestige();

        ItemStack showRangeItem = new ItemStack(Material.REDSTONE, 1);
        ItemMeta showRangeItemMeta = showRangeItem.getItemMeta();
        showRangeItemMeta.displayName(Component.text("Show range"));
        showRangeItemMeta.lore(List.of(
                Component.text("Display the tower range", NamedTextColor.GREEN),
                Component.text("with particles for 3 seconds!", NamedTextColor.GREEN)
        ));
        showRangeItem.setItemMeta(showRangeItemMeta);

        ItemStack statsItem = new ItemStack(Material.PAPER, 1);
        ItemMeta statsItemMeta = statsItem.getItemMeta();
        statsItemMeta.displayName(Component.text("Stats"));
        statsItemMeta.lore(List.of(
                Component.text("Shots: ", NamedTextColor.YELLOW).append(Component.text(tower.getShots())),
                Component.text("Damage: ", NamedTextColor.YELLOW).append(Component.text(tower.getDamageDealt())),
                Component.text("Kills: ", NamedTextColor.YELLOW).append(Component.text(tower.getKills()))
        ));
        statsItem.setItemMeta(statsItemMeta);

        ItemStack upgradeItem = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta upgradeItemMeta = upgradeItem.getItemMeta();
        upgradeItemMeta.displayName(Component.text(tower.getTowerType().name() + " - " + intToRoman(level), NamedTextColor.WHITE));
        if (level < maxLevel) {
            int upgradeCost = Tower.getTowerBuyCost(tower.getTowerType(), level + 1, prestige) - Tower.getTowerBuyCost(tower.getTowerType(), level, prestige);
            int newDPS = (int) (cfg.getInt(key + ".levels." + (level + 1) + ".damage") / cfg.getDouble(key + ".levels." + (level + 1) + ".reload"));
            List<Component> lore = new ArrayList<>(List.of(
                    Component.text(" "),
                    Component.text("Upgrade for ", NamedTextColor.YELLOW).append(Component.text(upgradeCost, NamedTextColor.GOLD)).append(Component.text(" gold", NamedTextColor.YELLOW)),
                    Component.text(" "),
                    Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".levels." + (level + 1) + ".damage"), NamedTextColor.GREEN)),
                    Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".levels." + (level + 1) + ".reload"), NamedTextColor.GREEN)),
                    Component.text("DPS: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage()/tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + newDPS, NamedTextColor.GREEN)),
                    Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(tower.getRange(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".levels." + (level + 1) + ".range"), NamedTextColor.GREEN)),
                    Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(tower.getSplash(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".levels." + (level + 1) + ".splash"), NamedTextColor.GREEN))
            ));
            String rawSpecial = cfg.getString(key + ".levels." + (level + 1) + ".special");
            if (rawSpecial != null && !rawSpecial.isEmpty()) {
                lore.add(Component.text(" "));
                lore.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
                for (String line : rawSpecial.split("//")) {
                    lore.add(Component.text(line, NamedTextColor.AQUA));
                }
            }
            upgradeItemMeta.lore(lore);
        } else {
            List<Component> lore = new ArrayList<>(List.of(
                    Component.text(""),
                    Component.text("MAX LEVEL", NamedTextColor.RED),
                    Component.text(" "),
                    Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage(), NamedTextColor.YELLOW)),
                    Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(tower.getReload(), NamedTextColor.YELLOW)),
                    Component.text("DPS: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage()/tower.getReload(), NamedTextColor.YELLOW)),
                    Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(tower.getRange(), NamedTextColor.YELLOW)),
                    Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(tower.getSplash(), NamedTextColor.YELLOW))
            ));
            if (level == maxLevel) {
                //ni prestiga
                String rawSpecial = cfg.getString(key + ".levels." + level + ".special");
                if (rawSpecial != null && !rawSpecial.isEmpty()) {
                    lore.add(Component.text(" "));
                    lore.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
                    for (String line : rawSpecial.split("//")) {
                        lore.add(Component.text(line, NamedTextColor.AQUA));
                    }
                }
                upgradeItemMeta.lore(lore);
            } else {
                //prestige zakupljen
                String rawSpecial = cfg.getString(key + ".prestiges." + prestige + ".special");
                if (rawSpecial != null && !rawSpecial.isEmpty()) {
                    lore.add(Component.text(" "));
                    lore.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
                    for (String line : rawSpecial.split("//")) {
                        lore.add(Component.text(line, NamedTextColor.AQUA));
                    }
                }
                upgradeItemMeta.lore(lore);
            }
        }
        upgradeItem.setItemMeta(upgradeItemMeta);

        ItemStack sellItem = new ItemStack(Material.EMERALD, 1);
        ItemMeta sellItemMeta = sellItem.getItemMeta();
        sellItemMeta.displayName(Component.text("Sell"));
        int sellCost = Tower.getTowerBuyCost(tower.getTowerType(), level, prestige) / 2;
        sellItemMeta.lore(List.of(
                Component.text("Sell it for ", NamedTextColor.GREEN).append(Component.text(sellCost, NamedTextColor.YELLOW)).append(Component.text("!", NamedTextColor.GREEN))
        ));
        sellItem.setItemMeta(sellItemMeta);

        ItemStack replaceItem = new ItemStack(Material.CRAFTING_TABLE, 1);
        ItemMeta replaceItemMeta = replaceItem.getItemMeta();
        replaceItemMeta.displayName(Component.text("Replace this tower!"));
        replaceItemMeta.lore(List.of(
                Component.text("Sell it to buy a new one!", NamedTextColor.GREEN)
        ));
        replaceItem.setItemMeta(replaceItemMeta);

        inventory.setItem(9, showRangeItem);
        inventory.setItem(10, statsItem);
        inventory.setItem(13, upgradeItem);
        inventory.setItem(16, sellItem);
        inventory.setItem(17, replaceItem);

        if (level >= maxLevel && type.equals("normal") && prestige == 0) {
            Material prestige1ItemMaterial = Material.valueOf(cfg.getString(key + ".prestiges.1.shop_material"));
            ItemStack prestige1Item = new ItemStack(prestige1ItemMaterial, 1);
            ItemMeta prestige1ItemMeta = prestige1Item.getItemMeta();
            prestige1ItemMeta.displayName(Component.text(cfg.getString(key + ".prestiges.1.name"), NamedTextColor.WHITE));
            int upgradeCost1 = cfg.getInt(key + ".prestiges.1.cost") - Tower.getTowerBuyCost(tower.getTowerType(), 3, 0);
            int newDPS1 = (int) (cfg.getInt(key + ".prestiges.1.damage") / cfg.getDouble(key + ".prestiges.1.reload"));
            List<Component> lore1 = new ArrayList<>(List.of(
                    Component.text(" "),
                    Component.text("Only one specialization is", NamedTextColor.YELLOW),
                    Component.text("available per tower!", NamedTextColor.YELLOW),
                    Component.text(" "),
                    Component.text("Upgrade for ", NamedTextColor.YELLOW).append(Component.text(upgradeCost1, NamedTextColor.GOLD)).append(Component.text(" gold", NamedTextColor.YELLOW)),
                    Component.text(" "),
                    Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.1.damage"), NamedTextColor.GREEN)),
                    Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.1.reload"), NamedTextColor.GREEN)),
                    Component.text("DPS: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage()/tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + newDPS1, NamedTextColor.GREEN)),
                    Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(tower.getRange(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.1.range"), NamedTextColor.GREEN)),
                    Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(tower.getSplash(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.1.splash"), NamedTextColor.GREEN))
            ));
            String rawSpecial1 = cfg.getString(key + ".prestiges.1.special");
            if (rawSpecial1 != null && !rawSpecial1.isEmpty()) {
                lore1.add(Component.text(" "));
                lore1.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
                for (String line : rawSpecial1.split("//")) {
                    lore1.add(Component.text(line, NamedTextColor.AQUA));
                }
            }
            prestige1ItemMeta.lore(lore1);
            prestige1Item.setItemMeta(prestige1ItemMeta);

            Material prestige2ItemMaterial = Material.valueOf(cfg.getString(key + ".prestiges.2.shop_material"));
            ItemStack prestige2Item = new ItemStack(prestige2ItemMaterial, 1);
            ItemMeta prestige2ItemMeta = prestige2Item.getItemMeta();
            prestige2ItemMeta.displayName(Component.text(cfg.getString(key + ".prestiges.2.name"), NamedTextColor.WHITE));
            int upgradeCost2 = cfg.getInt(key + ".prestiges.2.cost") - Tower.getTowerBuyCost(tower.getTowerType(), 3, 0);
            int newDPS2 = (int) (cfg.getInt(key + ".prestiges.2.damage") / cfg.getDouble(key + ".prestiges.2.reload"));
            List<Component> lore2 = new ArrayList<>(List.of(
                    Component.text(" "),
                    Component.text("Only one specialization is", NamedTextColor.YELLOW),
                    Component.text("available per tower!", NamedTextColor.YELLOW),
                    Component.text(" "),
                    Component.text("Upgrade for ", NamedTextColor.YELLOW).append(Component.text(upgradeCost2, NamedTextColor.GOLD)).append(Component.text(" gold", NamedTextColor.YELLOW)),
                    Component.text(" "),
                    Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.2.damage"), NamedTextColor.GREEN)),
                    Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.2.reload"), NamedTextColor.GREEN)),
                    Component.text("DPS: ", NamedTextColor.AQUA).append(Component.text(tower.getDamage()/tower.getReload(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + newDPS2, NamedTextColor.GREEN)),
                    Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(tower.getRange(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.2.range"), NamedTextColor.GREEN)),
                    Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(tower.getSplash(), NamedTextColor.YELLOW)).append(Component.text(" >>> " + cfg.getString(key + ".prestiges.2.splash"), NamedTextColor.GREEN))
            ));
            String rawSpecial2 = cfg.getString(key + ".prestiges.2.special");
            if (rawSpecial2 != null && !rawSpecial2.isEmpty()) {
                lore2.add(Component.text(" "));
                lore2.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
                for (String line : rawSpecial2.split("//")) {
                    lore2.add(Component.text(line, NamedTextColor.AQUA));
                }
            }
            prestige2ItemMeta.lore(lore2);
            prestige2Item.setItemMeta(prestige2ItemMeta);
            inventory.setItem(21, prestige1Item);
            inventory.setItem(23, prestige2Item);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void itemClick(ItemStack item) {
        String key = tower.getTowerType().name();
        String type = cfg.getString(key + ".type");

        if (item.getType() == Material.REDSTONE) {
            tower.showRange();
        }
        if (item.getType() == Material.EMERALD) {
            tower.sell();
            inventory.close();
        }
        if (type.equals("normal")) {
            if (item.getType() == Material.valueOf(cfg.getString(key + ".prestiges.1.shop_material"))) {
                int cost = Tower.getTowerBuyCost(tower.getTowerType(), 4, 1) - Tower.getTowerBuyCost(tower.getTowerType(), 3, 0);
                boolean success = twPlayer.buyForCoin(cost);
                if (success) {
                    tower.prestige(1);
                    reopen();
                } else {
                    notEnoughCoins();
                }
            }
            if (item.getType() == Material.valueOf(cfg.getString(key + ".prestiges.2.shop_material"))) {
                int cost = Tower.getTowerBuyCost(tower.getTowerType(), 4, 2) - Tower.getTowerBuyCost(tower.getTowerType(), 3, 0);
                boolean success = twPlayer.buyForCoin(cost);
                if (success) {
                    tower.prestige(2);
                    reopen();
                } else {
                    notEnoughCoins();
                }
            }
        }
        if (item.getType() == Material.CRAFTING_TABLE) {
            tower.sell();
            inventory.close();
            twPlayer.openPlaceTowerInventory(tower.getLocation());
        }
        if (item.getType() == Material.EXPERIENCE_BOTTLE) {
            int maxLevel = 3;
            if (type.equals("support")) maxLevel = 2;
            if (tower.getLevel() < maxLevel) {
                int cost = Tower.getTowerBuyCost(tower.getTowerType(), tower.getLevel() + 1, 0) - Tower.getTowerBuyCost(tower.getTowerType(), tower.getLevel(), 0);
                boolean success = twPlayer.buyForCoin(cost);
                if (success) {
                    tower.upgrade();
                    reopen();
                } else {
                    notEnoughCoins();
                }
            }
        }
    }

    private void reopen() {
        inventory.close();
        twPlayer.openTowerMenu(location);
    }
    private void notEnoughCoins() {
        inventory.close();
        twPlayer.getPlayer().sendMessage(Component.text("Not enough gold!", NamedTextColor.RED));
    }

    private String intToRoman(int i) {
        if (i == 1) return "I";
        if (i == 2) return "II";
        if (i == 3) return "III";
        if (i == 4) return "IV";
        return "";
    }
}