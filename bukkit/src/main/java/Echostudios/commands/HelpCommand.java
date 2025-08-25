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
import java.util.List;

public class HelpCommand implements CommandExecutor, TabCompleter {
    
    private final EchoCore plugin;
    
    public HelpCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showMainHelp(sender);
        } else if (args.length == 1) {
            showCategoryHelp(sender, args[0]);
        } else {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/help [category]"));
        }
        return true;
    }
    
    private void showMainHelp(CommandSender sender) {
        List<String> helpLines = plugin.getMessagesConfig().getStringList("help.main");
        

        
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&b&l                    EchoCore Help System                    "));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage("");
        
        for (String line : helpLines) {
            sender.sendMessage(Utils.colorize(line));
        }
        
        sender.sendMessage("");
        sender.sendMessage(Utils.colorize("&7Use &b/help <category> &7for detailed information"));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
    }
    
    private void showCategoryHelp(CommandSender sender, String category) {
        String categoryKey = "help.categories." + category.toLowerCase();
        List<String> categoryLines = plugin.getMessagesConfig().getStringList(categoryKey);
        
        if (categoryLines.isEmpty()) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "help.category-not-found", "&cCategory &e{category} &cnot found!")
                    .replace("{category}", category));
            return;
        }
        
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&b&l                    " + category.toUpperCase() + " Commands                    "));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage("");
        
        for (String line : categoryLines) {
            sender.sendMessage(Utils.colorize(line));
        }
        
        sender.sendMessage("");
        sender.sendMessage(Utils.colorize("&7Use &b/help &7to return to main menu"));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Add all available categories
            completions.add("gamemode");
            completions.add("teleport");
            completions.add("fly");
            completions.add("vanish");
            completions.add("moderation");
            completions.add("staff");
            completions.add("chat");
            completions.add("inventory");
            
            // Filter based on input
            String input = args[0].toLowerCase();
            completions.removeIf(category -> !category.toLowerCase().startsWith(input));
        }
        
        return completions;
    }
}
