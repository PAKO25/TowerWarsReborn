package io.github.pako25.towerWars.GameManagment;

import io.github.pako25.towerWars.CustomConfig;
import io.github.pako25.towerWars.Player.TWPlayer;
import io.github.pako25.towerWars.TowerWars;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PlayerStats {
    private static final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    private static Connection connection;
    public static boolean trackingEnabled = false;
    private static boolean isCacheNotUpToDate = true;

    private static final List<Pair<String, Integer>> gamesWonLeaderboard = new ArrayList<>();
    private static final List<Pair<String, Integer>> gamesLostLeaderboard = new ArrayList<>();
    private static final List<Pair<String, Integer>> mobKillsLeaderboard = new ArrayList<>();
    private static final List<Pair<String, Integer>> towersPlacedLeaderboard = new ArrayList<>();
    private static final List<Pair<String, Integer>> goldSpentLeaderboard = new ArrayList<>();
    private static final List<Pair<String, Integer>> mobsSentLeaderboard = new ArrayList<>();

    private final UUID uuid;
    private int games_won = 0;
    private int games_lost = 0;
    private int mob_kills = 0;
    private int towers_placed = 0;
    private int gold_spent = 0;
    private int mobs_sent = 0;

    private PlayerStats(UUID uuid) {
        this.uuid = uuid;
        loadStats();
    }

    public static PlayerStats getStats(UUID uuid) {
        if (playerStatsMap.containsKey(uuid))
            return playerStatsMap.get(uuid);
        playerStatsMap.put(uuid, new PlayerStats(uuid));
        return playerStatsMap.get(uuid);
    }

    public static void initialiseDatabseConnection() throws IOException, SQLException {
        FileConfiguration cfg = CustomConfig.getFileConfiguration("config");
        trackingEnabled = cfg.getBoolean("statTrackingEnabled");
        TowerWars.getPlugin().getLogger().info(trackingEnabled ? "Stat tracking enabled." : "Stat tracking disabled.");
        if (!trackingEnabled) return;

        File dbFile = new File(TowerWars.getPlugin().getDataFolder(), "stats.db");
        boolean existed = dbFile.exists();
        if (!existed) dbFile.createNewFile();
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        if (!existed) initialiseDatabaseOnCreate();
    }

    public static void closeServer() {
        for (UUID uuid : playerStatsMap.keySet())
            closePlayerInstance(uuid);
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    TowerWars.getPlugin().getLogger().info("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                TowerWars.getPlugin().getLogger().severe("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public static void closePlayerInstance(UUID uuid) {
        playerStatsMap.remove(uuid);
    }

    private static void initialiseDatabaseOnCreate() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        uuid TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        games_won INTEGER NOT NULL DEFAULT 0,
                        games_lost INTEGER NOT NULL DEFAULT 0,
                        mob_kills INTEGER NOT NULL DEFAULT 0,
                        towers_placed INTEGER NOT NULL DEFAULT 0,
                        gold_spent INTEGER NOT NULL DEFAULT 0,
                        mobs_sent INTEGER NOT NULL DEFAULT 0
                    );
                """);
        statement.close();
    }

    private void loadStats() {
        if (!trackingEnabled) return;
        String sql = """
                SELECT games_won,
                       games_lost,
                       mob_kills,
                       towers_placed,
                       gold_spent,
                       mobs_sent
                  FROM player_stats
                 WHERE uuid = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    games_won = rs.getInt("games_won");
                    games_lost = rs.getInt("games_lost");
                    mob_kills = rs.getInt("mob_kills");
                    towers_placed = rs.getInt("towers_placed");
                    gold_spent = rs.getInt("gold_spent");
                    mobs_sent = rs.getInt("mobs_sent");
                }
            }
        } catch (Exception e) {
            TowerWars.getPlugin().getLogger().severe("Error when reading data from the database!");
        }
    }

    public void saveToDatabase() {
        if (!trackingEnabled) return;
        String sql = """
                REPLACE INTO player_stats (
                    uuid,
                    games_won,
                    games_lost,
                    mob_kills,
                    towers_placed,
                    gold_spent,
                    mobs_sent,
                    name
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String name = TWPlayer.getTWPlayer(uuid).getPlayer().getName();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, games_won);
            ps.setInt(3, games_lost);
            ps.setInt(4, mob_kills);
            ps.setInt(5, towers_placed);
            ps.setInt(6, gold_spent);
            ps.setInt(7, mobs_sent);
            ps.setString(8, name);
            ps.executeUpdate();
        } catch (Exception e) {
            TowerWars.getPlugin().getLogger().severe("Error when saving data to the database!");
        }
        isCacheNotUpToDate = true;
    }

    public int getGames_won() {
        return games_won;
    }

    public int getGames_lost() {
        return games_lost;
    }

    public int getMob_kills() {
        return mob_kills;
    }

    public int getTowers_placed() {
        return towers_placed;
    }

    public int getGold_spent() {
        return gold_spent;
    }

    public int getMobs_sent() {
        return mobs_sent;
    }

    public void increaseGames_won() {
        games_won++;
    }

    public void increaseGames_lost() {
        games_lost++;
    }

    public void increaseMob_kills() {
        mob_kills++;
    }

    public void increaseTowers_placed() {
        towers_placed++;
    }

    public void increaseGold_spent(int amount) {
        gold_spent += amount;
    }

    public void increaseMobs_sent() {
        mobs_sent++;
    }

    private static void refreshCache() {
        isCacheNotUpToDate = false;
        if (!trackingEnabled) return;

        //games won
        gamesWonLeaderboard.clear();
        String sql1 = """
                SELECT games_won, name
                FROM player_stats
                ORDER BY games_won DESC
                LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql1)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    gamesWonLeaderboard.add(Pair.of(rs.getString("name"), rs.getInt("games_won")));
                }
            }
        } catch (Exception e) {
            TowerWars.getPlugin().getLogger().severe("Error reading games_won leaderboard: " + e.getMessage());
        }

        //games lost
        gamesLostLeaderboard.clear();
        String sql2 = """
                    SELECT games_lost, name
                      FROM player_stats
                     ORDER BY games_lost DESC
                     LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql2);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                gamesLostLeaderboard.add(Pair.of(
                        rs.getString("name"),
                        rs.getInt("games_lost")
                ));
            }
        } catch (SQLException e) {
            TowerWars.getPlugin().getLogger().severe("Error reading games_lost leaderboard: " + e.getMessage());
        }

        //mob kills
        mobKillsLeaderboard.clear();
        String sql3 = """
                    SELECT mob_kills, name
                      FROM player_stats
                     ORDER BY mob_kills DESC
                     LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql3);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mobKillsLeaderboard.add(Pair.of(
                        rs.getString("name"),
                        rs.getInt("mob_kills")
                ));
            }
        } catch (SQLException e) {
            TowerWars.getPlugin().getLogger().severe("Error reading mob_kills leaderboard: " + e.getMessage());
        }

        //towers placed
        towersPlacedLeaderboard.clear();
        String sql4 = """
                    SELECT towers_placed, name
                      FROM player_stats
                     ORDER BY towers_placed DESC
                     LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql4);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                towersPlacedLeaderboard.add(Pair.of(
                        rs.getString("name"),
                        rs.getInt("towers_placed")
                ));
            }
        } catch (SQLException e) {
            TowerWars.getPlugin().getLogger().severe("Error reading towers_placed leaderboard: " + e.getMessage());
        }

        //gold spent
        goldSpentLeaderboard.clear();
        String sql5 = """
                    SELECT gold_spent, name
                      FROM player_stats
                     ORDER BY gold_spent DESC
                     LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql5);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                goldSpentLeaderboard.add(Pair.of(
                        rs.getString("name"),
                        rs.getInt("gold_spent")
                ));
            }
        } catch (SQLException e) {
            TowerWars.getPlugin().getLogger().severe("Error reading gold_spent leaderboard: " + e.getMessage());
        }

        //mobs sent
        mobsSentLeaderboard.clear();
        String sql6 = """
                    SELECT mobs_sent, name
                      FROM player_stats
                     ORDER BY mobs_sent DESC
                     LIMIT 10
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql6);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mobsSentLeaderboard.add(Pair.of(
                        rs.getString("name"),
                        rs.getInt("mobs_sent")
                ));
            }
        } catch (SQLException e) {
            TowerWars.getPlugin().getLogger().severe("Error reading mobs_sent leaderboard: " + e.getMessage());
        }
    }

    public static List<Pair<String, Integer>> getGames_wonLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return gamesWonLeaderboard;
    }

    public static List<Pair<String, Integer>> getGames_lostLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return gamesLostLeaderboard;
    }

    public static List<Pair<String, Integer>> getMob_killsLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return mobKillsLeaderboard;
    }

    public static List<Pair<String, Integer>> getTowers_placedLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return towersPlacedLeaderboard;
    }

    public static List<Pair<String, Integer>> getGold_spentLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return goldSpentLeaderboard;
    }

    public static List<Pair<String, Integer>> getMobs_sentLeaderBoard() {
        if (isCacheNotUpToDate) refreshCache();
        return mobsSentLeaderboard;
    }
}