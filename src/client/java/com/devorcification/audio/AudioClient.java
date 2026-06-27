package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class AudioClient {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(DirectedAudioPayload.TYPE, (payload, player, sender) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (!client.player.getUuid().equals(payload.targetUuid())) return;
            client.execute(() -> playClientSide(payload));
        });
    }

    private static void playClientSide(DirectedAudioPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundEvent sound = Registries.SOUND_EVENT.get(payload.soundId());
        if (sound == null) return;
        Random random = client.world != null ? client.world.getRandom() : Random.create(0L);

        if (payload.bypassDistance()) {
            client.getSoundManager().play(new PositionedSoundInstance(
                sound,
                SoundCategory.PLAYERS,
                payload.volume(),
                payload.pitch(),
                random,
                client.player.getBlockPos()
            ));
        } else {
            Identifier soundId = Registries.SOUND_EVENT.getId(sound);
            if (soundId == null) return;
            client.getSoundManager().play(new PositionedSoundInstance(
                soundId,
                SoundCategory.PLAYERS,
                payload.volume(),
                payload.pitch(),
                random,
                false,
                0,
                SoundInstance.AttenuationType.LINEAR,
                payload.origin().getX(),
                payload.origin().getY(),
                payload.origin().getZ(),
                false
            ));
        }

        if (payload.whisperSubtitle() && !payload.subtitleText().isEmpty()) {
            client.inGameHud.setOverlayMessage(Text.literal(payload.subtitleText()), false);
        }

        AudioManager.lastEventAtMs.put(client.player.getUuid(), System.currentTimeMillis());
    }
}
