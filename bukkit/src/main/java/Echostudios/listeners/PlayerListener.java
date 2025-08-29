package Echostudios.listeners;

import Echostudios.EchoCore;
import Echostudios.utils.Utils;
import Echostudios.utils.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final EchoCore plugin;

    public PlayerListener(EchoCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfig().getBoolean("tablist.apply-on-join", true)) return;

        RankManager rankManager = new RankManager(plugin);
        rankManager.formatDisplayName(player).thenAccept(formatted ->
            Bukkit.getScheduler().runTask(plugin, () -> player.setPlayerListName(formatted))
        );
    }
}