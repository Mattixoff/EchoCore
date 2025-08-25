package Echostudios.common.api.chat.data;

import java.util.List;
import java.util.UUID;

/**
 * Data class representing an item tag for chat
 * This contains the information needed to display an item preview
 */
public class ItemTag {
    
    private final UUID playerUUID;
    private final String playerName;
    private final InventoryItem item;
    private final long timestamp;
    private final String serverName;
    private final String itemName;
    
    public ItemTag(UUID playerUUID, String playerName, InventoryItem item, 
                   long timestamp, String serverName, String itemName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.item = item;
        this.timestamp = timestamp;
        this.serverName = serverName;
        this.itemName = itemName;
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
     * Get the item data
     * @return Inventory item
     */
    public InventoryItem getItem() {
        return item;
    }
    
    /**
     * Get the timestamp when this tag was created
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the server name where the item was found
     * @return Server name
     */
    public String getServerName() {
        return serverName;
    }
    
    /**
     * Get the name used to search for the item
     * @return Item search name
     */
    public String getItemName() {
        return itemName;
    }
    
    /**
     * Get the formatted display text for this item tag
     * @return Formatted display text
     */
    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6").append(itemName).append(" §7from ");
        sb.append(playerName).append("'s inventory");
        if (serverName != null && !serverName.isEmpty()) {
            sb.append(" §7(").append(serverName).append(")");
        }
        sb.append("§r");
        return sb.toString();
    }
    
    /**
     * Get the hover text for this item tag
     * @return Hover text with item details
     */
    public String getHoverText() {
        if (item == null) {
            return "§cItem not found";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("§6").append(itemName).append("\n");
        sb.append("§7Owner: §f").append(playerName).append("\n");
        sb.append("§7Material: §f").append(item.getMaterialName()).append("\n");
        sb.append("§7Amount: §f").append(item.getAmount()).append("\n");
        
        if (item.getDurability() > 0) {
            sb.append("§7Durability: §f").append(item.getDurability()).append("\n");
        }
        
        if (item.isGlowing()) {
            sb.append("§7Glowing: §aYes\n");
        }
        
        if (item.getCustomModelData() != null && !item.getCustomModelData().isEmpty()) {
            sb.append("§7Custom Model Data: §f").append(item.getCustomModelData()).append("\n");
        }
        
        if (item.getLore() != null && !item.getLore().isEmpty()) {
            sb.append("\n§7Lore:\n");
            for (String loreLine : item.getLore()) {
                sb.append("§f").append(loreLine).append("\n");
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Check if the item was found
     * @return true if found, false otherwise
     */
    public boolean isItemFound() {
        return item != null;
    }
    
    /**
     * Get the item's display name
     * @return Item display name or null if not found
     */
    public String getItemDisplayName() {
        return item != null ? item.getDisplayName() : null;
    }
    
    /**
     * Get the item's lore
     * @return Item lore or null if not found
     */
    public List<String> getItemLore() {
        return item != null ? item.getLore() : null;
    }
    
    /**
     * Get the item's amount
     * @return Item amount or 0 if not found
     */
    public int getItemAmount() {
        return item != null ? item.getAmount() : 0;
    }
}
