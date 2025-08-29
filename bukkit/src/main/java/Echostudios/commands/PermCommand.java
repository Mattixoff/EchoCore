package Echostudios.commands;

import Echostudios.EchoCore;
import Echostudios.utils.PermissionsManager;
import Echostudios.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PermCommand implements CommandExecutor, TabCompleter {

    private final EchoCore plugin;
    private final PermissionsManager perms;

    public PermCommand(EchoCore plugin, PermissionsManager perms) {
        this.plugin = plugin;
        this.perms = perms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("echocore.perm")) {
            sender.sendMessage(Utils.getMessageWithPrefix(plugin, "general.no-permission", "&cNo permission"));
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "user":
                return handleUser(sender, label, args);
            case "group":
                return handleGroup(sender, label, args);
            case "check":
                return handleCheck(sender, args);
            default:
                sendUsage(sender, label);
                return true;
        }
    }

    private boolean handleUser(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, label);
            return true;
        }
        String name = args[1];
        if (args.length >= 5 && args[2].equalsIgnoreCase("set")) {
            String node = args[3];
            String val = args[4];
            boolean value = Boolean.parseBoolean(val);
            perms.setUserPermission(name, node, value);
            try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
            sender.sendMessage(Utils.colorize("&aImpostato &e" + node + " &aa &e" + value + " &aper l'utente &e" + name));
            return true;
        }
        if (args.length >= 5 && args[2].equalsIgnoreCase("group") && args[3].equalsIgnoreCase("add")) {
            String group = args[4];
            perms.addUserGroup(name, group);
            try { plugin.getPermissionSyncManager().syncPlayer(name); } catch (Throwable ignored) {}
            sender.sendMessage(Utils.colorize("&aAggiunto gruppo &e" + group + " &aall'utente &e" + name));
            return true;
        }
        sendUsage(sender, label);
        return true;
    }

    private boolean handleGroup(CommandSender sender, String label, String[] args) {
        if (args.length >= 5 && args[2].equalsIgnoreCase("set")) {
            String group = args[1];
            String node = args[3];
            boolean value = Boolean.parseBoolean(args[4]);
            perms.setGroupPermission(group, node, value);
            try { plugin.getPermissionSyncManager().syncAllOnline(); } catch (Throwable ignored) {}
            sender.sendMessage(Utils.colorize("&aImpostato &e" + node + " &aa &e" + value + " &al per gruppo &e" + group));
            return true;
        }
        sendUsage(sender, label);
        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Utils.colorize("&cUso: /perm check <nome> <permesso>"));
            return true;
        }
        String name = args[1];
        String node = args[2];
        boolean has = perms.hasPermission(name, node);
        sender.sendMessage(Utils.colorize("&b" + name + " &7ha '&e" + node + "&7'? &f" + has));
        Player target = Bukkit.getPlayerExact(name);
        if (target != null) {
            target.sendMessage(Utils.colorize("&7[PermCheck] &7'&e" + node + "&7' = &f" + has));
        }
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Utils.colorize("&bUso:"));
        sender.sendMessage(Utils.colorize("&7/" + label + " user <nome> set <permesso> <true|false>"));
        sender.sendMessage(Utils.colorize("&7/" + label + " group <gruppo> set <permesso> <true|false>"));
        sender.sendMessage(Utils.colorize("&7/" + label + " user <nome> group add <gruppo>"));
        sender.sendMessage(Utils.colorize("&7/" + label + " check <nome> <permesso>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        String input = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";

        if (args.length == 1) {
            out.add("user");
            out.add("group");
            out.add("check");
            return filter(out, input);
        }

        if (args[0].equalsIgnoreCase("user")) {
            if (args.length == 2) {
                out.addAll(perms.getAllUsers());
                out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                return filter(out, input);
            }
            if (args.length == 3) {
                out.add("set");
                out.add("group");
                return filter(out, input);
            }
            if (args.length == 4 && args[2].equalsIgnoreCase("set")) {
                out.addAll(perms.getUserPermissions(args[1]));
                return filter(out, input);
            }
            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                out.add("true");
                out.add("false");
                return filter(out, input);
            }
            if (args.length == 4 && args[2].equalsIgnoreCase("group")) {
                out.add("add");
                return filter(out, input);
            }
            if (args.length == 5 && args[2].equalsIgnoreCase("group") && args[3].equalsIgnoreCase("add")) {
                out.addAll(perms.getAllGroups());
                return filter(out, input);
            }
        }

        if (args[0].equalsIgnoreCase("group")) {
            if (args.length == 2) {
                out.addAll(perms.getAllGroups());
                return filter(out, input);
            }
            if (args.length == 3) {
                out.add("set");
                return filter(out, input);
            }
            if (args.length == 4 && args[2].equalsIgnoreCase("set")) {
                out.addAll(perms.getGroupPermissions(args[1]));
                return filter(out, input);
            }
            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                out.add("true");
                out.add("false");
                return filter(out, input);
            }
        }

        if (args[0].equalsIgnoreCase("check")) {
            if (args.length == 2) {
                out.addAll(perms.getAllUsers());
                out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                return filter(out, input);
            }
        }
        return filter(out, input);
    }

    private List<String> filter(List<String> items, String input) {
        return items.stream().distinct().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input)).collect(Collectors.toList());
    }
}


