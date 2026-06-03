package com.loopcorrupted.client;

import com.loopcorrupted.LoopCorruptedMod;
import net.fabricmc.api.ClientModInitializer;

public class LoopCorruptedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LoopCorruptedMod.LOGGER.info("[{}] Client entry point initialized.", LoopCorruptedMod.MOD_ID);
    }
}
