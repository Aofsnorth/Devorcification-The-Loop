package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class FakePlayerEntity extends AbstractClientPlayerEntity {
    public UUID mimicTargetUuid;
    public String mimicTargetName;
    public long spawnAtMs;
    public int durationTicks;
    public String behavior = "stand_stare";
    public final Deque<Vec3d> positionHistory = new ArrayDeque<>();

    public FakePlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
        this.spawnAtMs = System.currentTimeMillis();
    }

    public void recordMimicPosition(Vec3d pos) {
        positionHistory.addLast(pos);
        while (positionHistory.size() > 120 * 20) positionHistory.pollFirst();
    }

    public Vec3d replayPosition() {
        if (positionHistory.isEmpty()) return this.getPos();
        int delayTicks = 60 + (int) (Math.random() * 60);
        if (positionHistory.size() < delayTicks) return this.getPos();
        Vec3d[] arr = positionHistory.toArray(new Vec3d[0]);
        return arr[arr.length - 1 - delayTicks];
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior == null ? "stand_stare" : behavior;
    }

    public boolean shouldDespawn() {
        if (durationTicks <= 0) return false;
        return (System.currentTimeMillis() - spawnAtMs) / 50L > durationTicks;
    }

    public static FakePlayerEntity spawnClientSide(UUID target, UUID mimic, String mimicName, Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return null;
        GameProfile profile = new GameProfile(mimic, mimicName == null ? "Ghost" : mimicName);
        FakePlayerEntity fake = new FakePlayerEntity(client.world, profile);
        fake.mimicTargetUuid = mimic;
        fake.mimicTargetName = mimicName;
        fake.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0f, 0f);
        client.world.addEntity(fake.getId(), fake);
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Fake player spawned mimicking {} at {}", mimicName, pos);
        return fake;
    }
}
