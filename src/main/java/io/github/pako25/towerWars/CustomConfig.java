package io.github.pako25.towerWars;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CustomConfig {
    private final File file;
    private FileConfiguration customFile;
    private static final Map<String, CustomConfig> customConfigMap = new HashMap<>();
    private final JavaPlugin plugin;
    private final String configFilePath;

    private CustomConfig(String configFileName) {
        this.plugin = TowerWars.getPlugin();
        this.configFilePath = configFileName + ".yml";
        file = new File(plugin.getDataFolder(), configFilePath);

        if (!file.exists()) {
            InputStream resourceStream = plugin.getResource(configFilePath);
            if (resourceStream != null) {
                plugin.saveResource(configFilePath, false);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Couldn't create an empty config file. " + e.getMessage());
                }
            }
        }

        customFile = YamlConfiguration.loadConfiguration(file);

        // Load defaults from resource stream, so they are available as defaults
        try (InputStream defaultStream = plugin.getResource(configFilePath)) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                customFile.setDefaults(defaultConfig);
                customFile.options().copyDefaults(true);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Couldn’t load defaults for " + configFilePath + ": " + e.getMessage());
        }

        save();
    }

    public static FileConfiguration getFileConfiguration(String configFileName) {
        return customConfigMap.get(configFileName).getCustomFile();
    }

    public static CustomConfig getCustomConfig(String configFileName) {
        return customConfigMap.get(configFileName);
    }

    public static void setup(String configFileName) {
        if (customConfigMap.containsKey(configFileName)) {
            System.out.println("INVALID SETUP!!!!! configuration already present -> reloading");
            customConfigMap.get(configFileName).reload();
        } else {
            CustomConfig customConfig = new CustomConfig(configFileName);
            customConfigMap.put(configFileName, customConfig);
        }
    }

    public FileConfiguration getCustomFile() {
        return customFile;
    }

    public void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reload();
    }

    public void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);

        try (InputStream defaultStream = plugin.getResource(configFilePath)) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                customFile.setDefaults(defaultConfig);
                customFile.options().copyDefaults(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}