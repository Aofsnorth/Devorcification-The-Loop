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
        }
        if (action.shaderIntensity > 0.0f) {
            Devorcification.LOGGER.info("[Devorcification AI] Would set shader intensity {} for {}",
                action.shaderIntensity, playerId);
        }
        if (action.desyncDelta != 0) {
            Devorcification.LOGGER.info("[Devorcification AI] Would apply desync delta {} for {}",
                action.desyncDelta, playerId);
        }
    }
}
