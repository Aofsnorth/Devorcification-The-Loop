package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.UUID;

public class AsymmetricAudioEngine {
    public static void playAsymmetricSound(MinecraftServer server, Set<UUID> targets, SoundEvent sound, BlockPos pos, float volume, float pitch, boolean bypass) {
        if (server == null || targets == null || targets.isEmpty()) return;
        int sent = 0;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!targets.contains(p.getUuid())) continue;
            if (bypass) {
                AudioManager.playBypassedSound(p, sound, volume, pitch);
            } else {
                AudioManager.playDirectedSound(p, sound, pos, volume, pitch);
            }
            sent++;
        }
        Devorcification.LOGGER.info("[Devorcification Audio] Asymmetric sound {} sent to {} of {} players",
            sound.getId(), sent, targets.size());
    }
}
