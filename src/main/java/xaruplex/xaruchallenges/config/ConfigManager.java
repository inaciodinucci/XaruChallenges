package xaruplex.xaruchallenges.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
            createDefaultConfig();
        }

        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void createDefaultConfig() {
        config = plugin.getConfig();

        // Configuration header
        config.options().header("XaruChallenges Configuration\n\n" +
                "Database Configuration:\n" +
                "  database.enabled: true/false\n" +
                "  database.type: SQLite/MySQL\n" +
                "  database.sqlite.filename: challenges.db\n" +
                "  database.mysql.host: localhost\n" +
                "  database.mysql.port: 3306\n" +
                "  database.mysql.database: minecraft\n" +
                "  database.mysql.username: user\n" +
                "  database.mysql.password: pass\n");

        // Op Management
        config.addDefault("op-management", true);

        // Database Configuration
        config.addDefault("database.enabled", true);
        config.addDefault("database.type", "SQLite");

        // SQLite defaults
        config.addDefault("database.sqlite.filename", "challenges.db");

        // MySQL defaults
        config.addDefault("database.mysql.host", "localhost");
        config.addDefault("database.mysql.port", 3306);
        config.addDefault("database.mysql.database", "minecraft");
        config.addDefault("database.mysql.username", "user");
        config.addDefault("database.mysql.password", "pass");

        // Challenge Configurations
        addChallengeDefaults();

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    private void addChallengeDefaults() {
        // Piranha Challenge
        config.addDefault("Piranha.damage-outside-water", 2.0);
        config.addDefault("Piranha.allow-vehicles", false);
        config.addDefault("Piranha.underwater-effect", "WATER_BREATHING");

        List<String> piranhaDisallowedFood = new ArrayList<>();
        piranhaDisallowedFood.add("COD");
        piranhaDisallowedFood.add("COOKED_COD");
        piranhaDisallowedFood.add("SALMON");
        piranhaDisallowedFood.add("COOKED_SALMON");
        piranhaDisallowedFood.add("TROPICAL_FISH");
        piranhaDisallowedFood.add("PUFFERFISH");
        piranhaDisallowedFood.add("DRIED_KELP");
        piranhaDisallowedFood.add("KELP");
        config.addDefault("Piranha.disallowed-food", piranhaDisallowedFood);
        config.addDefault("Piranha.allowed-food", new ArrayList<String>());

        // Other challenges...
        config.addDefault("Vulture.allowed-food", "ROTTEN_FLESH");
        config.addDefault("NakedAndAfraid.armor-disabled", true);
        config.addDefault("NakedAndAfraid.drop-equipped-armor", true);
    }

    // Database Configuration Getters
    public boolean isDatabaseEnabled() {
        return config.getBoolean("database.enabled", true);
    }

    public String getDatabaseType() {
        return config.getString("database.type", "SQLite");
    }

    public String getSQLiteFilename() {
        return config.getString("database.sqlite.filename", "challenges.db");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "minecraft");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "user");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "pass");
    }

    // Existing configuration getters
    public boolean isOpManagementEnabled() {
        return config.getBoolean("op-management", true);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public List<Material> getMaterialList(String path) {
        List<String> stringList = config.getStringList(path);
        List<Material> materials = new ArrayList<>();

        for (String materialName : stringList) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                materials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in config: " + materialName);
            }
        }
        return materials;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }
}