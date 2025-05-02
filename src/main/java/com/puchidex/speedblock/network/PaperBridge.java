package com.puchidex.speedblock.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaperBridge implements PlatformBridge, PluginMessageListener {
    private static final String COMMAND_CHANNEL = "speedblock:commands";
    private static final String RESPONSE_CHANNEL = "speedblock:responses";
    
    private final Plugin plugin;
    private final Logger logger;
    private final String serverName;
    
    private final Map<Integer, Consumer<String>> responseHandlers = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    
    public PaperBridge(Object plugin) {
        this.plugin = (Plugin) plugin;
        this.logger = this.plugin.getLogger();
        this.serverName = Bukkit.getServer().getName();
    }
    
    @Override
    public void initialize() {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, COMMAND_CHANNEL);
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, RESPONSE_CHANNEL, this);
    }
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(RESPONSE_CHANNEL)) {
            return;
        }
        
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            int requestId = in.readInt();
            String response = in.readUTF();
            
            Consumer<String> handler = responseHandlers.remove(requestId);
            if (handler != null) {
                handler.accept(response);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to decode response from proxy", e);
        }
    }
    
    @Override
    public void sendCommand(String command, Consumer<String> responseHandler) {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            responseHandler.accept("§cCannot send command to proxy: no players online");
            return;
        }
        
        try {
            int requestId = requestCounter.incrementAndGet();
            
            responseHandlers.put(requestId, responseHandler);
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(requestId);
            out.writeUTF(command);
            
            Player player = Bukkit.getOnlinePlayers().iterator().next();
            player.sendPluginMessage(plugin, COMMAND_CHANNEL, stream.toByteArray());
            
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                Consumer<String> handler = responseHandlers.remove(requestId);
                if (handler != null) {
                    handler.accept("§cNo response received from proxy (timeout)");
                }
            }, 20 * 10);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send command to proxy", e);
            responseHandler.accept("§cFailed to send command to proxy: " + e.getMessage());
        }
    }
    
    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.hasPermission(permission);
    }
    
    @Override
    public String getServerName() {
        return serverName;
    }
    
    @Override
    public void logInfo(String message) {
        logger.info(message);
    }
    
    @Override
    public void logError(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }
}