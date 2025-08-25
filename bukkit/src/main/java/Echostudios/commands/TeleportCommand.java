package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    
    public TeleportCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("echocore.teleport")) {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/tp <player>"));
            return true;
        }
        
        String targetName = args[0];
        List<Player> possibleTargets = new ArrayList<>();
        
        // First try exact match
        Player exactTarget = Bukkit.getPlayer(targetName);
        if (exactTarget != null) {
            possibleTargets.add(exactTarget);
        }
        
        // Then try partial matches
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().startsWith(targetName.toLowerCase()) && 
                !possibleTargets.contains(onlinePlayer)) {
                possibleTargets.add(onlinePlayer);
            }
        }
        
        if (possibleTargets.isEmpty()) {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                    .replace("{player}", targetName));
            return true;
        }
        
        if (possibleTargets.size() == 1) {
            Player target = possibleTargets.get(0);
            player.teleport(target);
            String message = Utils.getMessageWithPrefix(plugin, "teleport.teleported", "&aYou have been teleported to &e{player}&a!")
                    .replace("{player}", target.getName());
            player.sendMessage(Utils.colorize(message));
        } else {
            // Multiple matches found
            String playerList = possibleTargets.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
            
            String message = Utils.getMessageWithPrefix(plugin, "teleport.multiple-players-found", "&eMultiple players found starting with &a{input}&e: &f{players}")
                    .replace("{players}", playerList)
                    .replace("{input}", targetName);
            player.sendMessage(Utils.colorize(message));
            
            // Suggest using more characters
            String suggestion = Utils.getMessageWithPrefix(plugin, "teleport.suggest-more-characters", "&7Try using more characters: &e{suggestion}")
                    .replace("{suggestion}", getSuggestion(targetName, possibleTargets));
            player.sendMessage(Utils.colorize(suggestion));
        }
        
        return true;
    }
    
    private String getSuggestion(String input, List<Player> players) {
        if (input.length() >= 3) return input;
        
        for (int i = input.length(); i < 3; i++) {
            String testInput = input + "a";
            List<Player> matches = players.stream()
                    .filter(p -> p.getName().toLowerCase().startsWith(testInput.toLowerCase()))
                    .collect(Collectors.toList());
            
            if (matches.size() == 1) {
                return testInput;
            }
        }
        
        return input + "a";
    }
}
