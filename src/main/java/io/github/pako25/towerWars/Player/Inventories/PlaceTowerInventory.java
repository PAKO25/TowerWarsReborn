package io.github.pako25.towerWars.Player.Inventories;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.Tower.TowerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaceTowerInventory implements InventoryHolder {

    private final Inventory inventory;
    private final TWPlayer twPlayer;
    private final FileConfiguration cfg;
    private final JavaPlugin plugin;

    public PlaceTowerInventory(JavaPlugin plugin, TWPlayer twPlayer) {
        this.inventory = plugin.getServer().createInventory(this, 54);
        this.twPlayer = twPlayer;
        this.plugin = plugin;
        cfg = CustomConfig.getFileConfiguration("towerConfig");
    }

    public void loadInventory() {
        int i = 0;
        for (String key : cfg.getKeys(false)) {
            String type = cfg.getString(key + ".type");
            if (type.equals("normal")) {
                Material levelsMaterial = Material.valueOf(cfg.getString(key + ".shop_material"));
                int income = twPlayer.getIncome();
                boolean level1Unlocked = cfg.getInt(key + ".levels.1.cost") < (income * 5);
                boolean level2Unlocked = cfg.getInt(key + ".levels.2.cost") < (income * 5);
                boolean level3Unlocked = cfg.getInt(key + ".levels.3.cost") < (income * 5);
                boolean prestige1Unlocked = cfg.getInt(key + ".prestiges.1.cost") < (income * 5);
                boolean prestige2Unlocked = cfg.getInt(key + ".prestiges.2.cost") < (income * 5);

                ItemStack level1Item = new ItemStack(level1Unlocked ? levelsMaterial : Material.BARRIER, 1);
                applyLevelsMeta(level1Item, key, 1);
                ItemStack level2Item = new ItemStack(level2Unlocked ? levelsMaterial : Material.BARRIER, 2);
                applyLevelsMeta(level2Item, key, 2);
                ItemStack level3Item = new ItemStack(level3Unlocked ? levelsMaterial : Material.BARRIER, 3);
                applyLevelsMeta(level3Item, key, 3);

                Material prestige1Material = Material.valueOf(cfg.getString(key + ".prestiges.1.shop_material"));
                ItemStack prestige1Item = new ItemStack(prestige1Unlocked ? prestige1Material : Material.BARRIER, 1);
                applyPrestigeLore(prestige1Item, key, 1);

                Material prestige2Material = Material.valueOf(cfg.getString(key + ".prestiges.2.shop_material"));
                ItemStack prestige2Item = new ItemStack(prestige2Unlocked ? prestige2Material : Material.BARRIER, 1);
                applyPrestigeLore(prestige2Item, key, 2);

                inventory.setItem(i, level1Item);
                inventory.setItem(i + 9, level2Item);
                inventory.setItem(i + 18, level3Item);
                inventory.setItem(i + 36, prestige1Item);
                inventory.setItem(i + 45, prestige2Item);
            }
            if (type.equals("support")) {
                int income = twPlayer.getIncome();
                boolean level1Unlocked = cfg.getInt(key + ".levels.1.cost") < (income * 5);
                boolean level2Unlocked = cfg.getInt(key + ".levels.2.cost") < (income * 5);

                Material levelsMaterial = Material.valueOf(cfg.getString(key + ".shop_material"));
                ItemStack level1Item = new ItemStack(level1Unlocked ? levelsMaterial : Material.BARRIER, 1);
                applyLevelsMeta(level1Item, key, 1);
                ItemStack level2Item = new ItemStack(level2Unlocked ? levelsMaterial : Material.BARRIER, 2);
                applyLevelsMeta(level2Item, key, 2);

                inventory.setItem(i, level1Item);
                inventory.setItem(i + 9, level2Item);
            }
            i++;
        }
    }

    private void applyLevelsMeta(ItemStack item, String key, int level) {
        String levelText = " I";
        if (level == 2) levelText = " II";
        if (level == 3) levelText = " III";

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(cfg.getString(key + ".name") + levelText, NamedTextColor.YELLOW));
        List<Component> lore = new ArrayList<>(List.of(
                Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".levels." + level + ".damage"), NamedTextColor.YELLOW)),
                Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".levels." + level + ".reload"), NamedTextColor.YELLOW)),
                Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".levels." + level + ".range"), NamedTextColor.YELLOW)),
                Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".levels." + level + ".splash"), NamedTextColor.YELLOW)),
                Component.text(" "),
                Component.text("Cost: ", NamedTextColor.YELLOW).append(Component.text(cfg.getString(key + ".levels." + level + ".cost"), NamedTextColor.GOLD))
        ));
        String rawSpecial = cfg.getString(key + ".levels." + level + ".special");
        if (rawSpecial != null && !rawSpecial.isEmpty()) {
            lore.add(Component.text(" "));
            lore.add(Component.text("Special: ", NamedTextColor.LIGHT_PURPLE));
            for (String line : rawSpecial.split("//")) {
                lore.add(Component.text(line, NamedTextColor.AQUA));
            }
        }
        meta.lore(lore);
        applyMetaStorage(meta, key, level, 0);
        item.setItemMeta(meta);
    }

    private void applyPrestigeLore(ItemStack item, String key, int prestige) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(cfg.getString(key + ".prestiges." + prestige + ".name"), NamedTextColor.YELLOW));
        List<Component> lore = new ArrayList<>(List.of(
                Component.text("Damage: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".prestiges." + prestige + ".damage"), NamedTextColor.YELLOW)),
                Component.text("Reload: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".prestiges." + prestige + ".reload"), NamedTextColor.YELLOW)),
                Component.text("Range: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".prestiges." + prestige + ".range"), NamedTextColor.YELLOW)),
                Component.text("Splash: ", NamedTextColor.AQUA).append(Component.text(cfg.getString(key + ".prestiges." + prestige + ".splash"), NamedTextColor.YELLOW)),
                Component.text(" "),
                Component.text("Cost: ", NamedTextColor.YELLOW).append(Component.text(cfg.getString(key + ".prestiges." + prestige + ".cost"), NamedTextColor.GOLD)),
                Component.text(" "),
                Component.text("Special: ", NamedTextColor.LIGHT_PURPLE)
        ));
        String rawSpecial = cfg.getString(key + ".prestiges." + prestige + ".special");
        for (String line : rawSpecial.split("//")) {
            lore.add(Component.text(line, NamedTextColor.AQUA));
        }
        meta.lore(lore);
        applyMetaStorage(meta, key, 4, prestige);
        item.setItemMeta(meta);
    }

    private void applyMetaStorage(ItemMeta meta, String key, int level, int prestige) {
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "TowerType"), PersistentDataType.STRING, key);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Level"), PersistentDataType.INTEGER, level);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Prestige"), PersistentDataType.INTEGER, prestige);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void itemClick(ItemStack item) {
        if (item.getType() == Material.BARRIER) return;

        ItemMeta meta = item.getItemMeta();
        TowerType towerType = TowerType.valueOf(meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "TowerType"), PersistentDataType.STRING));
        int level = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "Level"), PersistentDataType.INTEGER);
        int prestige = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "Prestige"), PersistentDataType.INTEGER);

        boolean success = twPlayer.placeTower(towerType, level, prestige, meta.displayName());
        if (success) {
            inventory.close();
        }
    }
}