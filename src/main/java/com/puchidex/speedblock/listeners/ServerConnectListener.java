package com.puchidex.speedblock.listeners;

import com.puchidex.speedblock.SpeedBlock;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Optional;

public class ServerConnectListener {
    private final SpeedBlock plugin;
    
    public ServerConnectListener(SpeedBlock plugin) {
        this.plugin = plugin;
    }
    
    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        Optional<RegisteredServer> targetServer = event.getResult().getServer();
        
        if (targetServer.isEmpty()) {
            return;
        }
        
        String serverName = targetServer.get().getServerInfo().getName();
        List<String> configuredServers = plugin.getConfigFactory().getServerList();
        
        if (!configuredServers.contains(serverName)) {
            return;
        }
        
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        
        if (!plugin.getWhitelistManager().isWhitelisted(serverName, playerName, player.getUniqueId())) {
            String kickMessage = plugin.getConfigFactory().getMessage("not-whitelisted");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            
            Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    plugin.getConfigFactory().getMessage("prefix") + kickMessage
            );
            
            player.sendMessage(message);
        }
    }
}