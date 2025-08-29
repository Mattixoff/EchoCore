package Echostudios.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import Echostudios.EchoCore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^:]+):([^:]+):([^>]+)>");
    private static final Pattern UNICODE_GRADIENT_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})&l([^&#]+)");
    
    /**
     * Colorize a message with support for legacy colors, hex colors, and gradients
     */
    public static String colorize(String msg) {
        if (msg == null) return "";
        
        // Replace newlines first
        msg = msg.replace("\\n", "\n");
        
        // Process Unicode gradients (&#048FFF&lᴏ&#1788FF&lᴡ&#2A80FF&lɴ&#3D79FF&lᴇ&#5071FF&lʀ)
        msg = processUnicodeGradients(msg);
        
        // Process regular gradients
        msg = processGradients(msg);
        
        // Process hex colors
        msg = processHexColors(msg);
        
        // Process legacy colors
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        
        return msg;
    }
    
    /**
     * Colorize a message with a prefix
     */
    public static String colorizeWithPrefix(String msg, String prefix) {
        if (msg == null) return "";
        if (prefix == null) prefix = "";
        
        return colorize(prefix + msg);
    }

    /**
     * Apply PlaceholderAPI placeholders if available
     */
    public static String applyPlaceholders(org.bukkit.entity.Player player, String text) {
        if (text == null) return "";
        try {
            if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            }
        } catch (Throwable ignored) {}
        return text;
    }
    
    /**
     * Format a message with prefix, placeholders, and color codes
     */
    public static String formatMessage(String message, String prefix, Object... placeholders) {
        if (message == null) return "";
        
        String formatted = message;
        
        // Apply prefix
        if (prefix != null && !prefix.isEmpty()) {
            formatted = prefix + formatted;
        }
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "{" + placeholders[i] + "}";
                String value = String.valueOf(placeholders[i + 1]);
                formatted = formatted.replace(placeholder, value);
            }
        }
        
        // Colorize the final message
        return colorize(formatted);
    }
    
    /**
     * Get a message from the messages configuration file
     */
    public static String getMessage(EchoCore plugin, String path, String defaultValue) {
        if (plugin == null) {
            return defaultValue;
        }
        
        FileConfiguration messagesConfig = plugin.getMessagesConfig();
        if (messagesConfig == null) {
            return defaultValue;
        }
        
        String message = messagesConfig.getString(path, defaultValue);
        if (message == null || message.isEmpty()) {
            return defaultValue;
        }
        
        return message;
    }
    
    /**
     * Get a message from the messages configuration file with prefix
     */
    public static String getMessageWithPrefix(EchoCore plugin, String path, String defaultValue) {
        if (plugin == null) {
            return defaultValue;
        }
        
        String message = getMessage(plugin, path, defaultValue);
        String prefix = "";
        
        try {
            prefix = plugin.getMessagesConfig().getString("prefix", "&8[&bEchoCore&8] &r");
        } catch (Exception e) {
            // If messages config is not available, use default prefix
            prefix = "&8[&bEchoCore&8] &r";
        }
        
        // Replace {prefix} placeholder with actual prefix
        message = message.replace("{prefix}", prefix);
        
        return colorize(message);
    }
    
    /**
     * Format duration from milliseconds to human readable string
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds <= 0) return "0s";
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("d ");
        }
        if (hours % 24 > 0) {
            result.append(hours % 24).append("h ");
        }
        if (minutes % 60 > 0) {
            result.append(minutes % 60).append("m ");
        }
        if (seconds % 60 > 0) {
            result.append(seconds % 60).append("s");
        }
        
        return result.toString().trim();
    }
    
    /**
     * Parse duration string to milliseconds
     * Supports: 30s, 5m, 2h, 1d
     */
    public static long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;
        
        duration = duration.toLowerCase().trim();
        long total = 0;
        
        // Parse the duration string
        Pattern pattern = Pattern.compile("(\\d+)([smhd])");
        Matcher matcher = pattern.matcher(duration);
        
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "s":
                    total += value * 1000; // seconds to milliseconds
                    break;
                case "m":
                    total += value * 60 * 1000; // minutes to milliseconds
                    break;
                case "h":
                    total += value * 60 * 60 * 1000; // hours to milliseconds
                    break;
                case "d":
                    total += value * 24 * 60 * 60 * 1000; // days to milliseconds
                    break;
            }
        }
        
        return total;
    }
    
    /**
     * Process Unicode gradient colors in text (&#048FFF&lᴏ&#1788FF&lᴡ&#2A80FF&lɴ&#3D79FF&lᴇ&#5071FF&lʀ)
     */
    private static String processUnicodeGradients(String text) {
        Matcher matcher = UNICODE_GRADIENT_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String content = matcher.group(2);
            
            // Convert hex to color codes
            String replacement = "§x";
            for (char c : hexColor.toCharArray()) {
                replacement += "§" + c;
            }
            replacement += "§l" + content;
            
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Process gradient colors in text
     */
    private static String processGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String color1 = matcher.group(1);
            String color2 = matcher.group(2);
            String content = matcher.group(3);
            
            // Simple gradient implementation
            String gradientText = createSimpleGradient(content, color1, color2);
            matcher.appendReplacement(result, gradientText);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Create a simple gradient effect
     */
    private static String createSimpleGradient(String text, String color1, String color2) {
        if (text.length() <= 1) {
            return ChatColor.translateAlternateColorCodes('&', "&" + color1 + text);
        }
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }
            
            // Simple color alternation for gradient effect
            String color = (i % 2 == 0) ? color1 : color2;
            result.append("&").append(color).append(c);
        }
        
        return result.toString();
    }
    
    /**
     * Process hex colors in text
     */
    private static String processHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = "§x";
            
            // Convert hex to color codes
            for (char c : hexColor.toCharArray()) {
                replacement += "§" + c;
            }
            
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}