package Echostudios.utils;

import Echostudios.EchoCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiManager {
    
    private final EchoCore plugin;
    
    public GuiManager(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Opens the enderchest GUI for a player
     * @param viewer The player viewing the GUI
     * @param target The player whose enderchest to show (can be the same as viewer)
     */
    public void openEnderchestGui(Player viewer, Player target) {
        if (!viewer.hasPermission("echocore.chat.enderchest")) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
            viewer.sendMessage(Utils.colorize(noPermMessage));
            return;
        }
        
        // Get GUI title from config
        String title = Utils.getMessageWithPrefix(plugin, "chat.gui.enderchest-title", "&8Enderchest of &e{player}");
        title = title.replace("{player}", target.getName());
        title = Utils.colorize(title);
        
        // Create inventory (enderchest has 27 slots)
        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        // Get enderchest contents
        ItemStack[] enderchestContents = target.getEnderChest().getContents();
        
        // Check if enderchest is empty
        boolean isEmpty = true;
        for (ItemStack item : enderchestContents) {
            if (item != null && item.getType() != Material.AIR) {
                isEmpty = false;
                break;
            }
        }
        
        if (isEmpty) {
            // Fill with decorative placeholders to simulate content (configurable)
            fillPlaceholders(gui, 27, "chat.gui.placeholders.enderchest");
        } else {
            // Fill GUI with enderchest contents
            for (int i = 0; i < enderchestContents.length && i < 27; i++) {
                ItemStack item = enderchestContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    gui.setItem(i, item.clone());
                }
            }
        }
        
        // Open GUI
        viewer.openInventory(gui);
        
        // Register inventory click event to prevent item taking
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(gui) && event.getWhoClicked().equals(viewer)) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
    }
    
    /**
     * Opens the staff inventory GUI for a player (with clear inventory option)
     * @param viewer The staff member viewing the GUI
     * @param target The player whose inventory to show
     */
    public void openStaffInventoryGui(Player viewer, Player target) {
        if (!viewer.hasPermission("echocore.staff.inventory")) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cNo Permission");
            viewer.sendMessage(Utils.colorize(noPermMessage));
            return;
        }
        
        // Get GUI title from config
        String title = Utils.getMessageWithPrefix(plugin, "chat.gui.staff-inventory-title", "&c&lStaff View: &8Inventory of &e{player}");
        title = title.replace("{player}", target.getName());
        title = Utils.colorize(title);
        
        // Create inventory (54 slots for better layout)
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        // Get inventory contents
        ItemStack[] inventoryContents = target.getInventory().getContents();
        ItemStack[] armorContents = target.getInventory().getArmorContents();
        
        // Fill GUI with inventory contents
        // Main inventory (0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventoryContents[i];
            if (item != null && item.getType() != Material.AIR) {
                gui.setItem(i, item.clone());
            }
        }
        
        // Armor slots (top row)
        int armorStart = 45;
        for (int i = 0; i < 4; i++) {
            ItemStack armor = armorContents[i];
            if (armor != null && armor.getType() != Material.AIR) {
                gui.setItem(armorStart + i, armor.clone());
            }
        }
        
        // Offhand slot
        ItemStack offhand = target.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            gui.setItem(49, offhand.clone());
        }
        
        // Main hand slot (item currently held)
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            ItemStack mainHandDisplay = mainHand.clone();
            ItemMeta meta = mainHandDisplay.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(0, Utils.colorize("&6&lMain Hand Item"));
                meta.setLore(lore);
                mainHandDisplay.setItemMeta(meta);
            }
            gui.setItem(53, mainHandDisplay);
        }
        
        // Add clear inventory button in bottom right corner
        ItemStack clearButton = createInfoItem(Material.TNT, "&c&lClear Inventory", 
            "&7Click to clear &e" + target.getName() + "'s &7inventory", 
            "&c&lWARNING: This action cannot be undone!");
        gui.setItem(52, clearButton);
        
        // Open GUI
        viewer.openInventory(gui);
        
        // Register inventory click event to prevent item taking and handle clear button
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(gui) && event.getWhoClicked().equals(viewer)) {
                    event.setCancelled(true);
                    
                    // Handle clear inventory button
                    if (event.getSlot() == 52) {
                        // Clear target's inventory
                        target.getInventory().clear();
                        target.getInventory().setArmorContents(null);
                        target.getInventory().setItemInOffHand(null);
                        
                        // Send confirmation message
                        String clearMessage = Utils.getMessageWithPrefix(plugin, "staff.inventory-cleared", 
                            "&aYou have cleared &e{player}'s &ainventory!")
                            .replace("{player}", target.getName());
                        viewer.sendMessage(Utils.colorize(clearMessage));
                        
                        // Close GUI
                        viewer.closeInventory();
                    }
                }
            }
        }, plugin);
    }
    
    /**
     * Opens the inventory GUI for a player
     * @param viewer The player viewing the GUI
     * @param target The player whose inventory to show (can be the same as viewer)
     */
    public void openInventoryGui(Player viewer, Player target) {
        if (!viewer.hasPermission("echocore.chat.inventory")) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
            viewer.sendMessage(Utils.colorize(noPermMessage));
            return;
        }
        
        // Get GUI title from config
        String title = Utils.getMessageWithPrefix(plugin, "chat.gui.inventory-title", "&8Inventory of &e{player}");
        title = title.replace("{player}", target.getName());
        title = Utils.colorize(title);
        
        // Create inventory (player inventory has 36 slots + 4 armor slots = 40 total)
        Inventory gui = Bukkit.createInventory(null, 54, title); // Using 54 slots for better layout
        
        // Get inventory contents
        ItemStack[] inventoryContents = target.getInventory().getContents();
        ItemStack[] armorContents = target.getInventory().getArmorContents();
        
        // Check if inventory is empty
        boolean isEmpty = true;
        for (ItemStack item : inventoryContents) {
            if (item != null && item.getType() != Material.AIR) {
                isEmpty = false;
                break;
            }
        }
        
        if (isEmpty) {
            // Fill with decorative placeholders to simulate content (configurable)
            fillPlaceholders(gui, 54, "chat.gui.placeholders.inventory");
        } else {
            // Fill GUI with inventory contents
            // Main inventory (0-35)
            for (int i = 0; i < 36; i++) {
                ItemStack item = inventoryContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    gui.setItem(i, item.clone());
                }
            }
            
            // Armor slots (top row)
            int armorStart = 45;
            for (int i = 0; i < 4; i++) {
                ItemStack armor = armorContents[i];
                if (armor != null && armor.getType() != Material.AIR) {
                    gui.setItem(armorStart + i, armor.clone());
                }
            }
            
            // Offhand slot
            ItemStack offhand = target.getInventory().getItemInOffHand();
            if (offhand != null && offhand.getType() != Material.AIR) {
                gui.setItem(49, offhand.clone());
            }
            
            // Main hand slot (item currently held)
            ItemStack mainHand = target.getInventory().getItemInMainHand();
            if (mainHand != null && mainHand.getType() != Material.AIR) {
                // Create a special item with lore indicating it's the main hand item
                ItemStack mainHandDisplay = mainHand.clone();
                ItemMeta meta = mainHandDisplay.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add(0, Utils.colorize("&6&lMain Hand Item"));
                    meta.setLore(lore);
                    mainHandDisplay.setItemMeta(meta);
                }
                gui.setItem(53, mainHandDisplay); // Bottom right corner
            }
        }
        
        // Open GUI
        viewer.openInventory(gui);
        
        // Register inventory click event to prevent item taking
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(gui) && event.getWhoClicked().equals(viewer)) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
    }
    
    /**
     * Opens the target's live inventory for editing by the viewer (staff only)
     */
    public void openEditableTargetInventory(Player viewer, Player target) {
        if (!viewer.hasPermission("echocore.staff.inventory")) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cNo Permission");
            viewer.sendMessage(Utils.colorize(noPermMessage));
            return;
        }
        // This opens the actual target inventory; edits will apply directly
        viewer.openInventory(target.getInventory());
    }
    
    /**
     * Creates an info item for display purposes
     */
    private ItemStack createInfoItem(Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(Utils.colorize(displayName));
            
            if (lore.length > 0) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(Utils.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createPlaceholderPane(String name, String materialName) {
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Utils.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillPlaceholders(Inventory gui, int size, String basePath) {
        String material = plugin.getMessagesConfig().getString(basePath + ".material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getMessagesConfig().getString(basePath + ".name", size == 27 ? "&8Enderchest" : "&8Inventory");
        String pattern = plugin.getMessagesConfig().getString(basePath + ".pattern", "fill");

        ItemStack placeholder = createPlaceholderPane(name, material);

        switch (pattern.toLowerCase()) {
            case "border":
                for (int i = 0; i < size; i++) {
                    int row = i / 9;
                    int col = i % 9;
                    if (row == 0 || row == (size / 9) - 1 || col == 0 || col == 8) {
                        gui.setItem(i, placeholder);
                    }
                }
                break;
            case "checker":
                for (int i = 0; i < size; i++) {
                    int row = i / 9;
                    int col = i % 9;
                    if ((row + col) % 2 == 0) {
                        gui.setItem(i, placeholder);
                    }
                }
                break;
            default: // fill
                for (int i = 0; i < size; i++) {
                    gui.setItem(i, placeholder);
                }
        }
    }
    
    /**
     * Opens a custom GUI based on configuration
     * @param viewer The player viewing the GUI
     * @param target The player target (can be the same as viewer)
     * @param guiName The name of the custom GUI
     * @return true if GUI was opened successfully, false otherwise
     */
    public boolean openCustomGui(Player viewer, Player target, String guiName) {
        // Check if custom GUI exists in config
        if (!plugin.getMessagesConfig().contains("gui.custom." + guiName)) {
            return false;
        }
        
        // Get GUI configuration
        String title = plugin.getMessagesConfig().getString("gui.custom." + guiName + ".title", "&8Custom GUI");
        int size = plugin.getMessagesConfig().getInt("gui.custom." + guiName + ".size", 27);
        List<String> items = plugin.getMessagesConfig().getStringList("gui.custom." + guiName + ".items");
        
        // Create inventory
        title = title.replace("{player}", target.getName());
        title = Utils.colorize(title);
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Fill GUI with configured items
        for (String itemConfig : items) {
            String[] parts = itemConfig.split(":");
            if (parts.length >= 3) {
                int slot = Integer.parseInt(parts[0]);
                String material = parts[1];
                String displayName = parts[2];
                
                List<String> lore = new ArrayList<>();
                if (parts.length > 3) {
                    for (int i = 3; i < parts.length; i++) {
                        lore.add(parts[i]);
                    }
                }
                
                ItemStack item = createInfoItem(Material.valueOf(material), displayName, lore.toArray(new String[0]));
                gui.setItem(slot, item);
            }
        }
        
        // Open GUI
        viewer.openInventory(gui);
        
        // Register inventory click event
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(gui) && event.getWhoClicked().equals(viewer)) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
        
        return true;
    }
}
