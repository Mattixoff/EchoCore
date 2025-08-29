package Echostudios.utils;

import Echostudios.EchoCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;

public class PermissionSyncManager {

    private final EchoCore plugin;
    private final PermissionsManager permissionsManager;
    private final Map<java.util.UUID, PermissionAttachment> attachments = new HashMap<>();

    public PermissionSyncManager(EchoCore plugin, PermissionsManager permissionsManager) {
        this.plugin = plugin;
        this.permissionsManager = permissionsManager;
    }

    public void syncAllOnline() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            syncPlayer(p);
        }
    }

    public void syncPlayer(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) syncPlayer(p);
    }

    public void syncPlayer(Player player) {
        // Only if mode is echoperms or hybrid we try to reflect into Bukkit layer
        String mode = plugin.getConfig().getString("runs.permissions.mode", "hybrid").toLowerCase();
        if (!(mode.equals("echoperms") || mode.equals("hybrid"))) return;

        PermissionAttachment attachment = attachments.computeIfAbsent(player.getUniqueId(),
            id -> player.addAttachment(plugin));

        // Clear previous grants on this attachment
        attachment.getPermissions().keySet().forEach(node -> attachment.unsetPermission(node));

        // Iterate known permissions and set according to EchoPerms resolution
        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            String node = perm.getName();
            boolean allow = permissionsManager.hasPermission(player.getName(), node);
            // Only force-allow true; do not force-deny. Let other providers (OP/LuckPerms) decide when false.
            if (allow) {
                attachment.setPermission(node, true);
            } else {
                if (attachment.getPermissions().containsKey(node)) {
                    attachment.unsetPermission(node);
                }
            }
        }
        player.recalculatePermissions();
    }
}


