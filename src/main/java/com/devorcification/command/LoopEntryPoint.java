package com.devorcification.command;

import com.devorcification.cycle.CycleManager;
import com.devorcification.world.LoopDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.literal;

public class LoopEntryPoint {
    private static final BlockPos END_PLATE_POS = new BlockPos(1, 61, 59);

    public static void register() {
        CommandRegistrationCallback.EVENT.register(LoopEntryPoint::registerCommand);
        ServerTickEvents.START_SERVER_TICK.register(LoopEntryPoint::onServerTick);
    }

    private static void onServerTick(net.minecraft.server.MinecraftServer server) {
        CycleManager.tickCooldowns(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!LoopDimension.isLoopDimension(player.getWorld())) continue;
            if (CycleManager.isEndDoorCooldown(player.getUuid())) continue;
            BlockPos feet = player.getBlockPos();
            if (feet.equals(END_PLATE_POS)) {
                if (player.getWorld().getBlockState(END_PLATE_POS).isOf(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)) {
                    CycleManager.onCycleComplete(player);
                }
            }
        }

        ServerWorld loop = LoopDimension.getLoopWorld(server);
        if (loop != null) {
            for (net.minecraft.entity.Entity e : loop.getEntityLookup().iterate()) {
                if (e instanceof net.minecraft.entity.projectile.thrown.EnderPearlEntity pearl) {
                    if (pearl.getOwner() instanceof ServerPlayerEntity owner
                        && LoopDimension.isLoopDimension(owner.getWorld())) {
                        pearl.discard();
                    }
                }
            }
        }
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("loop")
            .then(literal("enter")
                .executes(LoopEntryPoint::executeEnter)));
    }

    private static int executeEnter(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(net.minecraft.text.Text.literal("/loop enter must be run as a player."));
            return 0;
        }
        ServerWorld loopWorld = LoopDimension.getLoopWorld(player.getServer());
        if (loopWorld == null) {
            source.sendError(net.minecraft.text.Text.literal("The Loop dimension is not available."));
            return 0;
        }
        CycleManager.onPlayerEnterLoop(player);
        source.sendFeedback(() -> net.minecraft.text.Text.literal("Entering The Loop..."), false);
        return 1;
    }
}
