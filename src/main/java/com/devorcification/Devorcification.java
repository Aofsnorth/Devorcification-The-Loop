package com.devorcification;

import com.devorcification.config.DevorcificationConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Devorcification implements ModInitializer {
    public static final String MOD_ID = "devorcification";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Devorcification: The Loop] Initialized");
        DevorcificationConfig.load();
        LOGGER.info("[Devorcification: The Loop] Config loaded from {}", DevorcificationConfig.getConfigPath());
    }
}
