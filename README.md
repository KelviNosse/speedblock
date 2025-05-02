# SpeedBlock

A whitelist management plugin for Velocity proxy and Paper backend servers.

## Overview

SpeedBlock is a flexible whitelist management system that works on both Velocity proxies and Paper backend servers. It allows server administrators to control which players can access specific backend servers in a Velocity proxy network.

### Key Features

- **Platform Detection**: Automatically detects whether it's running on Velocity or Paper
- **Whitelist Management**: Create, manage, and enforce whitelists for backend servers
- **Cross-Platform Communication**: Seamlessly communicate between Velocity and Paper servers
- **Permission-Based Access**: Configure command access with permissions

## Installation

1. Download the SpeedBlock.jar file
2. Place it in both your Velocity proxy's `/plugins` folder and your Paper servers' `/plugins` folders
3. Start or restart your servers

## Configuration

### Velocity Configuration (config.yml)

```yaml
servers:
  - survival
  - creative
  - skyblock
  - minigames

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

### Paper Configuration (paper-config.yml)

```yaml
# List of known proxy servers to suggest in tab completion
known-servers:
  - survival
  - creative
  - skyblock
  - minigames
  - lobby

# Whether to enable debug logging
debug: false
```

## Commands

### From Velocity Proxy or Any Backend Server with the Plugin

- `/speedblock create <serverName>` - Creates a whitelist for a backend server
- `/speedblock <serverName> add <playerName>` - Adds a player to a server's whitelist
- `/speedblock <serverName> remove <playerName>` - Removes a player from a server's whitelist
- `/speedblock <serverName> clear` - Clears a server's whitelist
- `/speedblock reload` - Reloads the plugin configuration

## Permissions

- `speedblock.admin` - Allows access to all SpeedBlock commands

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For help and support, please create an issue on our GitHub repository.