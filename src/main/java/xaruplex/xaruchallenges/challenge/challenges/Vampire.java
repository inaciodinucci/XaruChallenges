package xaruplex.xaruchallenges.challenge.challenges;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vampire implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<UUID, Long> lastHungerDecay = new HashMap<>();
    private final Map<UUID, Long> lastBloodDrain = new HashMap<>();
    private static final long HUNGER_DECAY_DELAY = 8000; // 8 seconds (4x slower than normal)
    private static final long BLOOD_DRAIN_COOLDOWN = 1000; // 1 second cooldown

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
                "Right-click on players or villagers with an empty bottle to collect their blood. ";
    }

    @Override
    public boolean applyChallenge(Player player) {
        UUID playerId = player.getUniqueId();
        lastHungerDecay.put(playerId, System.currentTimeMillis());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(playerId);
                    return;
                }

                if (isInSunlight(player)) {
                    double damage = configManager.getDouble("Vampire.damage-in-sunlight", 1.0);
                    player.setFireTicks(20);
                    player.damage(damage);
                }

                // Apply Night Vision regardless of the time of day
                applyNightBuffs(player);

                // Apply night-specific buffs only during the night
                if (isNight(player.getWorld())) {
                    applyNightSpecificBuffs(player);
                }

                // Prevent insomnia (phantoms) by resetting the player's insomnia timer
                player.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);

                // Make villagers afraid of the vampire
                makeVillagersAfraid(player);
            }
        }.runTaskTimer(plugin, 0L, 10L);

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
        removeNightBuffs(player);
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
        } else if (event instanceof PlayerInteractAtEntityEvent) {
            return handleInteractAtEntityEvent((PlayerInteractAtEntityEvent) event, player);
        }
        return false;
    }

    private boolean handleDamageEvent(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager().equals(player) && (event.getEntity() instanceof Player || event.getEntity() instanceof Villager)) {
            healPlayer(player, 2.0, 2, 2);

            if (event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();
                victim.damage(2.0);
                victim.setFoodLevel(Math.max(victim.getFoodLevel() - 2, 0));
            }

            player.sendMessage(ChatColor.DARK_RED + "You drain their life force!");
            return true;
        }
        return false;
    }

    private boolean handleDeathEvent(EntityDeathEvent event, Player player) {
        if (event.getEntity().getKiller() != null && event.getEntity().getKiller().equals(player)) {
            if (event.getEntity() instanceof Player) {
                healPlayer(player, player.getAttribute(Attribute.MAX_HEALTH).getValue(), 20, 20);
                player.sendMessage(ChatColor.DARK_RED + "You feast on their blood, restoring yourself completely!");
                return true;
            }
        }
        return false;
    }

    private boolean handleHungerEvent(FoodLevelChangeEvent event, Player player) {
        if (!event.getEntity().equals(player)) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHungerDecay.getOrDefault(player.getUniqueId(), 0L) < HUNGER_DECAY_DELAY) {
            event.setCancelled(true);
            return true;
        }

        lastHungerDecay.put(player.getUniqueId(), currentTime);
        return false;
    }

    private boolean handleConsumeEvent(PlayerItemConsumeEvent event, Player player) {
        ItemStack item = event.getItem();

        if (isBloodPotion(item)) {
            event.setCancelled(true);

            if (!activeTasks.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "The blood burns in your throat! Only vampires can drink this.");
                return true;
            }

            // Apply stored effects from the blood potion
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta != null) {
                for (PotionEffect effect : meta.getCustomEffects()) {
                    if (effect.getType() == PotionEffectType.REGENERATION) {
                        healPlayer(player, 4.0, 5, 6);
                    } else if (effect.getType() == PotionEffectType.FIRE_RESISTANCE) {
                        player.setHealth(0);
                        player.sendMessage(ChatColor.RED + "The blood contained Fire Resistance! You have been killed!");
                        return true;
                    }
                }
            }

            player.sendMessage(ChatColor.DARK_RED + "You drink the blood, restoring your health and hunger!");
            return true;
        }

        if (!isAllowedFood(item.getType())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only eat golden apples and drink potions (except Fire Resistance)!");
            return true;
        }

        if (isFireResistancePotion(item)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use Fire Resistance potions!");
            return true;
        }

        return false;
    }

    private boolean handlePotionEffectEvent(EntityPotionEffectEvent event, Player player) {
        if (event.getEntity().equals(player) && event.getNewEffect() != null &&
                event.getNewEffect().getType() == PotionEffectType.FIRE_RESISTANCE) {
            player.setHealth(0);
            player.sendMessage(ChatColor.RED + "You have been killed for gaining Fire Resistance!");
            return true;
        }
        return false;
    }

    private boolean handleInteractEntityEvent(PlayerInteractEntityEvent event, Player player) {
        return processEntityInteraction(player, event.getRightClicked(), event.getHand());
    }

    private boolean handleInteractAtEntityEvent(PlayerInteractAtEntityEvent event, Player player) {
        return processEntityInteraction(player, event.getRightClicked(), event.getHand());
    }

    private boolean processEntityInteraction(Player player, Entity target, EquipmentSlot hand) {
        if (!activeTasks.containsKey(player.getUniqueId())) return false;

        // Check if the player is sneaking (shift + right-click)
        if (!player.isSneaking()) return false;

        // Ensure the interaction is with the main hand
        if (hand != EquipmentSlot.HAND) return false;

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBloodDrain.getOrDefault(player.getUniqueId(), 0L) < BLOOD_DRAIN_COOLDOWN) {
            return false;
        }

        // Ensure the target is a player or villager
        if (!(target instanceof Player) && !(target instanceof Villager)) return false;

        // Ensure the player is holding an empty glass bottle
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.GLASS_BOTTLE || item.getAmount() != 1) return false;

        lastBloodDrain.put(player.getUniqueId(), currentTime);

        // Create blood potion with stored effects
        ItemStack bloodPotion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bloodPotion.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_RED + "Blood");
            meta.setColor(org.bukkit.Color.fromRGB(139, 0, 0)); // Dark red color

            // Store regeneration effect for health and hunger restoration
            meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 0, false, false), true);

            // If target has Fire Resistance, store it in the blood (will kill vampire when consumed)
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (livingTarget.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1, 0, false, false), true);
                }
            }

            bloodPotion.setItemMeta(meta);
        }

        // Damage and hunger drain logic
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            livingTarget.damage(2.0);

            if (target instanceof Player) {
                Player victim = (Player) target;
                victim.setFoodLevel(Math.max(victim.getFoodLevel() - 3, 0));
                victim.setSaturation(Math.max(victim.getSaturation() - 3, 0));
                victim.sendMessage(ChatColor.RED + "A vampire has drained your blood into a bottle!");
            }
        }

        // Give blood potion to vampire
        if (item.getAmount() == 1) {
            player.getInventory().setItemInMainHand(bloodPotion);
        } else {
            item.setAmount(item.getAmount() - 1);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(bloodPotion);
            if (!overflow.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), bloodPotion);
            }
        }

        player.sendMessage(ChatColor.DARK_RED + "You collected blood in a bottle! Drink it to restore yourself.");
        return true;
    }

    private void healPlayer(Player player, double health, int food, float saturation) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(player.getHealth() + health, maxHealth));
        player.setFoodLevel(Math.min(player.getFoodLevel() + food, 20));
        player.setSaturation(Math.min(player.getSaturation() + saturation, 20));
    }

    private boolean isAllowedFood(Material material) {
        return material == Material.GOLDEN_APPLE ||
                material == Material.ENCHANTED_GOLDEN_APPLE ||
                material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION;
    }

    private boolean isFireResistancePotion(ItemStack item) {
        if (item.getType() == Material.POTION ||
                item.getType() == Material.SPLASH_POTION ||
                item.getType() == Material.LINGERING_POTION) {

            PotionMeta meta = (PotionMeta) item.getItemMeta();
            return meta != null && meta.hasCustomEffect(PotionEffectType.FIRE_RESISTANCE);
        }
        return false;
    }

    private boolean isBloodPotion(ItemStack item) {
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            return meta.hasDisplayName() &&
                    ChatColor.stripColor(meta.getDisplayName()).equals("Blood") &&
                    meta.getColor().equals(org.bukkit.Color.fromRGB(139, 0, 0));
        }
        return false;
    }

    private boolean isInSunlight(Player player) {
        World world = player.getWorld();
        if (world.hasStorm()) return false;

        long time = world.getTime();
        if (time >= 13000 && time <= 23000) return false;

        Block highestBlock = world.getHighestBlockAt(player.getLocation());
        if (player.getLocation().getY() < highestBlock.getY()) return false;

        return player.getLocation().getBlock().getLightFromSky() >= 12;
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    private void applyNightBuffs(Player player) {
        // Apply Night Vision regardless of the time of day
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, true, false));
    }

    private void applyNightSpecificBuffs(Player player) {
        // Apply night-specific buffs (Speed and Jump Boost)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 1, true, false));
    }

    private void removeNightBuffs(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    private void makeVillagersAfraid(Player player) {
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                // Make the villager run away from the vampire
                villager.setTarget(player);
            }
        }
    }
}