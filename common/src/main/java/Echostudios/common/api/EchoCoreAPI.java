package Echostudios.common.api;

import Echostudios.common.api.player.PlayerAPI;
import Echostudios.common.api.server.ServerAPI;
import Echostudios.common.api.config.ConfigurationAPI;
import Echostudios.common.api.database.DatabaseAPI;
import Echostudios.common.api.events.EventAPI;
import Echostudios.common.api.scheduler.SchedulerAPI;
import Echostudios.common.api.messaging.MessagingAPI;
import Echostudios.common.api.metrics.MetricsAPI;
import Echostudios.common.api.platform.PlatformType;

import java.util.concurrent.CompletableFuture;

/**
 * Main API interface for EchoCore
 * This interface provides access to all core functionality
 * and is implemented by each platform (Bukkit, BungeeCord, Velocity)
 */
public interface EchoCoreAPI {
    
    /**
     * Get the platform type this implementation is running on
     * @return PlatformType enum value
     */
    PlatformType getPlatformType();
    
    /**
     * Get the version of EchoCore
     * @return Version string
     */
    String getVersion();
    
    /**
     * Get the player API for player-related operations
     * @return PlayerAPI instance
     */
    PlayerAPI getPlayerAPI();
    
    /**
     * Get the server API for server-related operations
     * @return ServerAPI instance
     */
    ServerAPI getServerAPI();
    
    /**
     * Get the configuration API for config management
     * @return ConfigurationAPI instance
     */
    ConfigurationAPI getConfigurationAPI();
    
    /**
     * Get the database API for database operations
     * @return DatabaseAPI instance
     */
    DatabaseAPI getDatabaseAPI();
    
    /**
     * Get the event API for event management
     * @return EventAPI instance
     */
    EventAPI getEventAPI();
    
    /**
     * Get the scheduler API for task scheduling
     * @return SchedulerAPI instance
     */
    SchedulerAPI getSchedulerAPI();
    
    /**
     * Get the messaging API for cross-platform communication
     * @return MessagingAPI instance
     */
    MessagingAPI getMessagingAPI();
    
    /**
     * Get the metrics API for performance monitoring
     * @return MetricsAPI instance
     */
    MetricsAPI getMetricsAPI();
    
    /**
     * Initialize the core system
     * @return CompletableFuture that completes when initialization is done
     */
    CompletableFuture<Void> initialize();
    
    /**
     * Shutdown the core system
     * @return CompletableFuture that completes when shutdown is done
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Check if the core is running
     * @return true if running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Get the logger instance
     * @return Logger instance
     */
    Logger getLogger();
    
    /**
     * Reload the core configuration
     * @return CompletableFuture that completes when reload is done
     */
    CompletableFuture<Void> reload();
}
