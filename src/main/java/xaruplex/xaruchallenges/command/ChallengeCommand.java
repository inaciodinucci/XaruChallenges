package xaruplex.xaruchallenges.command;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChallengeCommand implements CommandExecutor, TabCompleter {

    private final XaruChallenges plugin;
    private final ChallengeManager challengeManager;
    private final ConfigManager configManager;

    public ChallengeCommand(XaruChallenges plugin, ChallengeManager challengeManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                return true;
            case "list":
                sendChallengeList(sender);
                return true;
            case "add":
                return handleAddCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "losecounter":
                return handleLoseCounterCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "challenges":
                return handleChallengesCommand(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Type /xc help for help.");
                return false;
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== XaruChallenges Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/xc help " + ChatColor.WHITE + "- Display this help message");
        sender.sendMessage(ChatColor.YELLOW + "/xc list " + ChatColor.WHITE + "- List available challenges");
        sender.sendMessage(ChatColor.YELLOW + "/xc add <challenge> <player> " + ChatColor.WHITE + "- Add a challenge to a player");
        sender.sendMessage(ChatColor.YELLOW + "/xc remove <challenge> <player> " + ChatColor.WHITE + "- Remove a challenge from a player");
        sender.sendMessage(ChatColor.YELLOW + "/xc losecounter enable " + ChatColor.WHITE + "- Enable the lose counter display");
        sender.sendMessage(ChatColor.YELLOW + "/xc losecounter disable " + ChatColor.WHITE + "- Disable the lose counter display");
        sender.sendMessage(ChatColor.YELLOW + "/xc reload " + ChatColor.WHITE + "- Reload the plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/xc challenges <player> " + ChatColor.WHITE + "- View active challenges for a player");
    }

    private void sendChallengeList(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== XaruChallenges Available Challenges ===");

        for (String challengeName : challengeManager.getAllChallengeNames()) {
            Challenge challenge = challengeManager.getChallengeByName(challengeName);
            sender.sendMessage(ChatColor.YELLOW + challenge.getName() + ": " + ChatColor.WHITE + challenge.getDescription());
        }
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /xc add <challenge> <player>");
            return false;
        }

        // Check permission if op-management is enabled
        if (configManager.isOpManagementEnabled() && !hasAdminPermission(sender)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to add challenges.");
            return false;
        }

        String challengeName = args[1];
        String playerName = args[2];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
            return false;
        }

        if (challengeManager.addChallenge(targetPlayer, challengeName)) {
            sender.sendMessage(ChatColor.GREEN + "Challenge " + challengeName + " added to " + targetPlayer.getName());
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to add challenge. Check if the challenge exists.");
            return false;
        }
    }

    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /xc remove <challenge> <player>");
            return false;
        }

        // Check permission if op-management is enabled
        if (configManager.isOpManagementEnabled() && !hasAdminPermission(sender)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to remove challenges.");
            return false;
        }

        String challengeName = args[1];
        String playerName = args[2];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
            return false;
        }

        if (challengeManager.removeChallenge(targetPlayer, challengeName)) {
            sender.sendMessage(ChatColor.GREEN + "Challenge " + challengeName + " removed from " + targetPlayer.getName());
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to remove challenge. Check if the player has this challenge.");
            return false;
        }
    }

    private boolean handleLoseCounterCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /xc losecounter <enable|disable>");
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;
        String option = args[1].toLowerCase();

        if (option.equals("enable")) {
            challengeManager.setLoseCounterEnabled(player, true);
            sender.sendMessage(ChatColor.GREEN + "Lose counter enabled.");
            return true;
        } else if (option.equals("disable")) {
            challengeManager.setLoseCounterEnabled(player, false);
            sender.sendMessage(ChatColor.GREEN + "Lose counter disabled.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid option. Use 'enable' or 'disable'.");
            return false;
        }
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!hasAdminPermission(sender)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the configuration.");
            return false;
        }

        configManager.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
        return true;
    }

    private boolean handleChallengesCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /xc challenges <player>");
            return false;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
            return false;
        }

        List<Challenge> challenges = challengeManager.getPlayerChallenges(targetPlayer);
        if (challenges.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " has no active challenges.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "=== Active Challenges for " + targetPlayer.getName() + " ===");
            for (Challenge challenge : challenges) {
                sender.sendMessage(ChatColor.YELLOW + challenge.getName() + ": " + ChatColor.WHITE + challenge.getDescription());
            }
        }
        return true;
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("xaruchallenges.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommand
            String partialCommand = args[0].toLowerCase();
            List<String> commands = Arrays.asList("help", "list", "add", "remove", "losecounter", "reload", "challenges");

            for (String cmd : commands) {
                if (cmd.startsWith(partialCommand)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // Second argument - challenge name, losecounter option, or player name
            String subCommand = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            if (subCommand.equals("add") || subCommand.equals("remove")) {
                // Challenge name completion
                List<String> challenges = challengeManager.getAllChallengeNames();

                for (String challenge : challenges) {
                    if (challenge.toLowerCase().startsWith(partial)) {
                        completions.add(challenge);
                    }
                }
            } else if (subCommand.equals("losecounter")) {
                // LoseCounter options
                List<String> options = Arrays.asList("enable", "disable");

                for (String option : options) {
                    if (option.startsWith(partial)) {
                        completions.add(option);
                    }
                }
            } else if (subCommand.equals("challenges")) {
                // Player name completion
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            // Third argument - player name for add/remove commands
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("add") || subCommand.equals("remove")) {
                String partial = args[2].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}