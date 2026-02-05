package com.allaymc.blockrewards.listener;

import com.allaymc.blockrewards.BlockRewards;
import com.allaymc.blockrewards.manager.RewardManager;
import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.block.BlockBreakEvent;
import org.allaymc.api.item.ItemStack;

/**
 * Listens for block break events and awards rewards to players
 */
public class BlockBreakListener {

    private final BlockRewards plugin;

    public BlockBreakListener(BlockRewards plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle block break events and check for rewards
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if event was cancelled
        if (event.isCancelled()) {
            return;
        }

        // Get player
        var entity = event.getEntity();
        if (!(entity instanceof EntityPlayer player)) {
            return;
        }

        // Get block information
        var block = event.getBlock();
        var blockState = block.getBlockState();
        String blockId = blockState.getBlockType().getIdentifier().toString();

        // Get reward manager
        RewardManager rewardManager = plugin.getRewardManager();

        // Check if this block has any configured rewards
        var rewards = rewardManager.getRewards(blockId);
        if (rewards.isEmpty()) {
            return;
        }

        // Get player UUID
        var playerUuid = player.getUniqueId();

        // Process each reward configuration for this block
        for (var rewardConfig : rewards) {
            // Check if player is on cooldown
            if (!rewardManager.canReceiveReward(playerUuid, blockId)) {
                continue;
            }

            // Roll for reward
            if (rewardManager.rollForReward(rewardConfig.getChance())) {
                // Create the reward item
                ItemStack rewardItem = rewardManager.createItemStack(rewardConfig);
                if (rewardItem == null) {
                    continue;
                }

                // Give the reward to player
                boolean success = player.tryAddItem(rewardItem);

                if (success) {
                    // Record the reward
                    rewardManager.recordReward(playerUuid, blockId);

                    // Send success message
                    String message = "§aYou received §e" +
                        rewardConfig.getAmount() + "x " +
                        rewardItem.getItemType().getIdentifier().toString() +
                        "§a for breaking §e" + blockId + "§a!";

                    player.sendMessage(message);
                } else {
                    // Player's inventory is full, drop the item on the ground
                    player.getDimension().dropItem(rewardItem, player.getLocation());

                    // Drop message
                    String message = "§eYour inventory is full! Reward dropped on the ground.";

                    player.sendMessage(message);
                }

                // Only give one reward per block break
                break;
            }
        }
    }
}
