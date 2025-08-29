package Echoproxy;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "echoproxy", name = "EchoProxy", version = "1.0.0", authors = {"Echostudios"})
public class EchoProxy {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public EchoProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        logger.info("EchoProxy starting up (proxy-side EchoCore)");
        loadPermissions();
        discoverEchoCoreServers();
    }

    private void loadPermissions() {
        try {
            Path dir = server.getPluginManager().fromInstance(this).get().getSource().get().getParent();
            Path data = dir.resolve("echoproxy");
            Files.createDirectories(data);
            Path yml = data.resolve("permissions.yml");
            if (!Files.exists(yml)) Files.createFile(yml);
            // Keep as YAML content, but Velocity commonly uses TOML/JSON; we only ensure the file exists here
        } catch (IOException e) {
            logger.error("Failed to prepare permissions.yml", e);
        }
    }

    private void discoverEchoCoreServers() {
        int count = 0;
        for (com.velocitypowered.api.proxy.server.RegisteredServer rs : server.getAllServers()) {
            logger.info("Discovered server entry: {} (assuming EchoCore if installed)", rs.getServerInfo().getName());
            count++;
        }
        logger.info("EchoProxy discovery complete. Servers found: {}", count);
    }
}


