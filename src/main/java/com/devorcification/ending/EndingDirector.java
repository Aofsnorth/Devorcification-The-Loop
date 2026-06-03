package com.devorcification.ending;

import com.devorcification.Devorcification;
import com.devorcification.cycle.CycleManager;
import com.devorcification.multiplayer.SocialGraphEngine;
import com.devorcification.multiplayer.AsymmetricStateManager;
import com.devorcification.pacing.MenaceGauge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EndingDirector {
    public enum Ending {
        ESCAPE,
        MERGE,
        SACRIFICE,
        INFINITE,
        DIVORCE;

        public static Ending fromString(String s) {
            if (s == null) return null;
            try { return valueOf(s.toUpperCase()); } catch (Exception e) { return null; }
        }
    }

    public static final Map<UUID, SessionStats> activeStats = new HashMap<>();
    public static final Map<UUID, String> votedFor = new HashMap<>();

    public static SessionStats ensureStats(ServerPlayerEntity p) {
        return activeStats.computeIfAbsent(p.getUuid(), id -> new SessionStats(id, p.getName().getString()));
    }

    public static Ending evaluate(MinecraftServer server, ServerPlayerEntity player, int cycle) {
        if (server == null || player == null) return null;

        if (player.getHealth() <= 0 || cycle == Integer.MAX_VALUE) {
            return Ending.INFINITE;
        }

        if (cycle >= 10 && player.getHealth() > 10.0f && !votedFor.containsKey(player.getUuid())) {
            return Ending.ESCAPE;
        }

        int desync = AsymmetricStateManager.getDesyncLevel(player.getUuid());
        if (desync > 90) {
            return Ending.MERGE;
        }

        if (server.getPlayerManager().getPlayerList().size() > 1) {
            boolean allZeroTrust = true;
            for (var other : server.getPlayerManager().getPlayerList()) {
                if (other.getUuid().equals(player.getUuid())) continue;
                double t = SocialGraphEngine.getTrust(player.getUuid(), other.getUuid());
                if (t > -50.0) { allZeroTrust = false; break; }
            }
            if (allZeroTrust && cycle >= 6) {
                return Ending.DIVORCE;
            }
            if (votedFor.containsKey(player.getUuid())) {
                return Ending.SACRIFICE;
            }
        }

        if (MenaceGauge.getIntensity(player.getUuid()) == MenaceGauge.Intensity.PANIC && cycle >= 9) {
            return Ending.INFINITE;
        }

        return null;
    }

    public static void recordVote(UUID player, String choice) {
        votedFor.put(player, choice);
        Devorcification.LOGGER.info("[Devorcification Ending] Vote recorded from {} = {}", player, choice);
    }

    public static void forceEnding(ServerPlayerEntity player, Ending ending) {
        SessionStats stats = ensureStats(player);
        stats.totalCycles = CycleManager.getCycle(player.getUuid());
        stats.end(ending.name());
        EndingSequence.execute(player, ending);
    }
}
