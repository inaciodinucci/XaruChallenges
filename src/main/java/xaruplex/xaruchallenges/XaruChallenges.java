package xaruplex.xaruchallenges;

import xaruplex.xaruchallenges.command.ChallengeCommand;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import xaruplex.xaruchallenges.config.ConfigManager;
import xaruplex.xaruchallenges.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public class XaruChallenges extends JavaPlugin {

    private ConfigManager configManager;
    private ChallengeManager challengeManager;

    @Override
    public void onEnable() {
        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize challenge manager
        challengeManager = new ChallengeManager(this, configManager);

        // Register command
        getCommand("xaruchallenges").setExecutor(new ChallengeCommand(this, challengeManager, configManager));

        // Register event listeners
        registerEventListeners();

        getLogger().info("XaruChallenges has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up any active challenges
        if (challengeManager != null) {
            challengeManager.cleanupAllChallenges();
        }

        getLogger().info("XaruChallenges has been disabled!");
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this, challengeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeListener(this, challengeManager), this);
  }
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }
}