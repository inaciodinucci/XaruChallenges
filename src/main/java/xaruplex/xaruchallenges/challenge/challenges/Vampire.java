package xaruplex.xaruchallenges.challenge.challenges;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
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
    private static final long HUNGER_DECAY_DELAY = 8000;
    private static final long BLOOD_DRAIN_COOLDOWN = 1000;
    private static final Material BLOOD_MATERIAL = Material.POTION;

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
        return "You can only walk in shadows and at night. Sunlight causes fire damage. "
                + "Regain health and hunger by attacking players or villagers. Killing players fully restores you. "
                + "You can only eat golden apples and drink potions (except Fire Resistance). "
                + "Hit players or villagers with an empty bottle to collect their blood.";
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

                applyNightBuffs(player);
                if (isNight(player.getWorld())) {
                    applyNightSpecificBuffs(player);
                }

                player.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);
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
        }
        return false;
    }

    private boolean handleDamageEvent(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager().equals(player)) {
            // Handle blood bottle collection
            if ((event.getEntity() instanceof Player || event.getEntity() instanceof Villager) &&
                    isHoldingEmptyBottle(player)) {

                // Check cooldown
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBloodDrain.getOrDefault(player.getUniqueId(), 0L) < BLOOD_DRAIN_COOLDOWN) {
                    player.sendMessage(ChatColor.RED + "You must wait before draining blood again!");
                    return false;
                }

                lastBloodDrain.put(player.getUniqueId(), currentTime);

                // Process blood collection
                LivingEntity target = (LivingEntity) event.getEntity();
                collectBlood(player, target);

                event.setCancelled(true);
                return true;
            }

            // Regular vampire attack
            else if (event.getEntity() instanceof Player || event.getEntity() instanceof Villager) {
                event.setCancelled(true);
                healPlayer(player, 2.0, 2, 2);

                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.damage(2.0);

                    if (target instanceof Player) {
                        Player victim = (Player) target;
                        victim.setFoodLevel(Math.max(victim.getFoodLevel() - 2, 0));
                    }
                }

                player.sendMessage(ChatColor.DARK_RED + "You drain their life force!");
                return true;
            }
        }
        return false;
    }

    private boolean isHoldingEmptyBottle(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return mainHand.getType() == Material.GLASS_BOTTLE || offHand.getType() == Material.GLASS_BOTTLE;
    }

    private void collectBlood(Player player, LivingEntity target) {
        // Create blood potion
        ItemStack bloodPotion = createBloodPotion(target);

        // Damage logic
        target.damage(2.0, player);

        if (target instanceof Player) {
            Player victim = (Player) target;
            victim.setFoodLevel(Math.max(victim.getFoodLevel() - 3, 0));
            victim.setSaturation(Math.max(victim.getSaturation() - 3, 0));
            victim.sendMessage(ChatColor.RED + "A vampire has drained your blood into a bottle!");
        }

        // Heal vampire
        healPlayer(player, 2.0, 2, 2);

        // Find and process bottle
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.GLASS_BOTTLE) {
            mainHand.setAmount(mainHand.getAmount() - 1);

            // Try to add blood to existing stack or create new one
            addBloodToInventory(player, bloodPotion);
        } else {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            offHand.setAmount(offHand.getAmount() - 1);

            // Try to add blood to existing stack or create new one
            addBloodToInventory(player, bloodPotion);
        }

        player.sendMessage(ChatColor.DARK_RED + "You collected blood in a bottle! Drink it to restore yourself.");
    }

    private void addBloodToInventory(Player player, ItemStack bloodPotion) {
        // Try to find an existing blood stack first
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isBloodPotion(item) && item.getAmount() < 64) {
                item.setAmount(item.getAmount() + 1);
                return;
            }
        }

        // No existing stack or all stacks are full, add as new item
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodPotion);
        leftover.values().forEach(leftoverItem ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem)
        );
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

            healPlayer(player, 2.0, 2, 2);

            // Consume the potion
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.isSimilar(item)) {
                if (mainHand.getAmount() > 1) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                }
            } else {
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (offHand.getAmount() > 1) {
                    offHand.setAmount(offHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInOffHand(new ItemStack(Material.GLASS_BOTTLE));
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

    private ItemStack createBloodPotion(LivingEntity target) {
        ItemStack bloodPotion = new ItemStack(BLOOD_MATERIAL);
        PotionMeta meta = (PotionMeta) bloodPotion.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_RED + "Blood");
        meta.setColor(org.bukkit.Color.fromRGB(139, 0, 0));

        bloodPotion.setItemMeta(meta);
        return bloodPotion;
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
            if (meta != null) {
                // Check base potion type
                PotionType baseType = meta.getBasePotionType();
                if (baseType != null && baseType.getEffectType() == PotionEffectType.FIRE_RESISTANCE) {
                    return true;
                }

                // Check custom effects
                for (PotionEffect effect : meta.getCustomEffects()) {
                    if (effect.getType() == PotionEffectType.FIRE_RESISTANCE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isBloodPotion(ItemStack item) {
        if (item.getType() == BLOOD_MATERIAL && item.hasItemMeta()) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            return meta != null &&
                    meta.hasDisplayName() &&
                    ChatColor.stripColor(meta.getDisplayName()).equals("Blood") &&
                    meta.getColor() != null &&
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, true, false));
    }

    private void applyNightSpecificBuffs(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 1, true, false));
    }

    private void removeNightBuffs(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }
}