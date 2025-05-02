package com.puchidex.speedblock.network;

public class BridgeFactory {
    private static PlatformBridge instance;

    public static PlatformBridge createBridge(Object plugin) {
        if (instance != null) {
            return instance;
        }

        PlatformDetector.Platform platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case VELOCITY:
                try {
                    Class<?> velocityBridgeClass = Class.forName("com.puchidex.speedblock.network.VelocityBridge");
                    instance = (PlatformBridge) velocityBridgeClass.getConstructor(Object.class).newInstance(plugin);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create Velocity bridge", e);
                }
                break;
            case PAPER:
                try {
                    Class<?> paperBridgeClass = Class.forName("com.puchidex.speedblock.network.PaperBridge");
                    instance = (PlatformBridge) paperBridgeClass.getConstructor(Object.class).newInstance(plugin);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create Paper bridge", e);
                }
                break;
            default:
                throw new RuntimeException("Unknown platform: " + platform);
        }
        
        return instance;
    }
}