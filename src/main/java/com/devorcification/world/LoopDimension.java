package com.devorcification.world;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class LoopDimension {
    public static final RegistryKey<DimensionOptions> DIMENSION_KEY = RegistryKey.of(
        RegistryKeys.WORLD, new Identifier(Devorcification.MOD_ID, "the_loop"));
    public static final RegistryKey<World> WORLD_KEY = RegistryKey.of(
        RegistryKeys.WORLD, new Identifier(Devorcification.MOD_ID, "the_loop"));
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_KEY = RegistryKey.of(
        RegistryKeys.DIMENSION_TYPE, new Identifier(Devorcification.MOD_ID, "the_loop"));

    public static final int FLOOR_Y = 60;
    public static final int VOID_Y = 50;

    public static void bootstrap() {
        ServerLifecycleEvents.SERVER_STARTED.register(LoopDimension::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        if (server.getWorld(WORLD_KEY) == null) {
            Devorcification.LOGGER.warn(
                "[Devorcification] The Loop dimension not loaded. Add it to your world's dat/level.dat dimension list. Falling back to overworld for /loop enter.");
        }
    }

    public static RegistryKey<DimensionType> getDimensionTypeKey() {
        return DIMENSION_TYPE_KEY;
    }

    public static ServerWorld getLoopWorld(MinecraftServer server) {
        ServerWorld loop = server.getWorld(WORLD_KEY);
        if (loop != null) return loop;
        return server.getWorld(World.OVERWORLD);
    }

    public static boolean isLoopDimension(World world) {
        return world != null && world.getRegistryKey() == WORLD_KEY;
    }
}
