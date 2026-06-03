package com.devorcification.cycle;

import com.devorcification.Devorcification;
import com.devorcification.ai.AIDirectorClient;
import com.devorcification.ai.ActionPlan;
import com.devorcification.ai.ActionPlanExecutor;
import com.devorcification.ai.PlayerObserver;
import com.devorcification.ai.PlayerSnapshot;
import com.devorcification.ai.ProceduralDirector;
import com.devorcification.structure.LoopStructureManager;
import com.devorcification.world.LoopDimension;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CycleManager {
    private static final Map<UUID, Integer> cycleCounters = new HashMap<>();
    private static final Map<UUID, Long> sessionStart = new HashMap<>();
    private static final Map<UUID, Boolean> endDoorCooldown = new HashMap<>();

    public static final BlockPos START_POS = new BlockPos(0, 62, 0);
    public static final int BLIND_DURATION_TICKS = 30;
    public static final int NAUSEA_DURATION_TICKS = 30;
    public static final int END_DOOR_COOLDOWN_TICKS = 60;

    public static void onPlayerEnterLoop(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        cycleCounters.putIfAbsent(id, 0);
        sessionStart.putIfAbsent(id, System.currentTimeMillis());
        ServerWorld world = LoopDimension.getLoopWorld(player.getServer());
        if (world != null) {
            player.teleport(world, START_POS.getX() + 0.5, START_POS.getY(), START_POS.getZ() + 0.5, 0f, 0f);
            LoopStructureManager.placeLoopStructure(world, START_POS.down(2));
        }
        Devorcification.LOGGER.info("[Devorcification] Player {} entered The Loop at cycle {}",
            player.getName().getString(), cycleCounters.get(id));
    }

    public static void onCycleComplete(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (endDoorCooldown.getOrDefault(id, false)) return;

        int cycle = cycleCounters.getOrDefault(id, 0) + 1;
        cycleCounters.put(id, cycle);

        ServerWorld world = LoopDimension.getLoopWorld(player.getServer());
        if (world == null) {
            Devorcification.LOGGER.warn("[Devorcification] Cycle complete but Loop world is null.");
            return;
        }

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, BLIND_DURATION_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, NAUSEA_DURATION_TICKS, 0));
        player.setVelocity(Vec3d.ZERO);
        player.velocityModified = true;
        player.fallDistance = 0;

        player.teleport(world, START_POS.getX() + 0.5, START_POS.getY(), START_POS.getZ() + 0.5, 0f, 0f);

        endDoorCooldown.put(id, true);
        Devorcification.LOGGER.info("[Devorcification] Player {} entered Cycle {}",
            player.getName().getString(), cycle);

        requestAndExecutePlan(player, cycle);
    }

    private static void requestAndExecutePlan(ServerPlayerEntity player, int cycle) {
        List<PlayerSnapshot> snapshots = new ArrayList<>();
        PlayerSnapshot current = PlayerObserver.collectSnapshots().get(player.getUuid());
        if (current != null) snapshots.add(current);

        AIDirectorClient.requestPlan(cycle, snapshots).whenComplete((planOpt, err) -> {
            ActionPlan plan = planOpt.orElseGet(() -> {
                Devorcification.LOGGER.info("[Devorcification AI] Backend unavailable, using procedural fallback for cycle {}", cycle);
                return ProceduralDirector.generateFallback(cycle, snapshots);
            });
            if (err != null) {
                Devorcification.LOGGER.warn("[Devorcification AI] Plan request error: {}", err.getMessage());
                plan = ProceduralDirector.generateFallback(cycle, snapshots);
            }
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.execute(() -> ActionPlanExecutor.execute(plan));
            } else {
                ActionPlanExecutor.execute(plan);
            }
        });
    }

    public static void tickCooldowns(MinecraftServer server) {
        for (UUID id : endDoorCooldown.keySet()) {
            if (endDoorCooldown.get(id)) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
                if (player == null) {
                    endDoorCooldown.put(id, false);
                    continue;
                }
                BlockPos feet = player.getBlockPos();
                if (feet.getX() != START_POS.getX() || feet.getZ() != START_POS.getZ()) {
                    endDoorCooldown.put(id, false);
                }
            }
        }
    }

    public static int getCycle(UUID id) {
        return cycleCounters.getOrDefault(id, 0);
    }

    public static boolean isEndDoorCooldown(UUID id) {
        return endDoorCooldown.getOrDefault(id, false);
    }
}
