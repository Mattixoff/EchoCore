package Echostudios.common.api.player;

import Echostudios.common.api.player.data.PlayerData;
import Echostudios.common.api.player.data.PlayerInventory;
import Echostudios.common.api.player.data.PlayerStats;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API for player-related operations across all platforms
 */
public interface PlayerAPI {
    
    /**
     * Get a player by UUID
     * @param uuid Player UUID
     * @return Optional containing the player if found
     */
    Optional<Player> getPlayer(UUID uuid);
    
    /**
     * Get a player by name
     * @param name Player name
     * @return Optional containing the player if found
     */
    Optional<Player> getPlayer(String name);
    
    /**
     * Get all online players
     * @return Collection of online players
     */
    Collection<Player> getOnlinePlayers();
    
    /**
     * Get player count
     * @return Number of online players
     */
    int getPlayerCount();
    
    /**
     * Get maximum player count
     * @return Maximum number of players
     */
    int getMaxPlayerCount();
    
    /**
     * Check if a player is online
     * @param uuid Player UUID
     * @return true if online, false otherwise
     */
    boolean isOnline(UUID uuid);
    
    /**
     * Get player data from database
     * @param uuid Player UUID
     * @return CompletableFuture containing player data
     */
    CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid);
    
    /**
     * Save player data to database
     * @param playerData Player data to save
     * @return CompletableFuture that completes when save is done
     */
    CompletableFuture<Void> savePlayerData(PlayerData playerData);
    
    /**
     * Get player inventory
     * @param uuid Player UUID
     * @return CompletableFuture containing player inventory
     */
    CompletableFuture<Optional<PlayerInventory>> getPlayerInventory(UUID uuid);
    
    /**
     * Save player inventory
     * @param uuid Player UUID
     * @param inventory Inventory to save
     * @return CompletableFuture that completes when save is done
     */
    CompletableFuture<Void> savePlayerInventory(UUID uuid, PlayerInventory inventory);
    
    /**
     * Get player statistics
     * @param uuid Player UUID
     * @return CompletableFuture containing player stats
     */
    CompletableFuture<Optional<PlayerStats>> getPlayerStats(UUID uuid);
    
    /**
     * Update player statistics
     * @param uuid Player UUID
     * @param stats New statistics
     * @return CompletableFuture that completes when update is done
     */
    CompletableFuture<Void> updatePlayerStats(UUID uuid, PlayerStats stats);
    
    /**
     * Send message to player
     * @param uuid Player UUID
     * @param message Message to send
     * @return CompletableFuture that completes when message is sent
     */
    CompletableFuture<Void> sendMessage(UUID uuid, String message);
    
    /**
     * Send message to all online players
     * @param message Message to send
     * @return CompletableFuture that completes when messages are sent
     */
    CompletableFuture<Void> broadcastMessage(String message);
    
    /**
     * Send message to players with permission
     * @param message Message to send
     * @param permission Permission required
     * @return CompletableFuture that completes when messages are sent
     */
    CompletableFuture<Void> broadcastMessage(String message, String permission);
    
    /**
     * Kick a player
     * @param uuid Player UUID
     * @param reason Kick reason
     * @return CompletableFuture that completes when player is kicked
     */
    CompletableFuture<Void> kickPlayer(UUID uuid, String reason);
    
    /**
     * Ban a player
     * @param uuid Player UUID
     * @param reason Ban reason
     * @param duration Ban duration in milliseconds (0 for permanent)
     * @return CompletableFuture that completes when player is banned
     */
    CompletableFuture<Void> banPlayer(UUID uuid, String reason, long duration);
    
    /**
     * Unban a player
     * @param uuid Player UUID
     * @return CompletableFuture that completes when player is unbanned
     */
    CompletableFuture<Void> unbanPlayer(UUID uuid);
    
    /**
     * Check if player has permission
     * @param uuid Player UUID
     * @param permission Permission to check
     * @return CompletableFuture containing permission result
     */
    CompletableFuture<Boolean> hasPermission(UUID uuid, String permission);
    
    /**
     * Add permission to player
     * @param uuid Player UUID
     * @param permission Permission to add
     * @return CompletableFuture that completes when permission is added
     */
    CompletableFuture<Void> addPermission(UUID uuid, String permission);
    
    /**
     * Remove permission from player
     * @param uuid Player UUID
     * @param permission Permission to remove
     * @return CompletableFuture that completes when permission is removed
     */
    CompletableFuture<Void> removePermission(UUID uuid, String permission);
}
