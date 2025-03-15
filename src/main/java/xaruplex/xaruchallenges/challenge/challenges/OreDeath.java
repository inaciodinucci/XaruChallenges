package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OreDeath implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> inventoryCheckTasks = new HashMap<>();

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
        return "Mining, picking up, or having any forbidden ore in your inventory causes instant death.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Start a task to periodically check the player's inventory for forbidden ores
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    inventoryCheckTasks.remove(playerId);
                    return;
                }

                // Check the player's inventory for forbidden ores
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && isDeadlyOre(item.getType())) {
                        player.setHealth(0);
                        player.sendMessage(ChatColor.RED + "You had a forbidden ore in your inventory!");
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second

        inventoryCheckTasks.put(playerId, task);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel the inventory check task if it exists
        if (inventoryCheckTasks.containsKey(playerId)) {
            inventoryCheckTasks.get(playerId).cancel();
            inventoryCheckTasks.remove(playerId);
        }
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
        } else if (event instanceof PlayerPickupItemEvent) {
            PlayerPickupItemEvent pickupEvent = (PlayerPickupItemEvent) event;
            ItemStack item = pickupEvent.getItem().getItemStack();

            if (isDeadlyOre(item.getType())) {
                player.setHealth(0);
                player.sendMessage(ChatColor.RED + "You picked up a forbidden ore!");
                return true;
            }
        } else if (event instanceof InventoryClickEvent) {
            InventoryClickEvent clickEvent = (InventoryClickEvent) event;
            ItemStack item = clickEvent.getCurrentItem();

            if (item != null && isDeadlyOre(item.getType())) {
                player.setHealth(0);
                player.sendMessage(ChatColor.RED + "You interacted with a forbidden ore!");
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