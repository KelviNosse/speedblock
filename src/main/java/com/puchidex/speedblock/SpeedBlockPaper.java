package com.puchidex.speedblock;

import com.puchidex.speedblock.network.BridgeFactory;
import com.puchidex.speedblock.network.PlatformBridge;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpeedBlockPaper extends JavaPlugin implements Listener, TabCompleter {
    private PlatformBridge bridge;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        if (getResource("paper-config.yml") != null) {
            saveResource("paper-config.yml", false);
        }
        
        bridge = BridgeFactory.createBridge(this);
        bridge.initialize();
        
        getServer().getPluginManager().registerEvents(this, this);
        
        getCommand("speedblock").setExecutor(this);
        getCommand("speedblock").setTabCompleter(this);
        
        getLogger().info("SpeedBlock Paper client has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("SpeedBlock Paper client has been disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("speedblock")) {
            return false;
        }
        
        if (!sender.hasPermission("speedblock.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showUsage(sender);
            return true;
        }
        
        StringBuilder commandBuilder = new StringBuilder();
        for (String arg : args) {
            if (commandBuilder.length() > 0) {
                commandBuilder.append(" ");
            }
            commandBuilder.append(arg);
        }
        
        String fullCommand = commandBuilder.toString();
        
        bridge.sendCommand(fullCommand, response -> {
            sender.sendMessage(response);
        });
        
        return true;
    }
    
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("speedblock")) {
            return null;
        }
        
        if (!sender.hasPermission("speedblock.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("create", "reload"));
            
            completions.addAll(getConfig().getStringList("known-servers"));
            
            return filterStartingWith(args[0], completions);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return filterStartingWith(args[1], getConfig().getStringList("known-servers"));
            }
            
            return filterStartingWith(args[1], Arrays.asList("add", "remove", "clear"));
        }
        
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return filterStartingWith(args[2], playerNames);
            }
        }
        
        return new ArrayList<>();
    }
    
    private List<String> filterStartingWith(String prefix, List<String> options) {
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                filtered.add(option);
            }
        }
        return filtered;
    }
    
    private void showUsage(CommandSender sender) {
        sender.sendMessage("§6SpeedBlock Commands:");
        sender.sendMessage("§e/speedblock create <serverName> §7- Creates whitelist for server");
        sender.sendMessage("§e/speedblock <serverName> add <playerName> §7- Adds player to whitelist");
        sender.sendMessage("§e/speedblock <serverName> remove <playerName> §7- Removes player from whitelist");
        sender.sendMessage("§e/speedblock <serverName> clear §7- Clears the whitelist");
        sender.sendMessage("§e/speedblock reload §7- Reloads the configuration");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    }
}