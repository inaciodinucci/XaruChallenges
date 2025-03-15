package xaruplex.xaruchallenges.listener;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final XaruChallenges plugin;
    private final ChallengeManager challengeManager;

    public PlayerQuitListener(XaruChallenges plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        challengeManager.savePlayerChallenges(event.getPlayer());
    }
}