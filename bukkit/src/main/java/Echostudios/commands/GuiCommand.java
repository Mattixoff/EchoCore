package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.GuiManager;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiCommand implements CommandExecutor, TabCompleter {
    
    private final EchoCore plugin;
    private final GuiManager guiManager;
    
    public GuiCommand(EchoCore plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage(Utils.colorize("§cUsage: /gui <gui_name> [player]"));
            return true;
        }
        
        String guiName = args[0].toLowerCase();
        Player target = player; // Default to self
        
        // Check if second argument is a player name
        if (args.length >= 2) {
            String targetName = args[1];
            Player foundPlayer = Bukkit.getPlayer(targetName);
            if (foundPlayer != null) {
                target = foundPlayer;
            } else {
                player.sendMessage(Utils.colorize("§cPlayer '" + targetName + "' not found!"));
                return true;
            }
        }
        
        // Check if player has permission to open this GUI
        String permission = plugin.getMessagesConfig().getString("gui.custom." + guiName + ".permission", "");
        if (!permission.isEmpty() && !plugin.getPermissionChecker().has(player, permission)) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "gui.no-permission", "&cYou don't have permission to open this GUI!");
            player.sendMessage(Utils.colorize(noPermMessage));
            return true;
        }
        
        // Open the custom GUI
        boolean success = guiManager.openCustomGui(player, target, guiName);
        if (!success) {
            player.sendMessage(Utils.colorize("§cGUI '" + guiName + "' not found!"));
            // Debug: Check if the GUI exists in config
            if (plugin.getMessagesConfig().contains("gui.custom." + guiName)) {
                player.sendMessage(Utils.colorize("§eDebug: GUI exists in config but failed to open"));
            } else {
                player.sendMessage(Utils.colorize("§eDebug: GUI does not exist in config"));
            }
        } else {
            player.sendMessage(Utils.colorize("§aGUI '" + guiName + "' opened successfully!"));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: GUI names
            // Retrieve custom GUI names from messages.yml
            if (plugin.getMessagesConfig().isConfigurationSection("gui.custom")) {
                completions.addAll(plugin.getMessagesConfig().getConfigurationSection("gui.custom").getKeys(false));
            }

            // Filter based on input
            completions = completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument: Player names
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            
            completions.addAll(playerNames);
            
            // Filter based on input
            completions = completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}
