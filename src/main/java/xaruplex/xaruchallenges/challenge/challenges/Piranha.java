package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Piranha implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public Piranha(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Piranha";
    }

    @Override
    public String getDescription() {
        return "You can only breathe underwater. Air will cause you to take damage. Cannot eat fish or kelp.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Start the task that checks if the player is in water
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(playerId);
                    return;
                }

                // Check if player is in water
                if (!player.isInWater()) {
                    // Player is not in water, apply damage
                    double damage = configManager.getDouble("Piranha.damage-outside-water", 2.0);
                    player.damage(damage);
                    player.sendMessage(ChatColor.RED + "You need to be in water!");
                } else {
                    // Player is in water, give water breathing
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second

        activeTasks.put(playerId, task);

        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel the task if it exists
        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }

        // Remove any potion effects
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof VehicleEnterEvent) {
            VehicleEnterEvent vehicleEvent = (VehicleEnterEvent) event;

            // Check if the player is entering a vehicle and vehicles are not allowed
            if (!configManager.getBoolean("Piranha.allow-vehicles", false) &&
                    vehicleEvent.getEntered().equals(player)) {
                vehicleEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use vehicles with the Piranha challenge!");
                return true;
            }
        } else if (event instanceof PlayerMoveEvent) {
            // Additional handling for player movement (optional)
            // For example, you could check if the player is moving from water to air
        } else if (event instanceof EntityDamageEvent) {
            EntityDamageEvent damageEvent = (EntityDamageEvent) event;

            // Prevent drowning damage
            if (damageEvent.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                damageEvent.setCancelled(true);
                return true;
            }
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            Material food = consumeEvent.getItem().getType();

            if (!isAllowedFood(food)) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot eat this food as a Piranha!");
                return true;
            }
        }

        return false;
    }

    private boolean isAllowedFood(Material food) {
        List<Material> allowedFood = configManager.getMaterialList("Piranha.allowed-food");
        List<Material> disallowedFood = configManager.getMaterialList("Piranha.disallowed-food");

        // If it's explicitly disallowed, return false
        if (disallowedFood.contains(food)) {
            return false;
        }

        // If there's an allowed food list, only allow those foods
        if (!allowedFood.isEmpty()) {
            return allowedFood.contains(food);
        }

        // By default, disallow all fish and kelp
        return !isFishOrKelp(food);
    }

    private boolean isFishOrKelp(Material material) {
        return material == Material.COD ||
                material == Material.COOKED_COD ||
                material == Material.SALMON ||
                material == Material.COOKED_SALMON ||
                material == Material.TROPICAL_FISH ||
                material == Material.PUFFERFISH ||
                material == Material.DRIED_KELP ||
                material == Material.KELP;
    }
}