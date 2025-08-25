package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.GuiManager;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    private final GuiManager guiManager;
    
    public InventoryCommand(EchoCore plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show own inventory
            if (player.hasPermission("echocore.chat.inventory")) {
                guiManager.openInventoryGui(player, player);
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            return true;
        }
        
        if (args.length == 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "enderchest":
                case "ec":
                    if (player.hasPermission("echocore.chat.enderchest")) {
                        guiManager.openEnderchestGui(player, player);
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                    }
                    break;
                    
                case "inventory":
                case "inv":
                    if (player.hasPermission("echocore.chat.inventory")) {
                        guiManager.openInventoryGui(player, player);
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                    }
                    break;
                    
                default:
                    // Check if it's a player name
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        if (player.hasPermission("echocore.chat.inventory")) {
                            guiManager.openInventoryGui(player, target);
                        } else {
                            String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                            player.sendMessage(Utils.colorize(noPermMessage));
                        }
                    } else {
                        sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                                .replace("{player}", args[0]));
                    }
                    break;
            }
            return true;
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String targetName = args[1];
            
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", targetName));
                return true;
            }
            
            switch (subCommand) {
                case "enderchest":
                case "ec":
                    if (player.hasPermission("echocore.chat.enderchest")) {
                        guiManager.openEnderchestGui(player, target);
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                    }
                    break;
                    
                case "inventory":
                case "inv":
                    if (player.hasPermission("echocore.chat.inventory")) {
                        guiManager.openInventoryGui(player, target);
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                    }
                    break;
                    
                default:
                    sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                            .replace("{usage}", "/inventory [enderchest|inventory] [player]"));
                    break;
            }
            return true;
        }
        
        sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                .replace("{usage}", "/inventory [enderchest|inventory] [player]"));
        return true;
    }
}
