package Echostudios;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import Echostudios.commands.*;
import Echostudios.events.ChatEvents;
import Echostudios.utils.WebhookManager;
import Echostudios.utils.DatabaseManager;

import java.io.File;
import java.io.IOException;

public class EchoCore extends JavaPlugin {

    private String CURRENT_VERSION = "1.0.0";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    
    // Command instances
    private GameModeCommands gameModeCommands;
    private TeleportCommand teleportCommand;
    private HelpCommand helpCommand;
    private ModerationCommands moderationCommands;
    private FlyCommand flyCommand;
    private VanishCommand vanishCommand;
    private ReloadCommand reloadCommand;
    private StaffChatCommand staffChatCommand;
    private StatsCommand statsCommand;
    private InventoryCommand inventoryCommand;
    private StaffInventoryCommand staffInventoryCommand;
    private GuiCommand guiCommand;
    private PermCommand permCommand;
    private EcCommand ecCommand;
    
    // Event instances
    private ChatEvents chatEvents;
    private Echostudios.listeners.PlayerListener playerListener;
    
    // Utility instances
    private WebhookManager webhookManager;
    private DatabaseManager databaseManager;
    private Echostudios.utils.PermissionsManager permissionsManager;
    private Echostudios.utils.Placeholders placeholders;
    private Echostudios.utils.PermissionChecker permissionChecker;
    private Echostudios.utils.PermissionSyncManager permissionSyncManager;
    
    // Configuration files
    private FileConfiguration messagesConfig;
    private FileConfiguration serverConfig;
    private File messagesFile;
    private File serverFile;

    @Override
    public void onEnable() {
        logColored(ANSI_CYAN + "----------------------------------------------");
        logColored(ANSI_CYAN + "               ECHOCORE | STARTING UP          ");
        logColored(ANSI_CYAN + "----------------------------------------------");
        logColored(ANSI_YELLOW + "     Version: " + CURRENT_VERSION);
        logColored(ANSI_YELLOW + "     State: Loading");
        logColored(ANSI_YELLOW + "----------------------------------------------" + ANSI_RESET);

        // Load all configuration files first
        loadConfigurations();

        // Read version from config
        if (getConfig().contains("Plugin.version")) {
            CURRENT_VERSION = getConfig().getString("Plugin.version", CURRENT_VERSION);
        }

        // Initialize database after configurations are loaded
        logColored(ANSI_YELLOW + "Initializing database...");
        initializeDatabaseManager();
        
        if (databaseManager != null && databaseManager.isConnected()) {
            logColored(ANSI_GREEN + "Database connected successfully!");
        } else {
            logColored(ANSI_YELLOW + "Database connection failed or disabled. Some features may not work.");
        }

        // Check Discord webhook in config
        boolean discordEnabled = false;
        String webhook = "";
        
        try {
            discordEnabled = getConfig().getBoolean("webhook.enabled", false);
            webhook = getConfig().getString("webhook.moderation-url", "");
        } catch (Exception e) {
            getLogger().warning("Could not read webhook configuration: " + e.getMessage());
        }
        
        if (discordEnabled) {
            if (webhook == null || webhook.isEmpty() || webhook.equals("your-webhook-url")) {
                getLogger().warning("Discord Webhook not found or not configured correctly in config.yml!");    
            } else {
                getLogger().log(java.util.logging.Level.INFO, "Discord Webhook found: {0}", webhook);
            }
        } else {
            getLogger().log(java.util.logging.Level.INFO, "Discord integration disabled in config.yml");
        }

        // Check for plugin updates
        UpdateResult updateResult = checkForUpdate(CURRENT_VERSION);
        if (updateResult != null && updateResult.isUpdateAvailable) {
            logColored(ANSI_YELLOW + "=================================");
            logColored(ANSI_YELLOW + "A new update is available!");
            logColored(ANSI_RED + "Current version: " + CURRENT_VERSION);
            logColored(ANSI_GREEN + "New version: " + updateResult.latestVersion);
            logColored(ANSI_CYAN + "Download at: " + updateResult.downloadUrl);
            logColored(ANSI_YELLOW + "=================================" + ANSI_RESET);
        }

        // Initialize managers and utilities
        webhookManager = new WebhookManager(this);

        // Initialize commands
        initializeCommands();

        // Initialize events
        initializeEvents();

        // Register PlaceholderAPI expansion if present
        try {
            if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                placeholders = new Echostudios.utils.Placeholders(this, permissionsManager);
                placeholders.register();
                getLogger().info("Registered EchoCore PlaceholderAPI expansion");
            }
        } catch (Throwable ignored) {}

