package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundEventRegistry {
    public static final SoundEvent AMBIENT_DRONE = register("ambient_drone");
    public static final SoundEvent HEARTBEAT_NORMAL = register("heartbeat_normal");
    public static final SoundEvent HEARTBEAT_FAST = register("heartbeat_fast");
    public static final SoundEvent HEARTBEAT_PANIC = register("heartbeat_panic");
    public static final SoundEvent WHISPER_GENERIC = register("whisper_generic");
    public static final SoundEvent WHISPER_NAME = register("whisper_name");
    public static final SoundEvent FOOTSTEP_BEHIND = register("footstep_behind");
    public static final SoundEvent TORCH_FLICKER = register("torch_flicker");
    public static final SoundEvent DOOR_CREAK = register("door_creak");
    public static final SoundEvent RADIO_STATIC = register("radio_static");
    public static final SoundEvent PHONE_RING = register("phone_ring");
    public static final SoundEvent BABY_CRY = register("baby_cry");
    public static final SoundEvent WATCHER_BREATH = register("watcher_breath");
    public static final SoundEvent WATCHER_HUNT = register("watcher_hunt");
    public static final SoundEvent SILENCE_MARKER = register("silence_marker");

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(Devorcification.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
        Devorcification.LOGGER.info("[Devorcification] Sound events registered");
    }
}
