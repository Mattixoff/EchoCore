package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    
    private final EchoCore plugin;
    
    public ReloadCommand(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("echocore.reload")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        
        if (args.length > 0) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}").replace("{usage}", "/reload"));
            return true;
        }
        
        try {
            // Salva la configurazione corrente se ci sono modifiche
            plugin.saveDefaultConfig();
            
            // Ricarica la configurazione
            plugin.reloadConfig();
            
            // Usa il metodo pubblico per ricaricare il plugin
            plugin.reloadPlugin();
            
            // Messaggio di successo
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "reload.success", "&aPlugin configuration reloaded successfully!"));
            
            // Log dell'azione
            plugin.getLogger().info("Plugin ricaricato da " + sender.getName());
            
        } catch (Exception e) {
            // Messaggio di errore
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "reload.error", "&cError occurred while reloading the plugin. Check console for details."));
            
            // Log dell'errore
            plugin.getLogger().severe("Errore durante il reload del plugin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Nessun completamento per questo comando
        return new ArrayList<>();
    }
}
