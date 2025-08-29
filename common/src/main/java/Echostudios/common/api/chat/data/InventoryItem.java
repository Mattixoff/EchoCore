package Echostudios.common.api.chat.data;

import java.util.List;

/**
 * Data class representing an individual item in an inventory
 */
public class InventoryItem {
    
    private final String materialName;
    private final String displayName;
    private final int amount;
    private final List<String> lore;
    private final int durability;
    private final boolean glowing;
    private final String customModelData;
    
    public InventoryItem(String materialName, String displayName, int amount, 
                        List<String> lore, int durability, boolean glowing, String customModelData) {
        this.materialName = materialName;
        this.displayName = displayName;
        this.amount = amount;
        this.lore = lore;
        this.durability = durability;
        this.glowing = glowing;
        this.customModelData = customModelData;
    }
    
    /**
     * Get the material name of the item
     * @return Material name
     */
    public String getMaterialName() {
        return materialName;
    }
    
    /**
     * Get the display name of the item
     * @return Display name
     */
    public String getDisplayName() {
        return displayName != null ? displayName : materialName;
    }
    
    /**
     * Get the amount of the item
     * @return Item amount
     */
    public int getAmount() {
        return amount;
    }
    
    /**
     * Get the lore of the item
     * @return List of lore lines
     */
    public List<String> getLore() {
        return lore;
    }
    
    /**
     * Get the durability of the item
     * @return Item durability
     */
    public int getDurability() {
        return durability;
    }
    
    /**
     * Check if the item is glowing
     * @return true if glowing, false otherwise
     */
    public boolean isGlowing() {
        return glowing;
    }
    
    /**
     * Get the custom model data of the item
     * @return Custom model data
     */
    public String getCustomModelData() {
        return customModelData;
    }
    
    /**
     * Get the formatted display text for this item
     * @return Formatted display text
     */
    public String getFormattedDisplayText() {
        StringBuilder sb = new StringBuilder();
        
        if (amount > 1) {
            sb.append("§f").append(amount).append("x ");
        }
        
        if (glowing) {
            sb.append("§b");
        }
        
        sb.append(displayName != null ? displayName : materialName);
        
        if (durability > 0) {
            sb.append(" §7(").append(durability).append(")");
        }
        
        if (customModelData != null && !customModelData.isEmpty()) {
            sb.append(" §7(CMD: ").append(customModelData).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Get the hover text for this item
     * @return Hover text with item details
     */
    public String getHoverText() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6").append(displayName != null ? displayName : materialName).append("\n");
        sb.append("§7Material: §f").append(materialName).append("\n");
        sb.append("§7Amount: §f").append(amount).append("\n");
        
        if (durability > 0) {
            sb.append("§7Durability: §f").append(durability).append("\n");
        }
        
        if (glowing) {
            sb.append("§7Glowing: §aYes\n");
        }
        
        if (customModelData != null && !customModelData.isEmpty()) {
            sb.append("§7Custom Model Data: §f").append(customModelData).append("\n");
        }
        
        if (lore != null && !lore.isEmpty()) {
            sb.append("\n§7Lore:\n");
            for (String loreLine : lore) {
                sb.append("§f").append(loreLine).append("\n");
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Check if the item has custom properties
     * @return true if has custom properties, false otherwise
     */
    public boolean hasCustomProperties() {
        return (displayName != null && !displayName.equals(materialName)) ||
               (lore != null && !lore.isEmpty()) ||
               glowing ||
               (customModelData != null && !customModelData.isEmpty());
    }
}

