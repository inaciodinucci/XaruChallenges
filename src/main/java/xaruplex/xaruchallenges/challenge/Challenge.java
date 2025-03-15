package xaruplex.xaruchallenges.challenge;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Interface for all challenges in the XaruChallenges plugin.
 */
public interface Challenge {

    /**
     * Get the name of this challenge.
     *
     * @return The challenge name
     */
    String getName();

    /**
     * Get the description of this challenge.
     *
     * @return The challenge description
     */
    String getDescription();

    /**
     * Apply this challenge to a player.
     *
     * @param player The player to apply the challenge to
     * @return True if the challenge was successfully applied, false otherwise
     */
    boolean applyChallenge(Player player);

    /**
     * Remove this challenge from a player.
     *
     * @param player The player to remove the challenge from
     */
    void removeChallenge(Player player);

    /**
     * Handle an event for this challenge.
     *
     * @param event The event that occurred
     * @param player The player associated with the event
     * @return True if the event was handled, false otherwise
     */
    boolean handleEvent(Event event, Player player);
}