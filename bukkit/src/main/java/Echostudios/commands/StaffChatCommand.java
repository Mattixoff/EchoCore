package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    
    public StaffChatCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getPermissionChecker().has(sender, "echocore.staffchat")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/staffchat <message>"));
            return true;
        }
        
        String message = String.join(" ", args);
        String staffMessage = Utils.getMessageWithPrefix(plugin, "staffchat.message", "&8[&cStaffChat&8] &e{sender}&8: &f{message}")
                .replace("{sender}", sender.getName())
                .replace("{message}", message);
        
        // Send to all online staff members
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getPermissionChecker().has(player, "echocore.staffchat")) {
                player.sendMessage(Utils.colorize(staffMessage));
            }
        }
        
        // Also send to console
        Bukkit.getConsoleSender().sendMessage(Utils.colorize(staffMessage));
        
        return true;
    }
}
