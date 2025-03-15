package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Chinchilla implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Chinchilla(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Chinchilla";
    }

    @Override
    public String getDescription() {
        return "You die if you touch water (including rain) and can only eat fish.";
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
        if (event instanceof PlayerMoveEvent) {
            PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
            if (isInWater(player)) {
                if (configManager.getBoolean("Chinchilla.water-death", true)) {
                    player.setHealth(0);
                    player.sendMessage(ChatColor.RED + "You died from touching water!");
                    return true;
                }
            }
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            Material food = consumeEvent.getItem().getType();

            if (!isAllowedFood(food)) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only eat fish!");
                return true;
            }
        }
        return false;
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().isLiquid() ||
                player.getLocation().getBlock().getType() == Material.WATER ||
                (player.getWorld().hasStorm() && player.getLocation().getBlock().getRelative(0, 1, 0).isEmpty());
    }

    private boolean isAllowedFood(Material food) {
        switch (food) {
            case COD:
            case COOKED_COD:
            case SALMON:
            case COOKED_SALMON:
            case TROPICAL_FISH:
            case PUFFERFISH:
                return true;
            default:
                return false;
        }
    }
}