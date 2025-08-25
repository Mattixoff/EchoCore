package Echostudios.common.api.chat;

import Echostudios.common.api.player.Player;
import Echostudios.common.api.chat.data.ChatMessage;
import Echostudios.common.api.chat.data.ChatTag;
import Echostudios.common.api.chat.data.InventoryTag;
import Echostudios.common.api.chat.data.ItemTag;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API for advanced chat functionality including tagging system
 */
public interface ChatAPI {
    
    /**
     * Send a chat message
     * @param sender Sender UUID
     * @param message Message content
     * @param format Chat format to use
     * @return CompletableFuture containing the sent message
     */
    CompletableFuture<ChatMessage> sendMessage(UUID sender, String message, String format);
    
    /**
     * Process a chat message with tags
     * @param sender Sender UUID
     * @param message Raw message content
     * @return CompletableFuture containing the processed message
     */
    CompletableFuture<ChatMessage> processMessage(UUID sender, String message);
    
    /**
     * Get chat format for a player
     * @param uuid Player UUID
     * @return Chat format string
     */
    String getChatFormat(UUID uuid);
    
    /**
     * Set chat format for a player
     * @param uuid Player UUID
     * @param format Chat format
     * @return CompletableFuture that completes when format is set
     */
    CompletableFuture<Void> setChatFormat(UUID uuid, String format);
    
    /**
     * Get chat tags for a player
     * @param uuid Player UUID
     * @return Collection of chat tags
     */
    Collection<ChatTag> getChatTags(UUID uuid);
    
    /**
     * Add chat tag to player
     * @param uuid Player UUID
     * @param tag Chat tag to add
     * @return CompletableFuture that completes when tag is added
     */
    CompletableFuture<Void> addChatTag(UUID uuid, ChatTag tag);
    
    /**
     * Remove chat tag from player
     * @param uuid Player UUID
     * @param tag Chat tag to remove
     * @return CompletableFuture that completes when tag is removed
     */
    CompletableFuture<Void> removeChatTag(UUID uuid, ChatTag tag);
    
    /**
     * Process inventory tag [inv]
     * @param sender Sender UUID
     * @param message Original message
     * @return Processed message with inventory preview
     */
    CompletableFuture<String> processInventoryTag(UUID sender, String message);
    
    /**
     * Process item tag for specific item in inventory
     * @param sender Sender UUID
     * @param itemName Item name to tag
     * @param message Original message
     * @return Processed message with item lore preview
     */
    CompletableFuture<String> processItemTag(UUID sender, String itemName, String message);
    
    /**
     * Get inventory preview for a player
     * @param uuid Player UUID
     * @return Inventory tag data
     */
    CompletableFuture<Optional<InventoryTag>> getInventoryPreview(UUID uuid);
    
    /**
     * Get item preview from player's inventory
     * @param uuid Player UUID
     * @param itemName Item name
     * @return Item tag data
     */
    CompletableFuture<Optional<ItemTag>> getItemPreview(UUID uuid, String itemName);
    
    /**
     * Check if chat is muted for a player
     * @param uuid Player UUID
     * @return true if muted, false otherwise
     */
    boolean isChatMuted(UUID uuid);
    
    /**
     * Mute player chat
     * @param uuid Player UUID
     * @param duration Mute duration in milliseconds
     * @param reason Mute reason
     * @return CompletableFuture that completes when player is muted
     */
    CompletableFuture<Void> mutePlayer(UUID uuid, long duration, String reason);
    
    /**
     * Unmute player chat
     * @param uuid Player UUID
     * @return CompletableFuture that completes when player is unmuted
     */
    CompletableFuture<Void> unmutePlayer(UUID uuid);
    
    /**
     * Clear chat for all players
     * @param sender Sender UUID (who cleared the chat)
     * @return CompletableFuture that completes when chat is cleared
     */
    CompletableFuture<Void> clearChat(UUID sender);
    
    /**
     * Get chat history for a player
     * @param uuid Player UUID
     * @param limit Number of messages to retrieve
     * @return Collection of chat messages
     */
    CompletableFuture<Collection<ChatMessage>> getChatHistory(UUID uuid, int limit);
    
    /**
     * Filter message for inappropriate content
     * @param message Message to filter
     * @return Filtered message
     */
    String filterMessage(String message);
    
    /**
     * Check if message contains inappropriate content
     * @param message Message to check
     * @return true if inappropriate, false otherwise
     */
    boolean isInappropriate(String message);
    
    /**
     * Get chat statistics
     * @return Chat statistics data
     */
    ChatStatistics getChatStatistics();
    
    /**
     * Register chat listener
     * @param listener Chat listener to register
     */
    void registerListener(ChatListener listener);
    
    /**
     * Unregister chat listener
     * @param listener Chat listener to unregister
     */
    void unregisterListener(ChatListener listener);
}
