package xaruplex.xaruchallenges.challenge;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.challenges.*;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChallengeManager {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<String, Challenge> availableChallenges;
    private final Map<UUID, List<Challenge>> activeChallenges;
    private final Map<UUID, Integer> loseCounters;
    private final Map<UUID, Boolean> loseCounterEnabled;

    public ChallengeManager(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.availableChallenges = new HashMap<>();
        this.activeChallenges = new HashMap<>();
        this.loseCounters = new HashMap<>();
        this.loseCounterEnabled = new HashMap<>();

        // Register all available challenges
        registerChallenges();
    }

    private void registerChallenges() {
        // Initialize and register all challenge implementations
        registerChallenge(new Piranha(plugin, configManager));
        registerChallenge(new Vampire(plugin, configManager));
        registerChallenge(new Daredevil(plugin, configManager));
        registerChallenge(new HollowBones(plugin, configManager));
        registerChallenge(new CaseOh(plugin, configManager));
        registerChallenge(new Chinchilla(plugin, configManager));
        registerChallenge(new Prasinophobia(plugin, configManager));
        registerChallenge(new Mole(plugin, configManager));
        registerChallenge(new OreDeath(plugin, configManager));
        registerChallenge(new Vegan(plugin, configManager));
        registerChallenge(new Vulture(plugin, configManager));
        registerChallenge(new NakedAndAfraid(plugin, configManager));
        registerChallenge(new Elf(plugin, configManager));
    }

    private void registerChallenge(Challenge challenge) {
        availableChallenges.put(challenge.getName().toLowerCase(), challenge);
    }

    public boolean addChallenge(Player player, String challengeName) {
        UUID playerId = player.getUniqueId();
        Challenge challenge = availableChallenges.get(challengeName.toLowerCase());

        if (challenge == null) {
            return false;
        }

        // Initialize player's challenge list if needed
        if (!activeChallenges.containsKey(playerId)) {
            activeChallenges.put(playerId, new ArrayList<>());
            loseCounters.put(playerId, 0);
        }

        List<Challenge> playerChallenges = activeChallenges.get(playerId);

        // Check if player already has this challenge
        for (Challenge existingChallenge : playerChallenges) {
            if (existingChallenge.getName().equalsIgnoreCase(challengeName)) {
                player.sendMessage(ChatColor.RED + "You already have the " + challenge.getName() + " challenge!");
                return false;
            }
        }

        // Apply the challenge
        if (challenge.applyChallenge(player)) {
            playerChallenges.add(challenge);
            player.sendMessage(ChatColor.GREEN + "Challenge " + challenge.getName() + " has been applied to you!");
            player.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + challenge.getDescription());
            return true;
        }

        return false;
    }

    public boolean removeChallenge(Player player, String challengeName) {
        UUID playerId = player.getUniqueId();

        if (!activeChallenges.containsKey(playerId)) {
            return false;
        }

        List<Challenge> playerChallenges = activeChallenges.get(playerId);
        Challenge toRemove = null;

        for (Challenge challenge : playerChallenges) {
            if (challenge.getName().equalsIgnoreCase(challengeName)) {
                toRemove = challenge;
                break;
            }
        }

        if (toRemove != null) {
            toRemove.removeChallenge(player);
            playerChallenges.remove(toRemove);
            player.sendMessage(ChatColor.GREEN + "Challenge " + toRemove.getName() + " has been removed!");

            // Remove player from tracking if they have no more challenges
            if (playerChallenges.isEmpty()) {
                activeChallenges.remove(playerId);
                loseCounters.remove(playerId);
                loseCounterEnabled.remove(playerId);
            }

            return true;
        }

        return false;
    }

    public void handleEvent(Event event, Player player) {
        UUID playerId = player.getUniqueId();

        if (!activeChallenges.containsKey(playerId)) {
            return;
        }

        List<Challenge> playerChallenges = activeChallenges.get(playerId);

        for (Challenge challenge : playerChallenges) {
            challenge.handleEvent(event, player);
        }
    }

    public boolean hasChallenge(Player player, String challengeName) {
        UUID playerId = player.getUniqueId();

        if (!activeChallenges.containsKey(playerId)) {
            return false;
        }

        List<Challenge> playerChallenges = activeChallenges.get(playerId);

        for (Challenge challenge : playerChallenges) {
            if (challenge.getName().equalsIgnoreCase(challengeName)) {
                return true;
            }
        }

        return false;
    }

    public List<Challenge> getPlayerChallenges(Player player) {
        UUID playerId = player.getUniqueId();

        if (!activeChallenges.containsKey(playerId)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(activeChallenges.get(playerId));
    }

    public List<String> getAllChallengeNames() {
        return new ArrayList<>(availableChallenges.keySet());
    }

    public Challenge getChallengeByName(String name) {
        return availableChallenges.get(name.toLowerCase());
    }

    public void incrementLoseCounter(Player player) {
        UUID playerId = player.getUniqueId();

        if (loseCounters.containsKey(playerId)) {
            int current = loseCounters.get(playerId);
            loseCounters.put(playerId, current + 1);

            // Update the player's scoreboard if lose counter is enabled
            if (isLoseCounterEnabled(player)) {
                updateLoseCounter(player);
            }
        }
    }

    public int getLoseCounter(Player player) {
        UUID playerId = player.getUniqueId();
        return loseCounters.getOrDefault(playerId, 0);
    }

    public void setLoseCounterEnabled(Player player, boolean enabled) {
        UUID playerId = player.getUniqueId();
        loseCounterEnabled.put(playerId, enabled);

        if (enabled) {
            updateLoseCounter(player);
        } else {
            // Remove the scoreboard display
            // Implementation depends on how you choose to display the lose counter
        }
    }

    public boolean isLoseCounterEnabled(Player player) {
        UUID playerId = player.getUniqueId();
        return loseCounterEnabled.getOrDefault(playerId, false);
    }

    private void updateLoseCounter(Player player) {
        // Implementation for updating the scoreboard or other display method
        // This could use Bukkit's scoreboard API or another method of your choice
    }

    public void cleanupAllChallenges() {
        // Remove all challenges from all players when the plugin disables
        for (UUID playerId : new ArrayList<>(activeChallenges.keySet())) {
            Player player = Bukkit.getPlayer(playerId);

            if (player != null && player.isOnline()) {
                List<Challenge> challenges = new ArrayList<>(activeChallenges.get(playerId));

                for (Challenge challenge : challenges) {
                    challenge.removeChallenge(player);
                }
            }
        }

        activeChallenges.clear();
        loseCounters.clear();
        loseCounterEnabled.clear();
    }
}