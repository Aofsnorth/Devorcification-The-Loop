package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SocialGraphEngine {
    public static final Map<UUID, Map<UUID, Double>> proximity = new HashMap<>();
    public static final Map<UUID, Integer> voiceActivity = new HashMap<>();
    public static final Map<String, Double> trustIndex = new HashMap<>();
    public static final Set<UUID> isolated = new HashSet<>();
    public static final Map<UUID, Long> lastProximityUpdate = new HashMap<>();

    private static int tickAccumulator = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SocialGraphEngine::onServerTick);
    }

    public static void onChatMessage(UUID speaker, String text) {
        voiceActivity.merge(speaker, 1, Integer::sum);
        if (text != null) {
            String lower = text.toLowerCase();
            if (lower.contains("lihat") || lower.contains("see") || lower.contains("ada?") || lower.contains("really")) {
                for (UUID other : voiceActivity.keySet()) {
                    if (other.equals(speaker)) continue;
                    String key = pairKey(speaker, other);
                    trustIndex.merge(key, -5.0, Double::sum);
                }
            }
        }
    }

    private static void onServerTick(MinecraftServer server) {
        tickAccumulator++;
        if (tickAccumulator < 100) return;
        tickAccumulator = 0;
        long now = System.currentTimeMillis();

        var players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity p : players) {
            UUID id = p.getUuid();
            Vec3d pos = p.getPos();
            isolated.remove(id);
            int nearCount = 0;
            for (ServerPlayerEntity other : players) {
                if (other.getUuid().equals(id)) continue;
                double dist = pos.distanceTo(other.getPos());
                Map<UUID, Double> prox = proximity.computeIfAbsent(id, k -> new HashMap<>());
                prox.put(other.getUuid(), dist);
                if (dist < 8.0) nearCount++;
            }
            if (nearCount == 0 && lastProximityUpdate.containsKey(id)) {
                Long last = lastProximityUpdate.get(id);
                if (last != null && now - last > 10000L) {
                    isolated.add(id);
                }
            }
            if (nearCount > 0) {
                lastProximityUpdate.put(id, now);
            }
        }
        Devorcification.LOGGER.info("[Devorcification Multiplayer] Social graph: {} players, {} isolated, {} trust pairs",
            players.size(), isolated.size(), trustIndex.size());
    }

    public static void exploitWeakestLink(MinecraftServer server) {
        String weakest = null;
        double lowest = 0.0;
        for (var e : trustIndex.entrySet()) {
            if (e.getValue() < lowest) {
                lowest = e.getValue();
                weakest = e.getKey();
            }
        }
        if (weakest != null && lowest < -10.0) {
            String[] parts = weakest.split("_");
            if (parts.length == 2) {
                try {
                    UUID a = UUID.fromString(parts[0]);
                    UUID b = UUID.fromString(parts[1]);
                    AsymmetricStateManager.queueFakeChat(a, b.toString(), "I'm not sure I trust you anymore.");
                    AsymmetricStateManager.queueFakeChat(b, a.toString(), "Did you see that? It moved when I wasn't looking.");
                    Devorcification.LOGGER.info("[Devorcification Multiplayer] Social sabotage on weakest link {}", weakest);
                } catch (Exception ignored) {}
            }
        }
    }

    public static String pairKey(UUID a, UUID b) {
        if (a.compareTo(b) < 0) return a + "_" + b;
        return b + "_" + a;
    }

    public static double getTrust(UUID a, UUID b) {
        return trustIndex.getOrDefault(pairKey(a, b), 0.0);
    }
}
