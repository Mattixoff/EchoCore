package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    
    public FlyCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Toggle own flight
            if (!player.hasPermission("echocore.fly")) {
                player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
            
            boolean flying = !player.getAllowFlight();
            player.setAllowFlight(flying);
            player.setFlying(flying);
            
            String message = flying ? 
                    Utils.getMessageWithPrefix(plugin, "fly.enabled", "&aFlight mode has been &aenabled&a!") :
                    Utils.getMessageWithPrefix(plugin, "fly.disabled", "&cFlight mode has been &cdisabled&c!");
            player.sendMessage(Utils.colorize(message));
            
        } else if (args.length == 1) {
            // Toggle flight for another player
            if (!player.hasPermission("echocore.fly.others")) {
                player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
            
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);
            
            if (target == null) {
                player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", targetName));
                return true;
            }
            
            boolean flying = !target.getAllowFlight();
            target.setAllowFlight(flying);
            target.setFlying(flying);
            
            String targetMessage = flying ? 
                    Utils.getMessageWithPrefix(plugin, "fly.enabled-by-other", "&aFlight mode has been &aenabled &aby &e{sender}&a!")
                            .replace("{sender}", player.getName()) :
                    Utils.getMessageWithPrefix(plugin, "fly.disabled-by-other", "&cFlight mode has been &cdisabled &cby &e{sender}&c!")
                            .replace("{sender}", player.getName());
            target.sendMessage(Utils.colorize(targetMessage));
            
            String senderMessage = flying ? 
                    Utils.getMessageWithPrefix(plugin, "fly.enabled-for-other", "&aFlight mode has been &aenabled &afor &e{player}&a!")
                            .replace("{player}", target.getName()) :
                    Utils.getMessageWithPrefix(plugin, "fly.disabled-for-other", "&cFlight mode has been &cdisabled &cfor &e{player}&c!")
                            .replace("{player}", target.getName());
            player.sendMessage(Utils.colorize(senderMessage));
            
        } else {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/fly [player]"));
        }
        
        return true;
    }
}
