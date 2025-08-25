package Echostudios.utils;

import Echostudios.EchoCore;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    
    private final EchoCore plugin;
    private final String databaseType;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    
    private Connection connection;
    private boolean isConnected = false;
    
    public DatabaseManager(EchoCore plugin) {
        this.plugin = plugin;
        
        // Try to get database config from server.yml first, fallback to main config
        ConfigurationSection dbConfig = null;
        try {
            dbConfig = plugin.getServerConfig().getConfigurationSection("database");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load database config from server.yml, trying main config...");
            dbConfig = plugin.getConfig().getConfigurationSection("database");
        }
        
        if (dbConfig == null) {
            plugin.getLogger().severe("No database configuration found! Using default SQLite settings.");
            this.databaseType = "sqlite";
            this.host = "localhost";
            this.port = 3306;
            this.database = "echocore";
            this.username = "root";
            this.password = "";
            this.tablePrefix = "ec_";
        } else {
            this.databaseType = dbConfig.getString("type", "sqlite").toLowerCase();
            this.host = dbConfig.getString("host", "localhost");
            this.port = dbConfig.getInt("port", 3306);
            this.database = dbConfig.getString("database", "echocore");
            this.username = dbConfig.getString("username", "root");
            this.password = dbConfig.getString("password", "");
            this.tablePrefix = dbConfig.getString("table-prefix", "ec_");
        }
        
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            if (databaseType.equals("mysql")) {
                initializeMySQL();
            } else {
                initializeSQLite();
            }
            
            createTables();
            isConnected = true;
            plugin.getLogger().info("Database connected successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            isConnected = false;
        }
    }
    
    private void initializeMySQL() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8", 
                host, port, database);
        connection = DriverManager.getConnection(url, username, password);
    }
    
    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + new File(dataFolder, "echocore.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }
    
    private void createTables() throws SQLException {
        // Players table
        String playersTable = String.format(
            "CREATE TABLE IF NOT EXISTS %splayers (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "username VARCHAR(16) NOT NULL, " +
            "first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "playtime BIGINT DEFAULT 0, " +
            "warnings INT DEFAULT 0, " +
            "muted BOOLEAN DEFAULT FALSE, " +
            "mute_reason TEXT, " +
            "banned BOOLEAN DEFAULT FALSE, " +
            "ban_reason TEXT, " +
            "ban_expires TIMESTAMP NULL" +
            ")", tablePrefix
        );
        
        // Moderation logs table
        String logsTable = String.format(
            "CREATE TABLE IF NOT EXISTS %smoderation_logs (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "action VARCHAR(20) NOT NULL, " +
            "target_uuid VARCHAR(36) NOT NULL, " +
            "target_username VARCHAR(16) NOT NULL, " +
            "moderator_uuid VARCHAR(36) NOT NULL, " +
            "moderator_username VARCHAR(16) NOT NULL, " +
            "reason TEXT, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "expires TIMESTAMP NULL" +
            ")", tablePrefix
        );
        
        // Player statistics table
        String statsTable = String.format(
            "CREATE TABLE IF NOT EXISTS %splayer_stats (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "total_logins INT DEFAULT 0, " +
            "total_playtime BIGINT DEFAULT 0, " +
            "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (uuid) REFERENCES %splayers(uuid) ON DELETE CASCADE" +
            ")", tablePrefix, tablePrefix
        );
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(logsTable);
            stmt.execute(statsTable);
        }
    }
    
    public CompletableFuture<Boolean> isPlayerBanned(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT banned, ban_expires FROM %splayers WHERE uuid = ?", tablePrefix);
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            boolean banned = rs.getBoolean("banned");
                            Timestamp expires = rs.getTimestamp("ban_expires");
                            
                            if (banned && expires != null && expires.before(new Timestamp(System.currentTimeMillis()))) {
                                // Ban expired, remove it
                                removeBan(playerUUID);
                                return false;
                            }
                            return banned;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking ban status", e);
            }
            return false;
        });
    }
    
    public CompletableFuture<Boolean> isPlayerMuted(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT muted FROM %splayers WHERE uuid = ?", tablePrefix);
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getBoolean("muted");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking mute status", e);
            }
            return false;
        });
    }
    
    public CompletableFuture<Integer> getPlayerWarnings(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT warnings FROM %splayers WHERE uuid = ?", tablePrefix);
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() ? rs.getInt("warnings") : 0;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting warnings", e);
            }
            return 0;
        });
    }
    
    public CompletableFuture<Void> savePlayer(UUID playerUUID, String username) {
        return CompletableFuture.runAsync(() -> {
            try {
                // SQLite doesn't support ON DUPLICATE KEY UPDATE, so we use INSERT OR REPLACE
                String sql = String.format(
                    "INSERT OR REPLACE INTO %splayers (uuid, username, last_join) VALUES (?, ?, CURRENT_TIMESTAMP)", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                }
                
                // Update stats
                updatePlayerStats(playerUUID);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving player", e);
            }
        });
    }
    
    public CompletableFuture<Void> banPlayer(UUID playerUUID, String reason, String moderator, long duration) {
        return CompletableFuture.runAsync(() -> {
            try {
                Timestamp expires = duration > 0 ? new Timestamp(System.currentTimeMillis() + duration) : null;
                
                String sql = String.format(
                    "UPDATE %splayers SET banned = TRUE, ban_reason = ? WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, reason);
                    stmt.setString(2, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                logModerationAction("BAN", playerUUID, moderator, reason, expires);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error banning player", e);
            }
        });
    }
    
    public CompletableFuture<Void> unbanPlayer(UUID playerUUID, String moderator) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = String.format(
                    "UPDATE %splayers SET banned = FALSE, ban_reason = NULL, ban_expires = NULL WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                logModerationAction("UNBAN", playerUUID, moderator, "Player unbanned", null);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error unbanning player", e);
            }
        });
    }
    
    public CompletableFuture<Void> mutePlayer(UUID playerUUID, String reason, String moderator, long duration) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = String.format(
                    "UPDATE %splayers SET muted = TRUE, mute_reason = ? WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, reason);
                    stmt.setString(2, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                Timestamp expires = duration > 0 ? new Timestamp(System.currentTimeMillis() + duration) : null;
                logModerationAction("MUTE", playerUUID, moderator, reason, expires);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error muting player", e);
            }
        });
    }
    
    public CompletableFuture<Void> unmutePlayer(UUID playerUUID, String moderator) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = String.format(
                    "UPDATE %splayers SET muted = FALSE, mute_reason = NULL WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                logModerationAction("UNMUTE", playerUUID, moderator, "Player unmuted", null);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error unmuting player", e);
            }
        });
    }
    
    public CompletableFuture<Void> warnPlayer(UUID playerUUID, String reason, String moderator) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = String.format(
                    "UPDATE %splayers SET warnings = warnings + 1 WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                logModerationAction("WARN", playerUUID, moderator, reason, null);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error warning player", e);
            }
        });
    }
    
    public CompletableFuture<Void> unwarnPlayer(UUID playerUUID, String moderator) {
        return CompletableFuture.runAsync(() -> {
            try {
                // SQLite doesn't support GREATEST function, so we use CASE statement
                String sql = String.format(
                    "UPDATE %splayers SET warnings = CASE WHEN warnings > 0 THEN warnings - 1 ELSE 0 END WHERE uuid = ?", tablePrefix
                );
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.executeUpdate();
                }
                
                // Log the action
                logModerationAction("UNWARN", playerUUID, moderator, "Warning removed", null);
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error removing warning", e);
            }
        });
    }
    
    private void removeBan(UUID playerUUID) {
        try {
            String sql = String.format(
                "UPDATE %splayers SET banned = FALSE, ban_reason = NULL, ban_expires = NULL WHERE uuid = ?", tablePrefix
            );
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing expired ban", e);
        }
    }
    
    private void logModerationAction(String action, UUID targetUUID, String moderator, String reason, Timestamp expires) {
        try {
            String sql = String.format(
                "INSERT INTO %smoderation_logs (action, target_uuid, target_username, moderator_uuid, moderator_username, reason, expires) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", tablePrefix
            );
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, action);
                stmt.setString(2, targetUUID.toString());
                stmt.setString(3, getPlayerUsername(targetUUID));
                stmt.setString(4, "00000000-0000-0000-0000-000000000000"); // Console UUID
                stmt.setString(5, moderator);
                stmt.setString(6, reason);
                stmt.setTimestamp(7, expires);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error logging moderation action", e);
        }
    }
    
    private String getPlayerUsername(UUID playerUUID) {
        try {
            String sql = String.format("SELECT username FROM %splayers WHERE uuid = ?", tablePrefix);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getString("username") : "Unknown";
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting username", e);
        }
        return "Unknown";
    }
    
    private void updatePlayerStats(UUID playerUUID) {
        try {
            // SQLite doesn't support ON DUPLICATE KEY UPDATE, so we use INSERT OR REPLACE
            // First check if record exists
            String checkSql = String.format("SELECT total_logins FROM %splayer_stats WHERE uuid = ?", tablePrefix);
            int currentLogins = 0;
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerUUID.toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        currentLogins = rs.getInt("total_logins");
                    }
                }
            }
            
            // Insert or replace with updated count
            String sql = String.format(
                "INSERT OR REPLACE INTO %splayer_stats (uuid, total_logins, last_seen) VALUES (?, ?, CURRENT_TIMESTAMP)", tablePrefix
            );
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setInt(2, currentLogins + 1);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating player stats", e);
        }
    }
    
    public boolean isConnected() {
        return isConnected && connection != null;
    }
    
    // New methods for StatsCommand
    public CompletableFuture<String> getFirstJoin(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT first_join FROM %splayers WHERE uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Timestamp firstJoin = rs.getTimestamp("first_join");
                            return firstJoin != null ? firstJoin.toString() : null;
                        }
                    }
                }
                return null;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting first join", e);
                return null;
            }
        });
    }
    
    public CompletableFuture<String> getLastSeen(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT last_join FROM %splayers WHERE uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Timestamp lastJoin = rs.getTimestamp("last_join");
                            return lastJoin != null ? lastJoin.toString() : null;
                        }
                    }
                }
                return null;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting last seen", e);
                return null;
            }
        });
    }
    
    public CompletableFuture<Integer> getTotalLogins(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT total_logins FROM %splayer_stats WHERE uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("total_logins");
                        }
                    }
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting total logins", e);
                return 0;
            }
        });
    }
    
    public CompletableFuture<Integer> getBans(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT COUNT(*) as ban_count FROM %smoderation_logs WHERE action = 'BAN' AND target_uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("ban_count");
                        }
                    }
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting ban count", e);
                return 0;
            }
        });
    }
    
    public CompletableFuture<Integer> getKicks(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT COUNT(*) as kick_count FROM %smoderation_logs WHERE action = 'KICK' AND target_uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("kick_count");
                        }
                    }
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting kick count", e);
                return 0;
            }
        });
    }
    
    public CompletableFuture<Integer> getMutes(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = String.format("SELECT COUNT(*) as mute_count FROM %smoderation_logs WHERE action = 'MUTE' AND target_uuid = ?", tablePrefix);
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("mute_count");
                        }
                    }
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting mute count", e);
                return 0;
            }
        });
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
        }
    }
}
