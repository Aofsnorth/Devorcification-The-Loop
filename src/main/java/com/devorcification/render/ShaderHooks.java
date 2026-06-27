package com.devorcification.render;

import com.devorcification.Devorcification;
import net.fabricmc.loader.api.FabricLoader;

public final class ShaderHooks {
    private ShaderHooks() {}

    public static void setIntensity(float intensity) {
        if (FabricLoader.getInstance().getEnvironmentType() != net.fabricmc.api.EnvType.CLIENT) return;
        try {
            Class<?> cls = Class.forName("com.devorcification.render.ShaderManager");
            cls.getMethod("setIntensity", float.class).invoke(null, intensity);
        } catch (Throwable t) {
            Devorcification.LOGGER.warn("[Devorcification] ShaderHooks.setIntensity dispatch failed: {}", t.getMessage());
        }
    }

    public static void pulseHeartbeat(float bpm) {
        if (FabricLoader.getInstance().getEnvironmentType() != net.fabricmc.api.EnvType.CLIENT) return;
        try {
            Class<?> cls = Class.forName("com.devorcification.render.ShaderManager");
            cls.getMethod("pulseHeartbeat", float.class).invoke(null, bpm);
        } catch (Throwable t) {
            Devorcification.LOGGER.warn("[Devorcification] ShaderHooks.pulseHeartbeat dispatch failed: {}", t.getMessage());
        }
    }

    public static float heartbeatBpm() {
        if (FabricLoader.getInstance().getEnvironmentType() != net.fabricmc.api.EnvType.CLIENT) return 60.0f;
        try {
            Class<?> cls = Class.forName("com.devorcification.render.ShaderManager");
            return ((Float) cls.getField("heartbeatBpm").get(null));
        } catch (Throwable t) {
            return 60.0f;
        }
    }
}
