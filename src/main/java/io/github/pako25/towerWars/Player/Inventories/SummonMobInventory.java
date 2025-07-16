package io.github.pako25.towerWars.Player.Inventories;

import io.github.pako25.towerWars.Arena.MobData.MobState;
import io.github.pako25.towerWars.Arena.MobData.MobStates;
import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Player.TWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
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

public class SummonMobInventory implements InventoryHolder {

    private final Inventory inventory;
    private final TWPlayer twPlayer;
    private final FileConfiguration cfg;
    private final JavaPlugin plugin;

    public SummonMobInventory(JavaPlugin plugin, TWPlayer twPlayer) {
        this.inventory = plugin.getServer().createInventory(this, 45);
        this.twPlayer = twPlayer;
        this.plugin = plugin;
        cfg = CustomConfig.getFileConfiguration("mobConfig");
        loadInventory();
    }

    public synchronized void loadInventory() {
        MobStates mobStates = twPlayer.getTrack().getMobStates();
        int i = 0;

        for (MobType key : MobType.values()) {
            MobState mobState = mobStates.getMobState(key);
            if (!mobState.isSummonable()) continue;

            boolean disabled = false;
            if (mobState.isAdvanced()) continue;
            if (mobState.getIncomeToUnlock() > twPlayer.getIncome()) disabled = true;

            if (mobState.getIncomeToPrestige() < twPlayer.getIncome()) { //naredi advanced form če ma dovolj incoma
                mobState = mobStates.getMobState(mobState.getAdvancedForm());
            }

            if (twPlayer.getStock() < 1) disabled = true;

            Material material = mobState.getMaterial();
            if (disabled) material = Material.BARRIER;

            ItemStack item = new ItemStack(material, disabled ? 1 : (twPlayer.getStock() > 0 ? twPlayer.getStock() : 1) );
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.removeAttributeModifier(Attribute.ARMOR);
                meta.removeAttributeModifier(Attribute.ARMOR_TOUGHNESS);

                meta.displayName(Component.text(mobState.getName() + mobState.getIncomeEvolutionText(twPlayer.getIncome()), NamedTextColor.AQUA));

                List<Component> lore = new ArrayList<>(List.of(
                        Component.text("Cost: ", NamedTextColor.GOLD).append(Component.text(mobState.getCost(twPlayer.getIncome()), NamedTextColor.YELLOW)),
                        Component.text("Health: ", NamedTextColor.GOLD).append(Component.text(mobState.getHealth(), NamedTextColor.YELLOW)),
                        Component.text("Speed: ", NamedTextColor.GOLD).append(Component.text(mobState.getSpeed(), NamedTextColor.YELLOW)),
                        Component.text("Income: ", NamedTextColor.GOLD).append(Component.text(mobState.getIncome(twPlayer.getIncome()), NamedTextColor.YELLOW))
                ));

                if (mobState.isAdvanced()) {
                    int incomeForNextEvolutionByIncome = mobState.getIncomeForNextEvolutionByIncome(twPlayer.getIncome());
                    lore.add(Component.text(" "));
                    if (incomeForNextEvolutionByIncome == 0) {
                        lore.add(Component.text("You have reached the max evolution!", NamedTextColor.YELLOW));
                    } else {
                        lore.add(Component.text("Reach " + incomeForNextEvolutionByIncome + " income to evolve!", NamedTextColor.YELLOW));
                    }
                    lore.add(Component.text(" "));
                    lore.add(Component.text("Stock: ", NamedTextColor.YELLOW).append(Component.text(twPlayer.getStock(), NamedTextColor.GOLD)));
                    lore.add(Component.text("Summoned: ", NamedTextColor.YELLOW).append(Component.text(mobState.getSummonCount(), NamedTextColor.GOLD)));
                    lore.add(Component.text("Summoned bonus: ", NamedTextColor.YELLOW));
                    String summonedBonus = mobState.getSummonedBonus();
                    for (String line : summonedBonus.split("//")) {
                        lore.add(Component.text("  " + line, NamedTextColor.GOLD));
                    }
                }

                if (!disabled)
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "MobType"), PersistentDataType.STRING, mobState.getMobType().name());

                meta.lore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(i, item);
            i++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void itemClick(MobType mobType) {
        boolean success = twPlayer.summonMob(mobType, twPlayer.getTrack().getMobStates().getMobState(mobType).getCost(twPlayer.getIncome()), twPlayer.getTrack().getMobStates().getMobState(mobType).getIncome(twPlayer.getIncome()));
        if (success) {
            loadInventory();
        } else {
            twPlayer.getPlayer().sendMessage(Component.text("Not enough gold!", NamedTextColor.RED));
        }
    }
}