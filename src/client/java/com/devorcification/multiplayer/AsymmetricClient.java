package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AsymmetricClient {
    public static final Map<BlockPos, BlockState> perceivedOverrides = new HashMap<>();
    public static final Map<UUID, FakePlayerEntity> activeFakeEntities = new HashMap<>();

    public static void register() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;
        PayloadTypeRegistry.playS2C().register(AsymmetricBlockPacket.ID, AsymmetricBlockPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AsymmetricEntityPacket.ID, AsymmetricEntityPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AsymmetricChatPacket.ID, AsymmetricChatPacket.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(AsymmetricBlockPacket.ID, (payload, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (!client.player.getUuid().equals(payload.targetUuid())) return;
            client.execute(() -> perceivedOverrides.put(payload.pos().toImmutable(), payload.fakeState()));
        });

        ClientPlayNetworking.registerGlobalReceiver(AsymmetricEntityPacket.ID, (payload, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (!client.player.getUuid().equals(payload.targetUuid())) return;
            client.execute(() -> handleEntityPacket(payload));
        });

        ClientPlayNetworking.registerGlobalReceiver(AsymmetricChatPacket.ID, (payload, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            if (!client.player.getUuid().equals(payload.targetUuid())) return;
            client.execute(() -> {
                net.minecraft.client.gui.hud.ChatHud chat = client.inGameHud.getChatHud();
                if (chat != null) {
                    long ago = System.currentTimeMillis() - payload.fakeTimestampMs();
                    String suffix = ago < 60000L ? " (" + (ago / 1000) + "s ago)" : "";
                    chat.addMessage(net.minecraft.text.Text.literal("<" + payload.fromPlayerName() + "> " + payload.message() + suffix));
                    Devorcification.LOGGER.info("[Devorcification Multiplayer] Fake chat injected from {}", payload.fromPlayerName());
                }
            });
        });
    }

    private static void handleEntityPacket(AsymmetricEntityPacket payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (payload.spawn()) {
            UUID id = payload.targetUuid();
            if ("devorcification:fake_player".equals(payload.entityType()) && payload.mimicTargetUuid() != null) {
                FakePlayerEntity fake = FakePlayerEntity.spawnClientSide(
                    id, payload.mimicTargetUuid(),
                    "Player_" + payload.mimicTargetUuid().toString().substring(0, 4),
                    payload.position());
                if (fake != null) {
                    fake.setBehavior(payload.behavior());
                    activeFakeEntities.put(id, fake);
                }
            } else {
                Devorcification.LOGGER.info("[Devorcification Multiplayer] Client fake entity {} @ {} visible to {}",
                    payload.entityType(), payload.position(), id);
            }
        } else {
            FakePlayerEntity fake = activeFakeEntities.remove(payload.targetUuid());
            if (fake != null) fake.remove(net.minecraft.entity.Entity.RemovalReason.DISCARDED);
        }
    }

    public static void clearAll() {
        perceivedOverrides.clear();
        for (FakePlayerEntity f : activeFakeEntities.values()) {
            f.remove(net.minecraft.entity.Entity.RemovalReason.DISCARDED);
        }
        activeFakeEntities.clear();
    }

    public static BlockState getPerceivedBlock(BlockPos pos) {
        BlockState s = perceivedOverrides.get(pos);
        return s;
    }
}
