package com.devorcification.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class DevorcificationConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final double MENACE_MIN = 0.5;
    private static final double MENACE_MAX = 2.0;

    private static DevorcificationConfig INSTANCE;

    private boolean enableAIDirector = true;
    private String aiBackendUrl = "http://localhost:8000";
    private double menaceBudgetMultiplier = 1.0;
    private boolean enableAsymmetricMultiplayer = true;
    private boolean enableCustomShaders = true;

    public static DevorcificationConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("devorcification.json");
    }

    public static synchronized void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            INSTANCE = new DevorcificationConfig();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            INSTANCE = GSON.fromJson(reader, DevorcificationConfig.class);
            if (INSTANCE == null) {
                INSTANCE = new DevorcificationConfig();
            }
        } catch (IOException e) {
            com.devorcification.Devorcification.LOGGER.warn("[Devorcification] Failed to read config, regenerating defaults: {}", e.getMessage());
            INSTANCE = new DevorcificationConfig();
            save();
        }
        INSTANCE.validate();
    }

    public static synchronized void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            com.devorcification.Devorcification.LOGGER.error("[Devorcification] Failed to write config: {}", e.getMessage());
        }
    }

    public void validate() {
        if (menaceBudgetMultiplier < MENACE_MIN) menaceBudgetMultiplier = MENACE_MIN;
        if (menaceBudgetMultiplier > MENACE_MAX) menaceBudgetMultiplier = MENACE_MAX;
        if (aiBackendUrl == null || aiBackendUrl.isBlank()) aiBackendUrl = "http://localhost:8000";
    }

    public boolean isEnableAIDirector() { return enableAIDirector; }
    public void setEnableAIDirector(boolean v) { this.enableAIDirector = v; }

    public String getAiBackendUrl() { return aiBackendUrl; }
    public void setAiBackendUrl(String v) { this.aiBackendUrl = v; }

    public double getMenaceBudgetMultiplier() { return menaceBudgetMultiplier; }
    public void setMenaceBudgetMultiplier(double v) {
        this.menaceBudgetMultiplier = Math.max(MENACE_MIN, Math.min(MENACE_MAX, v));
    }

    public boolean isEnableAsymmetricMultiplayer() { return enableAsymmetricMultiplayer; }
    public void setEnableAsymmetricMultiplayer(boolean v) { this.enableAsymmetricMultiplayer = v; }

    public boolean isEnableCustomShaders() { return enableCustomShaders; }
    public void setEnableCustomShaders(boolean v) { this.enableCustomShaders = v; }
}
