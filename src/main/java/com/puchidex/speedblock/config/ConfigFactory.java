package com.puchidex.speedblock.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigFactory {
    private final Path dataDirectory;
    private ConfigurationNode rootNode;
    private final Path configPath;
    
    public ConfigFactory(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");
        
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            
            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, configPath);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadConfig() {
        try {
            rootNode = YAMLConfigurationLoader.builder()
                    .setPath(configPath)
                    .build()
                    .load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<String> getServerList() {
        if (rootNode == null) {
            return Collections.emptyList();
        }
        
        return rootNode.getNode("servers").getList(Object::toString, new ArrayList<>());
    }
    
    public String getMessage(String key) {
        if (rootNode == null) {
            return "";
        }
        
        return rootNode.getNode("messages", key).getString("")
                .replace("&", "§");
    }
    
    public String formatMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        if (message.isEmpty()) {
            return "";
        }
        
        if (replacements.length % 2 != 0) {
            return message;
        }
        
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        
        return getMessage("prefix") + message;
    }
}