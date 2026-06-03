package com.devorcification;

import com.devorcification.command.LoopEntryPoint;
import com.devorcification.world.LoopDimension;
import com.devorcification.config.DevorcificationConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Devorcification implements ModInitializer {
    public static final String MOD_ID = "devorcification";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Devorcification: The Loop] Initialized");
        MidnightConfig.init(MOD_ID, DevorcificationConfig.class);

        LoopDimension.bootstrap();
        LoopEntryPoint.register();

        LOGGER.info("[Devorcification: The Loop] Core spatial system registered");
    }
}
