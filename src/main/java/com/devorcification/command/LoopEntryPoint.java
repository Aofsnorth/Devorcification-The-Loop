package com.devorcification.command;

import com.devorcification.cycle.CycleManager;
import com.devorcification.ending.EndingDirector;
import com.devorcification.ending.EndingSequence;
import com.devorcification.pacing.FalseReliefEvent;
import com.devorcification.pacing.MenaceGauge;
import com.devorcification.world.LoopDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
import static net.minecraft.server.command.CommandManager.argument;

public class LoopEntryPoint {
    private static final BlockPos END_PLATE_POS = new BlockPos(1, 61, 59);

    private static final SuggestionProvider<ServerCommandSource> ENDING_SUGGESTER = (context, builder) -> {
        for (EndingDirector.Ending e : EndingDirector.Ending.values()) {
            builder.suggest(e.name().toLowerCase());
        }
        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register(LoopEntryPoint::registerCommand);
        ServerTickEvents.START_SERVER_TICK.register(LoopEntryPoint::onServerTick);
    }

    private static void onServerTick(net.minecraft.server.MinecraftServer server) {
        CycleManager.tickCooldowns(server);
        MenaceGauge.tick();

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
                .executes(LoopEntryPoint::executeEnter))
            .then(literal("force_ending")
                .then(argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests(ENDING_SUGGESTER)
                    .executes(LoopEntryPoint::executeForceEnding)))
            .then(literal("false_relief")
                .then(argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, b) -> {
                        for (FalseReliefEvent.Type t : FalseReliefEvent.Type.values()) {
                            b.suggest(t.name().toLowerCase());
                        }
                        return b.buildFuture();
                    })
                    .executes(LoopEntryPoint::executeFalseRelief)))
            .then(literal("stats")
                .executes(LoopEntryPoint::executeStats)));
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

    private static int executeForceEnding(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(net.minecraft.text.Text.literal("/loop force_ending must be run as a player."));
            return 0;
        }
        String typeStr = ctx.getArgument("type", String.class).toUpperCase();
        EndingDirector.Ending ending = EndingDirector.Ending.fromString(typeStr);
        if (ending == null) {
            source.sendError(net.minecraft.text.Text.literal("Unknown ending: " + typeStr));
            return 0;
        }
        EndingDirector.forceEnding(player, ending);
        source.sendFeedback(() -> net.minecraft.text.Text.literal("Forced ending: " + ending.name()), false);
        return 1;
    }

    private static int executeFalseRelief(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(net.minecraft.text.Text.literal("/loop false_relief must be run as a player."));
            return 0;
        }
        String typeStr = ctx.getArgument("type", String.class).toUpperCase();
        try {
            FalseReliefEvent.Type type = FalseReliefEvent.Type.valueOf(typeStr);
            FalseReliefEvent.execute(player.getServer(), player, type);
            source.sendFeedback(() -> net.minecraft.text.Text.literal("False relief: " + type.name()), false);
            return 1;
        } catch (Exception e) {
            source.sendError(net.minecraft.text.Text.literal("Unknown type: " + typeStr));
            return 0;
        }
    }

    private static int executeStats(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            return 0;
        }
        var stats = EndingDirector.ensureStats(player);
        source.sendFeedback(() -> net.minecraft.text.Text.literal(stats.summary()), false);
        return 1;
    }
}
