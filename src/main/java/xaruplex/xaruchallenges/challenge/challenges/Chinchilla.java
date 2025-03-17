package xaruplex.xaruchallenges.challenge.challenges;

import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class Chinchilla implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> rainDamageTasks = new HashMap<>();

    // List of biomes where it can rain (updated to current Minecraft biomes)
    private final List<Biome> rainyBiomes = Arrays.asList(
            // Regular forests
            Biome.FOREST, Biome.FLOWER_FOREST, Biome.BIRCH_FOREST,
            Biome.OLD_GROWTH_BIRCH_FOREST, Biome.DARK_FOREST,

            // Taiga variants (that can have rain, not snow)
            Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA,

            // Jungle variants
            Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.BAMBOO_JUNGLE,

            // Plains variants
            Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.MEADOW,

            // Water-related biomes
            Biome.RIVER, Biome.BEACH, Biome.OCEAN, Biome.LUKEWARM_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_OCEAN,

            // Other temperate biomes
            Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.MUSHROOM_FIELDS,

            // Windswept variants (that can have rain, not snow)
            Biome.WINDSWEPT_FOREST, Biome.WINDSWEPT_HILLS,
            Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.WINDSWEPT_SAVANNA,

            // Cave biomes where rain can reach
            Biome.LUSH_CAVES, Biome.DRIPSTONE_CAVES,

            // Cherry biome
            Biome.CHERRY_GROVE,

            // Savanna variants (which can have rain occasionally)
            Biome.SAVANNA, Biome.SAVANNA_PLATEAU,

            // Other biomes where it can rain
            Biome.STONY_SHORE, Biome.GROVE
    );

    public Chinchilla(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Chinchilla";
    }

    @Override
    public String getDescription() {
        return "You die instantly if you touch or enter water. You take suffocation damage when exposed to rain. " +
                "You can only eat fish.";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Start a task to apply suffocation damage when exposed to rain
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    rainDamageTasks.remove(playerId);
                    return;
                }

                // Check if the player is exposed to rain
                if (isExposedToRain(player)) {
                    double damage = configManager.getDouble("Chinchilla.rain-damage", 1.0);
                    player.damage(damage);
                }
            }
        }.runTaskTimer(plugin, 0L, configManager.getInt("Chinchilla.rain-damage-interval", 20)); // Damage every second (20 ticks)

        rainDamageTasks.put(playerId, task);
        return true;
    }

    @Override
    public void removeChallenge(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel the rain damage task if it exists
        if (rainDamageTasks.containsKey(playerId)) {
            rainDamageTasks.get(playerId).cancel();
            rainDamageTasks.remove(playerId);
        }
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof PlayerMoveEvent) {
            PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;

            // Check if the player is in water (touching or submerged)
            if (isInWater(player)) {
                player.setHealth(0); // Kill the player instantly
                player.sendMessage(ChatColor.RED + "You died from touching or entering water!");
                return true;
            }

            // Check if the player is exposed to rain and start the damage task if necessary
            if (isExposedToRain(player)) {
                if (!rainDamageTasks.containsKey(player.getUniqueId())) {
                    applyChallenge(player);
                }
            } else {
                // If the player is no longer exposed to rain, cancel the task
                if (rainDamageTasks.containsKey(player.getUniqueId())) {
                    rainDamageTasks.get(player.getUniqueId()).cancel();
                    rainDamageTasks.remove(player.getUniqueId());
                }
            }
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            Material food = consumeEvent.getItem().getType();

            if (!isAllowedFood(food)) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only eat fish!");
                return true;
            }
        }
        return false;
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().isLiquid() ||
                player.getLocation().getBlock().getType() == Material.WATER;
    }

    private boolean isExposedToRain(Player player) {
        World world = player.getWorld();

        // Check if it's raining and the player is in a biome where it can rain
        if (!world.hasStorm() || !canRainInBiome(world.getBiome(player.getLocation()))) {
            return false;
        }

        // Check if the player is directly exposed to the sky (no blocks above them)
        int playerY = player.getLocation().getBlockY();
        int highestBlockY = world.getHighestBlockYAt(player.getLocation());

        return playerY >= highestBlockY;
    }

    private boolean canRainInBiome(@NotNull Biome biome) {
        // Using a predefined list for more maintainable code
        return rainyBiomes.contains(biome);
    }

    private boolean isAllowedFood(Material food) {
        switch (food) {
            case COD:
            case COOKED_COD:
            case SALMON:
            case COOKED_SALMON:
            case TROPICAL_FISH:
            case PUFFERFISH:
                return true;
            default:
                return false;
        }
    }
}