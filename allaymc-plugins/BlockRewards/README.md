# BlockRewards

A comprehensive reward system for AllayMC servers that gives players items when they break specific blocks.

## Features

- **Configurable Rewards**: Set up custom rewards for any block type
- **Chance-Based Rewards**: Control probability of rewards (0-100%)
- **Cooldown System**: Prevent reward spam with per-block cooldowns
- **Multiple Rewards Per Block**: Configure multiple possible rewards for a single block
- **Admin Commands**: Manage rewards with in-game commands

## Default Rewards

| Block | Reward | Chance | Cooldown |
|-------|--------|--------|----------|
| Diamond Ore | 1x Diamond | 20% | 5 seconds |
| Iron Ore | 1x Iron Ingot | 30% | 3 seconds |
| Gold Ore | 1x Gold Ingot | 25% | 4 seconds |
| Coal Ore | 2x Coal | 40% | 2 seconds |
| Lapis Ore | 3x Lapis Lazuli | 35% | 2.5 seconds |
| Redstone Ore | 3x Redstone | 30% | 3 seconds |
| Ancient Debris | 1x Netherite Scrap | 15% | 6 seconds |
| Copper Ore | 2x Copper Ingot | 35% | 2.5 seconds |

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/blockrewards reload` | `blockrewards.reload` | Reload reward configurations |
| `/blockrewards list` | `blockrewards.list` | List all configured rewards |
| `/blockrewards clearcooldown <player> <block>` | `blockrewards.clear` | Clear a player's cooldown for a specific block |
| `/blockrewards help` | - | Show command help |

## Installation

1. Download the latest `.jar` file from the [Releases](https://github.com/YourUsername/BlockRewards/releases) page
2. Place the `.jar` file in your AllayMC server's `plugins` folder
3. Restart your server or run `/reload`

## Building from Source

### Prerequisites

- Java 21 or higher
- Gradle 8.x or higher

### Build Steps

```bash
# Clone the repository
git clone https://github.com/YourUsername/BlockRewards.git
cd BlockRewards

# Build the plugin
./gradlew shadowJar -Dorg.gradle.jvmargs="-Xmx3G"

# Find the built JAR
ls build/libs/
```

The built JAR will be located at `build/libs/BlockRewards-<version>-all.jar`.

## Configuration

Currently, rewards are configured in code. Future versions will support a configuration file.

To add custom rewards, modify the `RewardManager.loadDefaultRewards()` method in the source code:

```java
// Add a reward
addReward("minecraft:block_id", "minecraft:item_id", amount, chance, cooldown);
```

Parameters:
- `block_id`: The identifier of the block (e.g., "minecraft:diamond_ore")
- `item_id`: The identifier of the item to reward (e.g., "minecraft:diamond")
- `amount`: Number of items to give
- `chance`: Percentage chance (0-100)
- `cooldown`: Cooldown time in milliseconds

## Usage Example

When a player breaks a diamond ore:
- 20% chance to receive 1x diamond
- 5-second cooldown before next reward from diamond ore
- Message: "You received 1x minecraft:diamond for breaking minecraft:diamond_ore!"

If the player's inventory is full, the reward is dropped on the ground and they receive a warning message.

## Permissions

- `blockrewards.admin` - Access to all admin commands
- `blockrewards.reload` - Reload reward configurations
- `blockrewards.list` - List configured rewards
- `blockrewards.clear` - Clear player cooldowns

## API Compatibility

- AllayMC API Version: 0.24.0
- Java Version: 21

## License

This plugin is licensed under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Issues

If you encounter any bugs or have feature requests, please open an issue on the [GitHub Issues](https://github.com/YourUsername/BlockRewards/issues) page.
