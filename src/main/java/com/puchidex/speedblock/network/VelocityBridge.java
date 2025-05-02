package com.puchidex.speedblock.network;

import com.puchidex.speedblock.SpeedBlock;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VelocityBridge implements PlatformBridge {
    private static final ChannelIdentifier COMMAND_CHANNEL = MinecraftChannelIdentifier.from("speedblock:commands");
    private static final ChannelIdentifier RESPONSE_CHANNEL = MinecraftChannelIdentifier.from("speedblock:responses");
    
    private final SpeedBlock plugin;
    private final ProxyServer server;
    private final Logger logger;
    
    public VelocityBridge(Object plugin) {
        this.plugin = (SpeedBlock) plugin;
        this.server = this.plugin.getServer();
        this.logger = this.plugin.getLogger();
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void sendCommand(String command, Consumer<String> responseHandler) {
        String[] args = command.split(" ");
        
        try {
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        responseHandler.accept("§cUsage: speedblock create <backendServerName>");
                        return;
                    }
                    
                    String serverName = args[1];
                    if (plugin.getWhitelistManager().createWhitelist(serverName)) {
                        responseHandler.accept(plugin.getConfigFactory().formatMessage("whitelist-created", "server", serverName));
                    } else {
                        responseHandler.accept(plugin.getConfigFactory().formatMessage("server-not-found", "server", serverName));
                    }
                    break;
                    
                case "reload":
                    plugin.reload();
                    responseHandler.accept(plugin.getConfigFactory().formatMessage("config-reloaded"));
                    break;
                
                default:
                    if (args.length < 2) {
                        responseHandler.accept("§cInvalid command format");
                        return;
                    }
                    
                    serverName = args[0];
                    if (!plugin.getConfigFactory().getServerList().contains(serverName)) {
                        responseHandler.accept(plugin.getConfigFactory().formatMessage("server-not-found", "server", serverName));
                        return;
                    }
                    
                    String action = args[1].toLowerCase();
                    switch (action) {
                        case "add":
                            if (args.length < 3) {
                                responseHandler.accept("§cUsage: speedblock <serverName> add <playerName>");
                                return;
                            }
                            
                            String playerName = args[2];
                            handleAddPlayer(serverName, playerName, responseHandler);
                            break;
                            
                        case "remove":
                            if (args.length < 3) {
                                responseHandler.accept("§cUsage: speedblock <serverName> remove <playerName>");
                                return;
                            }
                            
                            playerName = args[2];
                            if (plugin.getWhitelistManager().removePlayer(serverName, playerName)) {
                                responseHandler.accept(plugin.getConfigFactory().formatMessage("player-removed", "player", playerName, "server", serverName));
                            } else {
                                responseHandler.accept(plugin.getConfigFactory().formatMessage("player-not-found", "player", playerName));
                            }
                            break;
                            
                        case "clear":
                            plugin.getWhitelistManager().clearWhitelist(serverName);
                            responseHandler.accept(plugin.getConfigFactory().formatMessage("whitelist-cleared", "server", serverName));
                            break;
                            
                        default:
                            responseHandler.accept("§cInvalid command. Available commands: add, remove, clear");
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Error executing command", e);
            responseHandler.accept("§cAn error occurred while executing the command: " + e.getMessage());
        }
    }
    
    private void handleAddPlayer(String serverName, String playerName, Consumer<String> responseHandler) {
        Optional<Player> optionalPlayer = server.getPlayer(playerName);
        
        if (optionalPlayer.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
            if (plugin.getWhitelistManager().addPlayer(serverName, playerName, uuid)) {
                responseHandler.accept(plugin.getConfigFactory().formatMessage("player-added", "player", playerName, "server", serverName));
            }
            return;
        }
        
        Player player = optionalPlayer.get();
        if (plugin.getWhitelistManager().addPlayer(serverName, player.getUsername(), player.getUniqueId())) {
            responseHandler.accept(plugin.getConfigFactory().formatMessage("player-added", "player", player.getUsername(), "server", serverName));
        }
    }
    
    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        Optional<Player> player = server.getPlayer(uuid);
        return player.map(p -> p.hasPermission(permission)).orElse(false);
    }
    
    @Override
    public String getServerName() {
        return "velocity-proxy";
    }
    
    @Override
    public void logInfo(String message) {
        logger.info(message);
    }
    
    @Override
    public void logError(String message, Throwable error) {
        logger.error(message, error);
    }
}