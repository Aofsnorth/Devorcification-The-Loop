package com.devorcification.ai;

import com.devorcification.Devorcification;

import java.util.UUID;

public class ActionPlanExecutor {
    public static void execute(ActionPlan plan) {
        if (plan == null) {
            Devorcification.LOGGER.warn("[Devorcification AI] execute called with null plan");
            return;
        }
        Devorcification.LOGGER.info(
            "[Devorcification AI] Executing plan cycle={} strategy={} spend={} reserve={}",
            plan.cycle, plan.globalStrategy, plan.menaceBudgetSpend, plan.menaceBudgetReserve);

        if (plan.perPlayerActions == null) return;
        for (var entry : plan.perPlayerActions.entrySet()) {
            routePlayerActions(entry.getKey(), entry.getValue());
        }
    }

    private static void routePlayerActions(UUID playerId, ActionPlan.PlayerAction action) {
        if (action.asymmetricBlocks != null && !action.asymmetricBlocks.isEmpty()) {
            for (ActionPlan.BlockOverride b : action.asymmetricBlocks) {
                Devorcification.LOGGER.info("[Devorcification AI] Would apply asymmetric block {} @({},{},{}) for {}",
                    b.blockId, b.x, b.y, b.z, playerId);
            }
        }
        if (action.directedAudio != null) {
            Devorcification.LOGGER.info("[Devorcification AI] Would play directed audio {} vol={} pitch={} for {}",
                action.directedAudio.soundId, action.directedAudio.volume, action.directedAudio.pitch, playerId);
        }
        if (action.fakeChat != null) {
            Devorcification.LOGGER.info("[Devorcification AI] Would inject fake chat <{}> {} for {}",
                action.fakeChat.username, action.fakeChat.message, playerId);
        }
        if (action.fakeEntity != null) {
            Devorcification.LOGGER.info("[Devorcification AI] Would spawn fake entity {} @({},{},{}) for {}",
                action.fakeEntity.entityId, action.fakeEntity.x, action.fakeEntity.y, action.fakeEntity.z, playerId);
            routeShaderIntensity(action.shaderIntensity, 80.0f, 5.0f);
        }
        if (action.shaderIntensity > 0.0f) {
            routeShaderIntensity(action.shaderIntensity, 0.0f, 0.0f);
        }
        if (action.desyncDelta != 0) {
            Devorcification.LOGGER.info("[Devorcification AI] Would apply desync delta {} for {}",
                action.desyncDelta, playerId);
        }
    }

    private static void routeShaderIntensity(float intensity, float heartbeatBpm, float durationSec) {
        try {
            Class<?> sm = Class.forName("com.devorcification.render.ShaderManager");
            sm.getMethod("setIntensity", float.class).invoke(null, intensity);
            if (heartbeatBpm > 0.0f) {
                sm.getMethod("pulseHeartbeat", float.class).invoke(null, heartbeatBpm);
            }
        } catch (Throwable t) {
            Devorcification.LOGGER.debug("[Devorcification AI] ShaderManager unavailable (server side): {}", t.getMessage());
        }
    }
}
