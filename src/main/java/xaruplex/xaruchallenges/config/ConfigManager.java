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

    public void reloadConfig() {
        // Reload the configuration from the file
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
        config.addDefault("Piranha.disallowed-food", List.of("COD", "COOKED_COD", "SALMON", "COOKED_SALMON", "TROPICAL_FISH", "PUFFERFISH", "DRIED_KELP"));
        config.addDefault("Piranha.allowed-food", new ArrayList<String>());

        // Vampire Challenge
        config.addDefault("Vampire.damage-in-sunlight", 1.0);
        config.addDefault("Vampire.light-level-threshold", 12);
        config.addDefault("Vampire.allowed-food", List.of("GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "POTION"));
        config.addDefault("Vampire.forbidden-potions", List.of("FIRE_RESISTANCE"));

        // Daredevil Challenge
        config.addDefault("Daredevil.blindness-effect", true);

        // HollowBones Challenge
        config.addDefault("HollowBones.max-health", 2);

        // CaseOh Challenge
        config.addDefault("CaseOh.sprint-disabled", true);

        // Chinchilla Challenge
        config.addDefault("Chinchilla.rain-damage", 1.0);
        config.addDefault("Chinchilla.rain-damage-interval", 20);
        config.addDefault("Chinchilla.allowed-food", List.of("COD", "COOKED_COD", "SALMON", "COOKED_SALMON", "TROPICAL_FISH", "PUFFERFISH"));

        // Prasinophobia Challenge
        config.addDefault("Prasinophobia.disallowed-blocks", List.of("GRASS_BLOCK", "TALL_GRASS", "LEAVES", "VINE"));

        // Mole Challenge
        config.addDefault("Mole.allowed-blocks", List.of("DIRT", "COARSE_DIRT", "SAND"));

        // OreDeath Challenge
        config.addDefault("OreDeath.ore-list", List.of(
                "IRON_ORE", "RAW_IRON", "DEEPSLATE_IRON_ORE", "RAW_IRON_BLOCK", "IRON_BLOCK", "IRON_INGOT", "IRON_NUGGET",
                "GOLD_ORE", "RAW_GOLD", "DEEPSLATE_GOLD_ORE", "RAW_GOLD_BLOCK", "GOLD_BLOCK", "GOLD_INGOT", "NETHER_GOLD_ORE", "GOLD_NUGGET",
                "ANCIENT_DEBRIS", "NETHERITE_INGOT", "DIAMOND_ORE", "DIAMOND", "DEEPSLATE_DIAMOND_ORE", "EMERALD_ORE", "EMERALD",
                "COPPER_ORE", "RAW_COPPER_INGOT", "COPPER_INGOT", "REDSTONE_ORE", "REDSTONE_BLOCK", "LAPIS_ORE", "LAPIS_BLOCK", "LAPIS_LAZULI",
                "DEEPSLATE_LAPIS_ORE", "COAL_ORE", "COAL", "COAL_BLOCK", "DEEPSLATE_COAL_ORE", "AMETHYST_BLOCK", "AMETHYST_SHARD",
                "SMALL_AMETHYST_BUD", "MEDIUM_AMETHYST_BUD", "LARGE_AMETHYST_BUD", "AMETHYST_CLUSTER"
        ));

        // Vegan Challenge
        config.addDefault("Vegan.disallowed-food", List.of(
                "BEEF", "COOKED_BEEF", "CHICKEN", "COOKED_CHICKEN", "PORKCHOP", "COOKED_PORKCHOP", "MUTTON", "COOKED_MUTTON",
                "RABBIT", "COOKED_RABBIT", "COD", "COOKED_COD", "SALMON", "COOKED_SALMON", "RABBIT_STEW", "PUMPKIN_PIE", "CAKE",
                "TROPICAL_FISH", "PUFFERFISH", "MILK_BUCKET"
        ));
        config.addDefault("Vegan.forbidden-potions", List.of("SLOWNESS", "WEAKNESS", "INVISIBILITY", "INSTANT_DAMAGE"));
        config.addDefault("Vegan.divine-punishment.lightning-strikes", 30);

        // Vulture Challenge
        config.addDefault("Vulture.allowed-food", "ROTTEN_FLESH");

        // NakedAndAfraid Challenge
        config.addDefault("NakedAndAfraid.armor-disabled", true);
        config.addDefault("NakedAndAfraid.drop-equipped-armor", true);

        // Elf Challenge
        config.addDefault("Elf.allowed-weapons", List.of("BOW", "CROSSBOW"));
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

    // General Settings Getters
    public boolean isOpManagementEnabled() {
        return config.getBoolean("op-management", true);
    }

    // Challenge Configuration Getters
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