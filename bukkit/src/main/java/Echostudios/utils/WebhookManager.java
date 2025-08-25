package Echostudios.utils;

import Echostudios.EchoCore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class WebhookManager {
    
    private final EchoCore plugin;
    private final Gson gson;
    
    public WebhookManager(EchoCore plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }
    
    public void sendModerationWebhook(String action, String target, String moderator, String reason) {
        // Try to get webhook config from server.yml first, fallback to main config
        boolean webhookEnabled = false;
        String webhookUrl = "";
        
        try {
            webhookEnabled = plugin.getServerConfig().getBoolean("webhook.enabled", false);
            webhookUrl = plugin.getServerConfig().getString("webhook.moderation-url", "");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load webhook config from server.yml, trying main config...");
            webhookEnabled = plugin.getConfig().getBoolean("webhook.enabled", false);
            webhookUrl = plugin.getConfig().getString("webhook.moderation-url", "");
        }
        
        if (!webhookEnabled) {
            return;
        }
        
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("your-webhook-url")) {
            return;
        }
        
        try {
            // Get webhook configuration from main config (for customization)
            ConfigurationSection webhookConfig = plugin.getConfig().getConfigurationSection("webhook.moderation");
            String username = "EchoCore Moderation";
            String avatarUrl = "";
            
            if (webhookConfig != null) {
                username = webhookConfig.getString("username", "EchoCore Moderation");
                avatarUrl = webhookConfig.getString("avatar-url", "");
            }
            
            // Create embed
            JsonObject embed = createModerationEmbed(action, target, moderator, reason, webhookConfig);
            
            // Create the webhook payload
            JsonObject payload = new JsonObject();
            if (!username.isEmpty()) {
                payload.addProperty("username", username);
            }
            if (!avatarUrl.isEmpty()) {
                payload.addProperty("avatar_url", avatarUrl);
            }
            payload.add("embeds", gson.toJsonTree(new JsonObject[]{embed}));
            
            // Send the webhook
            sendWebhook(webhookUrl, payload.toString());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send moderation webhook: " + e.getMessage());
        }
    }
    
    public void sendJoinLeaveWebhook(String action, String playerName) {
        // Try to get webhook config from server.yml first, fallback to main config
        boolean webhookEnabled = false;
        String webhookUrl = "";
        
        try {
            webhookEnabled = plugin.getServerConfig().getBoolean("webhook.enabled", false);
            webhookUrl = plugin.getServerConfig().getString("webhook.join-leave-url", "");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load webhook config from server.yml, trying main config...");
            webhookEnabled = plugin.getConfig().getBoolean("webhook.enabled", false);
            webhookUrl = plugin.getConfig().getString("webhook.join-leave-url", "");
        }
        
        if (!webhookEnabled) {
            return;
        }
        
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("your-webhook-url")) {
            return;
        }
        
        try {
            // Get webhook configuration from main config (for customization)
            ConfigurationSection webhookConfig = plugin.getConfig().getConfigurationSection("webhook.join-leave");
            String username = "EchoCore Server";
            String avatarUrl = "";
            
            if (webhookConfig != null) {
                username = webhookConfig.getString("username", "EchoCore Server");
                avatarUrl = webhookConfig.getString("avatar-url", "");
            }
            
            // Create embed
            JsonObject embed = createJoinLeaveEmbed(action, playerName, webhookConfig);
            
            // Create the webhook payload
            JsonObject payload = new JsonObject();
            if (!username.isEmpty()) {
                payload.addProperty("username", username);
            }
            if (!avatarUrl.isEmpty()) {
                payload.addProperty("avatar_url", avatarUrl);
            }
            payload.add("embeds", gson.toJsonTree(new JsonObject[]{embed}));
            
            sendWebhook(webhookUrl, payload.toString());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send join/leave webhook: " + e.getMessage());
        }
    }
    
    private JsonObject createModerationEmbed(String action, String target, String moderator, String reason, ConfigurationSection config) {
        JsonObject embed = new JsonObject();
        
        // Title
        String title = config.getString("title", "{action} Action")
                .replace("{action}", action)
                .replace("{moderation}", action);
        embed.addProperty("title", title);
        
        // Description
        String description = config.getString("description", "A moderation action has been performed")
                .replace("{action}", action)
                .replace("{moderation}", action)
                .replace("{target}", target)
                .replace("{moderator}", moderator)
                .replace("{reason}", reason);
        embed.addProperty("description", description);
        
        // Color
        String colorHex = config.getString("color", "#FF0000");
        int color = parseColor(colorHex);
        embed.addProperty("color", color);
        
        // Timestamp
        if (config.getBoolean("show-timestamp", true)) {
            embed.addProperty("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        }
        
        // Footer
        if (config.getBoolean("show-footer", true)) {
            JsonObject footer = new JsonObject();
            String footerText = config.getString("footer.text", "EchoCore Moderation")
                    .replace("{action}", action)
                    .replace("{moderation}", action);
            footer.addProperty("text", footerText);
            
            String footerIcon = config.getString("footer.icon", "");
            if (!footerIcon.isEmpty()) {
                footer.addProperty("icon_url", footerIcon);
            }
            embed.add("footer", footer);
        }
        
        // Fields
        List<Map<?, ?>> fields = config.getMapList("fields");
        if (!fields.isEmpty()) {
            embed.add("fields", gson.toJsonTree(fields));
        } else {
            // Default fields
            embed.add("fields", gson.toJsonTree(createDefaultModerationFields(action, target, moderator, reason)));
        }
        
        return embed;
    }
    
    private JsonObject createJoinLeaveEmbed(String action, String playerName, ConfigurationSection config) {
        JsonObject embed = new JsonObject();
        
        // Title
        String title = config.getString("title", "Player {action}")
                .replace("{action}", action)
                .replace("{user}", playerName);
        embed.addProperty("title", title);
        
        // Description
        String description = config.getString("description", "A player has {action} the server")
                .replace("{action}", action.toLowerCase())
                .replace("{user}", playerName);
        embed.addProperty("description", description);
        
        // Color
        String colorHex = action.equalsIgnoreCase("Joined") ? 
                config.getString("join-color", "#00FF00") : 
                config.getString("leave-color", "#FF0000");
        int color = parseColor(colorHex);
        embed.addProperty("color", color);
        
        // Timestamp
        if (config.getBoolean("show-timestamp", true)) {
            embed.addProperty("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        }
        
        // Footer
        if (config.getBoolean("show-footer", true)) {
            JsonObject footer = new JsonObject();
            String footerText = config.getString("footer.text", "EchoCore Server")
                    .replace("{action}", action)
                    .replace("{user}", playerName);
            footer.addProperty("text", footerText);
            
            String footerIcon = config.getString("footer.icon", "");
            if (!footerIcon.isEmpty()) {
                footer.addProperty("icon_url", footerIcon);
            }
            embed.add("footer", footer);
        }
        
        // Fields
        List<Map<?, ?>> fields = config.getMapList("fields");
        if (!fields.isEmpty()) {
            embed.add("fields", gson.toJsonTree(fields));
        } else {
            // Default field
            JsonObject playerField = new JsonObject();
            playerField.addProperty("name", config.getString("player-field.name", "Player"));
            playerField.addProperty("value", playerName);
            playerField.addProperty("inline", true);
            embed.add("fields", gson.toJsonTree(new JsonObject[]{playerField}));
        }
        
        return embed;
    }
    
    private JsonObject[] createDefaultModerationFields(String action, String target, String moderator, String reason) {
        JsonObject targetField = new JsonObject();
        targetField.addProperty("name", "Target Player");
        targetField.addProperty("value", target);
        targetField.addProperty("inline", true);
        
        JsonObject moderatorField = new JsonObject();
        moderatorField.addProperty("name", "Moderator");
        moderatorField.addProperty("value", moderator);
        moderatorField.addProperty("inline", true);
        
        JsonObject reasonField = new JsonObject();
        reasonField.addProperty("name", "Reason");
        reasonField.addProperty("value", reason.isEmpty() ? "No reason specified" : reason);
        reasonField.addProperty("inline", false);
        
        return new JsonObject[]{targetField, moderatorField, reasonField};
    }
    
    private int parseColor(String colorHex) {
        if (colorHex.startsWith("#")) {
            colorHex = colorHex.substring(1);
        }
        
        try {
            return Integer.parseInt(colorHex, 16);
        } catch (NumberFormatException e) {
            // Default colors for common actions
            switch (colorHex.toLowerCase()) {
                case "red": return 0xFF0000;
                case "green": return 0x00FF00;
                case "blue": return 0x0000FF;
                case "yellow": return 0xFFFF00;
                case "orange": return 0xFFA500;
                case "purple": return 0x800080;
                case "gray": case "grey": return 0x808080;
                default: return 0x808080;
            }
        }
    }
    
    private void sendWebhook(String webhookUrl, String payload) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "EchoCore/1.0");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            plugin.getLogger().warning("Webhook request failed with response code: " + responseCode);
        }
        
        connection.disconnect();
    }
}
