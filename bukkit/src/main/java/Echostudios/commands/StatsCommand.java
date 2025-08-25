package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StatsCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    
    public StatsCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        
        if (args.length == 0) {
            // If no player specified, use the sender
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            String targetName = args[0];
            target = Bukkit.getPlayer(targetName);
            
            if (target == null) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", targetName));
                return true;
            }
        } else {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/stats [player]"));
            return true;
        }
        
        UUID targetUUID = target.getUniqueId();
        
        // Get player stats from database
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            final String targetName = target.getName(); // Store target name for error handling
            CompletableFuture.runAsync(() -> {
                try {
                    // Get player info
                    String firstJoin = plugin.getDatabaseManager().getFirstJoin(targetUUID).get();
                    String lastSeen = plugin.getDatabaseManager().getLastSeen(targetUUID).get();
                    int totalLogins = plugin.getDatabaseManager().getTotalLogins(targetUUID).get();
                    
                    // Get moderation stats
                    int warnings = plugin.getDatabaseManager().getPlayerWarnings(targetUUID).get();
                    int bans = plugin.getDatabaseManager().getBans(targetUUID).get();
                    int kicks = plugin.getDatabaseManager().getKicks(targetUUID).get();
                    int mutes = plugin.getDatabaseManager().getMutes(targetUUID).get();
                    
                    // Send stats to sender
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        showPlayerStats(sender, target, firstJoin, lastSeen, totalLogins, warnings, bans, kicks, mutes);
                    });
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Error getting stats for " + targetName + ": " + e.getMessage());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(Utils.colorize("&cError retrieving player statistics. Check console for details."));
                    });
                }
            });
        } else {
            // Fallback to basic stats
            showBasicStats(sender, target);
        }
        
        return true;
    }
    
    private void showPlayerStats(CommandSender sender, Player target, String firstJoin, String lastSeen, 
                                int totalLogins, int warnings, int bans, int kicks, int mutes) {
        
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&b&l                    Player Statistics                    "));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage("");
        
        // Player info
        sender.sendMessage(Utils.colorize("&b&lPlayer: &f" + target.getName()));
        sender.sendMessage(Utils.colorize("&b&lUUID: &f" + target.getUniqueId()));
        sender.sendMessage(Utils.colorize("&b&lStatus: &aOnline"));
        sender.sendMessage("");
        
        // Join statistics
        sender.sendMessage(Utils.colorize("&e&lJoin Statistics:"));
        sender.sendMessage(Utils.colorize("&7- &fFirst Join: &e" + (firstJoin != null ? firstJoin : "Unknown")));
        sender.sendMessage(Utils.colorize("&7- &fLast Seen: &e" + (lastSeen != null ? lastSeen : "Unknown")));
        sender.sendMessage(Utils.colorize("&7- &fTotal Logins: &e" + totalLogins));
        sender.sendMessage("");
        
        // Moderation statistics
        sender.sendMessage(Utils.colorize("&c&lModeration Statistics:"));
        sender.sendMessage(Utils.colorize("&7- &fWarnings: &e" + warnings));
        sender.sendMessage(Utils.colorize("&7- &fBans: &e" + bans));
        sender.sendMessage(Utils.colorize("&7- &fKicks: &e" + kicks));
        sender.sendMessage(Utils.colorize("&7- &fMutes: &e" + mutes));
        sender.sendMessage("");
        
        // Current status (only visible to staff)
        if (sender.hasPermission("echocore.stats.staff") || sender.isOp()) {
            if (plugin.getModerationCommands() != null) {
                boolean isMuted = plugin.getModerationCommands().isMuted(target.getUniqueId());
                boolean isVanished = plugin.getVanishCommand() != null && 
                    plugin.getVanishCommand().isVanished(target.getUniqueId());
                
                sender.sendMessage(Utils.colorize("&6&lCurrent Status:"));
                sender.sendMessage(Utils.colorize("&7- &fMuted: " + (isMuted ? "&cYes" : "&aNo")));
                sender.sendMessage(Utils.colorize("&7- &fVanished: " + (isVanished ? "&cYes" : "&aNo")));
                sender.sendMessage("");
            }
        }
        
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
    }
    
    private void showBasicStats(CommandSender sender, Player target) {
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&b&l                    Player Statistics                    "));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage("");
        
        sender.sendMessage(Utils.colorize("&b&lPlayer: &f" + target.getName()));
        sender.sendMessage(Utils.colorize("&b&lUUID: &f" + target.getUniqueId()));
        sender.sendMessage(Utils.colorize("&b&lStatus: &aOnline"));
        sender.sendMessage(Utils.colorize("&b&lGamemode: &f" + target.getGameMode().name()));
        sender.sendMessage(Utils.colorize("&b&lHealth: &f" + Math.round(target.getHealth()) + "/" + Math.round(target.getMaxHealth())));
        sender.sendMessage(Utils.colorize("&b&lFood: &f" + target.getFoodLevel() + "/20"));
        sender.sendMessage(Utils.colorize("&b&lXP Level: &f" + target.getLevel()));
        sender.sendMessage("");
        
        sender.sendMessage(Utils.colorize("&c&lNote: &7Database not connected. Limited statistics shown."));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
    }
}
