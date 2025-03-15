package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vampire implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<UUID, Long> lastHungerDecay = new HashMap<>();
    private static final long HUNGER_DECAY_DELAY = 8000; // 8 seconds (4x slower than normal)

    public Vampire(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Vampire";
    }

    @Override
    public String getDescription() {
        return "You can only walk in shadows and at night. Sunlight causes fire damage. " +
                "Regain health by attacking players or villagers. Killing players fully restores you.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID playerId = player.getUniqueId();
        lastHungerDecay.put(playerId, System.currentTimeMillis());

        // Start the task that checks if the player is in sunlight
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(playerId);
                    return;
                }

                // Check if it's daytime and player is exposed to sunlight
                if (isInSunlight(player)) {
                    double damage = configManager.getDouble("Vampire.damage-in-sunlight", 1.0);
                    player.setFireTicks(20); // Set on fire for 1 second
                    player.damage(damage);
                    player.sendMessage(ChatColor.RED + "The sunlight burns your vampire skin!");
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Check every half second

        activeTasks.put(playerId, task);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }

        lastHungerDecay.remove(playerId);
        player.setFireTicks(0);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof EntityDamageByEntityEvent) {
            return handleDamageEvent((EntityDamageByEntityEvent) event, player);
        } else if (event instanceof EntityDeathEvent) {
            return handleDeathEvent((EntityDeathEvent) event, player);
        } else if (event instanceof FoodLevelChangeEvent) {
            return handleHungerEvent((FoodLevelChangeEvent) event, player);
        }
        return false;
    }

    private boolean handleDamageEvent(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager().equals(player)) {
            if (event.getEntity() instanceof Player || event.getEntity() instanceof Villager) {
                // Heal the vampire when they hit a player or villager
                double currentHealth = player.getHealth();
                double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                double healAmount = 2.0; // Heal 1 heart per hit

                player.setHealth(Math.min(currentHealth + healAmount, maxHealth));
                player.sendMessage(ChatColor.DARK_RED + "You drain their life force!");
                return true;
            }
        }
        return false;
    }

    private boolean handleDeathEvent(EntityDeathEvent event, Player player) {
        if (event.getEntity() instanceof Player && event.getEntity().getKiller() != null
                && event.getEntity().getKiller().equals(player)) {
            // Fully restore health and hunger when killing a player
            player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.sendMessage(ChatColor.DARK_RED + "You feast on their blood, restoring yourself completely!");
            return true;
        }
        return false;
    }

    private boolean handleHungerEvent(FoodLevelChangeEvent event, Player player) {
        if (!event.getEntity().equals(player)) return false;

        // Slow down hunger decay
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHungerDecay.getOrDefault(player.getUniqueId(), 0L) < HUNGER_DECAY_DELAY) {
            event.setCancelled(true);
            return true;
        }

        lastHungerDecay.put(player.getUniqueId(), currentTime);
        return false;
    }

    private boolean isInSunlight(Player player) {
        World world = player.getWorld();

        // If it's night time or not in the overworld, player is safe
        long time = world.getTime();
        if (time >= 13000 && time <= 23000) {
            return false;
        }

        // Check if player is exposed to the sky
        Block highestBlock = world.getHighestBlockAt(player.getLocation());
        if (player.getLocation().getY() < highestBlock.getY()) {
            return false;
        }

        int lightLevel = player.getLocation().getBlock().getLightFromSky();
        int threshold = configManager.getInt("Vampire.light-level-threshold", 12);
        return lightLevel >= threshold;
    }
}