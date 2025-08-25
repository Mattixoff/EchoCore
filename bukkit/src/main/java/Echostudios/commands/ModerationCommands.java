package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import Echostudios.utils.WebhookManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModerationCommands implements CommandExecutor {
    
    private final EchoCore plugin;
    private final WebhookManager webhookManager;
    private final Map<UUID, String> mutedPlayers = new HashMap<>();
    private final Map<UUID, Integer> warnedPlayers = new HashMap<>();
    
    public ModerationCommands(EchoCore plugin) {
        this.plugin = plugin;
        this.webhookManager = new WebhookManager(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "ban":
                return handleBan(sender, args);
            case "unban":
                return handleUnban(sender, args);
            case "kick":
                return handleKick(sender, args);
            case "warn":
                return handleWarn(sender, args);
            case "unwarn":
                return handleUnwarn(sender, args);
            case "mute":
                return handleMute(sender, args);
            case "unmute":
                return handleUnmute(sender, args);
            default:
                return false;
        }
    }
    
    private boolean handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.ban")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/ban <player> [duration] [reason]"));
            return true;
        }
        
        String targetName = args[0];
        String reason = "";
        long duration = 0;
        
        if (args.length >= 2) {
            // Check if second argument is duration
            String durationStr = args[1];
            if (durationStr.matches("\\d+[dhms]")) {
                duration = Utils.parseDuration(durationStr);
                reason = args.length > 2 ? String.join(" ", args).substring(targetName.length() + durationStr.length() + 2) : 
                        Utils.getMessage(plugin, "moderation.default-ban-reason", "No reason specified");
            } else {
                reason = String.join(" ", args).substring(targetName.length() + 1);
            }
        } else {
            reason = Utils.getMessage(plugin, "moderation.default-ban-reason", "No reason specified");
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.kickPlayer(Utils.colorize(Utils.getMessage(plugin, "moderation.ban-message", "&cYou have been banned from this server!\\n&cReason: &e{reason}")
                    .replace("{reason}", reason)));
        }
        
        // Ban in Bukkit
        Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, reason, 
                duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration) : null, sender.getName());
        
        // Save to database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().banPlayer(target != null ? target.getUniqueId() : 
                    java.util.UUID.randomUUID(), reason, sender.getName(), duration);
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-banned", "&c{player} &chas been banned by &e{sender}&c!\\n&cReason: &e{reason}")
                .replace("{player}", targetName)
                .replace("{reason}", reason)
                .replace("{sender}", sender.getName());
        
        if (duration > 0) {
            message += "\n&cDuration: &e" + Utils.formatDuration(duration);
        }
        
        Bukkit.broadcastMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Ban", targetName, sender.getName(), reason);
        
        return true;
    }
    
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.unban")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/unban <player>"));
            return true;
        }
        
        String targetName = args[0];
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        
        // Update database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            // Note: We need to get the UUID from the database or use a different approach
            plugin.getDatabaseManager().unbanPlayer(java.util.UUID.randomUUID(), sender.getName());
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-unbanned", "&a{player} &ahas been unbanned by &e{sender}&a!")
                .replace("{player}", targetName)
                .replace("{sender}", sender.getName());
        
        sender.sendMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Unban", targetName, sender.getName(), "Player unbanned");
        
        return true;
    }
    
    private boolean handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.kick")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/kick <player> [reason]"));
            return true;
        }
        
        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", args).substring(targetName.length() + 1) : 
                Utils.getMessage(plugin, "moderation.default-kick-reason", "No reason specified");
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                    .replace("{player}", targetName));
            return true;
        }
        
        target.kickPlayer(Utils.colorize(Utils.getMessage(plugin, "moderation.kick-message", "&cYou have been kicked from this server!\\n&cReason: &e{reason}")
                .replace("{reason}", reason)));
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-kicked", "&c{player} &chas been kicked by &e{sender}&c!\\n&cReason: &e{reason}")
                .replace("{player}", targetName)
                .replace("{reason}", reason)
                .replace("{sender}", sender.getName());
        
        Bukkit.broadcastMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Kick", targetName, sender.getName(), reason);
        
        return true;
    }
    
    private boolean handleWarn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.warn")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/warn <player> <reason>"));
            return true;
        }
        
        String targetName = args[0];
        String reason = String.join(" ", args).substring(targetName.length() + 1);
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                    .replace("{player}", targetName));
            return true;
        }
        
        UUID targetUUID = target.getUniqueId();
        int warnings = warnedPlayers.getOrDefault(targetUUID, 0) + 1;
        warnedPlayers.put(targetUUID, warnings);
        
        // Update database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().warnPlayer(targetUUID, reason, sender.getName());
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-warned", "&e{player} &chas been warned by &e{sender}&c!\\n&cReason: &e{reason}\\n&cWarnings: &e{warnings}")
                .replace("{player}", targetName)
                .replace("{reason}", reason)
                .replace("{sender}", sender.getName())
                .replace("{warnings}", String.valueOf(warnings));
        
        Bukkit.broadcastMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Warn", targetName, sender.getName(), reason + " (Warning #" + warnings + ")");
        
        return true;
    }
    
    private boolean handleUnwarn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.unwarn")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/unwarn <player>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : null;
        
        if (targetUUID == null || !warnedPlayers.containsKey(targetUUID)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "moderation.player-not-warned", "&e{player} &chas no warnings to remove!")
                    .replace("{player}", targetName));
            return true;
        }
        
        int warnings = warnedPlayers.get(targetUUID) - 1;
        if (warnings <= 0) {
            warnedPlayers.remove(targetUUID);
        } else {
            warnedPlayers.put(targetUUID, warnings);
        }
        
        // Update database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().unwarnPlayer(targetUUID, sender.getName());
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-unwarned", "&aA warning has been removed from &e{player} &aby &e{sender}&a!\\n&cRemaining warnings: &e{warnings}")
                .replace("{player}", targetName)
                .replace("{sender}", sender.getName())
                .replace("{warnings}", String.valueOf(Math.max(0, warnings)));
        
        sender.sendMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Unwarn", targetName, sender.getName(), "Warning removed");
        
        return true;
    }
    
    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.mute")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/mute <player> [duration] [reason]"));
            return true;
        }
        
        String targetName = args[0];
        String reason = "";
        long duration = 0;
        
        if (args.length >= 2) {
            // Check if second argument is duration
            String durationStr = args[1];
            if (durationStr.matches("\\d+[dhms]")) {
                duration = Utils.parseDuration(durationStr);
                reason = args.length > 2 ? String.join(" ", args).substring(targetName.length() + durationStr.length() + 2) : 
                        Utils.getMessage(plugin, "moderation.default-mute-reason", "No reason specified");
            } else {
                reason = String.join(" ", args).substring(targetName.length() + 1);
            }
        } else {
            reason = Utils.getMessage(plugin, "moderation.default-mute-reason", "No reason specified");
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                    .replace("{player}", targetName));
            return true;
        }
        
        mutedPlayers.put(target.getUniqueId(), reason);
        
        // Update database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().mutePlayer(target.getUniqueId(), reason, sender.getName(), duration);
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-muted", "&c{player} &chas been muted by &e{sender}&c!\\n&cReason: &e{reason}")
                .replace("{player}", targetName)
                .replace("{reason}", reason)
                .replace("{sender}", sender.getName());
        
        if (duration > 0) {
            message += "\n&cDuration: &e" + Utils.formatDuration(duration);
        }
        
        Bukkit.broadcastMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Mute", targetName, sender.getName(), reason);
        
        return true;
    }
    
    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("echocore.moderation.unmute")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/unmute <player>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : null;
        
        if (targetUUID == null || !mutedPlayers.containsKey(targetUUID)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "moderation.player-not-muted", "&e{player} &cis not currently muted!")
                    .replace("{player}", targetName));
            return true;
        }
        
        mutedPlayers.remove(targetUUID);
        
        // Update database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            plugin.getDatabaseManager().unmutePlayer(targetUUID, sender.getName());
        }
        
        String message = Utils.getMessageWithPrefix(plugin, "moderation.player-unmuted", "&a{player} &ahas been unmuted by &e{sender}&a!")
                .replace("{player}", targetName)
                .replace("{sender}", sender.getName());
        
        Bukkit.broadcastMessage(Utils.colorize(message));
        
        // Send webhook notification
        webhookManager.sendModerationWebhook("Unmute", targetName, sender.getName(), "Player unmuted");
        
        return true;
    }
    
    public boolean isMuted(UUID playerUUID) {
        return mutedPlayers.containsKey(playerUUID);
    }
    
    public int getWarnings(UUID playerUUID) {
        return warnedPlayers.getOrDefault(playerUUID, 0);
    }
}
