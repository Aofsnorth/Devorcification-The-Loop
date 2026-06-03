package com.devorcification.pacing;

import com.devorcification.Devorcification;

import java.util.UUID;

public class PacingDirector {
    public enum Act {
        DESCENT(0, 2, 3, "DESCENT", "Profiling fear type, build unease"),
        HAUNTING(3, 5, 6, "HAUNTING", "Confirm 'something is wrong'"),
        BREAKDOWN(6, 8, 8, "BREAKDOWN", "Reality unreliable, trust breaks"),
        CLIMAX(9, Integer.MAX_VALUE, Integer.MAX_VALUE, "CLIMAX", "Catharsis or existential dread");

        public final int minCycle;
        public final int maxCycle;
        public final int budget;
        public final String label;
        public final String goal;

        Act(int min, int max, int budget, String label, String goal) {
            this.minCycle = min;
            this.maxCycle = max;
            this.budget = budget;
            this.label = label;
            this.goal = goal;
        }

        public boolean contains(int cycle) {
            return cycle >= minCycle && cycle <= maxCycle;
        }
    }

    public static Act getCurrentAct(int cycle) {
        for (Act a : Act.values()) {
            if (a.contains(cycle)) return a;
        }
        return Act.DESCENT;
    }

    public static int getBudgetForAct(Act act) {
        return act.budget;
    }

    public static boolean isIntenseEventAllowed(Act act) {
        return act != Act.DESCENT;
    }

    public static boolean shouldTriggerFalseRelief(UUID player, int cycle, long lastIntenseAtMs) {
        long now = System.currentTimeMillis();
        if (cycle < 3) return false;
        if (lastIntenseAtMs == 0) return false;
        long timeSinceIntense = now - lastIntenseAtMs;
        if (timeSinceIntense < 60000L) return false;
        if (MenaceGauge.isInCooldown(player)) return false;
        if (MenaceGauge.getIntensity(player).level >= 3) return Math.random() < 0.4;
        return Math.random() < 0.2;
    }

    public static String logAct(Act act) {
        Devorcification.LOGGER.info("[Devorcification Pacing] Act {} (cycles {}-{}, budget={}): {}",
            act.label, act.minCycle, act.maxCycle, act.budget, act.goal);
        return act.label;
    }
}
