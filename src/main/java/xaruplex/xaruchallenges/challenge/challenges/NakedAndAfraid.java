package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class NakedAndAfraid implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public NakedAndAfraid(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "NakedAndAfraid";
    }

    @Override
    public String getDescription() {
        return "You are prohibited from wearing any armor.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        // Remove any currently equipped armor
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        boolean hadArmor = false;
        for (int i = 0; i < armorContents.length; i++) {
            if (armorContents[i] != null && armorContents[i].getType() != Material.AIR) {
                // Drop the armor item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), armorContents[i]);
                armorContents[i] = null;
                hadArmor = true;
            }
        }

        if (hadArmor) {
            inventory.setArmorContents(armorContents);
            player.sendMessage(ChatColor.RED + "Your armor has been removed!");
        }

        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        // No cleanup needed
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof InventoryClickEvent) {
            InventoryClickEvent clickEvent = (InventoryClickEvent) event;

            // Check if the click is in an armor slot
            if (isArmorSlotClick(clickEvent)) {
                clickEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot wear armor!");
                return true;
            }

            // Check if player is trying to shift-click armor into armor slots
            if (clickEvent.isShiftClick() && clickEvent.getCurrentItem() != null) {
                Material type = clickEvent.getCurrentItem().getType();
                if (isArmor(type)) {
                    clickEvent.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot wear armor!");
                    return true;
                }
            }
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            ItemStack item = interactEvent.getItem();

            // Check if player is trying to right-click armor to equip it
            if (item != null && isArmor(item.getType())) {
                interactEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot wear armor!");
                return true;
            }
        }

        return false;
    }

    private boolean isArmorSlotClick(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return true;
        }

        // Check raw slot numbers for armor slots (these are the default armor slot numbers)
        int slot = event.getRawSlot();
        return slot >= 5 && slot <= 8;
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") ||
                name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") ||
                name.endsWith("_BOOTS") ||
                name.equals("ELYTRA");
    }
}