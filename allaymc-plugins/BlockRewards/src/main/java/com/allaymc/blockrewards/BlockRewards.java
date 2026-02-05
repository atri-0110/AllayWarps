package com.allaymc.blockrewards;

import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import com.allaymc.blockrewards.command.BlockRewardsCommand;
import com.allaymc.blockrewards.listener.BlockBreakListener;
import com.allaymc.blockrewards.manager.RewardManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BlockRewards - A plugin that rewards players with items when breaking specific blocks.
 *
 * Features:
 * - Configurable block-to-item rewards
 * - Per-player cooldown system
 * - Chance-based rewards with probability
 * - Admin commands to manage rewards
 */
public class BlockRewards extends Plugin {

    private static BlockRewards instance;

    private RewardManager rewardManager;
    private BlockBreakListener blockBreakListener;
    private Set<String> activeTasks;

    @Override
    public void onLoad() {
        this.pluginLogger.info("BlockRewards is loading...");
        instance = this;
        this.activeTasks = ConcurrentHashMap.newKeySet();
        this.rewardManager = new RewardManager(this);
        this.rewardManager.loadDefaultRewards();
    }

    @Override
    public void onEnable() {
        this.pluginLogger.info("BlockRewards is enabling...");

        // Register event listener
        this.blockBreakListener = new BlockBreakListener(this);
        Server.getInstance().getEventBus().registerListener(blockBreakListener);

        // Register commands
        Registries.COMMANDS.register(new BlockRewardsCommand(this));

        this.pluginLogger.info("BlockRewards has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        this.pluginLogger.info("BlockRewards is disabling...");

        // Unregister event listener
        if (blockBreakListener != null) {
            Server.getInstance().getEventBus().unregisterListener(blockBreakListener);
        }

        // Stop all scheduler tasks
        if (activeTasks != null) {
            activeTasks.clear();
        }

        this.pluginLogger.info("BlockRewards has been disabled!");
    }

    /**
     * Get the singleton instance of BlockRewards plugin
     *
     * @return Plugin instance
     */
    public static BlockRewards getInstance() {
        return instance;
    }

    /**
     * Get the reward manager
     *
     * @return RewardManager instance
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * Add a task to the active tasks tracking set
     *
     * @param taskId Unique task identifier
     */
    public void addActiveTask(String taskId) {
        activeTasks.add(taskId);
    }

    /**
     * Remove a task from the active tasks tracking set
     *
     * @param taskId Unique task identifier
     */
    public void removeActiveTask(String taskId) {
        activeTasks.remove(taskId);
    }

    /**
     * Check if a task is still active
     *
     * @param taskId Unique task identifier
     * @return true if task is active, false otherwise
     */
    public boolean isActiveTask(String taskId) {
        return activeTasks.contains(taskId);
    }

    /**
     * Helper method for logging info messages
     *
     * @param message Message to log
     */
    public void logInfo(String message) {
        this.pluginLogger.info(message);
    }
}
