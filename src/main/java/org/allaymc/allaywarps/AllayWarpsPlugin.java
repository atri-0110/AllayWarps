package org.allaymc.allaywarps;

import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.allaywarps.commands.HomeCommand;
import org.allaymc.allaywarps.commands.WarpCommand;
import org.allaymc.allaywarps.data.WarpDataManager;

public class AllayWarpsPlugin extends Plugin {

    private static AllayWarpsPlugin instance;
    private WarpDataManager warpDataManager;

    public static AllayWarpsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("AllayWarps is loading...");
    }

    @Override
    public void onEnable() {
        this.pluginLogger.info("AllayWarps is enabling...");

        this.warpDataManager = new WarpDataManager();

        Registries.COMMANDS.register(new WarpCommand(warpDataManager));
        Registries.COMMANDS.register(new HomeCommand(warpDataManager));

        this.pluginLogger.info("AllayWarps enabled successfully!");
        this.pluginLogger.info("Use /warp and /home commands to teleport");
        this.pluginLogger.info("Loaded " + warpDataManager.getWarpCount() + " warps");
    }

    @Override
    public void onDisable() {
        this.pluginLogger.info("AllayWarps is disabling...");
        if (warpDataManager != null) {
            warpDataManager.saveAll();
        }
        this.pluginLogger.info("AllayWarps disabled.");
    }

    public WarpDataManager getWarpDataManager() {
        return warpDataManager;
    }
}
