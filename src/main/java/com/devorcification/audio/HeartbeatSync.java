package com.devorcification.audio;

import com.devorcification.Devorcification;
import com.devorcification.render.ShaderManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class HeartbeatSync {
    public static final float BPM_NORMAL = 60.0f;
    public static final float BPM_ANXIETY = 80.0f;
    public static final float BPM_STRESS = 100.0f;
    public static final float BPM_PANIC = 120.0f;
    public static final float BPM_TERROR = 140.0f;

    public static void syncHeartbeat(ServerPlayerEntity target, float bpm) {
        ShaderManager.pulseHeartbeat(bpm);
        if (target != null) {
            AudioManager.playHeartbeat(target, bpm);
        }
        Devorcification.LOGGER.info("[Devorcification Heartbeat] synced BPM={} target={}", bpm,
            target == null ? "global" : target.getName().getString());
    }

    public static void fadeToNormal(ServerPlayerEntity target, float targetBpm, float durationSec) {
        new Thread(() -> {
            float start = ShaderManager.heartbeatBpm;
            long end = System.currentTimeMillis() + (long) (durationSec * 1000L);
            float startMs = System.currentTimeMillis();
            while (System.currentTimeMillis() < end) {
                float t = (System.currentTimeMillis() - startMs) / (durationSec * 1000L);
                float bpm = start + (targetBpm - start) * t;
                ShaderManager.pulseHeartbeat(bpm);
                if (target != null && target.isAlive()) {
                    AudioManager.playHeartbeat(target, bpm);
                }
                try { Thread.sleep(200L); } catch (InterruptedException e) { break; }
            }
            ShaderManager.pulseHeartbeat(targetBpm);
        }, "devorcification-heartbeat-fade").start();
    }

    public static void bpmForCycle(int cycle) {
        if (cycle < 3) syncHeartbeat(null, BPM_NORMAL);
        else if (cycle < 5) syncHeartbeat(null, BPM_ANXIETY);
        else if (cycle < 8) syncHeartbeat(null, BPM_STRESS);
        else syncHeartbeat(null, BPM_PANIC);
    }
}
