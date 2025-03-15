package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class Vulture implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Vulture(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Vulture";
    }

    @Override
    public String getDescription() {
        return "You can only eat rotten flesh; all other edible items are blocked.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        // No cleanup needed
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            Material food = consumeEvent.getItem().getType();

            if (!isAllowedFood(food)) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only eat rotten flesh!");
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedFood(Material food) {
        return food == Material.ROTTEN_FLESH;
    }
}