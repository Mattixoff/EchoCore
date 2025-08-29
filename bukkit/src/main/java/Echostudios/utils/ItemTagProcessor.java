package Echostudios.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processore per i tag degli oggetti in chat
 * Permette ai giocatori di usare [item], [sword], [armor], ecc. per mostrare oggetti interattivi
 */
public class ItemTagProcessor {
    
    private static final Pattern ITEM_TAG_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
    private static final Map<String, String> SLOT_MAPPINGS = new HashMap<>();
    
    static {
        // Mappature per slot specifici
        SLOT_MAPPINGS.put("item", "mainhand");
        SLOT_MAPPINGS.put("mainhand", "mainhand");
        SLOT_MAPPINGS.put("offhand", "offhand");
        SLOT_MAPPINGS.put("helmet", "helmet");
        SLOT_MAPPINGS.put("chestplate", "chestplate");
        SLOT_MAPPINGS.put("leggings", "leggings");
        SLOT_MAPPINGS.put("boots", "boots");
        SLOT_MAPPINGS.put("sword", "mainhand");
        SLOT_MAPPINGS.put("weapon", "mainhand");
        SLOT_MAPPINGS.put("armor", "helmet");
        SLOT_MAPPINGS.put("shield", "offhand");
    }
    
    /**
     * Processa un messaggio e sostituisce i tag degli oggetti con componenti interattivi
     */
    public static BaseComponent[] processItemTags(String message, Player player) {
        return processItemTags(message, player, null);
    }
    
    /**
     * Processa un messaggio e sostituisce i tag degli oggetti con componenti interattivi
     * @param message Il messaggio da processare
     * @param player Il giocatore che ha inviato il messaggio
     * @param config La configurazione dei messaggi (opzionale)
     */
    public static BaseComponent[] processItemTags(String message, Player player, FileConfiguration config) {
        ComponentBuilder builder = new ComponentBuilder();
        Matcher matcher = ITEM_TAG_PATTERN.matcher(message);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Aggiungi il testo prima del tag
            if (matcher.start() > lastEnd) {
                String before = message.substring(lastEnd, matcher.start());
                builder.append(TextComponent.fromLegacyText(before));
            }
            
            // Processa il tag
            String tagContent = matcher.group(1).toLowerCase();
            BaseComponent itemComponent = createItemComponent(tagContent, player, config);
            builder.append(itemComponent);
            
            lastEnd = matcher.end();
        }
        
        // Aggiungi il testo rimanente
        if (lastEnd < message.length()) {
            String remaining = message.substring(lastEnd);
            builder.append(TextComponent.fromLegacyText(remaining));
        }
        
