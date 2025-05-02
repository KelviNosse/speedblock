package com.puchidex.speedblock;

import com.puchidex.speedblock.network.PlatformDetector;
import com.puchidex.speedblock.network.PlatformDetector.Platform;

public class SpeedBlockLoader {
    
    public static String getMainClass() {
        Platform platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case VELOCITY:
                return "com.puchidex.speedblock.SpeedBlock";
            case PAPER:
                return "com.puchidex.speedblock.SpeedBlockPaper";
            default:
                throw new RuntimeException("Unsupported platform: " + platform);
        }
    }
}