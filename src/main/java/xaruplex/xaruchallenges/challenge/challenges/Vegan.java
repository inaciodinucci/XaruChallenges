package xaruplex.xaruchallenges.challenge.challenges;

import xaruplex.xaruchallenges.XaruChallenges;
import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

public class Vegan implements Challenge {

    private final XaruChallenges plugin;
    private final ConfigManager configManager;

    public Vegan(XaruChallenges plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "Vegan";
    }

    @Override
    public String getDescription() {
        return "You cannot consume any food of animal origin or use certain potions. " +
                "If you hit any living entity or eat non-vegan food, you receive divine punishment.";
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
            if (damageEvent.getDamager().equals(player) && damageEvent.getEntity() instanceof LivingEntity) {
                // Trigger divine punishment when the player hits any living entity
                executeDivinePunishment(player);
                return true;
            }
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            if (isNonVeganFood(consumeEvent.getItem().getType())) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot eat non-vegan food!");
                // Trigger divine punishment for attempting to eat non-vegan food
                executeDivinePunishment(player);
                return true;
            }
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            ItemStack item = interactEvent.getItem();
            if (item != null && isForbiddenPotion(item)) {
                interactEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use this potion!");
                return true;
            }
        }
        return false;
    }

    private boolean isNonVeganFood(Material material) {
        return configManager.getMaterialList("Vegan.disallowed-food").contains(material);
    }

    private boolean isForbiddenPotion(ItemStack item) {
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta != null) {
                // Check for forbidden potion effects
                return meta.hasCustomEffect(PotionEffectType.SLOWNESS) ||
                        meta.hasCustomEffect(PotionEffectType.WEAKNESS) ||
                        meta.hasCustomEffect(PotionEffectType.INVISIBILITY) ||
                        meta.hasCustomEffect(PotionEffectType.INSTANT_DAMAGE);
            }
        }
        return false;
    }

    private void executeDivinePunishment(Player player) {
        // Clear the player's inventory instantly
        player.getInventory().clear();

        // Strike the player with lightning multiple times
        int strikes = configManager.getInt("Vegan.divine-punishment.lightning-strikes", 30);
        for (int i = 0; i < strikes; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().strikeLightning(player.getLocation());
            }, i * 2L); // Strike every 2 ticks
        }

        // Kill the player after all lightning strikes
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.getHealth() > 0) {
                player.setHealth(0);
            }
        }, strikes * 2L + 5L);

        player.sendMessage(ChatColor.RED + "Divine punishment has been unleashed upon you for violating vegan principles!");
    }
}