package com.devorcification.ai;

import com.devorcification.Devorcification;
import com.devorcification.audio.AudioManager;
import com.devorcification.audio.HeartbeatSync;
import com.devorcification.multiplayer.AsymmetricStateManager;
import com.devorcification.multiplayer.FakeEntityData;
import com.devorcification.multiplayer.SocialGraphEngine;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ActionPlanExecutor {
    public static void execute(ActionPlan plan) {
        if (plan == null) {
            Devorcification.LOGGER.warn("[Devorcification AI] execute called with null plan");
            return;
        }
        Devorcification.LOGGER.info(
            "[Devorcification AI] Executing plan cycle={} strategy={} spend={} reserve={}",
            plan.cycle, plan.globalStrategy, plan.menaceBudgetSpend, plan.menaceBudgetReserve);

        if (plan.perPlayerActions == null) return;
        for (var entry : plan.perPlayerActions.entrySet()) {
            routePlayerActions(entry.getKey(), entry.getValue());
        }

        if (Devorcification.currentServer != null) {
            SocialGraphEngine.exploitWeakestLink(Devorcification.currentServer);
        }
    }

    private static void routePlayerActions(UUID playerId, ActionPlan.PlayerAction action) {
        if (action.asymmetricBlocks != null && !action.asymmetricBlocks.isEmpty()) {
            for (ActionPlan.BlockOverride b : action.asymmetricBlocks) {
                BlockState state = resolveBlockState(b.blockId);
                if (state != null) {
                    AsymmetricStateManager.setBlockOverride(playerId,
                        new BlockPos(b.x, b.y, b.z), state, action.shaderIntensity > 0.0f ? 200 : 100);
                }
            }
        }
        if (action.directedAudio != null) {
            AsymmetricStateManager.queueDirectedAudio(playerId, action.directedAudio);
        }
        if (action.fakeChat != null) {
            AsymmetricStateManager.queueFakeChat(playerId,
                action.fakeChat.fromPlayer == null ? "?" : action.fakeChat.fromPlayer,
                action.fakeChat.message == null ? "" : action.fakeChat.message);
            routeShaderIntensity(action.shaderIntensity, 80.0f);
        }
        if (action.fakeEntity != null) {
            FakeEntityData fed = new FakeEntityData();
            fed.entityType = action.fakeEntity.entityType;
            fed.position = parsePositionString(action.fakeEntity.position);
            fed.yaw = action.fakeEntity.yaw;
            fed.pitch = action.fakeEntity.pitch;
            fed.behavior = action.fakeEntity.behavior == null ? "stand_stare" : action.fakeEntity.behavior;
            fed.durationTicks = action.fakeEntity.durationTicks > 0 ? action.fakeEntity.durationTicks : 200;
            fed.mimicTargetUuid = action.fakeEntity.mimicTargetUuid;
            Set<UUID> visible = new HashSet<>();
            if (action.fakeEntity.visibleTo != null) {
                for (String s : action.fakeEntity.visibleTo) {
                    try { visible.add(UUID.fromString(s)); } catch (Exception ignored) {}
                }
            }
            if (visible.isEmpty()) visible.add(playerId);
            fed.visibleTo = visible;
            AsymmetricStateManager.spawnFakeEntity(playerId, fed);
            routeShaderIntensity(action.shaderIntensity, 80.0f);
            routeHeartbeat(playerId, HeartbeatSync.BPM_ANXIETY);
        }
        if (action.shaderIntensity > 0.0f) {
            routeShaderIntensity(action.shaderIntensity, 0.0f);
        }
        if (action.desyncDelta != 0) {
            AsymmetricStateManager.applyDesync(playerId, action.desyncDelta);
        }
    }

    private static BlockState resolveBlockState(String blockId) {
        if (blockId == null) return null;
        Identifier id = blockId.contains(":")
            ? new Identifier(blockId)
            : new Identifier("minecraft", blockId);
        var block = Registries.BLOCK.get(id);
        if (block == null) return null;
        return block.getDefaultState();
    }

    private static Vec3d parsePositionString(String pos) {
        if (pos == null || pos.isBlank()) return new Vec3d(0, 62, 30);
        String[] parts = pos.trim().split("\\s+");
        if (parts.length < 3) return new Vec3d(0, 62, 30);
        try {
            return new Vec3d(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])
            );
        } catch (NumberFormatException e) {
            return new Vec3d(0, 62, 30);
        }
    }

    private static void routeAudio(UUID playerId, ActionPlan.PlayerAction action) {
        SoundEvent sound = null;
        if (action.directedAudio.soundId != null) {
            var id = action.directedAudio.soundId.contains(":")
                ? new Identifier(action.directedAudio.soundId)
                : new Identifier("minecraft", action.directedAudio.soundId);
            sound = Registries.SOUND_EVENT.get(id);
        }
        if (sound == null) return;
        ServerPlayerEntity target = Devorcification.findPlayer(playerId);
        if (target == null) return;
        if (action.directedAudio.soundId.endsWith("whisper_name")) {
            AudioManager.playWhisper(target, "...did you hear that?");
        } else {
            AudioManager.playBypassedSound(target, sound, action.directedAudio.volume, action.directedAudio.pitch);
        }
    }

    private static void routeHeartbeat(UUID playerId, float bpm) {
        ServerPlayerEntity target = Devorcification.findPlayer(playerId);
        HeartbeatSync.syncHeartbeat(target, bpm);
    }

    private static void routeShaderIntensity(float intensity, float heartbeatBpm) {
        try {
            Class<?> sm = Class.forName("com.devorcification.render.ShaderManager");
            sm.getMethod("setIntensity", float.class).invoke(null, intensity);
            if (heartbeatBpm > 0.0f) {
                sm.getMethod("pulseHeartbeat", float.class).invoke(null, heartbeatBpm);
            }
        } catch (Throwable t) {
            Devorcification.LOGGER.debug("[Devorcification AI] ShaderManager unavailable: {}", t.getMessage());
        }
    }
}
