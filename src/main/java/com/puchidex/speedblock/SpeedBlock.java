package com.puchidex.speedblock;

import com.google.inject.Inject;
import com.puchidex.speedblock.commands.SpeedBlockCommand;
import com.puchidex.speedblock.config.ConfigFactory;
import com.puchidex.speedblock.listeners.PluginMessageListener;
import com.puchidex.speedblock.listeners.ServerConnectListener;
import com.puchidex.speedblock.managers.WhitelistManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "speedblock",
        name = "SpeedBlock",
        version = "1.0.0",
        description = "Backend server whitelist plugin for Velocity",
        authors = {"Kerumi"}
)
public class SpeedBlock {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigFactory configFactory;
    private WhitelistManager whitelistManager;

    @Inject
    public SpeedBlock(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configFactory = new ConfigFactory(dataDirectory);
        configFactory.loadConfig();
        
        whitelistManager = new WhitelistManager(dataDirectory, configFactory);
        whitelistManager.loadAllWhitelists();
        
        server.getEventManager().register(this, new ServerConnectListener(this));
        
        // Register the command
        server.getCommandManager().register("speedblock", new SpeedBlockCommand(this));
        
        // Register plugin messaging channels
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from("speedblock:commands"));
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from("speedblock:responses"));
        
        // Register the plugin messaging listener
        server.getEventManager().register(this, new PluginMessageListener(this));
        
        logger.info("SpeedBlock has been initialized!");
    }
    
    public void reload() {
        configFactory.loadConfig();
        whitelistManager.loadAllWhitelists();
        logger.info("SpeedBlock configuration reloaded");
    }
    
    public ProxyServer getServer() {
        return server;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public ConfigFactory getConfigFactory() {
        return configFactory;
    }
    
    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
}