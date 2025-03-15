package xaruplex.xaruchallenges.listener;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.ChallengeManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final XaruChallenges plugin;
    private final ChallengeManager challengeManager;

    public PlayerDamageListener(XaruChallenges plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Forward the event to the challenge manager
            challengeManager.handleEvent(event, player);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle cases where player is the attacker
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();

            // Forward the event to the challenge manager
            challengeManager.handleEvent(event, attacker);
        }

        // Handle cases where player is the victim
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // Forward the event to the challenge manager
            challengeManager.handleEvent(event, victim);
        }
    }
}