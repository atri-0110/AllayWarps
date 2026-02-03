package org.allaymc.allaywarps.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.allaymc.api.world.Dimension;
import org.allaymc.api.world.World;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarpLocation {
    private String name;
    private float x;
    private float y;
    private float z;
    private float yaw;
    private float pitch;
    private String worldName;
    private int dimensionId;
    private String creator;
    private long createdAt;
    private String description;

    public WarpLocation(String name, double x, double y, double z, float yaw, float pitch, Dimension dimension, String creator) {
        this.name = name;
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.yaw = yaw;
        this.pitch = pitch;
        World world = dimension.getWorld();
        this.worldName = world.getName();
        this.dimensionId = dimension.getDimensionInfo().dimensionId();
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
        this.description = "";
    }
}
