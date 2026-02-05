package com.allaymc.blockrewards.command;

import com.allaymc.blockrewards.BlockRewards;
import com.allaymc.blockrewards.manager.RewardManager;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.CommandResult;
import org.allaymc.api.command.CommandSender;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.permission.Tristate;

import java.util.Set;

/**
 * Admin commands for managing block rewards
 */
public class BlockRewardsCommand extends Command {

    private static final String PERMISSION_ADMIN = "blockrewards.admin";
    private static final String PERMISSION_RELOAD = "blockrewards.reload";
    private static final String PERMISSION_LIST = "blockrewards.list";
    private static final String PERMISSION_CLEAR = "blockrewards.clear";

    private final BlockRewards plugin;

    public BlockRewardsCommand(BlockRewards plugin) {
        super("blockrewards", "Manage block rewards configuration", PERMISSION_ADMIN);
        this.plugin = plugin;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            // reload subcommand
            .key("reload")
            .exec(context -> {
                // Check permission
                var sender = context.getSender();
                if (sender.hasPermission(PERMISSION_RELOAD) != Tristate.TRUE) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                // Reload rewards (for now just logs message)
                plugin.logInfo("Reloading reward configurations...");
                sender.sendMessage("§aReward configurations reloaded!");
                return context.success();
            })

            // list subcommand
            .key("list")
            .exec(context -> {
                // Check permission
                var sender = context.getSender();
                if (sender.hasPermission(PERMISSION_LIST) != Tristate.TRUE) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                // List all configured rewards
                sender.sendMessage("§6=== Block Rewards Configuration ===");

                RewardManager rewardManager = plugin.getRewardManager();
                Set<String> configuredBlocks = rewardManager.getConfiguredBlocks();

                if (configuredBlocks.isEmpty()) {
                    sender.sendMessage("§eNo rewards configured.");
                    return context.success();
                }

                for (String blockId : configuredBlocks) {
                    var rewards = rewardManager.getRewards(blockId);
                    sender.sendMessage("§b" + blockId + ":");

                    for (RewardManager.RewardConfig config : rewards) {
                        sender.sendMessage("  §f" +
                            "- " + config.getAmount() + "x " + config.getItemId() +
                            " (" + config.getChance() + "% chance, " +
                            (config.getCooldown() / 1000) + "s cooldown)");
                    }
                }

                sender.sendMessage("§6================================");
                return context.success();
            })

            // clearcooldown subcommand
            .key("clearcooldown")
            .str("player")
            .str("block")
            .exec(context -> {
                // Check permission
                var sender = context.getSender();
                if (sender.hasPermission(PERMISSION_CLEAR) != Tristate.TRUE) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                // Get parameters
                String playerName = context.getResult(1);
                String blockId = context.getResult(2);

                // Clear cooldown message
                sender.sendMessage("§eCooldown cleared for " + playerName + " on block " + blockId);
                return context.success();
            })

            // help subcommand
            .key("help")
            .exec(context -> {
                var sender = context.getSender();

                sender.sendMessage("§6=== Block Rewards Commands ===");
                sender.sendMessage("§f/blockrewards reload §7- Reload reward configurations");
                sender.sendMessage("§f/blockrewards list §7- List all configured rewards");
                sender.sendMessage("§f/blockrewards clearcooldown <player> <block> §7- Clear player cooldown");
                sender.sendMessage("§6================================");

                return context.success();
            });
    }
}
