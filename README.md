# EchoCore - Advanced Multi-Platform Minecraft Core System

[![Version](https://img.shields.io/badge/version-2.0.0--SNAPSHOT-blue.svg)](https://github.com/EchoStudios/EchoCore)
[![Java](https://img.shields.io/badge/java-21+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/maven-3.8+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> **EchoCore** is a sophisticated, high-performance Minecraft core system designed to replace the majority of plugins while providing advanced features across multiple platforms including Bukkit, BungeeCord, and Velocity.

## 🌟 Features

### 🎮 **Core Functionality**
- **Multi-Platform Support**: Runs on Bukkit, BungeeCord, and Velocity
- **Advanced Chat System**: Intelligent tagging system with clickable `[inv]` and `[ec]`
- **Player Management**: Comprehensive player data, permissions, and statistics
- **Lightweight Permissions (EchoPerms)**: YAML-backed users/groups, boolean perms, inheritance, basic `/perm` commands
- **Cross-Server Communication**: Seamless integration between servers and proxies
- **Performance Optimized**: Minimal resource usage with maximum efficiency

### 💬 **Advanced Chat System**
- **[inv] Tag (consent-based)**: Sender grants 60s consent when posting `[inv]`; viewers can click to open the sender’s inventory
- **[ec] Tag (consent-based)**: Sender grants 60s consent when posting `[ec]`; viewers can click to open the sender’s enderchest
- **Staff live view**: Staff with `echocore.staff.inventory` can open editable target inventories via `/inv <player>`
- **Item Tags System**: Interactive item display with hover details using `[item]`, `[sword]`, `[armor]`, `[helmet]`, etc.
- **Customizable Chat Format**: Personalize chat appearance with `{player}` and `{message}` placeholders
- **Smart Player Tagging**: Auto-complete player names with `@` prefix in tab completion
- **Smart Filtering**: Content moderation and spam protection (planned)
- **Chat History**: Persistent message storage and search functionality (planned)
- **Custom Formats**: Rank-based chat formatting with emoji support

### 🎯 **Enhanced Commands & Tab Completion**
- **Smart Tab Completion**: All commands feature intelligent tab completion for player names
- **Player Name Auto-complete**: Commands like `/fly`, `/gamemode`, `/tp`, `/vanish` automatically suggest online players
- **Inventory Management**: `/inventory` command with subcommand completion (`enderchest`, `inventory`)
- **Statistics**: `/stats` command with player name completion
- **Permission-based Suggestions**: Tab completion respects player permissions

### 🔧 **Developer Tools**
- **Common API**: Unified interface across all platforms
- **Event System**: Cross-platform event handling and management
- **Scheduler**: Advanced task scheduling and monitoring
- **Metrics**: Performance monitoring and analytics
- **Configuration**: YAML and HOCON support with validation

### 🚀 **Performance Features**
- **Async Operations**: Non-blocking I/O for optimal performance
- **Connection Pooling**: Efficient database connections with HikariCP
- **Caching System**: Smart caching for frequently accessed data
- **Resource Management**: Optimized memory and CPU usage
- **Load Balancing**: Intelligent resource distribution

## 🏗️ Architecture

### **Multi-Module Structure**
```
EchoCore/
├── common/           # Shared APIs and utilities
├── bukkit/           # Bukkit/Spigot/Paper implementation
├── bungeecord/       # BungeeCord proxy implementation
└── velocity/         # Velocity proxy implementation
```

### **Core Components**
- **EchoCoreAPI**: Main interface for all platform implementations
- **PlayerAPI**: Cross-platform player management
- **ChatAPI**: Advanced chat functionality
- **ServerAPI**: Server and proxy management
- **DatabaseAPI**: Data persistence and management
- **EventAPI**: Event handling and management
- **SchedulerAPI**: Task scheduling and execution
- **MessagingAPI**: Cross-platform communication
- **MetricsAPI**: Performance monitoring

## 📋 Requirements

### **System Requirements**
- **Java**: 21 or higher
- **Maven**: 3.8 or higher
- **Memory**: Minimum 512MB RAM per server
- **Storage**: 100MB free space

### **Platform Support**
- **Bukkit/Spigot/Paper**: 1.21 → 1.21.8
- **BungeeCord**: Latest version
- **Velocity**: 3.3.0+

## 🚀 Quick Start

### **1. Clone the Repository**
```bash
git clone https://github.com/EchoStudios/EchoCore.git
cd EchoCore
```

### **2. Build the Project (multi-version script)**
```powershell
# Interactive: choose platforms (bukkit/velocity/bungeecord)
./build-all.ps1

# Non-interactive (examples):
./build-all.ps1 -Platforms bukkit
./build-all.ps1 -Platforms bukkit,velocity -Versions 1.21.1,1.21.8
./build-all.ps1 -Platforms bukkit -MavenExe "C:\\Program Files\\apache-maven-3.9.11\\bin\\mvn.cmd"
```
Output jars: `build/output/<mcVersion>/<platform>/EchoCore-<Platform>-<mcVersion>.jar`

### **3. Install on Your Server**
- **Bukkit**: Place `bukkit/target/EchoCore-Bukkit-*.jar` or script output in your plugins folder
- **BungeeCord**: Place `bungeecord/target/EchoCore-BungeeCord-*.jar` in your plugins folder
- **Velocity**: Place `velocity/target/EchoCore-Velocity-*.jar` in your plugins folder

### **4. Configure**
Copy and modify the configuration files:
- `config.yml` - Main configuration
- `messages.yml` - Message customization
- `database.yml` - Database settings

## 🔧 Configuration

### **Main Configuration**
```yaml
# EchoCore Configuration
Plugin:
  version: "2.0.0-SNAPSHOT"
  debug: false
  language: "en"

Database:
  type: "mysql" # mysql, sqlite
  host: "localhost"
  port: 3306
  database: "echocore"
  username: "user"
  password: "password"
  pool-size: 10

Chat:
  enable-tags: true
  enable-filtering: true
  max-message-length: 256
  cooldown: 1000
  format: "&7<&e{player}&7> &f{message}"  # Customizable chat format
  item-tags:
    enabled: true
    permission: "echocore.chat.itemtags"

Performance:
  async-operations: true
  cache-size: 1000
  connection-timeout: 30000
```

### **Chat Tags Usage**
- Sender posts `[inv]` to grant inventory-view consent for 60s
- Sender posts `[ec]` to grant enderchest-view consent for 60s
- Viewers click the tag in chat to open the sender’s GUI (permissions apply)
- Staff with `echocore.staff.inventory` can always use `/inv <player>` to open a live editable inventory

### **Item Tags System**
Use special tags to display items with interactive hover details:
- `[item]` or `[mainhand]` - Show item in main hand
- `[helmet]`, `[chestplate]`, `[leggings]`, `[boots]` - Show equipped armor
- `[sword]`, `[weapon]`, `[armor]` - Show weapons/armor if present
- `[DIAMOND_SWORD]` - Show specific item type if in inventory
- `[Custom Name]` - Show item with custom display name

### **Dependencies (Bukkit)**
- `spigot-api` / `paper-api` (provided)
- `me.clip:placeholderapi` (provided)
- `net.luckperms:api` (optional, provided)
- `com.google.code.gson:gson` (shaded)
- `com.google.guava:guava` (shaded)

## 🔑 Lightweight Permissions (EchoPerms)

### Features
- **Users and Groups** with YAML storage: `plugins/EchoCore/permissions.yml`
- **Boolean permissions** with wildcard support (e.g., `echocore.*`), explicit deny via `-node`
- **Group inheritance** (e.g., `vip` inherits `default`)
- **Comprehensive `/ec` command system** (EchoPerms) with full LuckPerms-like functionality
- **Pretty audit logging** with colored messages broadcast to staff
- **Advanced tab completion** mirroring LuckPerms layout
- **Meta management** for prefixes, suffixes, weights, and display names

### File Format (permissions.yml)
```yaml
groups:
  default:
    permissions: ["echocore.help"]
    inherits: []
    meta:
      name: "Default"
      weight: 0
      prefix: "&7"
      suffix: ""
  vip:
    permissions: ["echocore.fly", "echocore.chat.colors"]
    inherits: ["default"]
    meta:
      name: "VIP"
      weight: 10
      prefix: "&6[VIP] "
      suffix: ""
users:
  Mattix_off:
    groups: ["vip"]
    permissions: ["-echocore.moderation.*"]
    meta:
      prefix: "&b[Admin] "
      suffix: "&r"
```

### Comprehensive `/ec` Command System

#### **Root Commands**
- `/ec info` - Display EchoCore version information
- `/ec listgroups` - List all available groups
- `/ec creategroup <group> [weight] [displayname]` - Create a new group
- `/ec deletegroup <group>` - Delete a group (removes from all users/inheritances)
- `/ec renamegroup <oldname> <newname>` - Rename a group (updates all references)

#### **User Management**
- `/ec user <name> info` - Display detailed user information
- `/ec user <name> permission set <node> <true|false>` - Set user permission
- `/ec user <name> permission unset <node>` - Remove user permission
- `/ec user <name> parent add <group>` - Add user to group
- `/ec user <name> parent remove <group>` - Remove user from group
- `/ec user <name> meta set <key> <value>` - Set user metadata
- `/ec user <name> meta unset <key>` - Remove user metadata

#### **Group Management**
- `/ec group <group> info` - Display detailed group information
- `/ec group <group> permission set <node> <true|false>` - Set group permission
- `/ec group <group> permission unset <node>` - Remove group permission
- `/ec group <group> parent add <parent>` - Add group inheritance
- `/ec group <group> parent remove <parent>` - Remove group inheritance
- `/ec group <group> meta set <key> <value>` - Set group metadata
- `/ec group <group> meta unset <key>` - Remove group metadata
- `/ec group <group> setweight <weight>` - Set group weight
- `/ec group <group> setdisplayname <name>` - Set group display name

#### **Audit Logging**
All permission changes are automatically logged with beautiful colored messages:
```
[EchoPerms] PlayerName » User Mattix_off perm echocore.fly = true
[EchoPerms] PlayerName » Group vip weight = 15
[EchoPerms] PlayerName » Created group admin (weight=20, name=Administrator)
```

#### **Advanced Tab Completion**
- **Context-aware suggestions** based on command structure
- **User suggestions** from YAML storage + online players
- **Group suggestions** from existing groups
- **Permission suggestions** from known permission nodes
- **Boolean suggestions** (true/false) for permission values
- **Meta key suggestions** for common metadata

### Legacy Commands
The original `/perm` command system is still available for basic operations:
- `/perm user <name> set <permission> <true|false>`
- `/perm group <group> set <permission> <true|false>`
- `/perm user <name> group add <group>`
- `/perm check <name> <permission>`

### API (simple)
Use `PermissionsManager#hasPermission(name, node)` to check in other plugins.

### PlaceholderAPI (Bukkit)
- `%echocore_prefix%` / `%echocore_group_prefix%`
- `%echocore_suffix%` / `%echocore_group_suffix%`
- `%echocore_group_name%`
- `%echocore_group_weight%`

### Modes and compatibility
Configure how permissions are resolved by the core:
```yaml
runs:
  permissions:
    mode: "hybrid"  # bukkit | echoperms | hybrid
```
- **bukkit**: uses only `player.hasPermission(...)` (LuckPerms/others manage everything)
- **echoperms**: uses only EchoPerms YAML
- **hybrid** (default): grants if (Bukkit OR EchoPerms) is true

In `echoperms`/`hybrid` modes the core automatically syncs EchoPerms into Bukkit `PermissionAttachment`:
- on startup (for online players), on player join, and after `/ec` changes
- so third-party plugins using `hasPermission(...)` see up-to-date permissions

## 🧭 Proxy Behavior (EchoProxy)

- On BungeeCord/Velocity, the plugin name is "EchoProxy".
- On startup, EchoProxy scans registered servers and logs discovery entries (future: active handshake with EchoCore servers).
- Permissions YAML is prepared under the proxy data folder for future proxy-side features.

## 💬 Chat System Usage

### **Inventory Tags**
Use `[inv]` in chat to allow others to view your inventory:
```
[inv] Check out my gear!
```
Clicking opens a GUI for viewers. Staff can open a live editable view via `/inv <player>`.

### **Item Tags**
Use special tags to display items with interactive hover details:
```
[item] this is my favorite weapon!
[helmet] my legendary helmet
[DIAMOND_SWORD] the sword I found
```
Each tag shows the item name with hover details including enchantments, durability, and lore.

### **Advanced Formatting**
Customize chat appearance with placeholders:
```yaml
chat:
  format: "&8[&b{player}&8] &7» &f{message}"  # Default format
  format: "&a{player} &7» &e{message}"        # Green player name
  format: "&6{player} &8&l» &f{message}"      # Gold player name with bold arrow
  format: "&7<&e{player}&7> &f{message}"      # Classic format
```
Use `{player}` for player name and `{message}` for message content.

## 🧪 Testing

### **Run Tests**
```bash
mvn test
```

## 📊 Performance Metrics
- **Memory Usage**: <50MB per server
- **Response Time**: <100ms for most operations
- **Chat Processing**: <5ms per message

## 🔒 Security Features
- **Consent system** for inventory/enderchest viewing
- **Access Control**: Role-based permissions
- **Audit Logging**: Planned

## 🔄 Version History

### **v2.0.0-SNAPSHOT** (Current)
- Clickable chat tags with consent
- Staff live inventory editing via `/inv <player>`
- Multi-version build script (1.21 → 1.21.8)
- Shaded Gson/Guava with relocation

### **v1.0.0-beta3** (Previous)
- Basic Bukkit functionality

---

**EchoCore** - Empowering Minecraft servers with advanced, efficient, and feature-rich core functionality.

*Built with ❤️ by the EchoStudios team*
