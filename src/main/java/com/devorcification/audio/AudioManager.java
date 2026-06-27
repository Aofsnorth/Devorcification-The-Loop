package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AudioManager {
    public static final Map<UUID, Long> lastEventAtMs = new HashMap<>();
    public static final Map<UUID, ActiveSound> active = new HashMap<>();
    public static final int SILENCE_THRESHOLD_MS = 5000;
    public static final long SILENCE_FLAG_COOLDOWN_MS = 30000;

    public static long lastSilenceFlagAtMs = 0;

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(DirectedAudioPayload.TYPE, (payload, player, sender) -> {
            Devorcification.LOGGER.info("[Devorcification Audio] Received directed audio server-side from {}", player.getName().getString());
        });

        ServerTickEvents.END_SERVER_TICK.register(AudioManager::tick);
    }

    public static void playDirectedSound(ServerPlayerEntity target, SoundEvent sound, BlockPos pos, float volume, float pitch) {
        if (target == null || sound == null) return;
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (id == null) return;
        DirectedAudioPayload payload = new DirectedAudioPayload(
            target.getUuid(), id, pos, volume, pitch, false, false, "");
        ServerPlayNetworking.send(target, payload);
        lastEventAtMs.put(target.getUuid(), System.currentTimeMillis());
        Devorcification.LOGGER.info("[Devorcification Audio] Directed sound {} -> {} at {}",
            id, target.getName().getString(), pos);
    }

    public static void playBypassedSound(ServerPlayerEntity target, SoundEvent sound, float volume, float pitch) {
        if (target == null || sound == null) return;
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (id == null) return;
        DirectedAudioPayload payload = new DirectedAudioPayload(
            target.getUuid(), id, target.getBlockPos(), volume, pitch, true, false, "");
        ServerPlayNetworking.send(target, payload);
        lastEventAtMs.put(target.getUuid(), System.currentTimeMillis());
    }

    public static void playWhisper(ServerPlayerEntity target, String message) {
        if (target == null) return;
        Identifier id = Registries.SOUND_EVENT.getId(SoundEventRegistry.WHISPER_NAME);
        if (id == null) return;
        DirectedAudioPayload payload = new DirectedAudioPayload(
            target.getUuid(), id, target.getBlockPos(), 0.25f, 1.4f, true, true, message);
        ServerPlayNetworking.send(target, payload);
        lastEventAtMs.put(target.getUuid(), System.currentTimeMillis());
    }

    public static void playAmbientLoop(ServerPlayerEntity target, SoundEvent sound, BlockPos pos, float volume, float pitch) {
        playDirectedSound(target, sound, pos, volume, pitch);
    }

    public static void playHeartbeat(ServerPlayerEntity target, float bpm) {
        SoundEvent s = bpm < 80 ? SoundEventRegistry.HEARTBEAT_NORMAL
            : (bpm < 120 ? SoundEventRegistry.HEARTBEAT_FAST : SoundEventRegistry.HEARTBEAT_PANIC);
        float pitchScale = 0.8f + (bpm / 140.0f) * 0.4f;
        playBypassedSound(target, s, 0.6f, pitchScale);
    }

    public static void stopHeartbeat() {
    }

    public static void pitchShiftAmbient(int cycle) {
        float pitch = 1.0f;
        if (cycle >= 5) pitch = 0.7f;
        else if (cycle >= 3) pitch = 0.85f;
        Devorcification.LOGGER.info("[Devorcification Audio] Ambient pitch target = {} (cycle {})", pitch, cycle);
    }

    private static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Long last = lastEventAtMs.get(player.getUuid());
            if (last != null && now - last > SILENCE_THRESHOLD_MS) {
                if (now - lastSilenceFlagAtMs > SILENCE_FLAG_COOLDOWN_MS) {
                    Devorcification.LOGGER.info("[Devorcification Audio] Silence > {}s for player {}, flagging AI Director",
                        SILENCE_THRESHOLD_MS / 1000, player.getName().getString());
                    lastSilenceFlagAtMs = now;
                }
                lastEventAtMs.put(player.getUuid(), now);
            }
        }
    }

    public static class ActiveSound {
        public UUID id;
        public SoundEvent event;
        public BlockPos origin;
        public long startedAt;
    }
}
