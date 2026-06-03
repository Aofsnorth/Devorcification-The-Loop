package com.devorcification.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class DevorcificationConfig extends MidnightConfig {
    @Comment(section = "devorcification")
    public static final String SECTION_GENERAL = "general";

    @Entry(section = SECTION_GENERAL, name = "enable_ai_director")
    public static boolean enableAIDirector = true;

    @Entry(section = SECTION_GENERAL, name = "ai_backend_url")
    public static String aiBackendUrl = "http://localhost:8000/director/plan";

    @Entry(section = SECTION_GENERAL, name = "ai_api_key", isSecret = true)
    public static String aiApiKey = "";

    @Entry(section = SECTION_GENERAL, name = "menace_budget_multiplier")
    public static double menaceBudgetMultiplier = 1.0;

    @Entry(section = SECTION_GENERAL, name = "enable_asymmetric_multiplayer")
    public static boolean enableAsymmetricMultiplayer = true;

    @Entry(section = SECTION_GENERAL, name = "enable_custom_shaders")
    public static boolean enableCustomShaders = true;

    @Entry(section = SECTION_GENERAL, name = "loop_corridor_length")
    public static int loopCorridorLength = 60;

    @Entry(section = SECTION_GENERAL, name = "max_players_per_session")
    public static int maxPlayersPerSession = 4;

    public static String getRedactedApiKey() {
        return (aiApiKey == null || aiApiKey.isEmpty()) ? "[NOT_SET]" : "[REDACTED]";
    }
}
