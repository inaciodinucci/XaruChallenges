package xaruplex.xaruchallenges;

import org.bukkit.entity.Player;
import xaruplex.xaruchallenges.command.ChallengeCommand;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import xaruplex.xaruchallenges.config.ConfigManager;
import xaruplex.xaruchallenges.data.DataManager;
import xaruplex.xaruchallenges.data.SQLiteDataManager;
import xaruplex.xaruchallenges.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public class XaruChallenges extends JavaPlugin {

    private ConfigManager configManager;
    private ChallengeManager challengeManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize data manager
        dataManager = new SQLiteDataManager(this);
        dataManager.initialize();

        // Initialize challenge manager
        challengeManager = new ChallengeManager(this, configManager, dataManager);

        // Register command
        getCommand("xaruchallenges").setExecutor(new ChallengeCommand(this, challengeManager, configManager));

        // Register event listeners
        registerEventListeners();

        getLogger().info("XaruChallenges has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save challenges for online players
        for (Player player : getServer().getOnlinePlayers()) {
            challengeManager.savePlayerChallenges(player);
        }

        // Clean up any active challenges
        if (challengeManager != null) {
            challengeManager.cleanupAllChallenges();
        }

        // Shutdown data manager
        if (dataManager != null) {
            dataManager.shutdown();
        }

        getLogger().info("XaruChallenges has been disabled!");
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, challengeManager), this); // Register PlayerJoinListener
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, challengeManager), this); // Register PlayerQuitListener
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}