package com.devorcification.ending;

import com.devorcification.Devorcification;

import java.util.UUID;

public class SessionStats {
    public UUID playerUuid;
    public String playerName;
    public int totalCycles;
    public long totalTimeInLoopMs;
    public long startedAtMs;
    public long endedAtMs;
    public String fearProfile;
    public String mostUsedTactic;
    public int freezeCount;
    public int backpedalCount;
    public int rapidLookCount;
    public int scareEffectivenessScore;
    public double trustIndexAverage;
    public String endingType;
    public boolean quitMidSession;
    public boolean reachedCycle10;

    public SessionStats() {}

    public SessionStats(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.startedAtMs = System.currentTimeMillis();
    }

    public void end(String endingType) {
        this.endedAtMs = System.currentTimeMillis();
        this.totalTimeInLoopMs = endedAtMs - startedAtMs;
        this.endingType = endingType;
        Devorcification.LOGGER.info("[Devorcification Ending] SessionStats for {}: cycles={} time={}ms fear={} ending={}",
            playerName, totalCycles, totalTimeInLoopMs, fearProfile, endingType);
    }

    public String summary() {
        return String.format("Cycles: %d | Time: %.1fs | Profile: %s | Tactic: %s | Scare score: %d | Ending: %s",
            totalCycles, totalTimeInLoopMs / 1000.0, fearProfile, mostUsedTactic, scareEffectivenessScore, endingType);
    }
}
