package Echostudios.utils;

import Echostudios.EchoCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionsManager {

    private final EchoCore plugin;
    private final File file;
    private FileConfiguration config;

    public PermissionsManager(EchoCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "permissions.yml");
        load();
    }

    public void load() {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
                // defaults
                config.set("groups.default.permissions", Collections.emptyList());
                config.set("groups.default.inherits", Collections.emptyList());
                config.set("groups.default.meta.name", "default");
                config.set("groups.default.meta.weight", 0);
                config.set("groups.default.meta.prefix", "&7");
                config.set("groups.default.meta.suffix", "");
                save();
            } else {
                config = YamlConfiguration.loadConfiguration(file);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create permissions.yml: " + e.getMessage());
            config = new YamlConfiguration();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save permissions.yml: " + e.getMessage());
        }
    }

    // Groups
    public void setGroupPermission(String group, String permission, boolean value) {
        String path = "groups." + group + ".permissions";
        List<String> list = new ArrayList<>(config.getStringList(path));
        list.removeIf(p -> p.equalsIgnoreCase(permission) || p.equalsIgnoreCase("-" + permission));
        list.add(value ? permission : ("-" + permission));
        config.set(path, list);
        save();
    }

    public void addGroupInheritance(String group, String parent) {
        String path = "groups." + group + ".inherits";
        List<String> list = new ArrayList<>(config.getStringList(path));
        if (!list.contains(parent)) list.add(parent);
        config.set(path, list);
        save();
    }

    public void setGroupMeta(String group, String key, Object value) {
        config.set("groups." + group + ".meta." + key, value);
        save();
    }

    public String getGroupMetaString(String group, String key, String def) {
        return config.getString("groups." + group + ".meta." + key, def);
    }

    public int getGroupWeight(String group) {
        return config.getInt("groups." + group + ".meta.weight", 0);
    }

    public java.util.Set<String> getAllGroups() {
        ConfigurationSection sec = config.getConfigurationSection("groups");
        return sec != null ? sec.getKeys(false) : java.util.Collections.emptySet();
    }

    public java.util.Set<String> getAllUsers() {
        ConfigurationSection sec = config.getConfigurationSection("users");
        return sec != null ? sec.getKeys(false) : java.util.Collections.emptySet();
    }

    public java.util.List<String> getGroupPermissions(String group) {
        return new ArrayList<>(config.getStringList("groups." + group + ".permissions"));
    }

    public java.util.List<String> getUserPermissions(String user) {
        String base = "users." + user.toLowerCase();
        return new ArrayList<>(config.getStringList(base + ".permissions"));
    }

    public java.util.List<String> getUserGroups(String user) {
        String base = "users." + user.toLowerCase();
        return new ArrayList<>(config.getStringList(base + ".groups"));
    }

    public java.util.List<String> getGroupInheritance(String group) {
        return new ArrayList<>(config.getStringList("groups." + group + ".inherits"));
    }

    // Group management
    public void createGroup(String group, int weight, String displayName) {
        config.set("groups." + group + ".permissions", Collections.emptyList());
        config.set("groups." + group + ".inherits", Collections.emptyList());
        config.set("groups." + group + ".meta.weight", weight);
        config.set("groups." + group + ".meta.name", displayName != null ? displayName : group);
        config.set("groups." + group + ".meta.prefix", "");
        config.set("groups." + group + ".meta.suffix", "");
        save();
    }

    public void deleteGroup(String group) {
        config.set("groups." + group, null);
        // Remove from all users
        for (String user : getAllUsers()) {
            removeUserGroup(user, group);
        }
        // Remove from all group inheritances
        for (String g : getAllGroups()) {
            removeGroupInheritance(g, group);
        }
        save();
    }

    public void renameGroup(String oldName, String newName) {
        if (oldName.equals(newName)) return;
        
        // Copy group data
        ConfigurationSection oldGroup = config.getConfigurationSection("groups." + oldName);
        if (oldGroup != null) {
            config.set("groups." + newName, oldGroup);
            config.set("groups." + oldName, null);
            
            // Update all user references
            for (String user : getAllUsers()) {
                List<String> groups = getUserGroups(user);
                if (groups.contains(oldName)) {
                    groups.remove(oldName);
                    groups.add(newName);
                    config.set("users." + user.toLowerCase() + ".groups", groups);
                }
            }
            
            // Update all group inheritances
            for (String group : getAllGroups()) {
                List<String> inherits = getGroupInheritance(group);
                if (inherits.contains(oldName)) {
                    inherits.remove(oldName);
                    inherits.add(newName);
                    config.set("groups." + group + ".inherits", inherits);
                }
            }
            save();
        }
    }

    public void removeUserGroup(String user, String group) {
        String base = "users." + user.toLowerCase();
        List<String> groups = new ArrayList<>(config.getStringList(base + ".groups"));
        groups.remove(group);
        config.set(base + ".groups", groups);
        save();
    }

    public void removeGroupInheritance(String group, String parent) {
        List<String> inherits = new ArrayList<>(config.getStringList("groups." + group + ".inherits"));
        inherits.remove(parent);
        config.set("groups." + group + ".inherits", inherits);
        save();
    }

    public void unsetUserPermission(String user, String permission) {
        String base = "users." + user.toLowerCase();
        List<String> perms = new ArrayList<>(config.getStringList(base + ".permissions"));
        perms.removeIf(p -> p.equalsIgnoreCase(permission) || p.equalsIgnoreCase("-" + permission));
        config.set(base + ".permissions", perms);
        save();
    }

    public void unsetGroupPermission(String group, String permission) {
        String path = "groups." + group + ".permissions";
        List<String> perms = new ArrayList<>(config.getStringList(path));
        perms.removeIf(p -> p.equalsIgnoreCase(permission) || p.equalsIgnoreCase("-" + permission));
        config.set(path, perms);
        save();
    }

    public void setUserMeta(String user, String key, Object value) {
        config.set("users." + user.toLowerCase() + ".meta." + key, value);
        save();
    }

    public String getUserMetaString(String user, String key, String def) {
        return config.getString("users." + user.toLowerCase() + ".meta." + key, def);
    }

    public void unsetUserMeta(String user, String key) {
        config.set("users." + user.toLowerCase() + ".meta." + key, null);
        save();
    }

    public void unsetGroupMeta(String group, String key) {
        config.set("groups." + group + ".meta." + key, null);
        save();
    }

    public String getGroupDisplayName(String group) {
        return getGroupMetaString(group, "name", group);
    }

    public String getGroupPrefix(String group) {
        return getGroupMetaString(group, "prefix", "");
    }

    public String getGroupSuffix(String group) {
        return getGroupMetaString(group, "suffix", "");
    }

    public String getUserPrefix(String user) {
        return getUserMetaString(user, "prefix", "");
    }

    public String getUserSuffix(String user) {
        return getUserMetaString(user, "suffix", "");
    }

    public void setUserPrefix(String user, String prefix) {
        setUserMeta(user, "prefix", prefix);
    }

    public void setUserSuffix(String user, String suffix) {
        setUserMeta(user, "suffix", suffix);
    }

    public void setGroupPrefix(String group, String prefix) {
        setGroupMeta(group, "prefix", prefix);
    }

    public void setGroupSuffix(String group, String suffix) {
        setGroupMeta(group, "suffix", suffix);
    }

    public void removeUserPrefix(String user) {
        unsetUserMeta(user, "prefix");
    }

    public void removeUserSuffix(String user) {
        unsetUserMeta(user, "suffix");
    }

    public void removeGroupPrefix(String group) {
        unsetGroupMeta(group, "prefix");
    }

    public void removeGroupSuffix(String group) {
        unsetGroupMeta(group, "suffix");
    }

    public boolean groupExists(String group) {
        return getAllGroups().contains(group);
    }

    public boolean userExists(String user) {
        return getAllUsers().contains(user.toLowerCase());
    }

    // Users
    public void setUserPermission(String name, String permission, boolean value) {
        String base = "users." + name.toLowerCase();
        List<String> perms = new ArrayList<>(config.getStringList(base + ".permissions"));
        perms.removeIf(p -> p.equalsIgnoreCase(permission) || p.equalsIgnoreCase("-" + permission));
        perms.add(value ? permission : ("-" + permission));
        config.set(base + ".permissions", perms);
        save();
    }

    public void addUserGroup(String name, String group) {
        String base = "users." + name.toLowerCase();
        List<String> groups = new ArrayList<>(config.getStringList(base + ".groups"));
        if (!groups.contains(group)) groups.add(group);
        config.set(base + ".groups", groups);
        save();
    }

    // Resolution
    public boolean hasPermission(String name, String permission) {
        String node = permission.toLowerCase();
        // direct user perms
        String base = "users." + name.toLowerCase();
        Tri result = evaluateList(config.getStringList(base + ".permissions"), node);
        if (result != Tri.UNSET) return result == Tri.ALLOW;

        // user groups (highest weight first)
        List<String> groups = new ArrayList<>(config.getStringList(base + ".groups"));
        groups.sort((a, b) -> Integer.compare(getGroupWeight(b), getGroupWeight(a)));
        for (String group : groups) {
            Tri g = hasGroupPermission(group, node, new HashSet<>());
            if (g != Tri.UNSET) return g == Tri.ALLOW;
        }

        // default group
        Tri def = hasGroupPermission("default", node, new HashSet<>());
        return def == Tri.ALLOW;
    }

    private Tri hasGroupPermission(String group, String node, Set<String> visited) {
        if (!visited.add(group)) return Tri.UNSET; // prevent cycles
        Tri result = evaluateList(config.getStringList("groups." + group + ".permissions"), node);
        if (result != Tri.UNSET) return result;
        for (String parent : config.getStringList("groups." + group + ".inherits")) {
            Tri parentRes = hasGroupPermission(parent, node, visited);
            if (parentRes != Tri.UNSET) return parentRes;
        }
        return Tri.UNSET;
    }

    private Tri evaluateList(List<String> entries, String node) {
        // last match wins
        Tri current = Tri.UNSET;
        for (String raw : entries) {
            String entry = raw.trim();
            boolean deny = entry.startsWith("-");
            String perm = deny ? entry.substring(1) : entry;
            if (matchesNode(perm.toLowerCase(), node)) {
                current = deny ? Tri.DENY : Tri.ALLOW;
            }
        }
        return current;
    }

    private boolean matchesNode(String pattern, String node) {
        if (pattern.equals("*")) return true;
        if (pattern.equals(node)) return true;
        // wildcard section support: echocore.*
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return node.equals(prefix) || node.startsWith(prefix + ".");
        }
        return false;
    }

    private enum Tri { ALLOW, DENY, UNSET }

    // Simple API exposure
    public boolean has(PlayerAdapter adapter, String permission) {
        return hasPermission(adapter.getName(), permission);
    }

    public interface PlayerAdapter {
        String getName();
    }

    // Utility methods for command completion
    public List<String> getKnownPermissions() {
        Set<String> perms = new HashSet<>();
        // Add common permission nodes
        perms.add("echocore.*");
        perms.add("echocore.perm");
        perms.add("echocore.fly");
        perms.add("echocore.gamemode");
        perms.add("echocore.teleport");
        perms.add("echocore.moderation");
        perms.add("echocore.staff");
        perms.add("echocore.gui");
        perms.add("echocore.stats");
        perms.add("echocore.inventory");
        perms.add("echocore.help");
        perms.add("echocore.reload");
        perms.add("echocore.vanish");
        perms.add("echocore.staffchat");
        perms.add("echocore.staffinventory");
        // Add from existing groups
        for (String group : getAllGroups()) {
            perms.addAll(getGroupPermissions(group));
        }
        // Add from existing users
        for (String user : getAllUsers()) {
            perms.addAll(getUserPermissions(user));
        }
        return new ArrayList<>(perms);
    }
}


