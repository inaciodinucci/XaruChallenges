package xaruplex.xaruchallenges.data;

import java.util.List;
import java.util.UUID;

public interface DataManager {
    void saveChallenges(UUID playerId, List<String> challenges);
    List<String> loadChallenges(UUID playerId);
    void initialize();
    void shutdown();
}