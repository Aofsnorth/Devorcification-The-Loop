package com.devorcification.ai;

import com.devorcification.Devorcification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProceduralDirector {
    public static ActionPlan generateFallback(int cycle, List<PlayerSnapshot> players) {
        ActionPlan plan = new ActionPlan();
        plan.cycle = cycle;
        plan.menaceBudgetSpend = 1;
        plan.menaceBudgetReserve = 1;
        plan.perPlayerActions = new HashMap<>();

        if (cycle <= 2) {
            plan.globalStrategy = "ESCALATE";
            for (PlayerSnapshot s : players) {
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                a.shaderIntensity = 0.2f + (cycle * 0.1f);
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: torch flicker (cycle {})", cycle);
        } else if (cycle <= 4) {
            plan.globalStrategy = "MAINTAIN";
            for (PlayerSnapshot s : players) {
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                ActionPlan.DirectedAudio audio = new ActionPlan.DirectedAudio();
                audio.soundId = "devorcification:ambient_distant";
                audio.volume = 0.4f;
                audio.pitch = 1.0f;
                a.directedAudio = audio;
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: distant ambient (cycle {})", cycle);
        } else {
            plan.globalStrategy = "DEESCALATE";
            for (PlayerSnapshot s : players) {
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                a.desyncDelta = -1;
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: desync release (cycle {})", cycle);
        }
        return plan;
    }
}
