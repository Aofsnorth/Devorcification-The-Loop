package com.devorcification.ai;

import com.devorcification.Devorcification;
import com.devorcification.cycle.CycleManager;
import com.devorcification.world.LoopDimension;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerObserver {
    private static final int SNAPSHOT_INTERVAL_TICKS = 100;
    private static final int HISTORY_SIZE = 12;
    private static final double RAPID_LOOK_THRESHOLD_DEG = 30.0;
    private static final double FREEZE_VELOCITY_THRESHOLD = 0.01;
    private static final long FREEZE_MS = 1000;

    private static final Gson GSON = new Gson();
    private static final Map<UUID, Deque<PlayerSnapshot>> history = new HashMap<>();
    private static final Map<UUID, Float> lastYaw = new HashMap<>();
    private static final Map<UUID, Integer> rapidLookCounter = new HashMap<>();
    private static final Map<UUID, Integer> backpedalCounter = new HashMap<>();
    private static final Map<UUID, Long> freezeStartMs = new HashMap<>();
    private static final Map<UUID, Long> freezeDurationMs = new HashMap<>();
    private static final Map<UUID, Integer> blockPlaceCounter = new HashMap<>();
    private static final Map<UUID, Integer> chatCounter = new HashMap<>();
    private static final Map<UUID, Integer> realityCheckCounter = new HashMap<>();
    private static final Map<UUID, Integer> socialConfusionCounter = new HashMap<>();
    private static final Map<UUID, String> fearProfile = new HashMap<>();
    private static final Map<UUID, Long> sessionStartMs = new HashMap<>();

    private static int tickAccumulator = 0;
    private static long lastSnapshotMs = System.currentTimeMillis();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerObserver::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        tickAccumulator++;
        if (tickAccumulator < SNAPSHOT_INTERVAL_TICKS) return;
        tickAccumulator = 0;

        long now = System.currentTimeMillis();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!LoopDimension.isLoopDimension(player.getWorld())) continue;
            PlayerSnapshot snap = buildSnapshot(player, now);
            Deque<PlayerSnapshot> ring = history.computeIfAbsent(player.getUuid(), k -> new ArrayDeque<>(HISTORY_SIZE));
            if (ring.size() >= HISTORY_SIZE) ring.pollFirst();
            ring.addLast(snap);
            Devorcification.LOGGER.info("[Devorcification AI] Snapshot: {}", GSON.toJson(snap));
        }
        lastSnapshotMs = now;
    }

    public static Map<UUID, PlayerSnapshot> collectSnapshots() {
        Map<UUID, PlayerSnapshot> latest = new HashMap<>();
        for (Map.Entry<UUID, Deque<PlayerSnapshot>> e : history.entrySet()) {
            PlayerSnapshot last = e.getValue().peekLast();
            if (last != null) latest.put(e.getKey(), last);
        }
        return latest;
    }

    public static List<PlayerSnapshot> latestHistory(UUID id) {
        Deque<PlayerSnapshot> ring = history.get(id);
        if (ring == null) return List.of();
        return List.copyOf(ring);
    }

    public static void recordBlockPlace(UUID id) {
        blockPlaceCounter.merge(id, 1, Integer::sum);
    }

    public static void recordChat(UUID id) {
        chatCounter.merge(id, 1, Integer::sum);
    }

    public static void recordRealityCheck(UUID id) {
        realityCheckCounter.merge(id, 1, Integer::sum);
    }

    public static void recordSocialConfusion(UUID id) {
        socialConfusionCounter.merge(id, 1, Integer::sum);
    }

    private static PlayerSnapshot buildSnapshot(ServerPlayerEntity player, long now) {
        UUID id = player.getUuid();
        PlayerSnapshot snap = new PlayerSnapshot(id, player.getName().getString());
        snap.position = player.getBlockPos();
        snap.velocity = player.getVelocity();
        snap.yaw = player.getYaw();
        snap.pitch = player.getPitch();
        snap.health = player.getHealth();
        snap.hunger = player.getHungerManager().getFoodLevel();
        BlockPos feet = player.getBlockPos();
        snap.lightLevel = player.getWorld().getLightLevel(feet);
        snap.lookingAt = raycastLookingAt(player);
        snap.isSprinting = player.isSprinting();
        snap.isSneaking = player.isSneaking();
        snap.isInventoryOpen = player.currentScreenHandler != player.playerScreenHandler;

        float prevYaw = lastYaw.getOrDefault(id, snap.yaw);
        double yawDelta = Math.abs(normalizeAngle(snap.yaw - prevYaw));
        if (yawDelta > RAPID_LOOK_THRESHOLD_DEG) {
            rapidLookCounter.merge(id, 1, Integer::sum);
        }
        lastYaw.put(id, snap.yaw);
        snap.rapidLookCount = rapidLookCounter.getOrDefault(id, 0);

        double dot = -Math.sin(Math.toRadians(snap.yaw)) * snap.velocity.x
            + Math.cos(Math.toRadians(snap.yaw)) * snap.velocity.z;
        if (dot > 0.1) {
            backpedalCounter.merge(id, 1, Integer::sum);
        }
        snap.backpedalCount = backpedalCounter.getOrDefault(id, 0);

        if (snap.velocity.lengthSquared() < FREEZE_VELOCITY_THRESHOLD) {
            freezeStartMs.putIfAbsent(id, now);
            Long start = freezeStartMs.get(id);
            if (start != null) {
                long dur = now - start;
                if (dur > FREEZE_MS) {
                    freezeDurationMs.merge(id, dur, Long::sum);
                    freezeStartMs.put(id, now);
                }
            }
        } else {
            freezeStartMs.remove(id);
        }
        snap.freezeDurationMs = freezeDurationMs.getOrDefault(id, 0L);

        snap.defensiveBlockPlacements = blockPlaceCounter.getOrDefault(id, 0);
        snap.chatMessageCount = chatCounter.getOrDefault(id, 0);
        snap.cycleNumber = CycleManager.getCycle(id);
        long startMs = sessionStartMs.computeIfAbsent(id, k -> now);
        snap.timeInLoopMs = now - startMs;
        snap.capturedAtMs = now;

        int rc = realityCheckCounter.getOrDefault(id, 0);
        int sc = socialConfusionCounter.getOrDefault(id, 0);
        if (rc > 3) fearProfile.put(id, "SKEPTIC");
        else if (sc > 5) fearProfile.put(id, "SOCIALLY_UNSTABLE");
        else if (snap.backpedalCount > 5) fearProfile.put(id, "COWARD");
        else if (snap.defensiveBlockPlacements > 0) fearProfile.put(id, "BUILDER");
        else if (snap.freezeDurationMs > 5000) fearProfile.put(id, "SKEPTIC");
        else fearProfile.put(id, "UNKNOWN");
        snap.fearProfile = fearProfile.get(id);

        rapidLookCounter.put(id, 0);
        backpedalCounter.put(id, 0);
        blockPlaceCounter.put(id, 0);
        chatCounter.put(id, 0);
        realityCheckCounter.put(id, 0);
        socialConfusionCounter.put(id, 0);
        freezeDurationMs.put(id, 0L);

        return snap;
    }

    private static String raycastLookingAt(ServerPlayerEntity player) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d dir = player.getRotationVec(1.0f);
        double reach = 6.0;
        Vec3d end = start.add(dir.multiply(reach));
        HitResult hit = player.getWorld().raycast(new RaycastContext(
            start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos bp = ((BlockHitResult) hit).getBlockPos();
            BlockState state = player.getWorld().getBlockState(bp);
            return state.getBlock().getTranslationKey();
        }
        Entity looked = pickEntity(player, start, end);
        if (looked != null) return "entity:" + looked.getType().getTranslationKey();
        return "air";
    }

    private static Entity pickEntity(ServerPlayerEntity player, Vec3d start, Vec3d end) {
        Entity closest = null;
        double closestDist = 6.0;
        for (Entity e : player.getWorld().getOtherEntities(player, player.getBoundingBox().stretch(end.subtract(start)).expand(1.0))) {
            if (e instanceof PlayerEntity) continue;
            double d = e.getBoundingBox().raycast(start, end).isPresent()
                ? start.distanceTo(e.getPos()) : Double.MAX_VALUE;
            if (d < closestDist) {
                closestDist = d;
                closest = e;
            }
        }
        return closest;
    }

    private static float normalizeAngle(float deg) {
        float d = deg % 360.0f;
        if (d > 180.0f) d -= 360.0f;
        if (d < -180.0f) d += 360.0f;
        return d;
    }
}
