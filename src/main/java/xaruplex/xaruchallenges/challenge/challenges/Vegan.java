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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

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
        return "You cannot attack any entity or consume any food of animal origin. " +
                "If you kill any living entity, you receive divine punishment.";
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
            if (damageEvent.getEntity() instanceof LivingEntity) {
                damageEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot harm living beings!");
                return true;
            }
        } else if (event instanceof EntityDeathEvent) {
            EntityDeathEvent deathEvent = (EntityDeathEvent) event;
            if (deathEvent.getEntity().getKiller() != null &&
                    deathEvent.getEntity().getKiller().equals(player)) {
                executeDivinePunishment(player);
                return true;
            }
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
            if (isNonVeganFood(consumeEvent.getItem().getType())) {
                consumeEvent.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot eat non-vegan food!");
                return true;
            }
        }
        return false;
    }

    private boolean isNonVeganFood(Material material) {
        return configManager.getMaterialList("Vegan.disallowed-food").contains(material);
    }

    private void executeDivinePunishment(Player player) {
        int strikes = configManager.getInt("Vegan.divine-punishment.lightning-strikes", 30);


        for (int i = 0; i < strikes; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().strikeLightning(player.getLocation());
            }, i * 2L); // Strike every 2 ticks
        }

        player.getInventory().clear();


        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.getHealth() > 0) {
                player.setHealth(0);
            }
        }, strikes * 2L + 5L);

        player.sendMessage(ChatColor.RED + "Divine punishment has been unleashed upon you!");
    }
}