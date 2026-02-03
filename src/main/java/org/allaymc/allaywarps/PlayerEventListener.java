package org.allaymc.allaywarps;

import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.server.PlayerQuitEvent;
import org.allaymc.allaywarps.data.WarpDataManager;

import java.util.UUID;

public class PlayerEventListener {
    private final WarpDataManager warpDataManager;

    public PlayerEventListener(WarpDataManager warpDataManager) {
        this.warpDataManager = warpDataManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getLoginData().getUuid();
        warpDataManager.savePlayerHomes(playerUuid);
    }
}
