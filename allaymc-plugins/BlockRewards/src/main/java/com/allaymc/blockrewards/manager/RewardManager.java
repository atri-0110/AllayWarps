package com.allaymc.blockrewards.manager;

import com.allaymc.blockrewards.BlockRewards;
import lombok.Data;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.api.utils.NBTIO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages block-to-item rewards configuration and player cooldowns
 */
public class RewardManager {

    private final BlockRewards plugin;

    // Map: Block identifier -> List of possible rewards
    private final Map<String, List<RewardConfig>> rewards;

    // Map: Player UUID -> Map of block -> last reward timestamp
    private final Map<UUID, Map<String, Long>> playerCooldowns;

    public RewardManager(BlockRewards plugin) {
        this.plugin = plugin;
        this.rewards = new ConcurrentHashMap<>();
        this.playerCooldowns = new ConcurrentHashMap<>();
    }

    /**
     * Load default reward configurations
     */
    public void loadDefaultRewards() {
        plugin.logInfo("Loading default reward configurations...");

        // Diamond Ore -> Diamond with 20% chance
        addReward("minecraft:diamond_ore", "minecraft:diamond", 1, 20, 5000);

        // Iron Ore -> Iron Ingot with 30% chance
        addReward("minecraft:iron_ore", "minecraft:iron_ingot", 1, 30, 3000);

        // Gold Ore -> Gold Ingot with 25% chance
        addReward("minecraft:gold_ore", "minecraft:gold_ingot", 1, 25, 4000);

        // Coal Ore -> Coal with 40% chance
        addReward("minecraft:coal_ore", "minecraft:coal", 2, 40, 2000);

        // Lapis Ore -> Lapis Lazuli with 35% chance
        addReward("minecraft:lapis_ore", "minecraft:lapis_lazuli", 3, 35, 2500);

        // Redstone Ore -> Redstone with 30% chance
        addReward("minecraft:redstone_ore", "minecraft:redstone", 3, 30, 3000);

        // Ancient Debris -> Netherite Scrap with 15% chance
        addReward("minecraft:ancient_debris", "minecraft:netherite_scrap", 1, 15, 6000);

        // Copper Ore -> Copper Ingot with 35% chance
        addReward("minecraft:copper_ore", "minecraft:copper_ingot", 2, 35, 2500);

        plugin.logInfo("Loaded " + rewards.size() + " reward configurations");
    }

    /**
     * Add a reward configuration
     *
     * @param blockId Block identifier (e.g., "minecraft:diamond_ore")
     * @param itemId Item identifier (e.g., "minecraft:diamond")
     * @param amount Number of items to reward
     * @param chance Chance percentage (0-100)
     * @param cooldown Cooldown in milliseconds
     */
    public void addReward(String blockId, String itemId, int amount, int chance, long cooldown) {
        var config = new RewardConfig();
        config.setBlockId(blockId);
        config.setItemId(itemId);
        config.setAmount(amount);
        config.setChance(chance);
        config.setCooldown(cooldown);

        rewards.computeIfAbsent(blockId, k -> new ArrayList<>()).add(config);
    }

    /**
     * Get all reward configurations for a specific block
     *
     * @param blockId Block identifier
     * @return List of reward configurations
     */
    public List<RewardConfig> getRewards(String blockId) {
        return rewards.getOrDefault(blockId, Collections.emptyList());
    }

    /**
     * Get all configured block identifiers
     *
     * @return Set of block IDs
     */
    public Set<String> getConfiguredBlocks() {
        return rewards.keySet();
    }

    /**
     * Check if a player can receive a reward for a specific block
     *
     * @param playerUuid Player UUID
     * @param blockId Block identifier
     * @return true if player can receive reward, false if on cooldown
     */
    public boolean canReceiveReward(UUID playerUuid, String blockId) {
        var playerCooldowns = this.playerCooldowns.get(playerUuid);
        if (playerCooldowns == null) {
            return true;
        }

        Long lastReward = playerCooldowns.get(blockId);
        if (lastReward == null) {
            return true;
        }

        for (var config : getRewards(blockId)) {
            if (System.currentTimeMillis() - lastReward >= config.getCooldown()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Record that a player received a reward for a specific block
     *
     * @param playerUuid Player UUID
     * @param blockId Block identifier
     */
    public void recordReward(UUID playerUuid, String blockId) {
        var playerCooldowns = this.playerCooldowns.computeIfAbsent(
            playerUuid,
            k -> new ConcurrentHashMap<>()
        );
        playerCooldowns.put(blockId, System.currentTimeMillis());
    }

    /**
     * Roll for a reward based on chance percentage
     *
     * @param chance Chance percentage (0-100)
     * @return true if reward should be given
     */
    public boolean rollForReward(int chance) {
        return new Random().nextInt(100) < chance;
    }

    /**
     * Create an ItemStack from the reward configuration
     *
     * @param config Reward configuration
     * @return ItemStack or null if item not found
     */
    public ItemStack createItemStack(RewardConfig config) {
        try {
            var itemType = ItemTypes.from(config.getItemId());
            if (itemType == null || itemType == ItemTypes.AIR) {
                return null;
            }
            return itemType.createItemStack(config.getAmount());
        } catch (Exception e) {
            plugin.logInfo("Failed to create ItemStack for " + config.getItemId() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Remove a player from cooldown tracking (on disconnect)
     *
     * @param playerUuid Player UUID
     */
    public void removePlayer(UUID playerUuid) {
        playerCooldowns.remove(playerUuid);
    }

    /**
     * Data class for reward configuration
     */
    @Data
    public static class RewardConfig {
        private String blockId;
        private String itemId;
        private int amount;
        private int chance;
        private long cooldown; // in milliseconds
    }
}
