package Echoproxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class EchoProxy extends Plugin {

    private net.md_5.bungee.config.Configuration permissionsConfig;

    @Override
    public void onEnable() {
        getLogger().info("EchoProxy starting up (proxy-side EchoCore)");
        loadPermissions();
        discoverEchoCoreServers();
    }

    private void loadPermissions() {
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            File file = new File(getDataFolder(), "permissions.yml");
            if (!file.exists()) file.createNewFile();
            net.md_5.bungee.config.ConfigurationProvider provider = net.md_5.bungee.config.ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class);
            permissionsConfig = provider.load(file);
            // defaults
            if (!permissionsConfig.contains("groups")) {
                permissionsConfig.set("groups.default.permissions", new java.util.ArrayList<>());
                permissionsConfig.set("groups.default.inherits", new java.util.ArrayList<>());
                permissionsConfig.set("groups.default.meta.name", "default");
                permissionsConfig.set("groups.default.meta.weight", 0);
                permissionsConfig.set("groups.default.meta.prefix", "&7");
                permissionsConfig.set("groups.default.meta.suffix", "");
                provider.save(permissionsConfig, file);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load permissions.yml", e);
        }
    }

    private void discoverEchoCoreServers() {
        int count = 0;
        for (String name : ProxyServer.getInstance().getServers().keySet()) {
            // Here we just log; a real discovery would use plugin messaging or a ping with brand
            getLogger().info("Discovered server entry: " + name + " (assuming EchoCore if installed)");
            count++;
        }
        getLogger().info("EchoProxy discovery complete. Servers found: " + count);
    }
}


