package org.allaymc.allaywarps.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.allaymc.api.world.Dimension;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarpLocation {
    private String name;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String dimensionId;
    private String creator;
    private long createdAt;
    private String description;

    public WarpLocation(String name, double x, double y, double z, float yaw, float pitch, Dimension dimension, String creator) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dimensionId = dimension.getDimensionId().toString();
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
        this.description = "";
    }
}
