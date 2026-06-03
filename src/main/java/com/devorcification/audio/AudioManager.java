package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AudioManager {
    public static final Map<UUID, Long> lastEventAtMs = new HashMap<>();
    public static final Map<UUID, ActiveSound> active = new HashMap<>();
    public static final int SILENCE_THRESHOLD_MS = 5000;
    public static final long SILENCE_FLAG_COOLDOWN_MS = 30000;

    public static long lastSilenceFlagAtMs = 0;

    public static void register() {
        PayloadTypeRegistry.playC2S().register(DirectedAudioPayload.ID, DirectedAudioPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DirectedAudioPayload.ID, DirectedAudioPayload.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerPlayNetworking.registerGlobalReceiver(DirectedAudioPayload.ID, (payload, context) -> {
                ServerPlayerEntity sender = context.player();
                Devorcification.LOGGER.info("[Devorcification Audio] Received directed audio server-side from {}", sender.getName().getString());
            });
        } else {
            ClientPlayNetworking.registerGlobalReceiver(DirectedAudioPayload.ID, (payload, context) -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null) return;
                if (!client.player.getUuid().equals(payload.targetUuid())) return;
                client.execute(() -> playClientSide(payload));
            });
        }

        ServerTickEvents.END_SERVER_TICK.register(AudioManager::tick);
    }

    private static void playClientSide(DirectedAudioPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        var registry = Registries.SOUND_EVENT.get(payload.soundId());
        if (registry.isEmpty()) return;
        SoundEvent sound = registry.get().value();

        if (payload.bypassDistance()) {
            client.getSoundManager().play(new net.minecraft.client.sound.PositionedSoundInstance(
                sound,
                net.minecraft.client.sound.SoundCategory.PLAYERS,
                payload.volume(),
                payload.pitch(),
                false,
                0,
                net.minecraft.client.sound.SoundInstance.AttenuationType.NONE,
                0.0, 0.0, 0.0,
                false
            ));
        } else {
            client.getSoundManager().play(new net.minecraft.client.sound.PositionedSoundInstance(
                sound,
                net.minecraft.client.sound.SoundCategory.PLAYERS,
                payload.volume(),
                payload.pitch(),
                false,
                0,
                net.minecraft.client.sound.SoundInstance.AttenuationType.LINEAR,
                (float) payload.origin().getX(),
                (float) payload.origin().getY(),
                (float) payload.origin().getZ(),
                false
            ));
        }

        if (payload.whisperSubtitle() && !payload.subtitleText().isEmpty()) {
            client.inGameHud.setOverlayMessage(Text.literal(payload.subtitleText()), false);
        }

        lastEventAtMs.put(client.player.getUuid(), System.currentTimeMillis());
    }

    public static void playDirectedSound(ServerPlayerEntity target, SoundEvent sound, BlockPos pos, float volume, float pitch) {
        if (target == null || sound == null) return;
        ServerWorld world = target.getServerWorld();
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
