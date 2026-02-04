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

# Plugins already reviewed (updated with ItemMail)
REVIEWED=(
    "AllayWarps"
    "ItemRepair"
    "LuckyBlocks"
    "AnnouncementSystem"
    "ParkourArena"
    "BlockLocker"
    "ServerAnnouncer"
    "RandomTeleport"
    "ItemMail"
)

# Find unreviewed plugins
count=0
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
        ((count++))
    fi
done

echo "---"
echo "Total unreviewed: $count"
