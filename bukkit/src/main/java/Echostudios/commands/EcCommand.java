package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.PermissionsManager;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EcCommand implements CommandExecutor, TabCompleter {

    private final EchoCore plugin;
    private final PermissionsManager perms;

    public EcCommand(EchoCore plugin, PermissionsManager perms) {
        this.plugin = plugin;
        this.perms = perms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getPermissionChecker().has(sender, "echocore.perm")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        try {
            switch (sub) {
                case "info":
                    sender.sendMessage(Utils.colorize("&bEchoCore &7v" + plugin.getDescription().getVersion()));
                    return true;
                case "listgroups":
                    sender.sendMessage(Utils.colorize("&bGroups: &f" + String.join(", ", perms.getAllGroups())));
                    return true;
                case "creategroup":
                    if (args.length < 2) return usage(sender, label, "creategroup <group> [weight] [displayname]");
                    String g = args[1];
                    int weight = args.length >= 3 ? Integer.parseInt(args[2]) : 0;
                    String displayName = args.length >= 4 ? args[3] : g;
                    perms.createGroup(g, weight, displayName);
                    broadcastAudit(sender, "Created group &e" + g + "&7 (weight=" + weight + ", name=" + displayName + ")");
                    return true;
                case "deletegroup":
                    if (args.length < 2) return usage(sender, label, "deletegroup <group>");
                    String dg = args[1];
                    if (!perms.groupExists(dg)) {
                        sender.sendMessage(Utils.colorize("&cGroup " + dg + " does not exist!"));
                        return true;
                    }
                    perms.deleteGroup(dg);
                    broadcastAudit(sender, "Deleted group &e" + dg);
                    return true;
                case "renamegroup":
                    if (args.length < 3) return usage(sender, label, "renamegroup <oldname> <newname>");
                    String oldName = args[1];
                    String newName = args[2];
                    if (!perms.groupExists(oldName)) {
                        sender.sendMessage(Utils.colorize("&cGroup " + oldName + " does not exist!"));
                        return true;
                    }
                    if (perms.groupExists(newName)) {
                        sender.sendMessage(Utils.colorize("&cGroup " + newName + " already exists!"));
                        return true;
                    }
                    perms.renameGroup(oldName, newName);
                    broadcastAudit(sender, "Renamed group &e" + oldName + "&7 to &e" + newName);
                    return true;
                case "user":
                    return handleUser(sender, label, args);
                case "group":
                    return handleGroup(sender, label, args);
                default:
                    sendHelp(sender, label);
                    return true;
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.colorize("&cError: " + e.getMessage()));
            return true;
        }
    }

    private boolean handleUser(CommandSender sender, String label, String[] args) {
        if (args.length < 3) return usage(sender, label, "user <name> <info|permission|parent|meta>");
        String name = args[1];
        String sec = args[2].toLowerCase(Locale.ROOT);
        switch (sec) {
            case "info":
                List<String> groups = perms.getUserGroups(name);
                List<String> permissions = perms.getUserPermissions(name);
                sender.sendMessage(Utils.colorize("&8&m                                                                                "));
                sender.sendMessage(Utils.colorize("&b&lUser Info: &e" + name));
                sender.sendMessage(Utils.colorize("&8&m                                                                                "));
                sender.sendMessage(Utils.colorize("&7Groups: &f" + (groups.isEmpty() ? "None" : String.join(", ", groups))));
                sender.sendMessage(Utils.colorize("&7Permissions: &f" + (permissions.isEmpty() ? "None" : String.join(", ", permissions))));
                sender.sendMessage(Utils.colorize("&7Prefix: &f" + perms.getUserPrefix(name)));
                sender.sendMessage(Utils.colorize("&7Suffix: &f" + perms.getUserSuffix(name)));
                return true;
            case "permission":
                if (args.length >= 6 && args[3].equalsIgnoreCase("set")) {
                    String node = args[4]; boolean val = Boolean.parseBoolean(args[5]);
                    perms.setUserPermission(name, node, val);
                    try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
                    broadcastAudit(sender, "User &e" + name + "&7 perm &e" + node + " &7= &e" + val);
                    return true;
                }
                if (args.length >= 5 && args[3].equalsIgnoreCase("unset")) {
                    String node = args[4];
                    perms.unsetUserPermission(name, node);
                    try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
                    broadcastAudit(sender, "User &e" + name + "&7 unset &e" + node);
                    return true;
                }
                return usage(sender, label, "user <name> permission set <node> <true|false> | unset <node>");
            case "parent":
                if (args.length >= 6 && args[3].equalsIgnoreCase("add")) {
                    String group = args[4];
                    if (!perms.groupExists(group)) {
                        sender.sendMessage(Utils.colorize("&cGroup " + group + " does not exist!"));
                        return true;
                    }
                    perms.addUserGroup(name, group);
                    try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
                    broadcastAudit(sender, "User &e" + name + "&7 add parent &e" + group);
                    return true;
                }
                if (args.length >= 6 && args[3].equalsIgnoreCase("remove")) {
                    String group = args[4];
                    perms.removeUserGroup(name, group);
                    try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
                    broadcastAudit(sender, "User &e" + name + "&7 remove parent &e" + group);
                    return true;
                }
                return usage(sender, label, "user <name> parent add <group> | remove <group>");
            case "meta":
                if (args.length >= 6 && args[3].equalsIgnoreCase("set")) {
                    String key = args[4]; String value = args[5];
                    perms.setUserMeta(name, key, value);
                    broadcastAudit(sender, "User &e" + name + "&7 meta &e" + key + "&7 = &e" + value);
                    return true;
                }
                if (args.length >= 5 && args[3].equalsIgnoreCase("unset")) {
                    String key = args[4];
                    perms.unsetUserMeta(name, key);
                    broadcastAudit(sender, "User &e" + name + "&7 unset meta &e" + key);
                    return true;
                }
                return usage(sender, label, "user <name> meta set <key> <value> | unset <key>");
            default:
                return usage(sender, label, "user <name> <info|permission|parent|meta>");
        }
    }

    private boolean handleGroup(CommandSender sender, String label, String[] args) {
        if (args.length < 3) return usage(sender, label, "group <group> <info|permission|parent|meta|setweight|setdisplayname>");
        String group = args[1];
        if (!perms.groupExists(group)) {
            sender.sendMessage(Utils.colorize("&cGroup " + group + " does not exist!"));
            return true;
        }
        String sec = args[2].toLowerCase(Locale.ROOT);
        switch (sec) {
            case "info":
                List<String> inherits = perms.getGroupInheritance(group);
                List<String> permissions = perms.getGroupPermissions(group);
                sender.sendMessage(Utils.colorize("&8&m                                                                                "));
                sender.sendMessage(Utils.colorize("&b&lGroup Info: &e" + group));
                sender.sendMessage(Utils.colorize("&8&m                                                                                "));
                sender.sendMessage(Utils.colorize("&7Display Name: &f" + perms.getGroupDisplayName(group)));
                sender.sendMessage(Utils.colorize("&7Weight: &f" + perms.getGroupWeight(group)));
                sender.sendMessage(Utils.colorize("&7Inherits: &f" + (inherits.isEmpty() ? "None" : String.join(", ", inherits))));
                sender.sendMessage(Utils.colorize("&7Permissions: &f" + (permissions.isEmpty() ? "None" : String.join(", ", permissions))));
                sender.sendMessage(Utils.colorize("&7Prefix: &f" + perms.getGroupPrefix(group)));
                sender.sendMessage(Utils.colorize("&7Suffix: &f" + perms.getGroupSuffix(group)));
                return true;
            case "permission":
                if (args.length >= 6 && args[3].equalsIgnoreCase("set")) {
                    String node = args[4]; boolean val = Boolean.parseBoolean(args[5]);
                    perms.setGroupPermission(group, node, val);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 perm &e" + node + " &7= &e" + val);
                    return true;
                }
                if (args.length >= 5 && args[3].equalsIgnoreCase("unset")) {
                    String node = args[4];
                    perms.unsetGroupPermission(group, node);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 unset &e" + node);
                    return true;
                }
                return usage(sender, label, "group <group> permission set <node> <true|false> | unset <node>");
            case "parent":
                if (args.length >= 6 && args[3].equalsIgnoreCase("add")) {
                    String parent = args[4];
                    if (!perms.groupExists(parent)) {
                        sender.sendMessage(Utils.colorize("&cGroup " + parent + " does not exist!"));
                        return true;
                    }
                    perms.addGroupInheritance(group, parent);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 add parent &e" + parent);
                    return true;
                }
                if (args.length >= 6 && args[3].equalsIgnoreCase("remove")) {
                    String parent = args[4];
                    perms.removeGroupInheritance(group, parent);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 remove parent &e" + parent);
                    return true;
                }
                return usage(sender, label, "group <group> parent add <parent> | remove <parent>");
            case "meta":
                if (args.length >= 6 && args[3].equalsIgnoreCase("set")) {
                    String key = args[4]; String value = args[5];
                    perms.setGroupMeta(group, key, value);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 meta &e" + key + "&7 = &e" + value);
                    return true;
                }
                if (args.length >= 5 && args[3].equalsIgnoreCase("unset")) {
                    String key = args[4];
                    perms.unsetGroupMeta(group, key);
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 unset meta &e" + key);
                    return true;
                }
                return usage(sender, label, "group <group> meta set <key> <value> | unset <key>");
            case "setweight":
                if (args.length >= 4) {
                    perms.setGroupMeta(group, "weight", Integer.parseInt(args[3]));
                    plugin.getPermissionSyncManager().syncAllOnline();
                    broadcastAudit(sender, "Group &e" + group + "&7 weight = &e" + args[3]);
                    return true;
                }
                return usage(sender, label, "group <group> setweight <weight>");
            case "setdisplayname":
                if (args.length >= 4) {
                    perms.setGroupMeta(group, "name", args[3]);
                    broadcastAudit(sender, "Group &e" + group + "&7 displayname = &e" + args[3]);
                    return true;
                }
                return usage(sender, label, "group <group> setdisplayname <name>");
            default:
                return usage(sender, label, "group <group> <info|permission|parent|meta|setweight|setdisplayname>");
        }
    }

    private void broadcastAudit(CommandSender actor, String action) {
        String msg = Utils.colorize("&8[&bEchoPerms&8] &7" + actor.getName() + " &f» " + action);
        Bukkit.getConsoleSender().sendMessage(msg);
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (plugin.getPermissionChecker().has(p, "echocore.perm")) {
                p.sendMessage(msg);
            }
        });
    }

    private boolean usage(CommandSender sender, String label, String usage) {
        sender.sendMessage(Utils.colorize("&cUsage: /" + label + " " + usage));
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&b&lEchoPerms Commands (&7/" + label + "&b)"));
        sender.sendMessage(Utils.colorize("&8&m                                                                                "));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " info"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " listgroups"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " creategroup <group> [weight] [displayname]"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " deletegroup <group>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " renamegroup <oldname> <newname>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " user <name> info"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " user <name> permission set <node> <true|false> | unset <node>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " user <name> parent add <group> | remove <group>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " user <name> meta set <key> <value> | unset <key>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> info"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> permission set <node> <true|false> | unset <node>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> parent add <parent> | remove <parent>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> meta set <key> <value> | unset <key>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> setweight <weight>"));
        sender.sendMessage(Utils.colorize("&7- &b/" + label + " group <group> setdisplayname <name>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        String last = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";
        if (!plugin.getPermissionChecker().has(sender, "echocore.perm")) return out;

        if (args.length == 1) {
            out.add("help"); out.add("info"); out.add("listgroups"); out.add("creategroup"); out.add("deletegroup"); out.add("renamegroup"); out.add("user"); out.add("group");
            return filter(out, last);
        }
        if (args[0].equalsIgnoreCase("user")) {
            if (args.length == 2) {
                out.addAll(perms.getAllUsers());
                out.addAll(Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList()));
                return filter(out, last);
            }
            if (args.length == 3) { out.add("info"); out.add("permission"); out.add("parent"); out.add("meta"); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("permission")) { out.add("set"); out.add("unset"); return filter(out, last);}    
            if (args.length == 5 && args[2].equalsIgnoreCase("permission") && args[3].equalsIgnoreCase("set")) { out.addAll(perms.getKnownPermissions()); return filter(out, last);}    
            if (args.length == 6 && args[2].equalsIgnoreCase("permission") && args[3].equalsIgnoreCase("set")) { out.add("true"); out.add("false"); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("parent")) { out.add("add"); out.add("remove"); return filter(out, last);}    
            if (args.length == 5 && args[2].equalsIgnoreCase("parent") && (args[3].equalsIgnoreCase("add") || args[3].equalsIgnoreCase("remove"))) { out.addAll(perms.getAllGroups()); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("meta")) { out.add("set"); out.add("unset"); return filter(out, last);}    
        }
        if (args[0].equalsIgnoreCase("group")) {
            if (args.length == 2) { out.addAll(perms.getAllGroups()); return filter(out, last);}    
            if (args.length == 3) { out.add("info"); out.add("permission"); out.add("parent"); out.add("meta"); out.add("setweight"); out.add("setdisplayname"); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("permission")) { out.add("set"); out.add("unset"); return filter(out, last);}    
            if (args.length == 5 && args[2].equalsIgnoreCase("permission") && args[3].equalsIgnoreCase("set")) { out.addAll(perms.getKnownPermissions()); return filter(out, last);}    
            if (args.length == 6 && args[2].equalsIgnoreCase("permission") && args[3].equalsIgnoreCase("set")) { out.add("true"); out.add("false"); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("parent")) { out.add("add"); out.add("remove"); return filter(out, last);}    
            if (args.length == 5 && args[2].equalsIgnoreCase("parent") && (args[3].equalsIgnoreCase("add") || args[3].equalsIgnoreCase("remove"))) { out.addAll(perms.getAllGroups()); return filter(out, last);}    
            if (args.length == 4 && args[2].equalsIgnoreCase("meta")) { out.add("set"); out.add("unset"); return filter(out, last);}    
        }
        return filter(out, last);
    }

    private List<String> filter(List<String> items, String last) {
        return items.stream().distinct().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
    }
}
