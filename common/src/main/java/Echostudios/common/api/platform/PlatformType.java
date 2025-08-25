package Echostudios.common.api.platform;

/**
 * Enum representing the different platforms EchoCore can run on
 */
public enum PlatformType {
    
    /**
     * Bukkit/Spigot/Paper server platform
     */
    BUKKIT("Bukkit", "bukkit"),
    
    /**
     * BungeeCord proxy platform
     */
    BUNGEECORD("BungeeCord", "bungeecord"),
    
    /**
     * Velocity proxy platform
     */
    VELOCITY("Velocity", "velocity");
    
    private final String displayName;
    private final String identifier;
    
    PlatformType(String displayName, String identifier) {
        this.displayName = displayName;
        this.identifier = identifier;
    }
    
    /**
     * Get the human-readable display name
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the platform identifier
     * @return Platform identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Check if this platform is a proxy (BungeeCord or Velocity)
     * @return true if proxy, false if server
     */
    public boolean isProxy() {
        return this == BUNGEECORD || this == VELOCITY;
    }
    
    /**
     * Check if this platform is a server (Bukkit)
     * @return true if server, false if proxy
     */
    public boolean isServer() {
        return this == BUKKIT;
    }
}
