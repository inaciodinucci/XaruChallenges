package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class Prasinophobia implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Prasinophobia(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Prasinophobia";
    }

    @Override
    public String getDescription() {
        return "You cannot touch any green block. (WIP, try this one in a newer version)";
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
            Block block = moveEvent.getTo().getBlock();

            if (isGreenBlock(block.getType())) {
                player.damage(2.0);
                player.sendMessage(ChatColor.RED + "You touched a green block!");
                return true;
            }
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            Block block = interactEvent.getClickedBlock();

            if (block != null && isGreenBlock(block.getType())) {
                interactEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot interact with green blocks!");
                return true;
            }
        }
        return false;
    }

    private boolean isGreenBlock(Material material) {
        List<Material> disallowedBlocks = configManager.getMaterialList("Prasinophobia.disallowed-blocks");
        return disallowedBlocks.contains(material);
    }
}