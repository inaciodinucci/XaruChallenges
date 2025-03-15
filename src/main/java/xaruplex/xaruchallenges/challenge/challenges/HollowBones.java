package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HollowBones implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public HollowBones(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "HollowBones";
    }

    @Override
    public String getDescription() {
        return "You only have one heart (2 HP maximum).";
    }

    @Override
    public boolean applyChallenge(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double newMaxHealth = configManager.getDouble("HollowBones.max-health", 2.0);
            maxHealth.setBaseValue(newMaxHealth);
            player.setHealth(newMaxHealth);
            return true;
        }
        return false;
    }

    @Override
    public void removeChallenge(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0); // Reset to default max health
            player.setHealth(20.0);
        }
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof EntityRegainHealthEvent) {
            EntityRegainHealthEvent healthEvent = (EntityRegainHealthEvent) event;
            double maxHealth = configManager.getDouble("HollowBones.max-health", 2.0);

            // Ensure healing doesn't exceed max health
            if (player.getHealth() + healthEvent.getAmount() > maxHealth) {
                healthEvent.setAmount(maxHealth - player.getHealth());
                if (healthEvent.getAmount() <= 0) {
                    healthEvent.setCancelled(true);
                }
                return true;
            }
        }
        return false;
    }
}