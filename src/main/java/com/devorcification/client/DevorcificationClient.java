package com.devorcification.client;

import com.devorcification.Devorcification;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class DevorcificationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Devorcification.LOGGER.info("[Devorcification: The Loop] Client ready");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // TODO: per-tick client logic placeholder
        });
    }
}
