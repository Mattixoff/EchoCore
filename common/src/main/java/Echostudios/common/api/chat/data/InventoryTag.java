package Echostudios.common.api.chat.data;

import java.util.List;
import java.util.UUID;

/**
 * Data class representing an inventory tag for chat
 * This contains the information needed to display an inventory preview
 */
public class InventoryTag {
    
    private final UUID playerUUID;
    private final String playerName;
    private final List<InventoryItem> items;
    private final long timestamp;
    private final String serverName;
    
    public InventoryTag(UUID playerUUID, String playerName, List<InventoryItem> items, 
                       long timestamp, String serverName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.items = items;
        this.timestamp = timestamp;
        this.serverName = serverName;
    }
    
    /**
     * Get the player's UUID
     * @return Player UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    /**
     * Get the player's name
     * @return Player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Get the list of items in the inventory
     * @return List of inventory items
     */
    public List<InventoryItem> getItems() {
        return items;
    }
    
    /**
     * Get the timestamp when this tag was created
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the server name where the inventory was captured
     * @return Server name
     */
    public String getServerName() {
        return serverName;
    }
    
    /**
     * Get the formatted display text for this inventory tag
     * @return Formatted display text
     */
    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6").append(playerName).append("'s Inventory");
        if (serverName != null && !serverName.isEmpty()) {
            sb.append(" §7(").append(serverName).append(")");
        }
        sb.append("§r");
        return sb.toString();
    }
    
    /**
     * Get the hover text for this inventory tag
     * @return Hover text with item details
     */
    public String getHoverText() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6").append(playerName).append("'s Inventory\n");
        sb.append("§7Items: §f").append(items.size()).append("\n\n");
        
        for (InventoryItem item : items) {
            if (item.getAmount() > 0) {
                sb.append("§f").append(item.getAmount()).append("x ");
                sb.append(item.getDisplayName()).append("\n");
                
                if (item.getLore() != null && !item.getLore().isEmpty()) {
                    for (String loreLine : item.getLore()) {
                        sb.append("§7").append(loreLine).append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Check if the inventory is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return items.stream().allMatch(item -> item.getAmount() <= 0);
    }
    
    /**
     * Get the total number of items in the inventory
     * @return Total item count
     */
    public int getTotalItemCount() {
        return items.stream().mapToInt(InventoryItem::getAmount).sum();
    }
}

