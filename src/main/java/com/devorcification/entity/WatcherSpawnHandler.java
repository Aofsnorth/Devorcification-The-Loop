package com.devorcification.entity;

import com.devorcification.Devorcification;
import com.devorcification.audio.HeartbeatSync;
import com.devorcification.audio.SoundEventRegistry;
import com.devorcification.audio.AudioManager;
import com.devorcification.world.LoopDimension;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WatcherSpawnHandler {
    private static final ConcurrentMap<UUID, Integer> pendingSpawns = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, WatcherEntity.State> lastKnownState = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WatcherSpawnHandler::onServerTick);
    }

    public static WatcherEntity spawnWatcher(ServerWorld world, ServerPlayerEntity target, WatcherEntity.State state) {
        if (world == null || target == null) return null;
        WatcherEntity entity = new WatcherEntity(ModEntityTypes.WATCHER, world);
        entity.setState(state);
        entity.setTargetPlayer(target.getUuid());

        Vec3d anchor = peripheralAnchorFor(target);
        entity.refreshPositionAndAngles(anchor.x, anchor.y, anchor.z, 0f, 0f);
        world.spawnEntity(entity);
        lastKnownState.put(target.getUuid(), state);

        onStateTransition(target, null, state);
        Devorcification.LOGGER.info("[Devorcification] Spawned Watcher state={} target={} pos=({},{},{})",
            state, target.getName().getString(), anchor.x, anchor.y, anchor.z);
        return entity;
    }

    public static void despawnWatcher(MinecraftServer server, UUID watcherUuid) {
        if (server == null) return;
        for (ServerWorld sw : server.getWorlds()) {
            Entity e = sw.getEntity(watcherUuid);
            if (e instanceof WatcherEntity w) {
                ServerPlayerEntity target = server.getPlayerManager().getPlayer(w.getTargetPlayer());
                w.discard();
                if (target != null) {
                    HeartbeatSync.fadeToNormal(target, HeartbeatSync.BPM_NORMAL, 3.0f);
                    lastKnownState.remove(target.getUuid());
                }
            }
        }
    }

    public static void scheduleSpawn(UUID targetPlayer, WatcherEntity.State state, int delayTicks) {
        pendingSpawns.put(targetPlayer, Math.max(0, delayTicks));
    }

    public static void onStateTransition(ServerPlayerEntity target, WatcherEntity.State from, WatcherEntity.State to) {
        if (target == null) return;
        if (to == WatcherEntity.State.PERIPHERAL) {
            HeartbeatSync.syncHeartbeat(target, HeartbeatSync.BPM_ANXIETY);
            AudioManager.playBypassedSound(target, SoundEventRegistry.WATCHER_BREATH, 0.4f, 0.8f);
        } else if (to == WatcherEntity.State.HUNT) {
            HeartbeatSync.syncHeartbeat(target, HeartbeatSync.BPM_PANIC);
            AudioManager.playBypassedSound(target, SoundEventRegistry.WATCHER_HUNT, 0.7f, 0.8f);
        } else if (to == WatcherEntity.State.MIRROR || to == WatcherEntity.State.DOPPELGANGER) {
            HeartbeatSync.syncHeartbeat(target, HeartbeatSync.BPM_STRESS);
        } else if (to == WatcherEntity.State.NULL) {
            HeartbeatSync.fadeToNormal(target, HeartbeatSync.BPM_NORMAL, 2.0f);
        }
    }

    private static void onServerTick(MinecraftServer server) {
        if (pendingSpawns.isEmpty()) return;
        var iter = pendingSpawns.entrySet().iterator();
        while (iter.hasNext()) {
            var e = iter.next();
            int remaining = e.getValue() - 1;
            if (remaining > 0) {
                e.setValue(remaining);
                continue;
            }
            iter.remove();
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(e.getKey());
            if (target == null) continue;
            ServerWorld loop = LoopDimension.getLoopWorld(server);
            if (loop == null) continue;
            spawnWatcher(loop, target, WatcherEntity.State.PERIPHERAL);
        }
    }

    private static Vec3d peripheralAnchorFor(ServerPlayerEntity target) {
        float yaw = target.getYaw() + 180.0f;
        double rad = Math.toRadians(yaw);
        double dist = 8.0 + Math.random() * 4.0;
        double dx = -Math.sin(rad) * dist;
        double dz = Math.cos(rad) * dist;
        return new Vec3d(target.getX() + dx, target.getY(), target.getZ() + dz);
    }
}
