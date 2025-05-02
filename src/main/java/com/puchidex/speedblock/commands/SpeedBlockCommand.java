package com.puchidex.speedblock.commands;

import com.puchidex.speedblock.SpeedBlock;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpeedBlockCommand implements SimpleCommand {
    private final SpeedBlock plugin;
    
    public SpeedBlockCommand(SpeedBlock plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        
        if (!source.hasPermission("speedblock.admin")) {
            sendMessage(source, "no-permission");
            return;
        }
        
        if (args.length == 0) {
            sendUsage(source);
            return;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    sendUsage(source);
                    return;
                }
                createWhitelist(source, args[1]);
                break;
                
            case "reload":
                plugin.reload();
                sendMessage(source, "config-reloaded");
                break;
                
            default:
                if (args.length < 2) {
                    sendUsage(source);
                    return;
                }
                
                String serverName = args[0];
                
                if (!plugin.getConfigFactory().getServerList().contains(serverName)) {
                    sendMessage(source, "server-not-found", "server", serverName);
                    return;
                }
                
                switch (args[1].toLowerCase()) {
                    case "add":
                        if (args.length < 3) {
                            sendUsage(source);
                            return;
                        }
                        addPlayer(source, serverName, args[2]);
                        break;
                        
                    case "remove":
                        if (args.length < 3) {
                            sendUsage(source);
                            return;
                        }
                        removePlayer(source, serverName, args[2]);
                        break;
                        
                    case "clear":
                        clearWhitelist(source, serverName);
                        break;
                        
                    default:
                        sendUsage(source);
                        break;
                }
        }
    }
    
    private void sendUsage(CommandSource source) {
        sendMessage(source, "speedblock create <serverName> - Creates whitelist for server");
        sendMessage(source, "speedblock <serverName> add <playerName> - Adds player to whitelist");
        sendMessage(source, "speedblock <serverName> remove <playerName> - Removes player from whitelist");
        sendMessage(source, "speedblock <serverName> clear - Clears the whitelist");
        sendMessage(source, "speedblock reload - Reloads the configuration");
    }
    
    private void createWhitelist(CommandSource source, String serverName) {
        if (plugin.getWhitelistManager().createWhitelist(serverName)) {
            sendMessage(source, "whitelist-created", "server", serverName);
        } else {
            sendMessage(source, "server-not-found", "server", serverName);
        }
    }
    
    private void addPlayer(CommandSource source, String serverName, String playerName) {
        Optional<Player> optionalPlayer = plugin.getServer().getPlayer(playerName);
        
        if (optionalPlayer.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
            if (plugin.getWhitelistManager().addPlayer(serverName, playerName, uuid)) {
                sendMessage(source, "player-added", "player", playerName, "server", serverName);
            }
            return;
        }
        
        Player player = optionalPlayer.get();
        if (plugin.getWhitelistManager().addPlayer(serverName, player.getUsername(), player.getUniqueId())) {
            sendMessage(source, "player-added", "player", player.getUsername(), "server", serverName);
        }
    }
    
    private void removePlayer(CommandSource source, String serverName, String playerName) {
        if (plugin.getWhitelistManager().removePlayer(serverName, playerName)) {
            sendMessage(source, "player-removed", "player", playerName, "server", serverName);
        } else {
            sendMessage(source, "player-not-found", "player", playerName);
        }
    }
    
    private void clearWhitelist(CommandSource source, String serverName) {
        plugin.getWhitelistManager().clearWhitelist(serverName);
        sendMessage(source, "whitelist-cleared", "server", serverName);
    }
    
    private void sendMessage(CommandSource source, String message) {
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
    
    private void sendMessage(CommandSource source, String key, String... replacements) {
        String message = plugin.getConfigFactory().formatMessage(key, replacements);
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
    
    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        
        if (!source.hasPermission("speedblock.admin")) {
            return List.of();
        }
        
        if (args.length == 0) {
            return List.of("create", "reload");
        }
        
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(Arrays.asList("create", "reload"));
            suggestions.addAll(plugin.getConfigFactory().getServerList());
            return filterStartingWith(args[0], suggestions);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return List.of();
            }
            
            if (plugin.getConfigFactory().getServerList().contains(args[0])) {
                return filterStartingWith(args[1], List.of("add", "remove", "clear"));
            }
        }
        
        if (args.length == 3) {
            if (plugin.getConfigFactory().getServerList().contains(args[0])) {
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    return plugin.getServer().getAllPlayers().stream()
                            .map(Player::getUsername)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .toList();
                }
            }
        }
        
        return List.of();
    }
    
    private List<String> filterStartingWith(String prefix, List<String> options) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .toList();
    }
    
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("speedblock.admin");
    }
}