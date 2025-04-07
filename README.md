# SpeedBlock

A Velocity plugin that handles whitelists for backend servers in a proxied Minecraft network.

## Features

- Per-server whitelist management
- Simple command-based interface
- Fully customizable messages
- Lightweight and efficient design

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your Velocity server's `plugins` directory
3. Start or restart your Velocity server
4. Edit the configuration file in `plugins/speedblock/config.yml` as needed

## Configuration

```yaml
servers:
  - lobby
  - survival
  - creative

messages:
  prefix: "&8[&bSpeedBlock&8] &7"
  not-whitelisted: "&cYou are not whitelisted on this server."
  player-added: "&aPlayer %player% has been added to the whitelist for %server%."
  player-removed: "&cPlayer %player% has been removed from the whitelist for %server%."
  whitelist-cleared: "&cThe whitelist for %server% has been cleared."
  whitelist-created: "&aWhitelist file for %server% has been created."
  config-reloaded: "&aConfiguration reloaded."
  player-not-found: "&cPlayer %player% not found."
  server-not-found: "&cServer %server% not found in the configuration."
  no-permission: "&cYou don't have permission to use this command."
```

## Commands

- `/speedblock create <backendServerName>` - Creates the whitelist JSON file for the specified backend server
- `/speedblock <backendServerName> add <playerName>` - Adds a player to the backend server whitelist
- `/speedblock <backendServerName> remove <playerName>` - Removes a player from the backend server whitelist
- `/speedblock <backendServerName> clear` - Clears all players and resets the backend server whitelist
- `/speedblock reload` - Reloads the plugin configuration file

## Permissions

- `speedblock.admin` - Allows use of all plugin commands

## Building from Source

1. Clone the repository
2. Run `./gradlew build` or `./gradlew shadowJar`
3. The compiled JAR will be in the `build/libs` directory