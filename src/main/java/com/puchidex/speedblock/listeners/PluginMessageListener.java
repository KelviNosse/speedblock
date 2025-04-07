package com.puchidex.speedblock.listeners;

import com.puchidex.speedblock.SpeedBlock;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PluginMessageListener {
    private static final ChannelIdentifier COMMAND_CHANNEL = MinecraftChannelIdentifier.from("speedblock:commands");
    private static final ChannelIdentifier RESPONSE_CHANNEL = MinecraftChannelIdentifier.from("speedblock:responses");
    
    private final SpeedBlock plugin;
    
    public PluginMessageListener(SpeedBlock plugin) {
        this.plugin = plugin;
    }
    
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(COMMAND_CHANNEL)) {
            return;
        }
        
        // Only handle messages from backend servers
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        
        ServerConnection connection = (ServerConnection) event.getSource();
        Player player = connection.getPlayer();
        
        // Convert the message bytes to a command string
        String command = new String(event.getData(), StandardCharsets.UTF_8);
        plugin.getLogger().info("Received command from backend server: " + command);
        
        // Execute the command as if it was typed by the console
        String[] commandArgs = command.split(" ");
        
        // Create CompletableFuture for the command result
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        
        // Capture command output
        resultFuture.thenAccept(result -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF(result);
                
                // Send the response back to the player
                player.getCurrentServer().ifPresent(server -> 
                    server.sendPluginMessage(RESPONSE_CHANNEL, stream.toByteArray())
                );
            } catch (IOException e) {
                plugin.getLogger().error("Failed to send response to backend server", e);
            }
        });
        
        // Process the command
        executeCommand(commandArgs, resultFuture);
        
        // Prevent the message from being forwarded
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }
    
    private void executeCommand(String[] args, CompletableFuture<String> resultFuture) {
        if (args.length == 0) {
            resultFuture.complete("§cInvalid command format");
            return;
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        
        try {
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        resultBuilder.append("§cUsage: speedblock create <backendServerName>");
                        break;
                    }
                    
                    String serverName = args[1];
                    if (plugin.getWhitelistManager().createWhitelist(serverName)) {
                        resultBuilder.append(plugin.getConfigFactory().formatMessage("whitelist-created", "server", serverName));
                    } else {
                        resultBuilder.append(plugin.getConfigFactory().formatMessage("server-not-found", "server", serverName));
                    }
                    break;
                    
                case "reload":
                    plugin.reload();
                    resultBuilder.append(plugin.getConfigFactory().formatMessage("config-reloaded"));
                    break;
                    
                default:
                    if (args.length < 2) {
                        resultBuilder.append("§cInvalid command format");
                        break;
                    }
                    
                    serverName = args[0];
                    if (!plugin.getConfigFactory().getServerList().contains(serverName)) {
                        resultBuilder.append(plugin.getConfigFactory().formatMessage("server-not-found", "server", serverName));
                        break;
                    }
                    
                    String action = args[1].toLowerCase();
                    switch (action) {
                        case "add":
                            if (args.length < 3) {
                                resultBuilder.append("§cUsage: speedblock <serverName> add <playerName>");
                                break;
                            }
                            
                            String playerName = args[2];
                            handleAddPlayer(serverName, playerName, resultBuilder);
                            break;
                            
                        case "remove":
                            if (args.length < 3) {
                                resultBuilder.append("§cUsage: speedblock <serverName> remove <playerName>");
                                break;
                            }
                            
                            playerName = args[2];
                            if (plugin.getWhitelistManager().removePlayer(serverName, playerName)) {
                                resultBuilder.append(plugin.getConfigFactory().formatMessage("player-removed", "player", playerName, "server", serverName));
                            } else {
                                resultBuilder.append(plugin.getConfigFactory().formatMessage("player-not-found", "player", playerName));
                            }
                            break;
                            
                        case "clear":
                            plugin.getWhitelistManager().clearWhitelist(serverName);
                            resultBuilder.append(plugin.getConfigFactory().formatMessage("whitelist-cleared", "server", serverName));
                            break;
                            
                        default:
                            resultBuilder.append("§cInvalid command. Available commands: add, remove, clear");
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().error("Error executing command", e);
            resultBuilder.append("§cAn error occurred while executing the command: ").append(e.getMessage());
        }
        
        resultFuture.complete(resultBuilder.toString());
    }
    
    private void handleAddPlayer(String serverName, String playerName, StringBuilder resultBuilder) {
        Optional<Player> optionalPlayer = plugin.getServer().getPlayer(playerName);
        
        if (optionalPlayer.isEmpty()) {
            // If player is not online, use a random UUID
            java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
            if (plugin.getWhitelistManager().addPlayer(serverName, playerName, uuid)) {
                resultBuilder.append(plugin.getConfigFactory().formatMessage("player-added", "player", playerName, "server", serverName));
            }
            return;
        }
        
        Player player = optionalPlayer.get();
        if (plugin.getWhitelistManager().addPlayer(serverName, player.getUsername(), player.getUniqueId())) {
            resultBuilder.append(plugin.getConfigFactory().formatMessage("player-added", "player", player.getUsername(), "server", serverName));
        }
    }
}