package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import com.devorcification.ai.ActionPlan;
import com.devorcification.audio.AudioManager;
import com.devorcification.audio.DirectedAudioPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class AsymmetricStateManager {
    private static final Map<UUID, Integer> playerDesyncLevel = new HashMap<>();
    private static final Map<UUID, Map<BlockPos, BlockState>> blockOverrides = new HashMap<>();
    private static final Map<UUID, Set<FakeEntityData>> clientSideEntities = new HashMap<>();
    private static final Map<UUID, Queue<com.devorcification.ai.ActionPlan.DirectedAudio>> pendingAudio = new HashMap<>();
    private static final Map<UUID, Queue<com.devorcification.ai.ActionPlan.FakeChat>> pendingChat = new HashMap<>();
    private static final Map<UUID, RealitySnapshot> lastReality = new HashMap<>();
    private static final Map<UUID, Long> lastBlockOverrideAtMs = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playS2C().register(AsymmetricBlockPacket.ID, AsymmetricBlockPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AsymmetricEntityPacket.ID, AsymmetricEntityPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AsymmetricChatPacket.ID, AsymmetricChatPacket.CODEC);

        ServerTickEvents.END_SERVER_TICK.register(AsymmetricStateManager::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            UUID id = p.getUuid();
            Queue<ActionPlan.DirectedAudio> audio = pendingAudio.get(id);
            if (audio != null && !audio.isEmpty()) {
                ActionPlan.DirectedAudio a = audio.poll();
                if (a != null) {
                    var opt = net.minecraft.registry.Registries.SOUND_EVENT.get(new net.minecraft.util.Identifier(a.soundId));
                    if (opt.isPresent()) {
                        AudioManager.playBypassedSound(p, opt.get().value(), a.volume, a.pitch);
                    }
                }
            }
            Queue<ActionPlan.FakeChat> chats = pendingChat.get(id);
            if (chats != null && !chats.isEmpty()) {
                ActionPlan.FakeChat c = chats.poll();
                if (c != null) {
                    ServerPlayNetworking.send(p, new AsymmetricChatPacket(
                        p.getUuid(), c.fromPlayer == null ? "?" : c.fromPlayer,
                        c.message == null ? "" : c.message,
                        System.currentTimeMillis() - 5000L));
                }
            }
        }
    }

    public static void applyDesync(UUID player, int delta) {
        int current = playerDesyncLevel.getOrDefault(player, 0);
        current = Math.max(0, Math.min(100, current + delta));
        playerDesyncLevel.put(player, current);
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Player {} desync level = {}", player, current);
    }

    public static int getDesyncLevel(UUID player) {
        return playerDesyncLevel.getOrDefault(player, 0);
    }

    public static void setBlockOverride(UUID target, BlockPos pos, BlockState fakeState, int durationTicks) {
        Map<BlockPos, BlockState> map = blockOverrides.computeIfAbsent(target, k -> new HashMap<>());
        map.put(pos.toImmutable(), fakeState);
        lastBlockOverrideAtMs.put(target, System.currentTimeMillis());

        ServerPlayerEntity player = com.devorcification.Devorcification.findPlayer(target);
        if (player != null) {
            ServerPlayNetworking.send(player, new AsymmetricBlockPacket(target, pos, fakeState, durationTicks));
        }
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Block override {} -> {} for {}", pos, fakeState, target);
    }

    public static BlockState getPerceivedReality(UUID player, BlockPos pos, ServerWorld world) {
        Map<BlockPos, BlockState> map = blockOverrides.get(player);
        if (map != null) {
            BlockState s = map.get(pos);
            if (s != null) return s;
        }
        return world.getBlockState(pos);
    }

    public static BlockState getGroundTruth(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    public static void spawnFakeEntity(UUID target, FakeEntityData data) {
        Set<FakeEntityData> set = clientSideEntities.computeIfAbsent(target, k -> new HashSet<>());
        set.add(data);
        ServerPlayerEntity player = com.devorcification.Devorcification.findPlayer(target);
        if (player != null) {
            ServerPlayNetworking.send(player, AsymmetricEntityPacket.spawn(target, data));
        }
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Fake entity {} spawned for {} visibleTo={}",
            data.entityType, target, data.visibleTo);
    }

    public static void despawnFakeEntity(UUID target, String entityType) {
        Set<FakeEntityData> set = clientSideEntities.get(target);
        if (set != null) set.removeIf(d -> entityType.equals(d.entityType));
        ServerPlayerEntity player = com.devorcification.Devorcification.findPlayer(target);
        if (player != null) {
            ServerPlayNetworking.send(player, AsymmetricEntityPacket.despawn(target, entityType));
        }
    }

    public static void queueDirectedAudio(UUID target, ActionPlan.DirectedAudio audio) {
        Queue<ActionPlan.DirectedAudio> q = pendingAudio.computeIfAbsent(target, k -> new LinkedList<>());
        q.add(audio);
    }

    public static void queueFakeChat(UUID target, String fromPlayer, String message) {
        Queue<ActionPlan.FakeChat> q = pendingChat.computeIfAbsent(target, k -> new LinkedList<>());
        ActionPlan.FakeChat c = new ActionPlan.FakeChat();
        c.fromPlayer = fromPlayer;
        c.message = message;
        c.target = target.toString();
        q.add(c);
    }

    public static void resetSession() {
        blockOverrides.clear();
        clientSideEntities.clear();
        pendingAudio.clear();
        pendingChat.clear();
        lastBlockOverrideAtMs.clear();
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Session reset (desync levels preserved)");
    }

    public static RealitySnapshot snapshot(UUID player, ServerWorld world) {
        RealitySnapshot snap = new RealitySnapshot(player);
        snap.timestamp = System.currentTimeMillis();
        ServerPlayerEntity p = com.devorcification.Devorcification.findPlayer(player);
        if (p != null) {
            BlockPos center = p.getBlockPos();
            for (int dx = -8; dx <= 8; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -8; dz <= 8; dz++) {
                        BlockPos pos = center.add(dx, dy, dz);
                        snap.perceivedBlocks.put(pos.toImmutable(), getPerceivedReality(player, pos, world));
                    }
                }
            }
        }
        lastReality.put(player, snap);
        return snap;
    }

    public static List<UUID> playersInDesyncRange(int minDesync, int maxDesync) {
        List<UUID> out = new java.util.ArrayList<>();
        for (var e : playerDesyncLevel.entrySet()) {
            if (e.getValue() >= minDesync && e.getValue() <= maxDesync) out.add(e.getKey());
        }
        return out;
    }
}
