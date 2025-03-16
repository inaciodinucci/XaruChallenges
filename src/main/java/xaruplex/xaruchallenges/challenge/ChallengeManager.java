package xaruplex.xaruchallenges.challenge;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.challenges.*;
import xaruplex.xaruchallenges.config.ConfigManager;
import xaruplex.xaruchallenges.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.*;
import java.util.stream.Collectors;

public class ChallengeManager {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final Map<String, Challenge> availableChallenges;
    private final Map<UUID, Set<Challenge>> activeChallenges;
    private final Map<UUID, Integer> loseCounters;
    private final Map<UUID, Boolean> loseCounterEnabled;

    public ChallengeManager(XaruChallenges plugin, ConfigManager configManager, DataManager dataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.availableChallenges = new HashMap<>();
        this.activeChallenges = new HashMap<>();
        this.loseCounters = new HashMap<>();
        this.loseCounterEnabled = new HashMap<>();

        registerChallenges();
    }

    private void registerChallenges() {
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

        Set<Challenge> playerChallenges = activeChallenges.computeIfAbsent(playerId, k -> new HashSet<>());

        if (playerChallenges.contains(challenge)) {
            player.sendMessage(ChatColor.RED + "You already have the " + challenge.getName() + " challenge!");
            return false;
        }

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
        Set<Challenge> playerChallenges = activeChallenges.get(playerId);

        if (playerChallenges == null) {
            return false;
        }

        Challenge toRemove = playerChallenges.stream()
                .filter(challenge -> challenge.getName().equalsIgnoreCase(challengeName))
                .findFirst()
                .orElse(null);

        if (toRemove != null) {
            toRemove.removeChallenge(player);
            playerChallenges.remove(toRemove);
            player.sendMessage(ChatColor.GREEN + "Challenge " + toRemove.getName() + " has been removed!");

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
        Set<Challenge> playerChallenges = activeChallenges.get(player.getUniqueId());

        if (playerChallenges != null) {
            playerChallenges.forEach(challenge -> challenge.handleEvent(event, player));
        }
    }

    public boolean hasChallenge(Player player, String challengeName) {
        Set<Challenge> playerChallenges = activeChallenges.get(player.getUniqueId());

        if (playerChallenges == null) {
            return false;
        }

        return playerChallenges.stream()
                .anyMatch(challenge -> challenge.getName().equalsIgnoreCase(challengeName));
    }

    public List<Challenge> getPlayerChallenges(Player player) {
        Set<Challenge> playerChallenges = activeChallenges.get(player.getUniqueId());
        return playerChallenges != null ? new ArrayList<>(playerChallenges) : new ArrayList<>();
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
            loseCounters.put(playerId, loseCounters.get(playerId) + 1);

            if (isLoseCounterEnabled(player)) {
                updateLoseCounter(player);
            }
        }
    }

    public int getLoseCounter(Player player) {
        return loseCounters.getOrDefault(player.getUniqueId(), 0);
    }

    public void setLoseCounterEnabled(Player player, boolean enabled) {
        UUID playerId = player.getUniqueId();
        loseCounterEnabled.put(playerId, enabled);

        if (enabled) {
            updateLoseCounter(player);
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public boolean isLoseCounterEnabled(Player player) {
        return loseCounterEnabled.getOrDefault(player.getUniqueId(), false);
    }

    private void updateLoseCounter(Player player) {
        int loseCount = loseCounters.getOrDefault(player.getUniqueId(), 0);
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("loseCounter", "dummy", ChatColor.RED + "Lose Counter");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score = objective.getScore(ChatColor.YELLOW + "Loses: " + loseCount);
        score.setScore(1);
        player.setScoreboard(scoreboard);
    }

    public void cleanupAllChallenges() {
        activeChallenges.keySet().forEach(playerId -> {
            Player player = Bukkit.getPlayer(playerId);

            if (player != null && player.isOnline()) {
                activeChallenges.get(playerId).forEach(challenge -> challenge.removeChallenge(player));
            }
        });

        activeChallenges.clear();
        loseCounters.clear();
        loseCounterEnabled.clear();
    }

    public void loadPlayerChallenges(Player player) {
        UUID playerId = player.getUniqueId();
        List<String> challengeNames = dataManager.loadChallenges(playerId);
        Set<Challenge> playerChallenges = activeChallenges.computeIfAbsent(playerId, k -> new HashSet<>());

        challengeNames.stream()
                .map(name -> availableChallenges.get(name.toLowerCase()))
                .filter(Objects::nonNull)
                .filter(challenge -> !playerChallenges.contains(challenge))
                .forEach(challenge -> {
                    if (challenge.applyChallenge(player)) {
                        playerChallenges.add(challenge);
                    }
                });
    }

    public void savePlayerChallenges(Player player) {
        UUID playerId = player.getUniqueId();
        Set<Challenge> challenges = activeChallenges.get(playerId);

        if (challenges != null) {
            List<String> challengeNames = challenges.stream()
                    .map(Challenge::getName)
                    .collect(Collectors.toList());
            dataManager.saveChallenges(playerId, challengeNames);
        }
    }
}