package io.github.pako25.towerWars.Player.Listeners;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.Editor.EditorOptionsInventory;
import io.github.pako25.towerWars.Player.Inventories.PlaceTowerInventory;
import io.github.pako25.towerWars.Player.Inventories.SummonMobInventory;
import io.github.pako25.towerWars.Player.Inventories.UpgradeTowerInventory;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;

        TWPlayer twPlayer = TWPlayer.getTWPlayer(event.getWhoClicked().getUniqueId());
        if (twPlayer == null) return;

        if (twPlayer.isInEditor() || twPlayer.isInLobby()) {
            event.setCancelled(true);
            if (inventory.getHolder(false) instanceof EditorOptionsInventory editorOptionsInventory) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null)
                    editorOptionsInventory.itemClick(event.getCurrentItem(), event.isLeftClick(), event.isRightClick());
            }
            return;
        }

        if (!twPlayer.isInGame()) return;
        event.setCancelled(true);

        if (inventory.getHolder(false) instanceof SummonMobInventory summonMobInventory) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null) {
                ItemMeta meta = clicked.getItemMeta();
                String storedMobType = meta.getPersistentDataContainer().get(new NamespacedKey(TowerWars.getPlugin(), "MobType"), PersistentDataType.STRING);
                if (storedMobType == null || storedMobType.isEmpty()) return;
                MobType mobType = MobType.valueOf(storedMobType);
                summonMobInventory.itemClick(mobType);
            }
        }
        if (inventory.getHolder(false) instanceof PlaceTowerInventory placeTowerInventory) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null) {
                placeTowerInventory.itemClick(clicked);
            }
        }
        if (inventory.getHolder(false) instanceof UpgradeTowerInventory upgradeTowerInventory) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null) {
                upgradeTowerInventory.itemClick(clicked);
            }
        }
    }
}

