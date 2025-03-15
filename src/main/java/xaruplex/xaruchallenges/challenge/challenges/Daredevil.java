package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Daredevil implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();

    public Daredevil(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Daredevil";
    }

    @Override
    public String getDescription() {
        return "Permanent blindness and speed effects.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    tasks.remove(uuid);
                    return;
                }

                // Apply Blindness effect
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        400, // 20 seconds
                        0,
                        true,
                        false
                ));

                // Apply Speed effect
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        400, // 20 seconds
                        1, // Speed level 2 (0 = level 1, 1 = level 2, etc.)
                        true,
                        false
                ));
            }
        };
        task.runTaskTimer(plugin, 0L, 200L); // Reapply every 10 seconds
        tasks.put(uuid, task);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID uuid = player.getUniqueId();
        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).cancel();
            tasks.remove(uuid);
        }
        // Remove both Blindness and Speed effects
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        return false; // No direct event handling needed
    }
}