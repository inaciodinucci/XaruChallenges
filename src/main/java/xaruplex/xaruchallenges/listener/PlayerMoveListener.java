package xaruplex.xaruchallenges.listener;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final XaruChallenges plugin;
    private final ChallengeManager challengeManager;

    public PlayerMoveListener(XaruChallenges plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only handle if the player actually moved blocks
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        challengeManager.handleEvent(event, player);
    }
}