package Echostudios.events;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import Echostudios.utils.WebhookManager;
import Echostudios.utils.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
// Component imports removed for now - will be re-added when component system is implemented

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatEvents implements Listener {
    
    private final EchoCore plugin;
    private final WebhookManager webhookManager;
    private final GuiManager guiManager;
    private final Set<UUID> mutedPlayers = new HashSet<>();
    
    public ChatEvents(EchoCore plugin) {
        this.plugin = plugin;
        this.webhookManager = new WebhookManager(plugin);
        this.guiManager = new GuiManager(plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Save player to database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().savePlayer(player.getUniqueId(), player.getName());
        }
        
        // Send join message
        String joinMessage = Utils.getMessageWithPrefix(plugin, "join-leave.player-join", "&a+ &e{player} &ajoined the server");
        joinMessage = joinMessage.replace("{player}", player.getName());
        event.setJoinMessage(Utils.colorize(joinMessage));
        
        // Send webhook notification (only if enabled)
        if (plugin.getConfig().getBoolean("webhook.enabled", false)) {
            webhookManager.sendJoinLeaveWebhook("Joined", player.getName());
        }
        
        // Handle vanish for new players
        if (plugin.getVanishCommand() != null) {
            plugin.getVanishCommand().handlePlayerJoin(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Send quit message
        String quitMessage = Utils.getMessageWithPrefix(plugin, "join-leave.player-quit", "&c- &e{player} &cleft the server");
        quitMessage = quitMessage.replace("{player}", player.getName());
        event.setQuitMessage(Utils.colorize(quitMessage));
        
        // Send webhook notification (only if enabled)
        if (plugin.getConfig().getBoolean("webhook.enabled", false)) {
            webhookManager.sendJoinLeaveWebhook("Left", player.getName());
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is muted (from database first, then memory)
        boolean isMuted = false;
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            try {
                isMuted = plugin.getDatabaseManager().isPlayerMuted(player.getUniqueId()).get();
            } catch (Exception e) {
                // Fallback to memory check
                isMuted = plugin.getModerationCommands() != null && 
                    plugin.getModerationCommands().isMuted(player.getUniqueId());
            }
        } else {
            // Fallback to memory check
            isMuted = plugin.getModerationCommands() != null && 
                plugin.getModerationCommands().isMuted(player.getUniqueId());
        }
        
        if (isMuted) {
            event.setCancelled(true);
            
            String muteMessage = Utils.getMessageWithPrefix(plugin, "moderation.chat-muted", "&cYou are currently muted and cannot send messages!");
            player.sendMessage(Utils.colorize(muteMessage));
            return;
        }
        
        // Check if player is vanished and should not be seen in chat
        if (plugin.getVanishCommand() != null && 
            plugin.getVanishCommand().isVanished(player.getUniqueId())) {
            // Only show chat to players with vanish.see permission
            event.getRecipients().removeIf(recipient -> 
                !recipient.hasPermission("echocore.vanish.see"));
        }
        
        // Process chat message for colors, gradients, and tags
        String message = event.getMessage();
        String processedMessage = processChatMessage(message, player);
        event.setMessage(processedMessage);
        
        // Set custom chat format
        final String chatFormat = plugin.getMessagesConfig().getString("chat.format", "&7<&e{player}&7> &f{message}")
            .replace("{player}", player.getName())
            .replace("{message}", processedMessage);
        event.setFormat(Utils.colorize(chatFormat));
        
        // For now, use normal chat format instead of components to avoid issues
        // TODO: Implement proper component system later
        event.setFormat(Utils.colorize(chatFormat));
    }
    
    private String processChatMessage(String message, Player player) {
        // Process special commands like [enderchest] and [inventory] FIRST
        message = processSpecialCommandsWithComponents(message, player);
        
        // Then process @player tags
        message = processPlayerTags(message);
        
        // Finally process colors and gradients (only if player has permission)
        if (player.hasPermission("echocore.chat.colors")) {
            message = Utils.colorize(message);
        }
        
        return message;
    }
    
    private String processSpecialCommands(String message, Player player) {
        // Check if tags are enabled
        boolean tagsEnabled = plugin.getMessagesConfig().getBoolean("chat.tags.enabled", true);
        if (!tagsEnabled) {
            return message;
        }
        
        // Process [enderchest] command
        if (message.contains("[enderchest]")) {
            if (player.hasPermission("echocore.chat.enderchest")) {
                // Open enderchest GUI instead of showing text
                Bukkit.getScheduler().runTask(plugin, () -> {
                    guiManager.openEnderchestGui(player, player);
                });
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            // Keep the [enderchest] in chat but make it colored with hover text
            String enderchestHoverText = plugin.getMessagesConfig().getString("chat.enderchest.hover-text", "&bEnderchest of {player}");
            enderchestHoverText = enderchestHoverText.replace("{player}", player.getName());
            String enderchestTag = "&b[enderchest]&r";
            message = message.replace("[enderchest]", enderchestTag);
        }
        
        // Process [inventory] command
        if (message.contains("[inventory]")) {
            if (player.hasPermission("echocore.chat.inventory")) {
                // Open inventory GUI instead of showing text
                Bukkit.getScheduler().runTask(plugin, () -> {
                    guiManager.openInventoryGui(player, player);
                });
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            // Keep the [inventory] in chat but make it colored with hover text
            String inventoryHoverText = plugin.getMessagesConfig().getString("chat.inventory.hover-text", "&bInventory of {player}");
            inventoryHoverText = inventoryHoverText.replace("{player}", player.getName());
            String inventoryTag = "&b[inventory]&r";
            message = message.replace("[inventory]", inventoryTag);
        }
        
        // Process custom commands from config
        if (plugin.getMessagesConfig().isConfigurationSection("custom-commands")) {
            plugin.getLogger().info("Processing custom commands for " + player.getName());
            for (String customCmd : plugin.getMessagesConfig().getConfigurationSection("custom-commands").getKeys(false)) {
                String commandName = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".name", customCmd);
                String permission = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".permission", "");
                String messageText = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".message", "");
                String color = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".color", "&b");
                
                plugin.getLogger().info("Checking custom command: " + commandName + " in message: " + message);
                
                if (message.contains("[" + commandName + "]")) {
                    plugin.getLogger().info("Found custom command: " + commandName);
                    if (permission.isEmpty() || player.hasPermission(permission)) {
                        // Send custom message if specified
                        if (!messageText.isEmpty()) {
                            String formattedMessage = messageText.replace("{player}", player.getName());
                            player.sendMessage(Utils.colorize(formattedMessage));
                            plugin.getLogger().info("Sent custom message: " + formattedMessage);
                        }
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                        plugin.getLogger().info("No permission for custom command: " + commandName);
                    }
                    // Keep the custom command in chat but make it colored
                    String customTag = color + "[" + commandName + "]&r";
                    message = message.replace("[" + commandName + "]", customTag);
                    plugin.getLogger().info("Replaced command with colored tag: " + customTag);
                }
            }
        } else {
            plugin.getLogger().warning("No custom-commands section found in messages.yml");
        }
        
        return message;
    }
    
    private String processPlayerTags(String message) {
        // Check if tags are enabled
        boolean tagsEnabled = plugin.getMessagesConfig().getBoolean("chat.tags.enabled", true);
        if (!tagsEnabled) {
            return message;
        }
        
        // Get tag formats from config
        String tagFormat = plugin.getMessagesConfig().getString("chat.tags.format", "&b@{player}&r");
        boolean autoTagEnabled = plugin.getMessagesConfig().getBoolean("chat.tags.auto-tag-enabled", true);
        String autoTagFormat = plugin.getMessagesConfig().getString("chat.tags.auto-tag-format", "&b@{player}&r");
        
        // Find @player patterns and convert them to colored tags
        String[] words = message.split(" ");
        for (int i = 0; i < words.length; i++) {
            // Check for @player tags
            if (words[i].startsWith("@")) {
                String playerName = words[i].substring(1); // Remove @
                Player target = Bukkit.getPlayer(playerName);
                if (target != null) {
                    // Convert to colored tag format using config
                    String formattedTag = tagFormat.replace("{player}", target.getName());
                    words[i] = Utils.colorize(formattedTag);
                } else {
                    // Player not found, but still color the @ symbol
                    words[i] = "&b" + words[i] + "&r";
                }
            }
            // Check for auto-tags (player names without @ that should become tags)
            else if (autoTagEnabled) {
                Player target = Bukkit.getPlayer(words[i]);
                if (target != null && words[i].equals(target.getName())) {
                    // This is a player name that should become a tag
                    String formattedTag = autoTagFormat.replace("{player}", target.getName());
                    words[i] = Utils.colorize(formattedTag);
                }
            }
        }
        return String.join(" ", words);
    }
    
    // TODO: Implement component system for interactive chat commands
    // This will be added in a future update
    
    // TODO: Component creation method will be re-implemented when component system is ready
    
    /**
     * Processes special commands with enhanced functionality
     */
    private String processSpecialCommandsWithComponents(String message, Player player) {
        // Check if tags are enabled
        boolean tagsEnabled = plugin.getMessagesConfig().getBoolean("chat.tags.enabled", true);
        if (!tagsEnabled) {
            return message;
        }
        
        // Process [enderchest] command
        if (message.contains("[enderchest]")) {
            if (player.hasPermission("echocore.chat.enderchest")) {
                // Open enderchest GUI instead of showing text
                Bukkit.getScheduler().runTask(plugin, () -> {
                    guiManager.openEnderchestGui(player, player);
                });
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            // Keep the [enderchest] in chat but make it colored
            String enderchestTag = "&b[enderchest]&r";
            message = message.replace("[enderchest]", enderchestTag);
        }
        
        // Process [inventory] command
        if (message.contains("[inventory]")) {
            if (player.hasPermission("echocore.chat.inventory")) {
                // Open inventory GUI instead of showing text
                Bukkit.getScheduler().runTask(plugin, () -> {
                    guiManager.openInventoryGui(player, player);
                });
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            // Keep the [inventory] in chat but make it colored
            String inventoryTag = "&b[inventory]&r";
            message = message.replace("[inventory]", inventoryTag);
        }
        
        return message;
    }
    
    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getSender();
        String buffer = event.getBuffer();
        
        // Debug: Log tab completion attempts (only for @ tags)
        if (buffer.contains("@")) {
            plugin.getLogger().info("Tab completion for " + player.getName() + ": '" + buffer + "'");
        }
        
        // Only handle chat tab completion (not command tab completion)
        if (buffer.startsWith("/")) {
            return;
        }
        
        // Get online players
        List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        
        // Filter based on current input
        String[] words = buffer.split(" ");
        String currentWord = words.length > 0 ? words[words.length - 1] : "";
        
        // Special handling for @player tags
        if (currentWord.startsWith("@")) {
            String playerName = currentWord.substring(1); // Remove @
            List<String> playerCompletions = onlinePlayers.stream()
                    .filter(name -> name.toLowerCase().startsWith(playerName.toLowerCase()))
                    .map(name -> "@" + name) // Add @ back
                    .collect(Collectors.toList());
            event.getCompletions().addAll(playerCompletions);
            plugin.getLogger().info("Added " + playerCompletions.size() + " @player completions for " + player.getName());
            return;
        }
        
        // Special handling for empty tab completion (show special options)
        if (currentWord.isEmpty()) {
            List<String> specialOptions = new ArrayList<>();
            specialOptions.add("[enderchest]");
            specialOptions.add("[inventory]");
            
            // Add online players
            specialOptions.addAll(onlinePlayers);
            
            event.getCompletions().addAll(specialOptions);
            plugin.getLogger().info("Added " + specialOptions.size() + " special options for " + player.getName());
            return;
        }
        
        // Special handling for [ commands
        if (currentWord.startsWith("[")) {
            List<String> bracketCompletions = new ArrayList<>();
            bracketCompletions.add("[enderchest]");
            bracketCompletions.add("[inventory]");
            
            // Add custom commands from config
            if (plugin.getMessagesConfig().isConfigurationSection("custom-commands")) {
                for (String customCmd : plugin.getMessagesConfig().getConfigurationSection("custom-commands").getKeys(false)) {
                    String commandName = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".name", customCmd);
                    bracketCompletions.add("[" + commandName + "]");
                }
            }
            
            bracketCompletions = bracketCompletions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(currentWord.toLowerCase()))
                    .collect(Collectors.toList());
            
            event.getCompletions().addAll(bracketCompletions);
            return;
        }
        
        // Regular completions
        List<String> completions = new ArrayList<>();
        completions.addAll(onlinePlayers);
        
        // Add inventory items (if player has permission)
        if (player.hasPermission("echocore.chat.inventory")) {
            completions.addAll(getInventoryItems(player));
        }
        
        // Add enderchest items (if player has permission)
        if (player.hasPermission("echocore.chat.enderchest")) {
            completions.addAll(getEnderchestItems(player));
        }
        
        // Add special chat commands
        completions.addAll(getChatCommands());
        
        completions = completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentWord.toLowerCase()))
                .collect(Collectors.toList());
        
        event.getCompletions().addAll(completions);
        
        // Debug: Log completions added
        plugin.getLogger().info("Added " + completions.size() + " completions for " + player.getName());
    }
    
    @EventHandler
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
        Player player = event.getPlayer();
        String lastToken = event.getLastToken();
        
        // Debug: Log chat tab completion
        plugin.getLogger().info("Chat tab completion for " + player.getName() + ": '" + lastToken + "'");
        
        // Special handling for @player tags
        if (lastToken.startsWith("@")) {
            String playerName = lastToken.substring(1); // Remove @
            List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            
            List<String> playerCompletions = onlinePlayers.stream()
                    .filter(name -> name.toLowerCase().startsWith(playerName.toLowerCase()))
                    .map(name -> "@" + name) // Add @ back
                    .collect(Collectors.toList());
            
            event.getTabCompletions().addAll(playerCompletions);
            plugin.getLogger().info("Added " + playerCompletions.size() + " @player completions for " + player.getName());
            return;
        }
        
        // Special handling for empty tab completion (show special options)
        if (lastToken.isEmpty()) {
            List<String> specialOptions = new ArrayList<>();
            specialOptions.add("[enderchest]");
            specialOptions.add("[inventory]");
            
            // Add online players
            List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            specialOptions.addAll(onlinePlayers);
            
            event.getTabCompletions().addAll(specialOptions);
            plugin.getLogger().info("Added " + specialOptions.size() + " special options for " + player.getName());
            return;
        }
        
        // Special handling for [ commands
        if (lastToken.startsWith("[")) {
            List<String> bracketCompletions = new ArrayList<>();
            bracketCompletions.add("[enderchest]");
            bracketCompletions.add("[inventory]");
            
            // Add custom commands from config
            if (plugin.getMessagesConfig().isConfigurationSection("custom-commands")) {
                for (String customCmd : plugin.getMessagesConfig().getConfigurationSection("custom-commands").getKeys(false)) {
                    String commandName = plugin.getMessagesConfig().getString("custom-commands." + customCmd + ".name", customCmd);
                    bracketCompletions.add("[" + commandName + "]");
                }
            }
            
            bracketCompletions = bracketCompletions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(lastToken.toLowerCase()))
                    .collect(Collectors.toList());
            
            event.getTabCompletions().addAll(bracketCompletions);
            return;
        }
        
        // Regular completions for chat
        List<String> completions = new ArrayList<>();
        
        // Add online players
        List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        completions.addAll(onlinePlayers);
        
        // Add inventory items (if player has permission)
        if (player.hasPermission("echocore.chat.inventory")) {
            completions.addAll(getInventoryItems(player));
        }
        
        // Add enderchest items (if player has permission)
        if (player.hasPermission("echocore.chat.enderchest")) {
            completions.addAll(getEnderchestItems(player));
        }
        
        // Add special chat commands
        completions.addAll(getChatCommands());
        
        // Filter based on current input
        completions = completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lastToken.toLowerCase()))
                .collect(Collectors.toList());
        
        event.getTabCompletions().addAll(completions);
        plugin.getLogger().info("Added " + completions.size() + " chat completions for " + player.getName());
    }
    
    private List<String> getInventoryItems(Player player) {
        List<String> items = new ArrayList<>();
        ItemStack[] inventory = player.getInventory().getContents();
        
        for (ItemStack item : inventory) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                items.add(item.getItemMeta().getDisplayName());
            } else if (item != null) {
                items.add(item.getType().name().toLowerCase().replace("_", " "));
            }
        }
        
        // Add main hand item (item currently held)
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            if (mainHand.hasItemMeta() && mainHand.getItemMeta().hasDisplayName()) {
                items.add("mainhand:" + mainHand.getItemMeta().getDisplayName());
            } else {
                items.add("mainhand:" + mainHand.getType().name().toLowerCase().replace("_", " "));
            }
        }
        
        return items;
    }
    
    private List<String> getEnderchestItems(Player player) {
        List<String> items = new ArrayList<>();
        ItemStack[] enderchest = player.getEnderChest().getContents();
        
        for (ItemStack item : enderchest) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                items.add("enderchest:" + item.getItemMeta().getDisplayName());
            } else if (item != null) {
                items.add("enderchest:" + item.getType().name().toLowerCase().replace("_", " "));
            }
        }
        
        return items;
    }
    
    private List<String> getChatCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("inventory");
        commands.add("enderchest");
        commands.add("stats");
        commands.add("help");
        commands.add("test");
        return commands;
    }
    
    public boolean isMuted(UUID playerUUID) {
        return mutedPlayers.contains(playerUUID);
    }
    
    public void setMuted(UUID playerUUID, boolean muted) {
        if (muted) {
            mutedPlayers.add(playerUUID);
        } else {
            mutedPlayers.remove(playerUUID);
        }
    }
}
