package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.GuiManager;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StaffInventoryCommand implements CommandExecutor {
    
    private final EchoCore plugin;
    private final GuiManager guiManager;
    
    public StaffInventoryCommand(EchoCore plugin) {
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
        
        if (!plugin.getPermissionChecker().has(player, "echocore.staff.inventory")) {
            String noPermMessage = Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!");
            player.sendMessage(Utils.colorize(noPermMessage));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.usage", "&cUsage: {usage}")
                    .replace("{usage}", "/inv <player>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.player-not-found", "&cPlayer &e{player} &cwas not found!")
                    .replace("{player}", targetName));
            return true;
        }
        
        // Open staff inventory GUI
        guiManager.openStaffInventoryGui(player, target);
        return true;
    }
}
