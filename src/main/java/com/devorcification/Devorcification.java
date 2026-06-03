package com.devorcification;

import com.devorcification.ai.PlayerObserver;
import com.devorcification.audio.AudioManager;
import com.devorcification.audio.SoundEventRegistry;
import com.devorcification.command.LoopEntryPoint;
import com.devorcification.entity.ModEntityTypes;
import com.devorcification.entity.WatcherSpawnHandler;
import com.devorcification.world.LoopDimension;
import com.devorcification.config.DevorcificationConfig;
import com.devorcification.multiplayer.AsymmetricStateManager;
import com.devorcification.multiplayer.SocialGraphEngine;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Devorcification implements ModInitializer {
    public static final String MOD_ID = "devorcification";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static volatile MinecraftServer currentServer = null;

    public static ServerPlayerEntity findPlayer(UUID id) {
        MinecraftServer s = currentServer;
        if (s == null) return null;
        return s.getPlayerManager().getPlayer(id);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[Devorcification: The Loop] Initialized");
        MidnightConfig.init(MOD_ID, DevorcificationConfig.class);

        ServerLifecycleEvents.SERVER_STARTED.register(s -> currentServer = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> currentServer = null);

        SoundEventRegistry.register();
        AudioManager.register();
        MirrorBlockTag.register();
        ModEntityTypes.register();
        LoopDimension.bootstrap();
        PlayerObserver.register();
        WatcherSpawnHandler.register();
        AsymmetricStateManager.register();
        SocialGraphEngine.register();
        LoopEntryPoint.register();

        LOGGER.info("[Devorcification: The Loop] Core + entities + AI + shaders + audio + asymmetric multiplayer registered");
    }
}