        return builder.create();
    }
    
    /**
     * Crea un componente per un oggetto specifico
     */
    private static BaseComponent createItemComponent(String tag, Player player) {
        return createItemComponent(tag, player, null);
    }
    
    /**
     * Crea un componente per un oggetto specifico
     */
    private static BaseComponent createItemComponent(String tag, Player player, FileConfiguration config) {
        ItemStack item = getItemFromTag(tag, player);
        
        if (item == null || item.getType() == Material.AIR) {
            // Oggetto non trovato o slot vuoto
            String emptyText = "[";
            if (config != null) {
                emptyText += config.getString("chat.item-tags.empty-slot-text", "(vuoto)");
            } else {
                emptyText += "(vuoto)";
            }
            emptyText += "]";
            
            TextComponent emptyComponent = new TextComponent(emptyText);
            emptyComponent.setColor(ChatColor.GRAY);
            emptyComponent.setItalic(true);
            
            String hoverText = "§7";
            if (config != null) {
                hoverText += config.getString("chat.item-tags.empty-slot-hover", "Slot vuoto");
            } else {
                hoverText += "Slot vuoto";
            }
            
            emptyComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText(hoverText)
            ));
            return emptyComponent;
        }
        
        // Crea il componente dell'oggetto
        TextComponent itemComponent = new TextComponent();
        
        // Imposta il testo (nome dell'oggetto o tipo)
        String displayName = getItemDisplayName(item);
        itemComponent.setText(displayName);
        
        // Colori basati sul tipo di oggetto
        ChatColor itemColor = getItemColor(item);
        itemComponent.setColor(itemColor);
        
        // Hover event per mostrare i dettagli dell'oggetto
        BaseComponent[] hoverText = createItemHoverText(item);
        itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        
        return itemComponent;
    }
    
    /**
     * Recupera un oggetto dall'inventario del giocatore basandosi sul tag
     */
    private static ItemStack getItemFromTag(String tag, Player player) {
        PlayerInventory inventory = player.getInventory();
        
        // Controlla se è un tag per slot specifici
        String slotType = SLOT_MAPPINGS.get(tag);
        if (slotType != null) {
            switch (slotType) {
                case "mainhand":
                    return inventory.getItemInMainHand();
                case "offhand":
                    return inventory.getItemInOffHand();
                case "helmet":
                    return inventory.getHelmet();
                case "chestplate":
                    return inventory.getChestplate();
                case "leggings":
                    return inventory.getLeggings();
                case "boots":
                    return inventory.getBoots();
            }
        }
        
        // Cerca per nome dell'oggetto nell'inventario
        ItemStack foundItem = findItemByName(tag, inventory);
        if (foundItem != null) {
            return foundItem;
        }
        
        // Cerca per tipo di materiale
        return findItemByMaterial(tag, inventory);
    }
    
    /**
     * Trova un oggetto per nome nell'inventario
     */
    private static ItemStack findItemByName(String name, PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName().toLowerCase();
                    if (displayName.contains(name.toLowerCase())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Trova un oggetto per tipo di materiale nell'inventario
     */
    private static ItemStack findItemByMaterial(String materialName, PlayerInventory inventory) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() == material) {
                    return item;
                }
            }
        } catch (IllegalArgumentException e) {
            // Materiale non valido, ignora
        }
        return null;
    }
    
    /**
     * Ottiene il nome visualizzato dell'oggetto
     */
    private static String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        
        // Nome predefinito basato sul tipo
        String materialName = item.getType().name().toLowerCase().replace("_", " ");
        return materialName.substring(0, 1).toUpperCase() + materialName.substring(1);
    }
    
    /**
     * Ottiene il colore appropriato per il tipo di oggetto
     */
    private static ChatColor getItemColor(ItemStack item) {
        Material type = item.getType();
        
        // Colori per armi
        if (type.name().contains("SWORD") || type.name().contains("AXE") || 
            type.name().contains("PICKAXE") || type.name().contains("SHOVEL") || 
            type.name().contains("HOE") || type.name().contains("BOW") || 
            type.name().contains("CROSSBOW") || type.name().contains("TRIDENT")) {
            return ChatColor.GOLD;
        }
        
        // Colori per armature
        if (type.name().contains("HELMET") || type.name().contains("CHESTPLATE") || 
            type.name().contains("LEGGINGS") || type.name().contains("BOOTS")) {
            return ChatColor.AQUA;
        }
        
        // Colori per oggetti speciali
        if (type.name().contains("DIAMOND")) return ChatColor.AQUA;
        if (type.name().contains("EMERALD")) return ChatColor.GREEN;
        if (type.name().contains("GOLD")) return ChatColor.YELLOW;
        if (type.name().contains("IRON")) return ChatColor.GRAY;
        if (type.name().contains("NETHERITE")) return ChatColor.DARK_PURPLE;
        
        // Colore predefinito
        return ChatColor.WHITE;
    }
    
    /**
     * Crea il testo hover per mostrare i dettagli dell'oggetto
     */
    private static BaseComponent[] createItemHoverText(ItemStack item) {
        ComponentBuilder builder = new ComponentBuilder();
        
        // Nome dell'oggetto
        String displayName = getItemDisplayName(item);
        builder.append(displayName + "\n").color(ChatColor.GOLD);
        
        // Tipo di materiale
        String materialName = item.getType().name().toLowerCase().replace("_", " ");
        builder.append("Tipo: " + materialName + "\n").color(ChatColor.GRAY);
        
        // Quantità
        if (item.getAmount() > 1) {
            builder.append("Quantità: " + item.getAmount() + "\n").color(ChatColor.GRAY);
        }
        
        // Durabilità
        if (item.getType().getMaxDurability() > 0) {
            int maxDurability = item.getType().getMaxDurability();
            int currentDurability = maxDurability - item.getDurability();
            double percentage = (double) currentDurability / maxDurability * 100;
            
            ChatColor durabilityColor = percentage > 75 ? ChatColor.GREEN : 
                                      percentage > 50 ? ChatColor.YELLOW : 
                                      percentage > 25 ? ChatColor.GOLD : ChatColor.RED;
            
            builder.append("Durabilità: " + currentDurability + "/" + maxDurability + 
                          " (" + String.format("%.1f", percentage) + "%)").color(durabilityColor);
        }
        
        // Enchantments
        if (item.getEnchantments().size() > 0) {
            builder.append("\n").reset();
            builder.append("Incantesimi:\n").color(ChatColor.LIGHT_PURPLE);
            
            item.getEnchantments().forEach((enchant, level) -> {
                String enchantName = enchant.getName().toLowerCase().replace("_", " ");
                enchantName = enchantName.substring(0, 1).toUpperCase() + enchantName.substring(1);
                builder.append("  " + enchantName + " " + level + "\n").color(ChatColor.LIGHT_PURPLE);
            });
        }
        
        // Lore personalizzata
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null && !lore.isEmpty()) {
                builder.append("\n").reset();
                builder.append("Lore:\n").italic(true);
                
                for (String loreLine : lore) {
                    builder.append("  " + loreLine + "\n").italic(true);
                }
            }
        }
        
        return builder.create();
    }
    
    /**
     * Controlla se un messaggio contiene tag di oggetti
     */
    public static boolean containsItemTags(String message) {
        return ITEM_TAG_PATTERN.matcher(message).find();
    }
    
    /**
     * Ottiene tutti i tag disponibili per un giocatore
     */
    public static List<String> getAvailableTags(Player player) {
        List<String> tags = new ArrayList<>();
        
        // Tag per slot specifici
        tags.add("[item]");
        tags.add("[mainhand]");
        tags.add("[offhand]");
        tags.add("[helmet]");
        tags.add("[chestplate]");
        tags.add("[leggings]");
        tags.add("[boots]");
        tags.add("[sword]");
        tags.add("[weapon]");
        tags.add("[armor]");
        tags.add("[shield]");
        
        // Tag per oggetti nell'inventario
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String displayName = item.getItemMeta().getDisplayName();
                    tags.add("[" + displayName + "]");
                } else {
                    String materialName = item.getType().name().toLowerCase().replace("_", " ");
                    tags.add("[" + materialName + "]");
                }
            }
        }
        
        return tags;
    }
}
