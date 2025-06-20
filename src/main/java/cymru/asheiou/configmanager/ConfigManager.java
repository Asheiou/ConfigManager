package cymru.asheiou.configmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class ConfigManager {
    JavaPlugin plugin;
    boolean purge;

    /*
    The main class of the library.
    @param plugin: The JavaPlugin
    @param purge: Whether the plugin should purge values found in the default config but not in the user config.
     */
    public ConfigManager(JavaPlugin plugin, boolean purge) {
        this.plugin = plugin;
        this.purge = purge;
    }

    Yaml yaml = new Yaml();

    // Recursively finds all missing keys (dot notation) in reference that are missing from target
    @SuppressWarnings("unchecked")
    public static Set<String> findMissingKeys(Map<String, Object> reference, Map<String, Object> target, String prefix) {
        Set<String> missing = new HashSet<>();
        for (String key : reference.keySet()) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Object refVal = reference.get(key);
            Object tgtVal = target != null ? target.get(key) : null;

            if (target == null || !target.containsKey(key)) {
                // If the key is missing entirely, add all subkeys if it's a map, else the key itself
                if (refVal instanceof Map) {
                    missing.addAll(getAllPaths((Map<String, Object>) refVal, path));
                } else {
                    missing.add(path);
                }
            } else if (refVal instanceof Map && tgtVal instanceof Map) {
                // Both are maps, go deeper
                missing.addAll(findMissingKeys((Map<String, Object>) refVal, (Map<String, Object>) tgtVal, path));
            } else if (refVal instanceof Map && !(tgtVal instanceof Map)) {
                // Type mismatch, treat as all subkeys missing
                missing.addAll(getAllPaths((Map<String, Object>) refVal, path));
            }
            // If refVal is not a map, and key exists, do nothing (exists and is a leaf)
        }
        return missing;
    }

    // Helper: Get all key paths in a nested map
    @SuppressWarnings("unchecked")
    private static Set<String> getAllPaths(Map<String, Object> map, String prefix) {
        Set<String> paths = new HashSet<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            String path = prefix + "." + key;
            if (value instanceof Map) {
                paths.addAll(getAllPaths((Map<String, Object>) value, path));
            } else {
                paths.add(path);
            }
        }
        return paths;
    }

    // Set value at given dotted path in Bukkit config
    private void setConfigValue(String key, Object value) {
        plugin.getConfig().set(key, value);
    }

    // Get value by path from nested Map
    @SuppressWarnings("unchecked")
    private Object getValueByPath(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }

    /*
    (Re)load the config. This can be called whenever, although it is blocking.
    Returns an Integer[], [0] is the number of values added, [1] is the number removed.
    If a config file is not found or unparsable this function will log it in the console and return {-1, -1}.
     */
    public Integer[] loadConfig() {
        FileInputStream userInput;
        File userConfigFile;
        try {
            userConfigFile = new File(plugin.getDataFolder(), "config.yml");
            userInput = new FileInputStream(userConfigFile);
        } catch (FileNotFoundException e) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Config file not found! Creating one.");
            return new Integer[]{-1, -1};
        }

        InputStream defaultInput = getClass().getClassLoader().getResourceAsStream("config.yml");

        Map<String, Object> userConfig;
        try {
            userConfig = yaml.load(userInput);
        } catch (ScannerException e) {
            String brokenConfigString = plugin.getDataFolder() + File.separator + "config-broken" + UUID.randomUUID() + ".yml";
            try {
                Files.copy(userConfigFile.toPath(), (new File(brokenConfigString).toPath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            userConfigFile.delete();
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Config file unreadable! Creating a new one. Your broken config can be found at " + brokenConfigString);
            return new Integer[]{-1, -1};
        }

        Map<String, Object> defaultConfig;
        try {
            defaultConfig = yaml.load(defaultInput);
        } catch (ScannerException e) {
            plugin.getLogger().severe("FATAL: Internal config.yml not found! Please check your build. Disabling.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return new Integer[]{-1, -1};
        }

        try {
            userInput.close();
            defaultInput.close();
        } catch (IOException e) {
            plugin.getLogger().warning("InputStreams not successfully closed! This may cause a memory leak.");
        }

        if (userConfig == null) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Config file empty! Creating a new one.");
            plugin.reloadConfig();
            return new Integer[]{-1, -1};
        }

        int amountRemoved = 0;
        if (purge) {
            for (String uniqueKey : findMissingKeys(userConfig, defaultConfig, "")) {
                setConfigValue(uniqueKey, null); // Remove keys not in default
                amountRemoved++;
            }
        }

        int amountAdded = 0;
        for (String uniqueKey : findMissingKeys(defaultConfig, userConfig, "")) {
            Object value = getValueByPath(defaultConfig, uniqueKey);
            setConfigValue(uniqueKey, value);
            amountAdded++;
        }

        if (amountAdded > 0 || amountRemoved > 0) {
            plugin.saveConfig();
            plugin.getLogger().info(amountAdded + " config value"
                    + (amountAdded == 1 ? " " : "s ") + "added, " + amountRemoved + " removed.");
        }
        plugin.reloadConfig();
        return new Integer[]{amountAdded, amountRemoved};
    }
}