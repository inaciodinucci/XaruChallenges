package xaruplex.xaruchallenges.listener;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final XaruChallenges plugin;
    private final ChallengeManager challengeManager;

    public PlayerJoinListener(XaruChallenges plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        challengeManager.loadPlayerChallenges(event.getPlayer());
    }
}