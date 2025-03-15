package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaseOh implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();

    public CaseOh(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "CaseOh";
    }

    @Override
    public String getDescription() {
        return "You are not allowed to sprint. Permanent slowness is applied.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID uuid = player.getUniqueId();

        // Apply permanent Slowness effect
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    tasks.remove(uuid);
                    return;
                }

                // Apply Slowness effect (level 3 to make sprinting impossible)
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        400, // 20 seconds
                        2, // Slowness level 3 (0 = level 1, 1 = level 2, 2 = level 3)
                        true,
                        false
                ));
            }
        };
        task.runTaskTimer(plugin, 0L, 200L); // Reapply every 10 seconds
        tasks.put(uuid, task);

        // Immediately disable sprinting
        player.setSprinting(false);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel the task if it exists
        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).cancel();
            tasks.remove(uuid);
        }

        // Remove the Slowness effect
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof PlayerToggleSprintEvent) {
            PlayerToggleSprintEvent sprintEvent = (PlayerToggleSprintEvent) event;

            // Prevent sprinting
            if (sprintEvent.isSprinting()) {
                sprintEvent.setCancelled(true);
                player.setSprinting(false);
                player.sendMessage(ChatColor.RED + "You cannot sprint with the CaseOh challenge!");
                return true;
            }
        }
        return false;
    }
}