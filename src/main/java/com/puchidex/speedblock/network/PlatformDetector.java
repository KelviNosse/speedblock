package com.puchidex.speedblock.network;

public class PlatformDetector {
    public enum Platform {
        VELOCITY,
        PAPER,
        UNKNOWN
    }
    
    private static Platform currentPlatform = null;
    
    public static Platform detectPlatform() {
        if (currentPlatform != null) {
            return currentPlatform;
        }
        
        try {
            Class.forName("com.velocitypowered.api.proxy.ProxyServer");
            currentPlatform = Platform.VELOCITY;
            return Platform.VELOCITY;
        } catch (ClassNotFoundException e) {
        }
        
        try {
            Class.forName("org.bukkit.Bukkit");
            currentPlatform = Platform.PAPER;
            return Platform.PAPER;
        } catch (ClassNotFoundException e) {
        }
        
        currentPlatform = Platform.UNKNOWN;
        return Platform.UNKNOWN;
    }
}