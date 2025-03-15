package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Elf implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Elf(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Elf";
    }

    @Override
    public String getDescription() {
        return "Can only use bows and crossbows.";
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
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            ItemStack weapon = player.getInventory().getItemInMainHand();

            if (!isAllowedWeapon(weapon.getType())) {
                damageEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Only bows and crossbows allowed!");
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedWeapon(Material weapon) {
        List<Material> allowed = configManager.getMaterialList("Elf.allowed-weapons");
        return allowed.contains(weapon);
    }
}