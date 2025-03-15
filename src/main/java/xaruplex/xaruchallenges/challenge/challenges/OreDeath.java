package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class OreDeath implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public OreDeath(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "OreDeath";
    }

    @Override
    public String getDescription() {
        return "Mining any ore causes instant death.";
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
        if (event instanceof BlockBreakEvent) {
            BlockBreakEvent breakEvent = (BlockBreakEvent) event;
            Material blockType = breakEvent.getBlock().getType();

            if (isDeadlyOre(blockType)) {
                player.setHealth(0);
                player.sendMessage(ChatColor.RED + "You mined a forbidden ore!");
                return true;
            }
        }
        return false;
    }

    private boolean isDeadlyOre(Material ore) {
        List<Material> deadlyOres = configManager.getMaterialList("OreDeath.ore-list");
        return deadlyOres.contains(ore);
    }
}