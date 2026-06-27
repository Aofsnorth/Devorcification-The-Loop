package com.devorcification.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class DevorcificationConfig extends MidnightConfig {
    public static final String GENERAL = "general";

    @Entry(category = GENERAL, name = "enable_ai_director")
    public static boolean enableAIDirector = true;

    @Entry(category = GENERAL, name = "ai_backend_url")
    public static String aiBackendUrl = "http://localhost:8000/director/plan";

    @Hidden
    @Entry(category = GENERAL, name = "ai_api_key")
    public static String aiApiKey = "";

    @Entry(category = GENERAL, name = "menace_budget_multiplier")
    public static double menaceBudgetMultiplier = 1.0;

    @Entry(category = GENERAL, name = "enable_asymmetric_multiplayer")
    public static boolean enableAsymmetricMultiplayer = true;

    @Entry(category = GENERAL, name = "enable_custom_shaders")
    public static boolean enableCustomShaders = true;

    @Entry(category = GENERAL, name = "loop_corridor_length")
    public static int loopCorridorLength = 60;

    @Entry(category = GENERAL, name = "max_players_per_session")
    public static int maxPlayersPerSession = 4;

    public static String getRedactedApiKey() {
        return (aiApiKey == null || aiApiKey.isEmpty()) ? "[NOT_SET]" : "[REDACTED]";
    }
}
