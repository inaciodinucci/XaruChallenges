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
        return "Permanent blindness effect.";
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
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        400, // 20 seconds
                        0,
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
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        return false; // No direct event handling needed
    }
}