package Echostudios.utils;

import Echostudios.EchoCore;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RankManager {

    private final EchoCore plugin;

    public RankManager(EchoCore plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<String> resolvePrefix(Player player) {
        String defaultPrefix = plugin.getConfig().getString("tablist.default-prefix", "");

        // LuckPerms prefix
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                LuckPerms lp = Bukkit.getServicesManager().load(LuckPerms.class);
                if (lp != null) {
                    User user = lp.getUserManager().getUser(player.getUniqueId());
                    if (user != null && user.getCachedData().getMetaData().getPrefix() != null) {
                        return CompletableFuture.completedFuture(user.getCachedData().getMetaData().getPrefix());
                    }
                }
            }
        } catch (Throwable ignored) {}

        UUID uuid = player.getUniqueId();
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            return plugin.getDatabaseManager().getPlayerPrefix(uuid).thenApply(prefix -> {
                if (prefix != null && !prefix.isEmpty()) return prefix;
                String rank = plugin.getDatabaseManager().getPlayerRank(uuid).join();
                return getConfiguredGroupPrefix(rank, defaultPrefix);
            });
        }

        // Check YAML permissions groups meta via PermissionsManager if present
        Echostudios.utils.PermissionsManager pm = getPermissionsManager();
        if (pm != null) {
            String group = getLuckPermsPrimaryGroup(player);
            String pfx = pm.getGroupMetaString(group, "prefix", null);
            if (pfx != null) return CompletableFuture.completedFuture(pfx);
        }

        String group = getLuckPermsPrimaryGroup(player);
        return CompletableFuture.completedFuture(getConfiguredGroupPrefix(group, defaultPrefix));
    }

    public CompletableFuture<String> resolveSuffix(Player player) {
        String defaultSuffix = plugin.getConfig().getString("tablist.default-suffix", "");
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                LuckPerms lp = Bukkit.getServicesManager().load(LuckPerms.class);
                if (lp != null) {
                    User user = lp.getUserManager().getUser(player.getUniqueId());
                    if (user != null && user.getCachedData().getMetaData().getSuffix() != null) {
                        return CompletableFuture.completedFuture(user.getCachedData().getMetaData().getSuffix());
                    }
                }
            }
        } catch (Throwable ignored) {}

        UUID uuid = player.getUniqueId();
        if (plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConnected()) {
            return plugin.getDatabaseManager().getPlayerSuffix(uuid).thenApply(suffix -> {
                if (suffix != null && !suffix.isEmpty()) return suffix;
                String rank = plugin.getDatabaseManager().getPlayerRank(uuid).join();
                return getConfiguredGroupSuffix(rank, defaultSuffix);
            });
        }

        Echostudios.utils.PermissionsManager pm = getPermissionsManager();
        if (pm != null) {
            String group = getLuckPermsPrimaryGroup(player);
            String sfx = pm.getGroupMetaString(group, "suffix", null);
            if (sfx != null) return CompletableFuture.completedFuture(sfx);
        }

        String group = getLuckPermsPrimaryGroup(player);
        return CompletableFuture.completedFuture(getConfiguredGroupSuffix(group, defaultSuffix));
    }

    public CompletableFuture<String> formatDisplayName(Player player) {
        String format = plugin.getConfig().getString("tablist.format", "{prefix}{player}{suffix}");
        return resolvePrefix(player).thenCombine(resolveSuffix(player), (pfx, sfx) -> {
            String formatted = format
                .replace("{prefix}", pfx == null ? "" : pfx)
                .replace("{player}", player.getName())
                .replace("{suffix}", sfx == null ? "" : sfx);
            formatted = Utils.applyPlaceholders(player, formatted);
            return Utils.colorize(formatted);
        });
    }

    private String getLuckPermsPrimaryGroup(Player player) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                LuckPerms lp = Bukkit.getServicesManager().load(LuckPerms.class);
                if (lp != null) {
                    User user = lp.getUserManager().getUser(player.getUniqueId());
                    if (user != null && user.getPrimaryGroup() != null) return user.getPrimaryGroup();
                }
            }
        } catch (Throwable ignored) {}
        return plugin.getConfig().getString("ranks.default", "default");
    }

    private String getConfiguredGroupPrefix(String group, String fallback) {
        if (group == null || group.isEmpty()) return fallback;
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("ranks.groups");
        if (groups != null && groups.isConfigurationSection(group)) {
            return groups.getConfigurationSection(group).getString("prefix", fallback);
        }
        return fallback;
    }

    private String getConfiguredGroupSuffix(String group, String fallback) {
        if (group == null || group.isEmpty()) return fallback;
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("ranks.groups");
        if (groups != null && groups.isConfigurationSection(group)) {
            return groups.getConfigurationSection(group).getString("suffix", fallback);
        }
        return fallback;
    }

    private Echostudios.utils.PermissionsManager getPermissionsManager() {
        try {
            java.lang.reflect.Field f = Echostudios.EchoCore.class.getDeclaredField("permissionsManager");
            f.setAccessible(true);
            Object obj = f.get(plugin);
            if (obj instanceof Echostudios.utils.PermissionsManager pm) return pm;
        } catch (Throwable ignored) {}
        return null;
    }
}


