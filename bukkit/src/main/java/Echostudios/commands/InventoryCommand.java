package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.GuiManager;
import Echostudios.utils.Utils;
import Echostudios.utils.ConsentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryCommand implements CommandExecutor, TabCompleter {
    
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
        boolean isStaff = plugin.getPermissionChecker().has(player, "echocore.staff.inventory");
        
        if (args.length == 0) {
            // Own inventory: open read-only GUI for regular users; staff can edit via live inv if they wish target=self
            if (isStaff) {
                guiManager.openEditableTargetInventory(player, player);
            } else if (plugin.getPermissionChecker().has(player, "echocore.chat.inventory")) {
                guiManager.openInventoryGui(player, player);
            } else {
                String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                player.sendMessage(Utils.colorize(noPermMessage));
            }
            return true;
        }
        
        if (args.length == 1) {
            String subCommandOrName = args[0].toLowerCase();
            
            if (subCommandOrName.equals("enderchest") || subCommandOrName.equals("ec")) {
                if (plugin.getPermissionChecker().has(player, "echocore.chat.enderchest")) {
                    guiManager.openEnderchestGui(player, player);
                } else {
                    String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                    player.sendMessage(Utils.colorize(noPermMessage));
                }
                return true;
            }
            
            if (subCommandOrName.equals("inventory") || subCommandOrName.equals("inv")) {
                if (isStaff) {
                    guiManager.openEditableTargetInventory(player, player);
                } else if (plugin.getPermissionChecker().has(player, "echocore.chat.inventory")) {
                    guiManager.openInventoryGui(player, player);
                } else {
                    String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                    player.sendMessage(Utils.colorize(noPermMessage));
                }
                return true;
            }
            
            // Treat as target player name
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (isStaff) {
                    // Staff: editable live inventory, no consent needed
                    guiManager.openEditableTargetInventory(player, target);
                } else if (player.hasPermission("echocore.chat.inventory")) {
                    // Regular user: require consent and open read-only snapshot GUI
                    if (ConsentManager.hasInventoryConsent(target.getUniqueId())) {
                        guiManager.openInventoryGui(player, target);
                    } else {
                        player.sendMessage(Utils.colorize(Utils.getMessageWithPrefix(plugin, "chat.gui.no-consent", "&cIl giocatore non ha dato il consenso recente per mostrare l'inventario (usa [inv]).")));
                    }
                } else {
                    String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                    player.sendMessage(Utils.colorize(noPermMessage));
                }
            } else {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", args[0]));
            }
            return true;
        }
        
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                        .replace("{player}", targetName));
                return true;
            }
            
            switch (sub) {
                case "enderchest":
                case "ec":
                    if (plugin.getPermissionChecker().has(player, "echocore.chat.enderchest")) {
                        // Consent for enderchest (regular users);
                        if (isStaff || ConsentManager.hasEnderchestConsent(target.getUniqueId())) {
                            guiManager.openEnderchestGui(player, target);
                        } else {
                            player.sendMessage(Utils.colorize(Utils.getMessageWithPrefix(plugin, "chat.gui.no-consent", "&cIl giocatore non ha dato il consenso recente per mostrare l'enderchest (usa [ec]).")));
                        }
                    } else {
                        String noPermMessage = Utils.getMessageWithPrefix(plugin, "chat.gui.no-permission", "&cNo Permission");
                        player.sendMessage(Utils.colorize(noPermMessage));
                    }
                    break;
                
                case "inventory":
                case "inv":
                    if (isStaff) {
                        guiManager.openEditableTargetInventory(player, target);
                    } else if (plugin.getPermissionChecker().has(player, "echocore.chat.inventory")) {
                        if (ConsentManager.hasInventoryConsent(target.getUniqueId())) {
                            guiManager.openInventoryGui(player, target);
                        } else {
                            player.sendMessage(Utils.colorize(Utils.getMessageWithPrefix(plugin, "chat.gui.no-consent", "&cIl giocatore non ha dato il consenso recente per mostrare l'inventario (usa [inv]).")));
                        }
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete subcommands
            List<String> subCommands = Arrays.asList("enderchest", "ec", "inventory", "inv");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
            
            // Also complete player names
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            completions.addAll(playerNames);
            
        } else if (args.length == 2) {
            // Complete player names for the second argument
            String input = args[1].toLowerCase();
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            completions.addAll(playerNames);
        }
        
        return completions;
    }
}
