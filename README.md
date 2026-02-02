# AllayWarps

A warp system for AllayMC servers providing server warp points and player homes functionality.

## Features

### Warps
- **Server Warps**: Admins can set public warp points that all players can use
- **Cross-Dimension Support**: Warps can be set in different dimensions (Overworld, Nether, End)
- **List View**: Use `/warps` to see all available warps
- **Descriptions**: Each warp can have a description

### Player Homes
- **Multiple Homes**: Players can set multiple homes
- **Cross-Dimension Support**: Homes work across different dimensions
- **Persistent Storage**: All warps and homes are saved to JSON files

## Commands

### Warp Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/warp <name>` | `allaywarps.warp.use` | Teleport to a specified warp |
| `/warp set <name> <description>` | `allaywarps.warp.set` | Set a new warp (admin) |
| `/warp delete <name>` | `allaywarps.warp.delete` | Delete a specified warp (admin) |
| `/warps` | `allaywarps.warp.use` | List all available warps |

### Player Home Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/home <name>` | `allaywarps.home.use` | Teleport to a home |
| `/home set <name>` | `allaywarps.home.set` | Set a home at current location |
| `/home delete <name>` | `allaywarps.home.delete` | Delete a home |
| `/home list` | `allaywarps.home.use` | List all your homes |
| `/home help` | `allaywarps.home.use` | Show help message |

## Installation

1. Download the latest `AllayWarps-0.1.0-shaded.jar`
2. Place the JAR file in your server's `plugins/` directory
3. Start or restart the server
4. The plugin will create configuration folders in `plugins/AllayWarps/`

## Building from Source

```bash
./gradlew shadowJar
```

The compiled JAR will be in `build/libs/AllayWarps-0.1.0-shaded.jar`

## Configuration

- **Warps**: Stored in `plugins/AllayWarps/warps.json`
- **Player Homes**: Stored in per-player JSON files at `plugins/AllayWarps/homes/<uuid>.json`

## Permissions

```
allaywarps.warp.use - Use warps
allaywarps.warp.set - Set warps (admin)
allaywarps.warp.delete - Delete warps (admin)
allaywarps.home.use - Use home commands
allaywarps.home.set - Set homes
allaywarps.home.delete - Delete homes
```

## Requirements

- AllayMC Server with API 0.24.0 or higher
- Java 21 or higher

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## Support

For support, please open an issue on [GitHub](https://github.com/atri-0110/AllayWarps/issues).
