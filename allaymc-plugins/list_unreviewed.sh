#!/bin/bash

# List of all plugins
ALL_PLUGINS=(
    "AllayWarps"
    "ItemMail"
    "DeathChest"
    "BlockLocker"
    "KitSystem"
    "AnnouncementSystem"
    "ItemRepair"
    "PlayerStatsTracker"
    "MobArena"
    "PlayerHomes"
    "BountyHunter"
    "TradePlugin"
    "ChatChannels"
    "CustomNPCs"
    "LuckyBlocks"
    "ParkourArena"
    "RandomTeleport"
    "ServerAnnouncer"
    "SimpleTPA"
    "PlayerStats"
    "PlayerTitles"
    "AuctionHouse"
)

# Plugins already reviewed
REVIEWED=(
    "AllayWarps"
    "ItemRepair"
    "LuckyBlocks"
    "AnnouncementSystem"
    "ParkourArena"
    "BlockLocker"
    "ServerAnnouncer"
    "RandomTeleport"
)

# Find unreviewed plugins
for plugin in "${ALL_PLUGINS[@]}"; do
    reviewed=false
    for reviewed_plugin in "${REVIEWED[@]}"; do
        if [ "$plugin" = "$reviewed_plugin" ]; then
            reviewed=true
            break
        fi
    done
    if [ "$reviewed" = false ]; then
        echo "$plugin"
    fi
done
