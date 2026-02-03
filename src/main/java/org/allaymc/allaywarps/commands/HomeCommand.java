package org.allaymc.allaywarps.commands;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.permission.Tristate;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.allaywarps.data.HomeLocation;
import org.allaymc.allaywarps.data.WarpDataManager;

import java.util.Map;

public class HomeCommand extends Command {
    private final WarpDataManager warpDataManager;

    public HomeCommand(WarpDataManager warpDataManager) {
        super("home", "Teleport to your home", "allaywarps.home.use");
        this.warpDataManager = warpDataManager;
        aliases.add("homes");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            .str("home_name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                String homeName = context.getResult(0);
                HomeLocation home = warpDataManager.getHome(player.getUniqueId(), homeName);

                if (home == null) {
                    player.sendMessage("Home '" + homeName + "' not found. Use /homes to see your homes.");
                    return context.fail();
                }

                org.allaymc.api.world.World world = Server.getInstance().getWorldPool().getWorld(home.getWorldName());
                if (world == null) {
                    world = player.getWorld();
                }
                
                Dimension dimension = world.getDimension(home.getDimensionId());
                if (dimension == null) {
                    player.sendMessage("The dimension for this home is no longer available.");
                    return context.fail();
                }

                org.allaymc.api.math.location.Location3dc loc = new Location3d(home.getX(), home.getY(), home.getZ(), home.getPitch(), home.getYaw(), dimension);
                player.teleport(loc);
                player.sendMessage("Teleported to home: " + home.getName());
                return context.success();
            })
            .root()
            .key("set")
            .str("name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }

                if (player.hasPermission("allaywarps.home.set") != Tristate.TRUE) {
                    player.sendMessage("You don't have permission to set homes!");
                    return context.fail();
                }

                String homeName = context.getResult(1);
                
                if (warpDataManager.homeExists(player.getUniqueId(), homeName)) {
                    player.sendMessage("A home with that name already exists. Use /home delete first to remove it.");
                    return context.fail();
                }

                var location = player.getLocation();
                var dimension = location.dimension();
                boolean success = warpDataManager.createHome(
                        player.getUniqueId(),
                        homeName,
                        location.x(),
                        location.y(),
                        location.z(),
                        (float) location.yaw(),
                        (float) location.pitch(),
                        dimension
                );

                if (success) {
                    player.sendMessage("Home '" + homeName + "' created successfully!");
                } else {
                    player.sendMessage("Failed to create home. You may have reached your home limit.");
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

                if (player.hasPermission("allaywarps.home.delete") != Tristate.TRUE) {
                    player.sendMessage("You don't have permission to delete homes!");
                    return context.fail();
                }

                String homeName = context.getResult(1);

                if (!warpDataManager.homeExists(player.getUniqueId(), homeName)) {
                    player.sendMessage("Home '" + homeName + "' does not exist.");
                    return context.fail();
                }

                boolean success = warpDataManager.deleteHome(player.getUniqueId(), homeName);
                if (success) {
                    player.sendMessage("Home '" + homeName + "' deleted successfully!");
                } else {
                    player.sendMessage("Failed to delete home. Please try again.");
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

                Map<String, HomeLocation> homes = warpDataManager.getPlayerHomes(player.getUniqueId());

                if (homes.isEmpty()) {
                    player.sendMessage("You have no homes set. Use /home set <name> to create one");
                    return context.success();
                }

                player.sendMessage("=== Your Homes ===");
                for (HomeLocation home : homes.values()) {
                    player.sendMessage("- " + home.getName());
                }
                player.sendMessage("Use /home <name> to teleport");
                return context.success();
            });
    }
}
