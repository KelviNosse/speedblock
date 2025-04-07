package com.puchidex.speedblock.models;

import java.util.UUID;

public class WhitelistPlayer {
    private final String name;
    private final UUID uuid;
    
    public WhitelistPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public UUID getUuid() {
        return uuid;
    }
}