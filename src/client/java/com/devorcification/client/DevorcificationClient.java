package com.devorcification.client;

import com.devorcification.Devorcification;
import com.devorcification.audio.SoundEventRegistry;
import com.devorcification.entity.ModEntityTypes;
import com.devorcification.entity.WatcherRenderer;
import com.devorcification.multiplayer.AsymmetricClient;
import com.devorcification.render.ShaderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DevorcificationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Devorcification.LOGGER.info("[Devorcification: The Loop] Client ready");

        EntityRendererRegistry.register(ModEntityTypes.WATCHER, WatcherRenderer::new);
        AsymmetricClient.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ShaderManager.masterIntensity *= 0.985f;
            if (ShaderManager.masterIntensity < 0.01f) {
                ShaderManager.masterIntensity = 0.0f;
                ShaderManager.heartbeatBpm = 60.0f;
            }
        });
    }
}
