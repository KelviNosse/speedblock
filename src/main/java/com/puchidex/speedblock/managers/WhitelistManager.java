package com.puchidex.speedblock.managers;

import com.puchidex.speedblock.config.ConfigFactory;
import com.puchidex.speedblock.models.WhitelistPlayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WhitelistManager {
    private final Path dataDirectory;
    private final ConfigFactory configFactory;
    private final Map<String, List<WhitelistPlayer>> whitelists;
    
    public WhitelistManager(Path dataDirectory, ConfigFactory configFactory) {
        this.dataDirectory = dataDirectory;
        this.configFactory = configFactory;
        this.whitelists = new HashMap<>();
        
        Path whitelistsDir = dataDirectory.resolve("whitelists");
        try {
            if (!Files.exists(whitelistsDir)) {
                Files.createDirectories(whitelistsDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadAllWhitelists() {
        whitelists.clear();
        
        for (String server : configFactory.getServerList()) {
            loadWhitelist(server);
        }
    }
    
    public void loadWhitelist(String server) {
        Path whitelistFile = getWhitelistPath(server);
        List<WhitelistPlayer> players = new ArrayList<>();
        
        if (Files.exists(whitelistFile)) {
            try {
                String content = Files.readString(whitelistFile);
                JSONArray array = new JSONArray(content);
                
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.getString("name");
                    UUID uuid = UUID.fromString(obj.getString("uuid"));
                    players.add(new WhitelistPlayer(name, uuid));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        
        whitelists.put(server, players);
    }
    
    public void saveWhitelist(String server) {
        Path whitelistFile = getWhitelistPath(server);
        List<WhitelistPlayer> players = whitelists.getOrDefault(server, new ArrayList<>());
        
        JSONArray array = new JSONArray();
        for (WhitelistPlayer player : players) {
            JSONObject obj = new JSONObject();
            obj.put("name", player.getName());
            obj.put("uuid", player.getUuid().toString());
            array.put(obj);
        }
        
        try {
            Files.writeString(whitelistFile, array.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isWhitelisted(String server, String name, UUID uuid) {
        List<WhitelistPlayer> players = whitelists.getOrDefault(server, new ArrayList<>());
        
        for (WhitelistPlayer player : players) {
            if (player.getUuid().equals(uuid) || player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean addPlayer(String server, String name, UUID uuid) {
        if (!whitelists.containsKey(server)) {
            return false;
        }
        
        List<WhitelistPlayer> players = whitelists.get(server);
        
        Optional<WhitelistPlayer> existing = players.stream()
                .filter(p -> p.getUuid().equals(uuid) || p.getName().equalsIgnoreCase(name))
                .findFirst();
        
        if (existing.isPresent()) {
            return false;
        }
        
        players.add(new WhitelistPlayer(name, uuid));
        saveWhitelist(server);
        return true;
    }
    
    public boolean removePlayer(String server, String name) {
        if (!whitelists.containsKey(server)) {
            return false;
        }
        
        List<WhitelistPlayer> players = whitelists.get(server);
        
        Optional<WhitelistPlayer> existing = players.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
        
        if (existing.isEmpty()) {
            return false;
        }
        
        players.remove(existing.get());
        saveWhitelist(server);
        return true;
    }
    
    public void clearWhitelist(String server) {
        if (!whitelists.containsKey(server)) {
            return;
        }
        
        whitelists.get(server).clear();
        saveWhitelist(server);
    }
    
    public boolean createWhitelist(String server) {
        if (!configFactory.getServerList().contains(server)) {
            return false;
        }
        
        if (!whitelists.containsKey(server)) {
            whitelists.put(server, new ArrayList<>());
            saveWhitelist(server);
        }
        
        return true;
    }
    
    private Path getWhitelistPath(String server) {
        return dataDirectory.resolve("whitelists").resolve(server + ".json");
    }
}