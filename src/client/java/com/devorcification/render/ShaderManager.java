package com.devorcification.render;

public class ShaderManager {
    public static volatile float vignetteStrength = 0.3f;
    public static volatile float chromaticAmount = 0.005f;
    public static volatile float grainIntensity = 0.08f;
    public static volatile float fogDensity = 0.4f;
    public static volatile float vhsIntensity = 0.1f;
    public static volatile float shaderIntensity = 0.0f;
    public static volatile float heartbeatBpm = 60.0f;
    public static volatile float masterIntensity = 0.0f;
    public static volatile long startMs = System.currentTimeMillis();

    public static float time() {
        return (System.currentTimeMillis() - startMs) / 1000.0f;
    }

    public static void setIntensity(float intensity) {
        masterIntensity = Math.max(0.0f, Math.min(1.0f, intensity));
        shaderIntensity = masterIntensity;
    }

    public static void pulseHeartbeat(float bpm) {
        heartbeatBpm = bpm;
    }

    public static float effectiveVignette() {
        float pulse = 0.8f + 0.2f * (float) Math.sin(time() * Math.PI * (heartbeatBpm / 60.0));
        return vignetteStrength * masterIntensity * pulse;
    }

    public static float effectiveChromatic() {
        return chromaticAmount * masterIntensity;
    }

    public static float effectiveGrain() {
        return grainIntensity * masterIntensity;
    }

    public static float effectiveFog() {
        return fogDensity * masterIntensity;
    }

    public static float effectiveVhs() {
        return vhsIntensity * masterIntensity;
    }

    public static boolean enabled() {
        return masterIntensity > 0.001f;
    }
}
