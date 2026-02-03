package org.allaymc.allaywarps.commands;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.permission.Tristate;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.allaywarps.data.WarpDataManager;
import org.allaymc.allaywarps.data.WarpLocation;

public class WarpCommand extends Command {
    private final WarpDataManager warpDataManager;

    public WarpCommand(WarpDataManager warpDataManager) {
        super("warp", "Teleport to a warp point", "allaywarps.warp.use");
        this.warpDataManager = warpDataManager;
        aliases.add("warps");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            .str("warp_name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                String warpName = context.getResult(0);
                WarpLocation warp = warpDataManager.getWarp(warpName);

                if (warp == null) {
                    player.sendMessage("Warp '" + warpName + "' not found. Use /warps to see available warps.");
                    return context.fail();
                }

                org.allaymc.api.world.World world = Server.getInstance().getWorldPool().getWorld(warp.getWorldName());
                if (world == null) {
                    world = player.getWorld();
                }
                
                Dimension dimension = world.getDimension(warp.getDimensionId());
                if (dimension == null) {
                    player.sendMessage("The dimension for this warp is no longer available.");
                    return context.fail();
                }

                org.allaymc.api.math.location.Location3dc loc = new Location3d(warp.getX(), warp.getY(), warp.getZ(), warp.getPitch(), warp.getYaw(), dimension);
                player.teleport(loc);
                player.sendMessage("Teleported to warp: " + warp.getName());
                return context.success();
            })
            .root()
            .key("set")
            .str("name")
            .str("description")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                if (player.hasPermission("allaywarps.warp.set") != Tristate.TRUE) {
                    player.sendMessage("You don't have permission to set warps!");
                    return context.fail();
                }

                String warpName = context.getResult(1);
                String description = context.getResult(2);

                if (warpDataManager.warpExists(warpName)) {
                    player.sendMessage("A warp with that name already exists. Use /warp delete first to remove it.");
                    return context.fail();
                }

                var location = player.getLocation();
                var dimension = location.dimension();
                boolean success = warpDataManager.createWarp(
                        warpName,
                        location.x(),
                        location.y(),
                        location.z(),
                        (float) location.yaw(),
                        (float) location.pitch(),
                        dimension,
                        player.getDisplayName(),
                        description
                );

                if (success) {
                    player.sendMessage("Warp '" + warpName + "' created successfully!");
                } else {
                    player.sendMessage("Failed to create warp. Please try again.");
                }
                return success ? context.success() : context.fail();
            })
            .root()
            .key("delete")
            .str("name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                if (player.hasPermission("allaywarps.warp.delete") != Tristate.TRUE) {
                    player.sendMessage("You don't have permission to delete warps!");
                    return context.fail();
                }

                String warpName = context.getResult(1);

                if (!warpDataManager.warpExists(warpName)) {
                    player.sendMessage("Warp '" + warpName + "' does not exist.");
                    return context.fail();
                }

                boolean success = warpDataManager.deleteWarp(warpName);
                if (success) {
                    player.sendMessage("Warp '" + warpName + "' deleted successfully!");
                } else {
                    player.sendMessage("Failed to delete warp. Please try again.");
                }
                return success ? context.success() : context.fail();
            })
            .root()
            .key("list")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                var warps = warpDataManager.getAllWarps();

                if (warps.isEmpty()) {
                    player.sendMessage("No warps available. Create one with /warp set <name>");
                    return context.success();
                }

                player.sendMessage("=== Available Warps ===");
                for (WarpLocation warp : warps) {
                    String desc = warp.getDescription().isEmpty() ? "" : " - " + warp.getDescription();
                    player.sendMessage("- " + warp.getName() + " (by " + warp.getCreator() + ")" + desc);
                }
                player.sendMessage("Use /warp <name> to teleport");
                return context.success();
            });
    }
}
