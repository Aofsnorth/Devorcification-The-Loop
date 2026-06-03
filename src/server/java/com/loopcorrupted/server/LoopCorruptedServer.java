package com.loopcorrupted.server;

import com.loopcorrupted.LoopCorruptedMod;
import net.fabricmc.api.DedicatedServerModInitializer;

public class LoopCorruptedServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        LoopCorruptedMod.LOGGER.info("[{}] Dedicated server entry point initialized.", LoopCorruptedMod.MOD_ID);
    }
}
