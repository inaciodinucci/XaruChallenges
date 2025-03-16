package xaruplex.xaruchallenges.challenge.challenges;

import org.bukkit.Material;
import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
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
                "Regain health and hunger by attacking players or villagers. Killing players fully restores you. " +
                "You can only eat golden apples and drink potions (except Fire Resistance). " +
                "If you somehow gain Fire Resistance, you will die instantly.";
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

                // Check if it's daytime and player is exposed to sunlight (but not rain)
                if (isInSunlight(player)) {
                    double damage = configManager.getDouble("Vampire.damage-in-sunlight", 1.0);
                    player.setFireTicks(20); // Set on fire for 1 second
                    player.damage(damage);
                }

                // Apply night buffs
                if (isNight(player.getWorld())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 1, true, false)); // Jump Boost II
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

        // Remove night buffs
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    @Override
    public boolean handleEvent(Event event, Player player) {
        if (event instanceof EntityDamageByEntityEvent) {
            return handleDamageEvent((EntityDamageByEntityEvent) event, player);
        } else if (event instanceof EntityDeathEvent) {
            return handleDeathEvent((EntityDeathEvent) event, player);
        } else if (event instanceof FoodLevelChangeEvent) {
            return handleHungerEvent((FoodLevelChangeEvent) event, player);
        } else if (event instanceof PlayerItemConsumeEvent) {
            return handleConsumeEvent((PlayerItemConsumeEvent) event, player);
        } else if (event instanceof EntityPotionEffectEvent) {
            return handlePotionEffectEvent((EntityPotionEffectEvent) event, player);
        } else if (event instanceof PlayerInteractEntityEvent) {
            return handleInteractEntityEvent((PlayerInteractEntityEvent) event, player);
        }
        return false;
    }

    private boolean handleDamageEvent(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager().equals(player)) {
            if (event.getEntity() instanceof Player || event.getEntity() instanceof Villager) {
                // Heal the vampire and restore hunger when they hit a player or villager
                double currentHealth = player.getHealth();
                double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                double healAmount = 2.0; // Heal 1 heart per hit

                player.setHealth(Math.min(currentHealth + healAmount, maxHealth));
                player.setFoodLevel(Math.min(player.getFoodLevel() + 2, 20)); // Restore 1 hunger point
                player.setSaturation(Math.min(player.getSaturation() + 2, 20)); // Restore saturation

                // Penalize the victim
                if (event.getEntity() instanceof Player) {
                    Player victim = (Player) event.getEntity();
                    victim.damage(2.0); // Damage the victim
                    victim.setFoodLevel(Math.max(victim.getFoodLevel() - 2, 0)); // Reduce victim's hunger
                }

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

    private boolean handleConsumeEvent(PlayerItemConsumeEvent event, Player player) {
        Material itemType = event.getItem().getType();
        if (!isAllowedFood(itemType)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only eat golden apples and drink potions (except Fire Resistance)!");
            return true;
        }

        // Check if the potion is Fire Resistance
        if (itemType == Material.POTION || itemType == Material.SPLASH_POTION || itemType == Material.LINGERING_POTION) {
            PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
            if (meta != null && meta.hasCustomEffect(PotionEffectType.FIRE_RESISTANCE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use Fire Resistance potions!");
                return true;
            }
        }

        // Handle blood potion consumption
        if (itemType == Material.POTION && event.getItem().getItemMeta().getDisplayName().equals("Blood")) {
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            player.setHealth(Math.min(currentHealth + 4.0, maxHealth)); // Heal 2 hearts
            player.setFoodLevel(Math.min(player.getFoodLevel() + 4, 20)); // Restore 2 hunger points
            player.setSaturation(Math.min(player.getSaturation() + 4, 20)); // Restore saturation
            player.sendMessage(ChatColor.DARK_RED + "You drink the blood, restoring your health and hunger!");
            return true;
        }

        return false;
    }

    private boolean handlePotionEffectEvent(EntityPotionEffectEvent event, Player player) {
        if (event.getEntity().equals(player) && event.getNewEffect() != null) {
            // Check if the new effect is Fire Resistance
            if (event.getNewEffect().getType() == PotionEffectType.FIRE_RESISTANCE) {
                // Kill the player instantly
                player.setHealth(0);
                player.sendMessage(ChatColor.RED + "You have been killed for gaining Fire Resistance!");
                return true;
            }
        }
        return false;
    }

    private boolean handleInteractEntityEvent(PlayerInteractEntityEvent event, Player player) {
        if (event.getRightClicked() instanceof Player || event.getRightClicked() instanceof Villager) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.GLASS_BOTTLE) {
                Entity target = event.getRightClicked();

                // Harm the victim (player or villager)
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    livingTarget.damage(2.0); // Damage the victim
                    if (target instanceof Player) {
                        Player victim = (Player) target;
                        victim.setFoodLevel(Math.max(victim.getFoodLevel() - 2, 0)); // Reduce victim's hunger
                    }
                }

                // Heal the vampire and restore hunger
                double currentHealth = player.getHealth();
                double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                player.setHealth(Math.min(currentHealth + 2.0, maxHealth)); // Heal 1 heart
                player.setFoodLevel(Math.min(player.getFoodLevel() + 2, 20)); // Restore 1 hunger point
                player.setSaturation(Math.min(player.getSaturation() + 2, 20)); // Restore saturation

                // Create a blood potion
                ItemStack bloodPotion = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) bloodPotion.getItemMeta();
                meta.setDisplayName("Blood");
                bloodPotion.setItemMeta(meta);

                // Remove the glass bottle and add the blood potion
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    player.getInventory().setItemInMainHand(bloodPotion);
                } else {
                    player.getInventory().addItem(bloodPotion);
                }

                player.sendMessage(ChatColor.DARK_RED + "You have collected blood!");
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedFood(Material material) {
        return material == Material.GOLDEN_APPLE || material == Material.ENCHANTED_GOLDEN_APPLE || material == Material.POTION;
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

        // Check if it's raining (rain does not cause sunlight damage)
        if (world.hasStorm()) {
            return false;
        }

        // Check light level from the sky
        int lightLevel = player.getLocation().getBlock().getLightFromSky();
        int threshold = configManager.getInt("Vampire.light-level-threshold", 12);
        return lightLevel >= threshold;
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }
}