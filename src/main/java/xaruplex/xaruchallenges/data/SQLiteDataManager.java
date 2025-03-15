package xaruplex.xaruchallenges.data;

import xaruplex.xaruchallenges.challenge.Challenge;
import xaruplex.xaruchallenges.XaruChallenges;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteDataManager implements DataManager {

    private final XaruChallenges plugin;
    private Connection connection;

    public SQLiteDataManager(XaruChallenges plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/challenges.db");
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_challenges (" +
                                "uuid TEXT PRIMARY KEY," +
                                "challenges TEXT)"
                );
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }

    @Override
    public void saveChallenges(UUID playerId, List<Challenge> challenges) {
        String challengeNames = String.join(",", challenges.stream().map(Challenge::getName).toList());
        String sql = "REPLACE INTO player_challenges (uuid, challenges) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, challengeNames);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save challenges: " + e.getMessage());
        }
    }

    @Override
    public List<String> loadChallenges(UUID playerId) {
        List<String> challenges = new ArrayList<>();
        String sql = "SELECT challenges FROM player_challenges WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String[] parts = rs.getString("challenges").split(",");
                challenges.addAll(List.of(parts));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load challenges: " + e.getMessage());
        }
        return challenges;
    }
}