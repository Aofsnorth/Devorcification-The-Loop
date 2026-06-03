package com.devorcification.pacing;

import com.devorcification.Devorcification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenaceGauge {
    public enum EventType {
        SUBTLE(0),
        AUDIO_HINT(1),
        LIGHT_FLICKER(1),
        PERIPHERAL_SPAWN(2),
        BLOCK_CORRUPTION(2),
        FAKE_CHAT(2),
        FAKE_ENTITY(3),
        HUNT(3),
        INVENTORY_CORRUPTION(3),
        REALITY_BREAK(3);

        public final int cost;
        EventType(int cost) { this.cost = cost; }
    }

    public enum Intensity {
        CALM(0),
        UNEASE(1),
        TENSION(2),
        DREAD(3),
        PANIC(4);

        public final int level;
        Intensity(int level) { this.level = level; }
    }

    public static final int BUDGET_PER_WINDOW = 8;
    public static final long WINDOW_MS = 10L * 60L * 1000L;
    public static final int MANDATORY_COOLDOWN_SEC = 120;
    public static final int INTENSE_THRESHOLD = 3;
    public static final int INTENSE_WINDOW_SEC = 300;

    private static final Map<UUID, Integer> budgetSpent = new HashMap<>();
    private static final Map<UUID, Integer> intensityLevel = new HashMap<>();
    private static final Map<UUID, Long> lastIntenseEventMs = new HashMap<>();
    private static final Map<UUID, Long> cooldownUntilMs = new HashMap<>();
    private static final Map<UUID, Integer> recentIntense = new HashMap<>();
    private static final Map<UUID, Long> windowStartMs = new HashMap<>();

    public static void tick() {
        long now = System.currentTimeMillis();
        for (UUID id : windowStartMs.keySet()) {
            Long start = windowStartMs.get(id);
            if (start != null && now - start > WINDOW_MS) {
                budgetSpent.put(id, 0);
                windowStartMs.put(id, now);
                Devorcification.LOGGER.info("[Devorcification Pacing] Budget window reset for {}", id);
            }
            if (isInCooldown(id) && now > cooldownUntilMs.getOrDefault(id, 0L)) {
                cooldownUntilMs.remove(id);
                Devorcification.LOGGER.info("[Devorcification Pacing] Cooldown ended for {}", id);
            }
        }
    }

    public static boolean canAfford(UUID player, EventType type) {
        if (isInCooldown(player)) {
            return type == EventType.SUBTLE || type == EventType.AUDIO_HINT || type == EventType.LIGHT_FLICKER;
        }
        int spent = budgetSpent.getOrDefault(player, 0);
        return spent + type.cost <= BUDGET_PER_WINDOW;
    }

    public static boolean spend(UUID player, EventType type) {
        if (!canAfford(player, type)) {
            Devorcification.LOGGER.debug("[Devorcification Pacing] Cannot afford {} for {}", type, player);
            return false;
        }
        budgetSpent.merge(player, type.cost, Integer::sum);
        windowStartMs.putIfAbsent(player, System.currentTimeMillis());
        if (type.cost >= 2) {
            lastIntenseEventMs.put(player, System.currentTimeMillis());
            recentIntense.merge(player, 1, Integer::sum);
            if (recentIntense.get(player) >= INTENSE_THRESHOLD) {
                forceCooldown(player, MANDATORY_COOLDOWN_SEC);
                recentIntense.put(player, 0);
            }
        }
        Devorcification.LOGGER.info("[Devorcification Pacing] Spent {} menace for {} (total {}/{})",
            type.cost, player, budgetSpent.get(player), BUDGET_PER_WINDOW);
        return true;
    }

    public static void forceCooldown(UUID player, int seconds) {
        cooldownUntilMs.put(player, System.currentTimeMillis() + seconds * 1000L);
        Devorcification.LOGGER.info("[Devorcification Pacing] Cooldown for {} = {}s", player, seconds);
    }

    public static boolean isInCooldown(UUID player) {
        long until = cooldownUntilMs.getOrDefault(player, 0L);
        return System.currentTimeMillis() < until;
    }

    public static Intensity getIntensity(UUID player) {
        int level = intensityLevel.getOrDefault(player, 0);
        if (level <= 0) return Intensity.CALM;
        if (level == 1) return Intensity.UNEASE;
        if (level == 2) return Intensity.TENSION;
        if (level == 3) return Intensity.DREAD;
        return Intensity.PANIC;
    }

    public static void setIntensity(UUID player, Intensity i) {
        intensityLevel.put(player, i.level);
    }

    public static int getIntensityForCycle(int cycle) {
        if (cycle <= 2) return Intensity.CALM.level;
        if (cycle <= 5) return Intensity.UNEASE.level;
        if (cycle <= 8) return Intensity.TENSION.level;
        return Intensity.DREAD.level;
    }

    public static int remainingBudget(UUID player) {
        return BUDGET_PER_WINDOW - budgetSpent.getOrDefault(player, 0);
    }

    public static long getLastIntenseEventMs(UUID player) {
        return lastIntenseEventMs.getOrDefault(player, 0L);
    }
}
