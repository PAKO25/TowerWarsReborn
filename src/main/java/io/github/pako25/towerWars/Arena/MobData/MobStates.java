package io.github.pako25.towerWars.Arena.MobData;

import io.github.pako25.towerWars.Arena.MobType;
import io.github.pako25.towerWars.CustomConfig;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MobStates {
    private HashMap<MobType, MobState> mobStates = new HashMap<>();
    private float powerCreepHealthMultiplyer = 1;

    public MobStates() {
        loadDefaultMobStates();
    }

    private void loadDefaultMobStates() {
        FileConfiguration cfg = CustomConfig.getFileConfiguration("mobConfig");

        for (String key : cfg.getKeys(false)) {
            Material material = Material.valueOf(cfg.getString(key + ".shop_material"));
            String name = cfg.getString(key + ".name");
            int cost = cfg.getInt(key + ".cost");
            int health = cfg.getInt(key + ".health");
            double speed = cfg.getDouble(key + ".speed");
            int income = cfg.getInt(key + ".income");
            String entityTypeString = cfg.getString(key + ".entity_type");
            String category = cfg.getString(key + ".category");
            boolean unsummonable = cfg.getBoolean(key + ".unsummonable");
            MobType mobType = MobType.valueOf(key);

            mobStates.put(mobType, new MobState(material, name, cost, health, speed, income, category.equals("advanced") ? true : false, mobType, EntityType.valueOf(entityTypeString), cfg, !unsummonable, this));
        }
    }

    public MobState getMobState(MobType mobType) {
        return mobStates.get(mobType);
    }
    public Set<Map.Entry<MobType, MobState>> getEntrySet() {
        return mobStates.entrySet();
    }

    public float getPowerCreepHealthMultiplyer() {
        return powerCreepHealthMultiplyer;
    }

    public void multiplyPowerCreepMultiplyer(float multiplyer) {
        powerCreepHealthMultiplyer = powerCreepHealthMultiplyer * (1 + multiplyer);
    }
}