        logColored(ANSI_CYAN + "----------------------------------------------");
        logColored(ANSI_CYAN + "               ECHOCORE | STARTED UP          ");
        logColored(ANSI_GREEN + "----------------------------------------------");
        logColored(ANSI_YELLOW + "     Version: " + CURRENT_VERSION);
        logColored(ANSI_GREEN + "     Status: ACTIVE ✓");
        logColored(ANSI_YELLOW + "     Author: Echostudios");
        logColored(ANSI_YELLOW + "----------------------------------------------" + ANSI_RESET);
    }

    @Override
    public void onDisable() {
        logColored(ANSI_CYAN + "----------------------------------------------");
        logColored(ANSI_CYAN + "               ECHOCORE | SHUTTING DOWN    ");
        logColored(ANSI_CYAN + "----------------------------------------------");
        logColored(ANSI_YELLOW + "     Version: " + CURRENT_VERSION);
        logColored(ANSI_RED + "     Status: OFFLINE ✗");
        logColored(ANSI_YELLOW + "     Author: Echostudios");
        logColored(ANSI_YELLOW + "----------------------------------------------" + ANSI_RESET);
        
        // Close database connection
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        logColored(ANSI_YELLOW + "Thanks for using EchoCore! See you next time!");
        logColored(ANSI_RED + "----------------------------------------------" + ANSI_RESET);
    }
    
    /**
     * Load all configuration files
     */
    private void loadConfigurations() {
        // Load main config.yml
        saveDefaultConfig();
        reloadConfig();
        
        // Load messages.yml
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                // Try to save the default resource
                saveResource("messages.yml", false);
            } catch (IllegalArgumentException e) {
                // If the resource doesn't exist in the JAR, create a basic one
                getLogger().warning("messages.yml not found in JAR, creating default file...");
                createDefaultMessagesFile();
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Load server.yml
        serverFile = new File(getDataFolder(), "server.yml");
        if (!serverFile.exists()) {
            try {
                // Try to save the default resource
                saveResource("server.yml", false);
            } catch (IllegalArgumentException e) {
                // If the resource doesn't exist in the JAR, create a basic one
                getLogger().warning("server.yml not found in JAR, creating default file...");
                createDefaultServerFile();
            }
        }
        serverConfig = YamlConfiguration.loadConfiguration(serverFile);
        
        getLogger().info("All configuration files loaded successfully!");
    }
    
    /**
     * Create a default messages.yml file if the resource is not found
     */
    private void createDefaultMessagesFile() {
        try {
            messagesFile.getParentFile().mkdirs();
            messagesFile.createNewFile();
            
            // Write default content
            java.io.FileWriter writer = new java.io.FileWriter(messagesFile);
            writer.write("# EchoCore Messages Configuration\n");
            writer.write("# All plugin messages can be customized here\n");
            writer.write("# Use color codes (&a, &c, &e), hex colors (#FF0000), and gradients (<gradient:red:blue:text>)\n");
            writer.write("# Use \\\\n for line breaks\n");
            writer.write("# Use {prefix} to include the global prefix, remove it if you don't want the prefix for that message\n\n");
            writer.write("# Global Prefix Configuration\n");
            writer.write("prefix: \"&8[&bEchoCore&8] &r\"\n\n");
            writer.write("# General Messages\n");
            writer.write("general:\n");
            writer.write("  player-only: \"{prefix}&cThis command can only be used by players!\"\n");
            writer.write("  no-permission: \"{prefix}&cYou don't have permission to use this command!\"\n");
            writer.write("  player-not-found: \"{prefix}&cPlayer &e{player} &cwas not found!\"\n");
            writer.write("  usage: \"{prefix}&cUsage: {usage}\"\n\n");
            writer.write("# Reload Messages\n");
            writer.write("reload:\n");
            writer.write("  success: \"{prefix}&aPlugin configuration reloaded successfully!\"\n");
            writer.write("  error: \"{prefix}&cError occurred while reloading the plugin. Check console for details.\"\n\n");
            writer.write("# Gamemode Messages\n");
            writer.write("gamemode:\n");
            writer.write("  changed: \"{prefix}&aYour gamemode has been changed to &e{gamemode}&a!\"\n");
            writer.write("  changed-others: \"{prefix}&aYou changed &e{player}'s &agamemode to &e{gamemode}&a!\"\n");
            writer.write("  changed-by-other: \"{prefix}&aYour gamemode was changed to &e{gamemode} &aby &e{sender}&a!\"\n\n");
            writer.write("# Teleport Messages\n");
            writer.write("teleport:\n");
            writer.write("  teleported: \"{prefix}&aYou have been teleported to &e{player}&a!\"\n");
            writer.write("  multiple-players-found: \"{prefix}&eMultiple players found starting with &a{input}&e: &f{players}\"\n");
            writer.write("  suggest-more-characters: \"{prefix}&7Try using more characters: &e{suggestion}\"\n\n");
            writer.write("# Fly Messages\n");
            writer.write("fly:\n");
            writer.write("  enabled: \"{prefix}&aFlight mode has been &aenabled&a!\"\n");
            writer.write("  disabled: \"{prefix}&cFlight mode has been &cdisabled&c!\"\n");
            writer.write("  enabled-for-other: \"{prefix}&aFlight mode has been &aenabled &afor &e{player}&a!\"\n");
            writer.write("  disabled-for-other: \"{prefix}&cFlight mode has been &cdisabled &cfor &e{player}&c!\"\n");
            writer.write("  enabled-by-other: \"{prefix}&aFlight mode has been &aenabled &aby &e{sender}&a!\"\n");
            writer.write("  disabled-by-other: \"{prefix}&cFlight mode has been &cdisabled &cby &e{sender}&c!\"\n\n");
            writer.write("# Vanish Messages\n");
            writer.write("vanish:\n");
            writer.write("  enabled: \"{prefix}&aYou are now &avinvisible&a!\"\n");
            writer.write("  disabled: \"{prefix}&cYou are now &cvisible&c!\"\n");
            writer.write("  enabled-for-other: \"{prefix}&aYou made &e{player} &ainvisible&a!\"\n");
            writer.write("  disabled-for-other: \"{prefix}&cYou made &e{player} &cvisible&c!\"\n");
            writer.write("  enabled-by-other: \"{prefix}&aYou have been made &ainvisible &aby &e{sender}&a!\"\n");
            writer.write("  disabled-by-other: \"{prefix}&cYou have been made &cvisible &cby &e{sender}&c!\"\n\n");
            writer.write("# Moderation Messages\n");
            writer.write("moderation:\n");
            writer.write("  default-ban-reason: \"No reason specified\"\n");
            writer.write("  default-kick-reason: \"No reason specified\"\n");
            writer.write("  default-mute-reason: \"No reason specified\"\n");
            writer.write("  ban-message: \"{prefix}&cYou have been banned from this server!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-banned: \"{prefix}&c{player} &chas been banned by &e{sender}&c!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-unbanned: \"{prefix}&a{player} &ahas been unbanned by &e{sender}&a!\"\n");
            writer.write("  kick-message: \"{prefix}&cYou have been kicked from this server!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-kicked: \"{prefix}&c{player} &chas been kicked by &e{sender}&c!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-warned: \"{prefix}&e{player} &chas been warned by &e{sender}&c!\\\\n&cReason: &e{reason}\\\\n&cWarnings: &e{warnings}\"\n");
            writer.write("  player-unwarned: \"{prefix}&aA warning has been removed from &e{player} &aby &e{sender}&a!\\\\n&cRemaining warnings: &e{warnings}\"\n");
            writer.write("  player-not-warned: \"{prefix}&e{player} &chas no warnings to remove!\"\n");
            writer.write("  player-muted: \"{prefix}&c{player} &chas been muted by &e{sender}&c!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-unmuted: \"{prefix}&a{player} &ahas been unmuted by &e{sender}&a!\"\n");
            writer.write("  player-not-muted: \"{prefix}&e{player} &cis not currently muted!\"\n");
            writer.write("  chat-muted: \"{prefix}&cYou are currently muted and cannot send messages!\"\n");
            writer.write("  player-banned: \"{prefix}&c{player} &chas been banned by &e{sender}&c!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-kicked: \"{prefix}&c{player} &chas been kicked by &e{sender}&c!\\\\n&cReason: &e{reason}\"\n");
            writer.write("  player-warned: \"{prefix}&e{player} &chas been warned by &e{sender}&c!\\\\n&cReason: &e{reason}\\\\n&cWarnings: &e{warnings}\"\n");
            writer.write("  player-unwarned: \"{prefix}&aA warning has been removed from &e{player} &aby &e{sender}&a!\\\\n&cRemaining warnings: &e{warnings}\"\n");
            writer.write("  player-not-warned: \"{prefix}&e{player} &chas no warnings to remove!\"\n\n");
            writer.write("# Join/Leave Messages\n");
            writer.write("join-leave:\n");
            writer.write("  player-join: \"&a+ &e{player} &ajoined the server\"\n");
            writer.write("  player-quit: \"&c- &e{player} &cleft the server\"\n\n");
            writer.write("# Help Messages\n");
            writer.write("help:\n");
            writer.write("  category-not-found: \"&cCategory &e{category} &cnot found!\"\n");
            writer.write("  # Main help menu\n");
            writer.write("  main:\n");
            writer.write("    - \"&b&lCommands:\"\n");
            writer.write("    - \"&7- &b/gmc, /gms, /gma, /gmsp &7- Change gamemode\"\n");
            writer.write("    - \"&7- &b/tp <player> &7- Teleport to player\"\n");
            writer.write("    - \"&7- &b/fly [player] &7- Toggle flight mode\"\n");
            writer.write("    - \"&7- &b/vanish [player] &7- Toggle invisibility\"\n");
            writer.write("    - \"&7- &b/ban, /unban, /kick, /warn, /unwarn, /mute, /unmute &7- Moderation\"\n");
            writer.write("    - \"&7- &b/staffchat <message> &7- Send staff-only message\"\n");
            writer.write("    - \"&7- &b/stats [player] &7- View player statistics\"\n");
            writer.write("    - \"&7- &b/inventory [enderchest|inventory] [player] &7- View inventory/enderchest via GUI\"\n");
            writer.write("    - \"&7- &b/inv <player> &7- Staff command to view and manage player inventory\"\n");
            writer.write("    - \"&7- &b/help [category] &7- Show this help\"\n");
            writer.write("    - \"&7- &b/reload &7- Reload plugin configuration\"\n");
            writer.write("  # Command categories\n");
            writer.write("  categories:\n");
            writer.write("    gamemode:\n");
            writer.write("      - \"&b&lGamemode Commands:\"\n");
            writer.write("      - \"&7- &b/gmc &7- Set gamemode to Creative\"\n");
            writer.write("      - \"&7- &b/gms &7- Set gamemode to Survival\"\n");
            writer.write("      - \"&7- &b/gma &7- Set gamemode to Adventure\"\n");
            writer.write("      - \"&7- &b/gmsp &7- Set gamemode to Spectator\"\n");
            writer.write("      - \"&7- &b/gamemode <player> &7- Change other player's gamemode\"\n");
            writer.write("    teleport:\n");
            writer.write("      - \"&b&lTeleport Commands:\"\n");
            writer.write("      - \"&7- &b/tp <player> &7- Teleport to player\"\n");
            writer.write("      - \"&7- &b/tp <partial-name> &7- Teleport using partial player name\"\n");
            writer.write("    fly:\n");
            writer.write("      - \"&b&lFly Commands:\"\n");
            writer.write("      - \"&7- &b/fly &7- Toggle your own flight mode\"\n");
            writer.write("      - \"&7- &b/fly <player> &7- Toggle another player's flight mode\"\n");
            writer.write("    vanish:\n");
            writer.write("      - \"&b&lVanish Commands:\"\n");
            writer.write("      - \"&7- &b/vanish &7- Toggle your own invisibility\"\n");
            writer.write("      - \"&7- &b/vanish <player> &7- Toggle another player's invisibility\"\n");
            writer.write("    moderation:\n");
            writer.write("      - \"&b&lModeration Commands:\"\n");
            writer.write("      - \"&7- &b/ban <player> [duration] [reason] &7- Ban a player\"\n");
            writer.write("      - \"&7- &b/unban <player> &7- Unban a player\"\n");
            writer.write("      - \"&7- &b/kick <player> [reason] &7- Kick a player\"\n");
            writer.write("      - \"&7- &b/warn <player> <reason> &7- Warn a player\"\n");
            writer.write("      - \"&7- &b/unwarn <player> &7- Remove a warning\"\n");
            writer.write("      - \"&7- &b/mute <player> [duration] [reason] &7- Mute a player\"\n");
            writer.write("      - \"&7- &b/unmute <player> &7- Unmute a player\"\n");
            writer.write("    staff:\n");
            writer.write("      - \"&b&lStaff Commands:\"\n");
            writer.write("      - \"&7- &b/staffchat <message> &7- Send staff-only message\"\n");
            writer.write("      - \"&7- &b/stats [player] &7- View detailed player statistics\"\n");
            writer.write("    chat:\n");
            writer.write("      - \"&b&lChat Features:\"\n");
            writer.write("      - \"&7- &b@player &7- Tag a player in chat (becomes &b@player&r)\"\n");
            writer.write("      - \"&7- &bColors &7- Use & colors if you have permission\"\n");
            writer.write("      - \"&7- &bGradients &7- Use <gradient:color1:color2:text> format\"\n");
            writer.write("      - \"&7- &bTab Completion &7- Press TAB to see inventory/enderchest items\"\n");
            writer.write("      - \"&7- &b[enderchest] &7- Open enderchest GUI in chat\"\n");
            writer.write("      - \"&7- &b[inventory] &7- Open inventory GUI in chat\"\n");
            writer.write("    inventory:\n");
            writer.write("      - \"&b&lInventory Management:\"\n");
            writer.write("      - \"&7- &b/inventory [enderchest|inventory] [player] &7- View inventory/enderchest via GUI\"\n");
            writer.write("      - \"&7- &b/inv <player> &7- Staff command to view and manage player inventory\"\n");
            writer.write("      - \"&7- &b[enderchest] &7- Quick access to enderchest GUI in chat\"\n");
            writer.write("      - \"&7- &b[inventory] &7- Quick access to inventory GUI in chat\"\n");
            writer.write("\n");
            writer.write("# Staff Chat Messages\n");
            writer.write("staffchat:\n");
            writer.write("  message: \"{prefix}&8[&cStaffChat&8] &e{sender}&8: &f{message}\"\n");
            writer.write("\n");
            writer.write("# Staff Commands\n");
            writer.write("staff:\n");
            writer.write("  inventory-cleared: \"{prefix}&aYou have cleared &e{player}'s &ainventory!\"\n");
            writer.write("\n");
            writer.write("# Stats Messages\n");
            writer.write("stats:\n");
            writer.write("  error: \"{prefix}&cError retrieving player statistics. Check console for details.\"\n");
            writer.write("  database-not-connected: \"{prefix}&eDatabase not connected. Showing limited statistics.\"\n");
            writer.write("\n");
            writer.write("# Chat System Configuration\n");
            writer.write("chat:\n");
            writer.write("  # Tag System\n");
            writer.write("  tags:\n");
            writer.write("    enabled: true\n");
            writer.write("    format: \"&b@{player}&r\"\n");
            writer.write("    auto-tag-enabled: true\n");
            writer.write("    auto-tag-format: \"&b@{player}&r\"\n");
            writer.write("  # GUI Titles\n");
            writer.write("  gui:\n");
            writer.write("    enderchest-title: \"&8Enderchest of &e{player}\"\n");
            writer.write("    inventory-title: \"&8Inventory of &e{player}\"\n");
            writer.write("    staff-inventory-title: \"&c&lStaff View: &8Inventory of &e{player}\"\n");
            writer.write("    no-permission: \"&cNo Permission\"\n");
            writer.write("    empty: \"&7Empty\"\n");
            writer.write("  # Tab Completion Messages\n");
            writer.write("  tab-completion:\n");
            writer.write("    no-permission: \"&cNo Permission\"\n");
            writer.write("    empty-inventory: \"&7Empty Inventory\"\n");
            writer.write("    empty-enderchest: \"&7Empty Enderchest\"\n");
            writer.close();
            
            getLogger().info("Created default messages.yml file");
        } catch (IOException e) {
            getLogger().severe("Failed to create default messages.yml: " + e.getMessage());
        }
    }
    
    /**
     * Create a default server.yml file if the resource is not found
     */
    private void createDefaultServerFile() {
        try {
            serverFile.getParentFile().mkdirs();
            serverFile.createNewFile();
            
            // Write default content
            java.io.FileWriter writer = new java.io.FileWriter(serverFile);
            writer.write("# EchoCore Server Configuration\n");
            writer.write("# Server-specific settings and preferences\n\n");
            writer.write("# Database Configuration\n");
            writer.write("database:\n");
            writer.write("  type: \"sqlite\" # \"sqlite\" or \"mysql\"\n");
            writer.write("  host: \"localhost\"\n");
            writer.write("  port: 3306\n");
            writer.write("  database: \"echocore\"\n");
            writer.write("  username: \"root\"\n");
            writer.write("  password: \"\"\n");
            writer.write("  table-prefix: \"ec_\"\n\n");
            writer.write("  # Connection Settings\n");
            writer.write("  connection:\n");
            writer.write("    timeout: 30 # seconds\n");
            writer.write("    max-connections: 10\n");
            writer.write("    auto-reconnect: true\n");
            writer.write("    use-ssl: false\n\n");
            writer.write("# Discord Webhook URLs\n");
            writer.write("webhook:\n");
            writer.write("  enabled: true\n");
            writer.write("  moderation-url: \"your-moderation-webhook-url-here\"\n");
            writer.write("  join-leave-url: \"your-join-leave-webhook-url-here\"\n\n");
            writer.write("  # Webhook Settings\n");
            writer.write("  settings:\n");
            writer.write("    timeout: 10 # seconds\n");
            writer.write("    retry-attempts: 3\n");
            writer.write("    retry-delay: 5 # seconds\n\n");
            writer.write("# Server Preferences\n");
            writer.write("server:\n");
            writer.write("  name: \"Your Server Name\"\n");
            writer.write("  motd: \"Welcome to our server!\"\n");
            writer.write("  max-players: 100\n");
            writer.write("  view-distance: 10\n");
            writer.write("  simulation-distance: 8\n\n");
            writer.write("  spawn:\n");
            writer.write("    world: \"world\"\n");
            writer.write("    x: 0\n");
            writer.write("    y: 64\n");
            writer.write("    z: 0\n");
            writer.write("    yaw: 0\n");
            writer.write("    pitch: 0\n\n");
            writer.write("# Plugin Behavior Settings\n");
            writer.write("behavior:\n");
            writer.write("  update-checker:\n");
            writer.write("    enabled: true\n");
            writer.write("    check-interval: 24 # hours\n");
            writer.write("    notify-ops: true\n");
            writer.write("    auto-download: false\n\n");
            writer.write("  logging:\n");
            writer.write("    enabled: true\n");
            writer.write("    level: \"INFO\" # DEBUG, INFO, WARNING, ERROR\n");
            writer.write("    save-to-file: true\n");
            writer.write("    max-file-size: \"10MB\"\n");
            writer.write("    max-files: 5\n\n");
            writer.write("  performance:\n");
            writer.write("    async-database: true\n");
            writer.write("    cache-size: 1000\n");
            writer.write("    cleanup-interval: 300 # seconds (5 minutes)\n\n");
            writer.write("# Security Settings\n");
            writer.write("security:\n");
            writer.write("  anti-spam:\n");
            writer.write("    enabled: true\n");
            writer.write("    max-messages-per-second: 3\n");
            writer.write("    mute-duration: \"30s\"\n\n");
            writer.write("  cooldowns:\n");
            writer.write("    enabled: true\n");
            writer.write("    teleport: \"3s\"\n");
            writer.write("    gamemode: \"1s\"\n");
            writer.write("    fly: \"2s\"\n");
            writer.write("    vanish: \"1s\"\n\n");
            writer.write("  ip-protection:\n");
            writer.write("    enabled: false\n");
            writer.write("    max-accounts-per-ip: 3\n");
            writer.write("    whitelist: []\n\n");
            writer.write("# Custom Settings\n");
            writer.write("custom:\n");
            writer.write("  welcome:\n");
            writer.write("    enabled: true\n");
            writer.write("    message: \"&aWelcome &e{player} &ato our server!\"\n");
            writer.write("    broadcast: true\n\n");
            writer.write("  goodbye:\n");
            writer.write("    enabled: true\n");
            writer.write("    message: \"&cGoodbye &e{player}&c! We hope to see you again!\"\n");
            writer.write("    broadcast: true\n\n");
            writer.write("  rules:\n");
            writer.write("    - \"&c&lServer Rules:\"\n");
            writer.write("    - \"&7- Be respectful to other players\"\n");
            writer.write("    - \"&7- No griefing or stealing\"\n");
            writer.write("    - \"&7- No inappropriate language\"\n");
            writer.write("    - \"&7- Follow staff instructions\"\n\n");
            writer.write("# Maintenance Mode\n");
            writer.write("maintenance:\n");
            writer.write("  enabled: false\n");
            writer.write("  message: \"&cServer is currently under maintenance. Please try again later.\"\n");
            writer.write("  whitelist: []\n");
            writer.write("  kick-existing: false\n\n");
            writer.write("# Backup Settings\n");
            writer.write("backup:\n");
            writer.write("  enabled: false\n");
            writer.write("  interval: \"6h\" # 6 hours\n");
            writer.write("  keep-files: 7\n");
            writer.write("  compress: true\n");
            writer.write("  path: \"./backups/\"\n");
            writer.close();
            
            getLogger().info("Created default server.yml file");
        } catch (IOException e) {
            getLogger().severe("Failed to create default server.yml: " + e.getMessage());
        }
    }
    
    /**
     * Reload all configuration files
     */
    public void reloadAllConfigurations() {
        reloadConfig();
        
        // Reload messages.yml
        if (messagesFile != null && messagesFile.exists()) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        } else {
            getLogger().warning("messages.yml not found, creating default file...");
            createDefaultMessagesFile();
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
        
        // Reload server.yml
        if (serverFile != null && serverFile.exists()) {
            serverConfig = YamlConfiguration.loadConfiguration(serverFile);
        } else {
            getLogger().warning("server.yml not found, creating default file...");
            createDefaultServerFile();
            serverConfig = YamlConfiguration.loadConfiguration(serverFile);
        }
        
        getLogger().info("All configuration files reloaded successfully!");
    }
    
    /**
     * Save all configuration files
     */
    public void saveAllConfigurations() {
        saveConfig();
        
        try {
            // Save messages.yml if it exists
            if (messagesFile != null && messagesFile.exists() && messagesConfig != null) {
                messagesConfig.save(messagesFile);
            } else {
                getLogger().warning("messages.yml not found, creating default file...");
                createDefaultMessagesFile();
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            }
            
            // Save server.yml if it exists
            if (serverFile != null && serverFile.exists() && serverConfig != null) {
                serverConfig.save(serverFile);
            } else {
                getLogger().warning("server.yml not found, creating default file...");
                createDefaultServerFile();
                serverConfig = YamlConfiguration.loadConfiguration(serverFile);
            }
            
        } catch (IOException e) {
            getLogger().severe("Could not save configuration files: " + e.getMessage());
        }
    }

    private void initializeCommands() {
        // Initialize command instances
        gameModeCommands = new GameModeCommands(this);
        teleportCommand = new TeleportCommand(this);
        helpCommand = new HelpCommand(this);
        moderationCommands = new ModerationCommands(this);
        flyCommand = new FlyCommand(this);
        vanishCommand = new VanishCommand(this);
        reloadCommand = new ReloadCommand(this);
        staffChatCommand = new StaffChatCommand(this);
        statsCommand = new StatsCommand(this);
        inventoryCommand = new InventoryCommand(this);
        staffInventoryCommand = new StaffInventoryCommand(this);
        guiCommand = new GuiCommand(this);
        permissionsManager = new Echostudios.utils.PermissionsManager(this);
        permissionChecker = new Echostudios.utils.PermissionChecker(this, permissionsManager);
        permissionSyncManager = new Echostudios.utils.PermissionSyncManager(this, permissionsManager);
        permCommand = new PermCommand(this, permissionsManager);
        ecCommand = new EcCommand(this, permissionsManager);

        // Register commands
        getCommand("gmc").setExecutor(gameModeCommands);
        getCommand("gms").setExecutor(gameModeCommands);
        getCommand("gma").setExecutor(gameModeCommands);
        getCommand("gmsp").setExecutor(gameModeCommands);
        getCommand("tp").setExecutor(teleportCommand);
        getCommand("help").setExecutor(helpCommand);
        getCommand("ban").setExecutor(moderationCommands);
        getCommand("unban").setExecutor(moderationCommands);
        getCommand("kick").setExecutor(moderationCommands);
        getCommand("warn").setExecutor(moderationCommands);
        getCommand("unwarn").setExecutor(moderationCommands);
        getCommand("mute").setExecutor(moderationCommands);
        getCommand("unmute").setExecutor(moderationCommands);
        getCommand("fly").setExecutor(flyCommand);
        getCommand("vanish").setExecutor(vanishCommand);
        getCommand("reload").setExecutor(reloadCommand);
        getCommand("staffchat").setExecutor(staffChatCommand);
        getCommand("stats").setExecutor(statsCommand);
        getCommand("inventory").setExecutor(inventoryCommand);
        getCommand("inv").setExecutor(staffInventoryCommand);
        getCommand("gui").setExecutor(guiCommand);
        getCommand("gui").setTabCompleter(guiCommand);
        if (getCommand("perm") != null) {
            getCommand("perm").setExecutor(permCommand);
        }
        if (getCommand("ec") != null) {
            getCommand("ec").setExecutor(ecCommand);
            getCommand("ec").setTabCompleter(ecCommand);
        }
        
        // Register tab completers (only for commands that implement TabCompleter)
        getCommand("reload").setTabCompleter(reloadCommand);
        getCommand("help").setTabCompleter(helpCommand);
        
        // Register chat tab completer for chat suggestions
        // Note: Chat tab completion is handled in ChatEvents for better integration
    }

    private void initializeEvents() {
        // Initialize event instances
        chatEvents = new ChatEvents(this);
        playerListener = new Echostudios.listeners.PlayerListener(this);

        // Register events
        getServer().getPluginManager().registerEvents(chatEvents, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        // Sync existing players into Bukkit layer so other plugins see EchoPerms (hybrid/echoperms modes)
        try {
            permissionSyncManager.syncAllOnline();
        } catch (Throwable ignored) {}
    }

    public void logColored(String msg) {
        getServer().getConsoleSender().sendMessage(msg + ANSI_RESET);
    }

    /**
     * Check if an update is available on GitHub
     */
    public UpdateResult checkForUpdate(String currentVersion) {
        java.util.Scanner s = null;
        java.io.InputStream is = null;
        try {
            java.net.URI uri = java.net.URI.create("https://api.github.com/repos/Mattixoff/EchoCore/releases/latest");
            java.net.URL url = uri.toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int status = conn.getResponseCode();
            if (status == 200) {
                is = conn.getInputStream();
                s = new java.util.Scanner(is).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                String latestVersion = extractJsonValue(response, "tag_name");
                String downloadUrl = extractJsonValue(response, "html_url");
                boolean updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);
                return new UpdateResult(updateAvailable, latestVersion, downloadUrl);
            }
        } catch (java.io.IOException e) {
            getLogger().log(java.util.logging.Level.WARNING, "Impossible to check for updates", e);
        } finally {
            if (s != null) {
                s.close();
            }
            if (is != null) {
                try { is.close(); } catch (java.io.IOException ignored) {}
            }
        }
        return null;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private void initializeDatabaseManager() {
        try {
            // Check if we have the necessary configuration files
            if (serverConfig == null && messagesConfig == null) {
                getLogger().warning("Configuration files not loaded yet, skipping database initialization");
                databaseManager = null;
                return;
            }
            
            databaseManager = new DatabaseManager(this);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database manager: " + e.getMessage());
            getLogger().warning("Plugin will continue without database support");
            databaseManager = null;
        }
    }
    
    public void reloadPlugin() {
        try {
            // Reload all configurations first
            reloadAllConfigurations();
            
            // Reinitialize database manager
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
            initializeDatabaseManager();
            
            // Reinitialize webhook manager
            webhookManager = new WebhookManager(this);
            
            getLogger().info("Plugin reloaded successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to reload plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getter methods for commands and events
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public WebhookManager getWebhookManager() {
        return webhookManager;
    }

    public Echostudios.utils.PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public Echostudios.utils.PermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    public Echostudios.utils.PermissionSyncManager getPermissionSyncManager() {
        return permissionSyncManager;
    }
    
    public GameModeCommands getGameModeCommands() {
        return gameModeCommands;
    }
    
    public TeleportCommand getTeleportCommand() {
        return teleportCommand;
    }
    
    public HelpCommand getHelpCommand() {
        return helpCommand;
    }
    
    public ModerationCommands getModerationCommands() {
        return moderationCommands;
    }
    
    public FlyCommand getFlyCommand() {
        return flyCommand;
    }
    
    public VanishCommand getVanishCommand() {
        return vanishCommand;
    }
    
    public ReloadCommand getReloadCommand() {
        return reloadCommand;
    }
    
    public StaffChatCommand getStaffChatCommand() {
        return staffChatCommand;
    }
    
    public StatsCommand getStatsCommand() {
        return statsCommand;
    }
    
    public InventoryCommand getInventoryCommand() {
        return inventoryCommand;
    }
    
    public StaffInventoryCommand getStaffInventoryCommand() {
        return staffInventoryCommand;
    }
    
    public ChatEvents getChatEvents() {
        return chatEvents;
    }
    
    // Configuration getters
    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            getLogger().warning("messages.yml not loaded, attempting to load...");
            if (messagesFile != null && messagesFile.exists()) {
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            } else {
                createDefaultMessagesFile();
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            }
        }
        return messagesConfig;
    }
    
    public FileConfiguration getServerConfig() {
        if (serverConfig == null) {
            getLogger().warning("server.yml not loaded, attempting to load...");
            if (serverFile != null && serverFile.exists()) {
                serverConfig = YamlConfiguration.loadConfiguration(serverFile);
            } else {
                createDefaultServerFile();
                serverConfig = YamlConfiguration.loadConfiguration(serverFile);
            }
        }
        return serverConfig;
    }

    public static class UpdateResult {
        public final boolean isUpdateAvailable;
        public final String latestVersion;
        public final String downloadUrl;
        public UpdateResult(boolean isUpdateAvailable, String latestVersion, String downloadUrl) {
            this.isUpdateAvailable = isUpdateAvailable;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
        }
    }

    // Example of sending update message to player
    public void sendUpdateMessage(org.bukkit.entity.Player player, UpdateResult result) {
        if (result != null && result.isUpdateAvailable) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + """
        ╔════════════════════════════════════╗
        ║         UPDATE AVAILABLE!          ║
        ╠════════════════════════════════════╣
        ║  Current: %s%s
        ║  Latest:  %s%s
        ║  Download: %s%s
        ╚════════════════════════════════════╝""".formatted(
                org.bukkit.ChatColor.RED, CURRENT_VERSION,
                org.bukkit.ChatColor.GREEN, result.latestVersion,
                org.bukkit.ChatColor.AQUA, result.downloadUrl));
        } else {
            player.sendMessage(org.bukkit.ChatColor.GREEN + """
        ╔════════════════════════════════════╗
        ║      ECHOCORE IS UP TO DATE!        ║
        ╠════════════════════════════════════╣
        ║  Current Version: %s               ║
        ╚════════════════════════════════════╝""".formatted(CURRENT_VERSION));
        }
    }
}