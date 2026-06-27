package com.devorcification.ending;

import com.devorcification.Devorcification;
import com.devorcification.audio.AudioManager;
import com.devorcification.audio.SoundEventRegistry;
import com.devorcification.cycle.CycleManager;
import com.devorcification.multiplayer.AsymmetricStateManager;
import com.devorcification.render.ShaderHooks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EndingSequence {
    public static void execute(ServerPlayerEntity player, EndingDirector.Ending ending) {
        if (player == null || ending == null) return;
        Devorcification.LOGGER.info("[Devorcification Ending] === ENDING: {} for {} ===",
            ending.name(), player.getName().getString());

        ShaderHooks.setIntensity(1.0f);
        AudioManager.playBypassedSound(player, SoundEventRegistry.WATCHER_HUNT, 0.8f, 0.6f);

        switch (ending) {
            case ESCAPE -> {
                player.sendMessage(Text.literal("").formatted(Formatting.GOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.GOLD), false);
                player.sendMessage(Text.literal("  THE ESCAPE").formatted(Formatting.YELLOW, Formatting.BOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.GOLD), false);
                player.sendMessage(Text.literal("You step through. The door closes behind you.").formatted(Formatting.GRAY), false);
                player.sendMessage(Text.literal(""), false);
                player.sendMessage(Text.literal("The next world loads.").formatted(Formatting.DARK_GRAY), false);
                player.sendMessage(Text.literal("...the corridor is still there, in the spawn chunks.").formatted(Formatting.DARK_RED, Formatting.ITALIC), false);
            }
            case MERGE -> {
                player.sendMessage(Text.literal("").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("  THE MERGE").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("Your reflection no longer matches.").formatted(Formatting.GRAY), false);
                player.sendMessage(Text.literal(""), false);
                player.sendMessage(Text.literal("You will host what hosts you.").formatted(Formatting.DARK_RED, Formatting.ITALIC), false);
                AsymmetricStateManager.applyDesync(player.getUuid(), 100);
            }
            case SACRIFICE -> {
                player.sendMessage(Text.literal("").formatted(Formatting.DARK_RED), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_RED), false);
                player.sendMessage(Text.literal("  THE SACRIFICE").formatted(Formatting.RED, Formatting.BOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_RED), false);
                player.sendMessage(Text.literal("One stays. The other walks free.").formatted(Formatting.GRAY), false);
                player.sendMessage(Text.literal("The free one believes they escaped.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
            }
            case INFINITE -> {
                player.sendMessage(Text.literal("").formatted(Formatting.BLACK), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_GRAY), false);
                player.sendMessage(Text.literal("  THE INFINITE").formatted(Formatting.GRAY, Formatting.BOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_GRAY), false);
                player.sendMessage(Text.literal("The credits roll.").formatted(Formatting.GRAY), false);
                player.sendMessage(Text.literal(""), false);
                player.sendMessage(Text.literal("Then cycle 0.").formatted(Formatting.RED, Formatting.ITALIC), false);
                player.sendMessage(Text.literal("Faster. Closer.").formatted(Formatting.DARK_RED, Formatting.ITALIC), false);
            }
            case DIVORCE -> {
                player.sendMessage(Text.literal("").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("  THE DIVORCE").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
                player.sendMessage(Text.literal("═══════════════════════").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("None of you are the real one.").formatted(Formatting.GRAY), false);
                player.sendMessage(Text.literal(""), false);
                player.sendMessage(Text.literal("Save file corrupted. Spawn point: ████. Inventory: ████.").formatted(Formatting.RED, Formatting.OBFUSCATED), false);
            }
        }

        SessionStats stats = EndingDirector.ensureStats(player);
        stats.totalCycles = CycleManager.getCycle(player.getUuid());
        stats.end(ending.name());
        player.sendMessage(Text.literal("").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("Session: " + stats.summary()).formatted(Formatting.DARK_GRAY), false);

        Devorcification.LOGGER.info("[Devorcification Ending] Sequence complete for {}", player.getName().getString());
    }
}
