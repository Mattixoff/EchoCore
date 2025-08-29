package Echostudios.common.api.chat;

import Echostudios.common.api.chat.data.InventoryTag;
import Echostudios.common.api.chat.data.ItemTag;
import Echostudios.common.api.chat.data.InventoryItem;
import Echostudios.common.api.player.Player;
import Echostudios.common.api.player.PlayerAPI;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Chat processor that handles advanced tagging system
 * Processes [inv] and item tags in chat messages
 */
public class ChatProcessor {
    
    private static final Pattern INVENTORY_TAG_PATTERN = Pattern.compile("\\[inv\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEM_TAG_PATTERN = Pattern.compile("\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    
    private final PlayerAPI playerAPI;
    private final ChatAPI chatAPI;
    
    public ChatProcessor(PlayerAPI playerAPI, ChatAPI chatAPI) {
        this.playerAPI = playerAPI;
        this.chatAPI = chatAPI;
    }
    
    /**
     * Process a chat message and replace tags with appropriate content
     * @param sender Sender UUID
     * @param message Raw message content
     * @return Processed message with tags replaced
     */
    public CompletableFuture<String> processMessage(UUID sender, String message) {
        CompletableFuture<String> future = CompletableFuture.completedFuture(message);
        
        // Process inventory tags first
        if (INVENTORY_TAG_PATTERN.matcher(message).find()) {
            future = future.thenCompose(msg -> processInventoryTags(sender, msg));
        }
        
        // Process item tags
        if (ITEM_TAG_PATTERN.matcher(message).find()) {
            future = future.thenCompose(msg -> processItemTags(sender, msg));
        }
        
        return future;
    }
    
    /**
     * Process inventory tags [inv] in the message
     * @param sender Sender UUID
     * @param message Message to process
     * @return Processed message
     */
    private CompletableFuture<String> processInventoryTags(UUID sender, String message) {
        return chatAPI.getInventoryPreview(sender).thenApply(inventoryOpt -> {
            if (inventoryOpt.isEmpty()) {
                return message.replaceAll("(?i)\\[inv\\]", "§c[Inventory Unavailable]");
            }
            
            InventoryTag inventory = inventoryOpt.get();
            String replacement = "§6[§e" + inventory.getPlayerName() + "'s Inventory§6]";
            
            return message.replaceAll("(?i)\\[inv\\]", replacement);
        });
    }
    
    /**
     * Process item tags [itemname] in the message
     * @param sender Sender UUID
     * @param message Message to process
     * @return Processed message
     */
    private CompletableFuture<String> processItemTags(UUID sender, String message) {
        Matcher matcher = ITEM_TAG_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String itemName = matcher.group(1);
            
            // Skip [inv] tags as they're handled separately
            if ("inv".equalsIgnoreCase(itemName)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            
            // Process item tag
            String replacement = processItemTag(sender, itemName);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return CompletableFuture.completedFuture(result.toString());
    }
    
    /**
     * Process a single item tag
     * @param sender Sender UUID
     * @param itemName Item name to search for
     * @return Formatted item tag
     */
    private String processItemTag(UUID sender, String itemName) {
        // For now, return a simple format
        // In the actual implementation, this would search the player's inventory
        // and return detailed item information
        return "§6[§e" + itemName + "§6]";
    }
    
    /**
     * Get inventory preview for a player
     * @param uuid Player UUID
     * @return Inventory preview data
     */
    public CompletableFuture<InventoryTag> getInventoryPreview(UUID uuid) {
        return playerAPI.getPlayerInventory(uuid).thenApply(inventoryOpt -> {
            if (inventoryOpt.isEmpty()) {
                return null;
            }
            
            // Convert platform-specific inventory to common format
            // This is a simplified version - actual implementation would be more complex
            List<InventoryItem> items = List.of(); // Convert from platform inventory
            
            return new InventoryTag(
                uuid,
                "Player", // Get actual player name
                items,
                System.currentTimeMillis(),
                "Server" // Get actual server name
            );
        });
    }
    
    /**
     * Get item preview from player's inventory
     * @param uuid Player UUID
     * @param itemName Item name to search for
     * @return Item preview data
     */
    public CompletableFuture<ItemTag> getItemPreview(UUID uuid, String itemName) {
        return playerAPI.getPlayerInventory(uuid).thenApply(inventoryOpt -> {
            if (inventoryOpt.isEmpty()) {
                return new ItemTag(uuid, "Player", null, System.currentTimeMillis(), "Server", itemName);
            }
            
            // Search for item in inventory
            // This is a simplified version - actual implementation would search the inventory
            InventoryItem foundItem = null; // Find item by name
            
            return new ItemTag(
                uuid,
                "Player", // Get actual player name
                foundItem,
                System.currentTimeMillis(),
                "Server", // Get actual server name
                itemName
            );
        });
    }
    
    /**
     * Create hover text for inventory tag
     * @param inventory Inventory data
     * @return Hover text
     */
    public String createInventoryHoverText(InventoryTag inventory) {
        if (inventory == null) {
            return "§cInventory unavailable";
        }
        
        return inventory.getHoverText();
    }
    
    /**
     * Create hover text for item tag
     * @param itemTag Item tag data
     * @return Hover text
     */
    public String createItemHoverText(ItemTag itemTag) {
        if (itemTag == null) {
            return "§cItem not found";
        }
        
        return itemTag.getHoverText();
    }
    
    /**
     * Check if a message contains any tags
     * @param message Message to check
     * @return true if contains tags, false otherwise
     */
    public boolean containsTags(String message) {
        return INVENTORY_TAG_PATTERN.matcher(message).find() || 
               ITEM_TAG_PATTERN.matcher(message).find();
    }
    
    /**
     * Get all tags found in a message
     * @param message Message to analyze
     * @return List of found tags
     */
    public List<String> extractTags(String message) {
        List<String> tags = new java.util.ArrayList<>();
        
        // Find inventory tags
        Matcher invMatcher = INVENTORY_TAG_PATTERN.matcher(message);
        while (invMatcher.find()) {
            tags.add("[inv]");
        }
        
        // Find item tags
        Matcher itemMatcher = ITEM_TAG_PATTERN.matcher(message);
        while (itemMatcher.find()) {
            String tag = itemMatcher.group(0);
            if (!"[inv]".equalsIgnoreCase(tag)) {
                tags.add(tag);
            }
        }
        
        return tags;
    }
}

