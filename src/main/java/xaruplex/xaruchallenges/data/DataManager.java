package xaruplex.xaruchallenges.data;

import xaruplex.xaruchallenges.challenge.Challenge;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public interface DataManager {
    void saveChallenges(UUID playerId, List<Challenge> challenges);
    List<String> loadChallenges(UUID playerId);
    void initialize();
    void shutdown();
}