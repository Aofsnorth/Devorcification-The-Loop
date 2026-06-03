package com.devorcification.server;

import com.devorcification.Devorcification;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DevorcificationServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Devorcification.LOGGER.info("[Devorcification: The Loop] Server initialized");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Devorcification.LOGGER.info("[Devorcification: The Loop] Server stopped");
        });
    }
}
