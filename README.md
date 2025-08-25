# EchoCore - Advanced Multi-Platform Minecraft Core System

[![Version](https://img.shields.io/badge/version-2.0.0--SNAPSHOT-blue.svg)](https://github.com/EchoStudios/EchoCore)
[![Java](https://img.shields.io/badge/java-21+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/maven-3.8+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> **EchoCore** is a sophisticated, high-performance Minecraft core system designed to replace the majority of plugins while providing advanced features across multiple platforms including Bukkit, BungeeCord, and Velocity.

## 🌟 Features

### 🎮 **Core Functionality**
- **Multi-Platform Support**: Runs on Bukkit, BungeeCord, and Velocity
- **Advanced Chat System**: Intelligent tagging system with [inv] and item previews
- **Player Management**: Comprehensive player data, permissions, and statistics
- **Cross-Server Communication**: Seamless integration between servers and proxies
- **Performance Optimized**: Minimal resource usage with maximum efficiency

### 💬 **Advanced Chat System**
- **[inv] Tag**: Display inventory contents with colored item names and quantities
- **Item Tags**: Show item lore on hover without opening GUIs
- **Smart Filtering**: Content moderation and spam protection
- **Chat History**: Persistent message storage and search functionality
- **Custom Formats**: Rank-based chat formatting with emoji support

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
- **Bukkit/Spigot**: 1.21+
- **Paper**: 1.21+
- **BungeeCord**: Latest version
- **Velocity**: 3.3.0+

## 🚀 Quick Start

### **1. Clone the Repository**
```bash
git clone https://github.com/EchoStudios/EchoCore.git
cd EchoCore
```

### **2. Build the Project**
```bash
mvn clean install
```

### **3. Install on Your Server**
- **Bukkit**: Place `bukkit/target/EchoCore-Bukkit-*.jar` in your plugins folder
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

Performance:
  async-operations: true
  cache-size: 1000
  connection-timeout: 30000
```

### **Chat Tags Configuration**
```yaml
ChatTags:
  inventory:
    enabled: true
    show-lore: true
    max-items: 36
    color-scheme: "default"
  
  items:
    enabled: true
    search-partial: true
    max-results: 5
    show-durability: true
```

## 💬 Chat System Usage

### **Inventory Tags**
Use `[inv]` in chat to show your inventory:
```
[inv] Check out my gear!
```
This will display a clickable inventory preview showing:
- Colored item names
- Quantities
- Item lore on hover
- Cross-server compatibility

### **Item Tags**
Use `[itemname]` to show specific items:
```
I found a [Diamond Sword] in the dungeon!
```
This will display the item's lore when hovered, without opening any GUI.

### **Advanced Formatting**
```yaml
ChatFormats:
  default: "&7[&f%player%&7] %message%"
  vip: "&6[&eVIP&6] &f%player% &7» %message%"
  staff: "&c[&4Staff&c] &f%player% &7» %message%"
```

## 🔌 API Usage

### **Basic Integration**
```java
// Get the EchoCore API
EchoCoreAPI api = EchoCore.getAPI();

// Send a message to a player
api.getPlayerAPI().sendMessage(playerUUID, "Hello World!");

// Process a chat message with tags
String processed = api.getChatAPI().processMessage(playerUUID, "Check [inv] my items!");
```

### **Custom Chat Listener**
```java
public class CustomChatListener implements ChatListener {
    @Override
    public void onMessage(ChatMessage message) {
        // Handle chat messages
        if (message.containsTags()) {
            // Process tags
            List<String> tags = message.extractTags();
            // Custom logic
        }
    }
}

// Register the listener
api.getChatAPI().registerListener(new CustomChatListener());
```

## 🧪 Testing

### **Run Tests**
```bash
# Run all tests
mvn test

# Run specific module tests
mvn test -pl common
mvn test -pl bukkit
mvn test -pl bungeecord
mvn test -pl velocity

# Run with coverage
mvn jacoco:report
```

### **Test Coverage**
- **Unit Tests**: 90%+ coverage target
- **Integration Tests**: Cross-platform compatibility
- **Performance Tests**: Load testing and benchmarking
- **Security Tests**: Vulnerability assessment

## 📊 Performance Metrics

### **Benchmarks**
- **Memory Usage**: <50MB per server
- **Response Time**: <100ms for most operations
- **Database Queries**: <10ms average
- **Chat Processing**: <5ms per message

### **Monitoring**
- Real-time performance metrics
- Resource usage tracking
- Error rate monitoring
- Performance alerts

## 🔒 Security Features

- **Data Encryption**: Sensitive data encryption at rest
- **Access Control**: Role-based access control (RBAC)
- **Audit Logging**: Comprehensive activity logging
- **Rate Limiting**: Protection against abuse
- **Input Validation**: Secure input handling

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### **Development Setup**
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

### **Code Style**
- Follow Java coding conventions
- Use meaningful variable names
- Add comprehensive documentation
- Include unit tests for new features

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **SpigotMC** for the excellent Bukkit API
- **BungeeCord** team for proxy functionality
- **Velocity** team for modern proxy features
- **PaperMC** for performance optimizations
- **HikariCP** for connection pooling
- **Netty** for networking capabilities

## 📞 Support

### **Getting Help**
- **Documentation**: [Wiki](https://github.com/EchoStudios/EchoCore/wiki)
- **Issues**: [GitHub Issues](https://github.com/EchoStudios/EchoCore/issues)
- **Discord**: [Join our Discord](https://discord.gg/echostudios)
- **Email**: support@echostudios.com

### **Community**
- **Discord Server**: Active community and support
- **GitHub Discussions**: Feature requests and questions
- **Wiki**: Comprehensive documentation
- **Examples**: Sample implementations and use cases

## 🔄 Version History

### **v2.0.0-SNAPSHOT** (Current)
- Complete rewrite with multi-platform support
- Advanced chat system with tagging
- Common API architecture
- Performance optimizations

### **v1.0.0-beta3** (Previous)
- Basic Bukkit functionality
- Simple chat features
- Basic player management

---

**EchoCore** - Empowering Minecraft servers with advanced, efficient, and feature-rich core functionality.

*Built with ❤️ by the EchoStudios team*
