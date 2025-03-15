package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class CaseOh implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public CaseOh(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "CaseOh";
    }

    @Override
    public String getDescription() {
        return "You are not allowed to sprint.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        player.setSprinting(false);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        // No cleanup needed
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof PlayerToggleSprintEvent) {
            PlayerToggleSprintEvent sprintEvent = (PlayerToggleSprintEvent) event;
            if (sprintEvent.isSprinting()) {
                sprintEvent.setCancelled(true);
                player.setSprinting(false);
                player.sendMessage(ChatColor.RED + "You cannot sprint with the CaseOh challenge!");
                return true;
            }
        }
        return false;
    }
}