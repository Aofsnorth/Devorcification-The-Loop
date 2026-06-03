package com.devorcification.pacing;

import com.devorcification.Devorcification;
import com.devorcification.audio.SoundEventRegistry;
import com.devorcification.audio.AudioManager;
import com.devorcification.entity.WatcherEntity;
import com.devorcification.entity.WatcherSpawnHandler;
import com.devorcification.world.LoopDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class FalseReliefEvent {
    public enum Type {
        GHOST_GONE(60, "no event for 60s"),
        ESCAPE_LOCKED(15, "escape door appears then locks"),
        FAKE_RESCUE(20, "fake player 'saves' but is entity");

        public final int durationSec;
        public final String description;

        Type(int s, String desc) {
            this.durationSec = s;
            this.description = desc;
        }
    }

    public static void execute(MinecraftServer server, ServerPlayerEntity target, Type type) {
        if (target == null) return;
        Devorcification.LOGGER.info("[Devorcification FalseRelief] Executing {} for {}",
            type.description, target.getName().getString());

        switch (type) {
            case GHOST_GONE -> {
                MenaceGauge.forceCooldown(target.getUuid(), type.durationSec);
                MenaceGauge.setIntensity(target.getUuid(), MenaceGauge.Intensity.CALM);
            }
            case ESCAPE_LOCKED -> {
                ServerWorld world = LoopDimension.getLoopWorld(server);
                if (world != null) {
                    BlockPos pos = target.getBlockPos().add(5, 0, 0);
                    world.setBlockState(pos, net.minecraft.block.Blocks.IRON_DOOR.getDefaultState());
                    MenaceGauge.forceCooldown(target.getUuid(), type.durationSec);
                }
            }
            case FAKE_RESCUE -> {
                ServerWorld world = LoopDimension.getLoopWorld(server);
                if (world != null) {
                    WatcherSpawnHandler.spawnWatcher(world, target, WatcherEntity.State.DOPPELGANGER);
                    MenaceGauge.forceCooldown(target.getUuid(), type.durationSec);
                }
            }
        }
    }

    public static void schedule(MinecraftServer server, ServerPlayerEntity target, Type type, long delaySec) {
        UUID id = target.getUuid();
        long fireAt = System.currentTimeMillis() + delaySec * 1000L;
        new Thread(() -> {
            try { Thread.sleep(delaySec * 1000L); } catch (InterruptedException e) { return; }
            if (server.getPlayerManager().getPlayer(id) != null) {
                server.execute(() -> execute(server, server.getPlayerManager().getPlayer(id), type));
            }
        }, "devorcification-false-relief-" + type.name()).start();
    }
}
