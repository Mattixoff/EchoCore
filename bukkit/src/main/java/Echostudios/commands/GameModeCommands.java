package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameModeCommands implements CommandExecutor {
    
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
            if (!player.hasPermission("echocore.gamemode." + targetMode.name().toLowerCase())) {
                player.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
            
            player.setGameMode(targetMode);
            String message = Utils.getMessageWithPrefix(plugin, "gamemode.changed", "&aYour gamemode has been changed to &e{gamemode}&a!")
                    .replace("{gamemode}", targetMode.name().toLowerCase());
            player.sendMessage(Utils.colorize(message));
            
        } else if (args.length == 1) {
            if (!sender.hasPermission("echocore.gamemode.others")) {
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
}
