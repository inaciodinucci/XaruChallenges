package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class Mole implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Mole(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Mole";
    }

    @Override
    public String getDescription() {
        return "You are only allowed to walk on specific blocks: dirt, coarse dirt, or sand.";
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
            Block block = moveEvent.getTo().getBlock().getRelative(0, -1, 0);

            if (!isAllowedBlock(block.getType())) {
                player.damage(2.0);
                player.sendMessage(ChatColor.RED + "You can only walk on dirt, coarse dirt, or sand!");
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedBlock(Material material) {
        List<Material> allowedBlocks = configManager.getMaterialList("Mole.allowed-blocks");
        return allowedBlocks.contains(material);
    }
}