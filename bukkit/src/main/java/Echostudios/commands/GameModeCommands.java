package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameModeCommands implements CommandExecutor, TabCompleter {
    
    private final EchoCore plugin;
    
    public GameModeCommands(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        GameMode targetMode = null;
        
        switch (commandName) {
            case "gmc":
                targetMode = GameMode.CREATIVE;
                break;
            case "gms":
                targetMode = GameMode.SURVIVAL;
                break;
            case "gma":
                targetMode = GameMode.ADVENTURE;
                break;
            case "gmsp":
                targetMode = GameMode.SPECTATOR;
                break;
        }
        
        if (targetMode == null) return false;
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
                return true;
            }
            
            Player player = (Player) sender;
            if (!plugin.getPermissionChecker().has(player, "echocore.gamemode." + targetMode.name().toLowerCase())) {
                player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
            
            player.setGameMode(targetMode);
            String message = Utils.getMessageWithPrefix(plugin, "gamemode.changed", "&aYour gamemode has been changed to &e{gamemode}&a!")
                    .replace("{gamemode}", targetMode.name().toLowerCase());
            player.sendMessage(Utils.colorize(message));
            
        } else if (args.length == 1) {
            if (!plugin.getPermissionChecker().has(sender, "echocore.gamemode.others")) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", args[0]));
                return true;
            }
            
            target.setGameMode(targetMode);
            String message = Utils.getMessageWithPrefix(plugin, "gamemode.changed-others", "&aYou changed &e{player}'s &agamemode to &e{gamemode}&a!")
                    .replace("{player}", target.getName())
                    .replace("{gamemode}", targetMode.name().toLowerCase());
            sender.sendMessage(Utils.colorize(message));
            
            String targetMessage = Utils.getMessageWithPrefix(plugin, "gamemode.changed-by-other", "&aYour gamemode was changed to &e{gamemode} &aby &e{sender}&a!")
                    .replace("{gamemode}", targetMode.name().toLowerCase())
                    .replace("{sender}", sender.getName());
            target.sendMessage(Utils.colorize(targetMessage));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names for the second argument
            if (plugin.getPermissionChecker().has(sender, "echocore.gamemode.others")) {
                String input = args[0].toLowerCase();
                List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
                completions.addAll(playerNames);
            }
        }
        
        return completions;
    }
}
