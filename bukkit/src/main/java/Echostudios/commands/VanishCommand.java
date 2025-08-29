package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VanishCommand implements CommandExecutor, TabCompleter {
    
    private final EchoCore plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    
    public VanishCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-only", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getPermissionChecker().has(player, "echocore.vanish")) {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            // Toggle own vanish
            toggleVanish(player);
            
        } else if (args.length == 1) {
            // Toggle vanish for another player
            if (!plugin.getPermissionChecker().has(player, "echocore.vanish.others")) {
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
            
            toggleVanishForOther(player, target);
            
        } else {
            player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/vanish [player]"));
        }
        
        return true;
    }
    
    private void toggleVanish(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean isVanished = vanishedPlayers.contains(playerUUID);
        
        if (isVanished) {
            // Show player
            vanishedPlayers.remove(playerUUID);
            showPlayer(player);
            String message = Utils.getMessageWithPrefix(plugin, "vanish.disabled", "&cYou are now &cvisible&c!");
            player.sendMessage(Utils.colorize(message));
        } else {
            // Hide player
            vanishedPlayers.add(playerUUID);
            hidePlayer(player);
            String message = Utils.getMessageWithPrefix(plugin, "vanish.enabled", "&aYou are now &avinvisible&a!");
            player.sendMessage(Utils.colorize(message));
        }
    }
    
    private void toggleVanishForOther(Player sender, Player target) {
        UUID targetUUID = target.getUniqueId();
        boolean isVanished = vanishedPlayers.contains(targetUUID);
        
        if (isVanished) {
            // Show player
            vanishedPlayers.remove(targetUUID);
            showPlayer(target);
            String targetMessage = Utils.getMessageWithPrefix(plugin, "vanish.disabled-by-other", "&cYou have been made &cvisible &cby &e{sender}&c!")
                    .replace("{sender}", sender.getName());
            target.sendMessage(Utils.colorize(targetMessage));
            
            String senderMessage = Utils.getMessageWithPrefix(plugin, "vanish.disabled-for-other", "&cYou made &e{player} &cvisible&c!")
                    .replace("{player}", target.getName());
            sender.sendMessage(Utils.colorize(senderMessage));
        } else {
            // Hide player
            vanishedPlayers.add(targetUUID);
            hidePlayer(target);
            String targetMessage = Utils.getMessageWithPrefix(plugin, "vanish.enabled-by-other", "&aYou have been made &ainvisible &aby &e{sender}&a!")
                    .replace("{sender}", sender.getName());
            target.sendMessage(Utils.colorize(targetMessage));
            
            String senderMessage = Utils.getMessageWithPrefix(plugin, "vanish.enabled-for-other", "&aYou made &e{player} &ainvisible&a!")
                    .replace("{player}", target.getName());
            sender.sendMessage(Utils.colorize(senderMessage));
        }
    }
    
    private void hidePlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getPermissionChecker().has(onlinePlayer, "echocore.vanish.see") && !onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
    }
    
    private void showPlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }
    
    public boolean isVanished(UUID playerUUID) {
        return vanishedPlayers.contains(playerUUID);
    }
    
    public void handlePlayerJoin(Player player) {
        // Hide vanished players from the new player
        for (UUID vanishedUUID : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUUID);
            if (vanishedPlayer != null && !plugin.getPermissionChecker().has(player, "echocore.vanish.see")) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names for the second argument
            if (plugin.getPermissionChecker().has(sender, "echocore.vanish.others")) {
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
