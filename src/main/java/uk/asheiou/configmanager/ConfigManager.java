package uk.asheiou.configmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class ConfigManager {
    JavaPlugin plugin;
    boolean purge;

    /**
     * The main class of the plugin that contains its functionality.
     *
     * @param plugin   Your plugin
     * @param purge Whether the plugin should delete config.yml keys that aren't present in the resources config.yml
     */
    public ConfigManager(JavaPlugin plugin, boolean purge) {
        this.plugin = plugin;
        this.purge = purge;
    }

    Yaml yaml = new Yaml();

    /**
     * A function to return all keys present in one Map but not the other
     *
     * @param first The Map<\String, Object> you want to use as a reference
     * @param second The Map<\String, Object> you want to test
     * @return A Set<\String> of all keys present in first that are not present in second
     */
    public static Set<String> checkUniqueKeys(
            Map<String, Object> first,
            Map<String, Object> second) {

        return first.keySet().stream()
                .filter(key -> !second.containsKey(key))
                .collect(Collectors.toSet());
    }

    /**
     * This plugin checks the user's config.yml against the internal config.yml.
     * It will fail and disable the plugin if it cannot locate an internal config.yml in its resources.
     * It currently cannot read or correct nested YAML values (i.e. foo.bar.value) unless the top layer is missing.
     *
     * @return An Integer[] saying how many keys were [0] added and [1] removed. [0] = -1 means the config has been reset due to it being missing or unreadable (if it's unreadable, it will be copied into a config-broken.yml)
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

        InputStream defaultInput;
        defaultInput = getClass().getClassLoader().getResourceAsStream("config.yml");

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
            for (String uniqueKey : checkUniqueKeys(userConfig, defaultConfig)) {
                plugin.getConfig().set(uniqueKey, null); // remove keys that are in the user-config and not default
                amountRemoved++;
            }
        }

        int amountAdded = 0;
        for (String uniqueKey : checkUniqueKeys(defaultConfig, userConfig)) {
            plugin.getConfig().set(uniqueKey, defaultConfig.get(uniqueKey));
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
