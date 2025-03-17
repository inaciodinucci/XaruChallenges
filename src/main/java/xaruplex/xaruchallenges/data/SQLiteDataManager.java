package xaruplex.xaruchallenges.data;

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
            connection.setAutoCommit(true); // Ensure auto-commit is enabled

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_challenges (" +
                                "uuid TEXT PRIMARY KEY, " +
                                "challenges TEXT" +
                                ")"
                );
            }
            plugin.getLogger().info("SQLite database initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveChallenges(UUID playerId, List<String> challenges) {
        if (challenges == null || challenges.isEmpty()) {
            // If no challenges, delete the entry
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM player_challenges WHERE uuid = ?")) {
                stmt.setString(1, playerId.toString());
                stmt.executeUpdate();
                plugin.getLogger().info("Removed challenges entry for " + playerId);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete challenges: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // Join challenge names with comma
        String challengeNames = String.join(",", challenges);

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_challenges (uuid, challenges) VALUES (?, ?)")) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, challengeNames);
            stmt.executeUpdate();
            plugin.getLogger().info("Saved challenges for " + playerId + ": " + challengeNames);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save challenges: " + e.getMessage());
            e.printStackTrace();
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
                String challengeData = rs.getString("challenges");
                if (challengeData != null && !challengeData.isEmpty()) {
                    String[] parts = challengeData.split(",");
                    for (String part : parts) {
                        if (!part.trim().isEmpty()) {
                            challenges.add(part.trim());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load challenges: " + e.getMessage());
            e.printStackTrace();
        }
        return challenges;
    }
}