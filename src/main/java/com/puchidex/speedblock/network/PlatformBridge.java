package com.puchidex.speedblock.network;

import java.util.UUID;
import java.util.function.Consumer;

public interface PlatformBridge {
    void initialize();
    
    void sendCommand(String command, Consumer<String> responseHandler);
    
    boolean hasPermission(UUID uuid, String permission);
    
    String getServerName();
    
    void logInfo(String message);
    
    void logError(String message, Throwable error);
}