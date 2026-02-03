package org.allaymc.allaywarps.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.allaywarps.AllayWarpsPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.UUID;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpDataManager {
    private static final String WARPS_FILE = "warps.json";
    private static final String HOMES_FILE = "homes.json";
    private final Gson gson;
    private final File dataFolder;

    @Getter
    private final Map<String, WarpLocation> warps;
    private final Map<UUID, Map<String, HomeLocation>> homes;

    public WarpDataManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = AllayWarpsPlugin.getInstance().getPluginContainer().dataFolder().toFile();
        this.warps = new ConcurrentHashMap<>();
        this.homes = new ConcurrentHashMap<>();

        loadData();
    }

    private void loadData() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadWarps();
        loadHomes();
    }

    private void loadWarps() {
        File warpsFile = new File(dataFolder, WARPS_FILE);
        if (warpsFile.exists()) {
            try (FileReader reader = new FileReader(warpsFile)) {
                Map<String, WarpLocation> loaded = gson.fromJson(reader, new TypeToken<Map<String, WarpLocation>>(){}.getType());
                if (loaded != null) {
                    warps.putAll(loaded);
                }
            } catch (IOException e) {
                AllayWarpsPlugin.getInstance().getPluginLogger().error("Failed to load warps", e);
            }
        }
    }

    private void loadHomes() {
        File homesFile = new File(dataFolder, HOMES_FILE);
        if (homesFile.exists()) {
            try (FileReader reader = new FileReader(homesFile)) {
                Map<String, Map<String, HomeLocation>> loaded = gson.fromJson(reader,
                        new TypeToken<Map<String, Map<String, HomeLocation>>>(){}.getType());
                if (loaded != null) {
                    loaded.forEach((uuid, homeMap) -> homes.put(UUID.fromString(uuid), homeMap));
                }
            } catch (IOException e) {
                AllayWarpsPlugin.getInstance().getPluginLogger().error("Failed to load homes", e);
            }
        }
    }

    public void saveAll() {
        saveWarps();
        saveHomes();
    }

    private void saveWarps() {
        File warpsFile = new File(dataFolder, WARPS_FILE);
        try (FileWriter writer = new FileWriter(warpsFile)) {
            gson.toJson(warps, writer);
        } catch (IOException e) {
            AllayWarpsPlugin.getInstance().getPluginLogger().error("Failed to save warps", e);
        }
    }

    private void saveHomes() {
        File homesFile = new File(dataFolder, HOMES_FILE);
        try (FileWriter writer = new FileWriter(homesFile)) {
            Map<String, Map<String, HomeLocation>> toSave = new HashMap<>();
            homes.forEach((uuid, homeMap) -> toSave.put(uuid.toString(), homeMap));
            gson.toJson(toSave, writer);
        } catch (IOException e) {
            AllayWarpsPlugin.getInstance().getPluginLogger().error("Failed to save homes", e);
        }
    }

    public boolean createWarp(String name, double x, double y, double z, float yaw, float pitch,
                              Dimension dimension, String creator, String description) {
        if (warps.containsKey(name.toLowerCase())) {
            return false;
        }

        WarpLocation warp = new WarpLocation(name, x, y, z, yaw, pitch, dimension, creator);
        warp.setDescription(description);
        warps.put(name.toLowerCase(), warp);
        saveWarps();
        return true;
    }

    public boolean deleteWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) {
            return false;
        }
        warps.remove(name.toLowerCase());
        saveWarps();
        return true;
    }

    public WarpLocation getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Collection<WarpLocation> getAllWarps() {
        return warps.values();
    }

    public int getWarpCount() {
        return warps.size();
    }

    public boolean createHome(UUID playerUuid, String name, double x, double y, double z,
                               float yaw, float pitch, Dimension dimension) {
        Map<String, HomeLocation> playerHomes = homes.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());

        if (playerHomes.size() >= getMaxHomes(playerUuid)) {
            return false;
        }

        HomeLocation home = new HomeLocation(name, x, y, z, yaw, pitch, dimension);
        playerHomes.put(name.toLowerCase(), home);
        saveHomes();
        return true;
    }

    public boolean deleteHome(UUID playerUuid, String name) {
        Map<String, HomeLocation> playerHomes = homes.get(playerUuid);
        if (playerHomes == null || !playerHomes.containsKey(name.toLowerCase())) {
            return false;
        }
        playerHomes.remove(name.toLowerCase());
        saveHomes();
        return true;
    }

    public HomeLocation getHome(UUID playerUuid, String name) {
        Map<String, HomeLocation> playerHomes = homes.get(playerUuid);
        return playerHomes != null ? playerHomes.get(name.toLowerCase()) : null;
    }

    public Map<String, HomeLocation> getPlayerHomes(UUID playerUuid) {
        return homes.getOrDefault(playerUuid, Collections.emptyMap());
    }

    public int getMaxHomes(UUID playerUuid) {
        return 5;
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public boolean homeExists(UUID playerUuid, String name) {
        Map<String, HomeLocation> playerHomes = homes.get(playerUuid);
        return playerHomes != null && playerHomes.containsKey(name.toLowerCase());
    }

    public void savePlayerHomes(UUID playerUuid) {
        if (!homes.containsKey(playerUuid)) {
            return;
        }

        File homesFile = new File(dataFolder, HOMES_FILE);
        try (FileWriter writer = new FileWriter(homesFile)) {
            Map<String, Map<String, HomeLocation>> toSave = new HashMap<>();
            homes.forEach((uuid, homeMap) -> toSave.put(uuid.toString(), homeMap));
            gson.toJson(toSave, writer);
        } catch (IOException e) {
            AllayWarpsPlugin.getInstance().getPluginLogger().error("Failed to save homes", e);
        }
    }
}
