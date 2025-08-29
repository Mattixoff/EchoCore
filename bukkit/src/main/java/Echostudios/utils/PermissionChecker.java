package Echostudios.utils;

import Echostudios.EchoCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionChecker {

    private final EchoCore plugin;
    private final PermissionsManager perms;
    private final Mode mode;

    public enum Mode { BUKKIT, ECHOPERMS, HYBRID }

    public PermissionChecker(EchoCore plugin, PermissionsManager perms) {
        this.plugin = plugin;
        this.perms = perms;
        String cfg = plugin.getConfig().getString("runs.permissions.mode", "hybrid").toLowerCase();
        this.mode = switch (cfg) {
            case "bukkit" -> Mode.BUKKIT;
            case "echoperms" -> Mode.ECHOPERMS;
            default -> Mode.HYBRID;
        };
    }

    public boolean has(CommandSender sender, String node) {
        if (!(sender instanceof Player)) {
            // Console: allow
            return true;
        }
        Player player = (Player) sender;
        return switch (mode) {
            case BUKKIT -> player.hasPermission(node);
            case ECHOPERMS -> perms != null && perms.hasPermission(player.getName(), node);
            case HYBRID -> player.hasPermission(node) || (perms != null && perms.hasPermission(player.getName(), node));
        };
    }
}


