package com.devorcification.multiplayer;

import net.minecraft.util.math.Vec3d;

import java.util.Set;
import java.util.UUID;

public class FakeEntityData {
    public String entityType;
    public UUID mimicTargetUuid;
    public Vec3d position;
    public float yaw;
    public float pitch;
    public String behavior;
    public int durationTicks;
    public Set<UUID> visibleTo;

    public FakeEntityData() {}

    public FakeEntityData(String entityType, Vec3d position, Set<UUID> visibleTo) {
        this.entityType = entityType;
        this.position = position;
        this.visibleTo = visibleTo;
        this.yaw = 0f;
        this.pitch = 0f;
        this.behavior = "stand_stare";
        this.durationTicks = 200;
    }
}
