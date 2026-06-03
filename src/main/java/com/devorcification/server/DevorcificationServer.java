package com.devorcification.server;

import com.devorcification.Devorcification;
import com.devorcification.config.DevorcificationConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DevorcificationServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Devorcification.LOGGER.info("[Devorcification: The Loop] Server initialized");
            Devorcification.LOGGER.info(
                "[Devorcification: The Loop] Config: ai_director={}, ai_backend_url={}, menace_budget={}, asymmetric={}, shaders={}, corridor_length={}, max_players={}, ai_api_key={}",
                DevorcificationConfig.enableAIDirector,
                DevorcificationConfig.aiBackendUrl,
                DevorcificationConfig.menaceBudgetMultiplier,
                DevorcificationConfig.enableAsymmetricMultiplayer,
                DevorcificationConfig.enableCustomShaders,
                DevorcificationConfig.loopCorridorLength,
                DevorcificationConfig.maxPlayersPerSession,
                DevorcificationConfig.getRedactedApiKey()
            );
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Devorcification.LOGGER.info("[Devorcification: The Loop] Server stopped");
        });
    }
}
