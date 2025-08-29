package Echostudios.utils;

import Echostudios.EchoCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private final EchoCore plugin;
    private final PermissionsManager perms;
    private final RankManager rankManager;

    public Placeholders(EchoCore plugin, PermissionsManager perms) {
        this.plugin = plugin;
        this.perms = perms;
        this.rankManager = new RankManager(plugin);
    }

    @Override
    public String getIdentifier() {
        return "echocore";
    }

    @Override
    public String getAuthor() {
        return "Echostudios";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        switch (params.toLowerCase()) {
            case "group_prefix":
            case "prefix":
                return rankManager.resolvePrefix(player).join();
            case "group_suffix":
            case "suffix":
                return rankManager.resolveSuffix(player).join();
            case "group_name": {
                String primary = getPrimaryGroup(player.getName());
                return primary != null ? primary : "default";
            }
            case "group_weight": {
                String primary = getPrimaryGroup(player.getName());
                if (primary == null) return "0";
                return String.valueOf(perms.getGroupWeight(primary));
            }
        }
        return "";
    }

    private String getPrimaryGroup(String name) {
        // best effort: first group with highest weight
        java.util.List<String> groups = plugin.getConfig().getStringList("users." + name.toLowerCase() + ".groups");
        if (groups == null || groups.isEmpty()) return "default";
        groups.sort((a,b) -> Integer.compare(perms.getGroupWeight(b), perms.getGroupWeight(a)));
        return groups.get(0);
    }
}


