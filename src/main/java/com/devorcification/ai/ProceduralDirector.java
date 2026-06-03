package com.devorcification.ai;

import com.devorcification.Devorcification;
import com.devorcification.pacing.MenaceGauge;
import com.devorcification.pacing.PacingDirector;

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

        PacingDirector.Act act = PacingDirector.getCurrentAct(cycle);
        Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback for act {} cycle {}", act.label, cycle);

        if (cycle <= 2) {
            plan.globalStrategy = "DESCENT";
            for (PlayerSnapshot s : players) {
                if (MenaceGauge.isInCooldown(s.playerId)) {
                    plan.perPlayerActions.put(s.playerId, new ActionPlan.PlayerAction());
                    continue;
                }
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                a.shaderIntensity = 0.2f + (cycle * 0.1f);
                if (MenaceGauge.canAfford(s.playerId, MenaceGauge.EventType.AUDIO_HINT)) {
                    ActionPlan.DirectedAudio audio = new ActionPlan.DirectedAudio();
                    audio.soundId = "devorcification:whisper_generic";
                    audio.volume = 0.15f;
                    audio.pitch = 1.0f;
                    a.directedAudio = audio;
                    MenaceGauge.spend(s.playerId, MenaceGauge.EventType.AUDIO_HINT);
                }
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: DESCENT - whisper + shader (cycle {})", cycle);
        } else if (cycle <= 5) {
            plan.globalStrategy = "HAUNTING";
            for (PlayerSnapshot s : players) {
                if (MenaceGauge.isInCooldown(s.playerId)) {
                    plan.perPlayerActions.put(s.playerId, new ActionPlan.PlayerAction());
                    continue;
                }
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                if (MenaceGauge.canAfford(s.playerId, MenaceGauge.EventType.PERIPHERAL_SPAWN)) {
                    ActionPlan.FakeEntity fe = new ActionPlan.FakeEntity();
                    fe.entityId = "devorcification:watcher";
                    fe.mimicTarget = s.playerId == null ? null : s.playerId.toString();
                    fe.visibleTo = java.util.List.of(s.playerId == null ? "" : s.playerId.toString());
                    fe.position = "0 62 30";
                    fe.behavior = "STARE";
                    a.fakeEntity = fe;
                    MenaceGauge.spend(s.playerId, MenaceGauge.EventType.PERIPHERAL_SPAWN);
                } else {
                    ActionPlan.DirectedAudio audio = new ActionPlan.DirectedAudio();
                    audio.soundId = "devorcification:footstep_behind";
                    audio.volume = 0.3f;
                    audio.pitch = 0.8f;
                    a.directedAudio = audio;
                }
                a.shaderIntensity = 0.3f;
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: HAUNTING - peripheral + audio (cycle {})", cycle);
        } else if (cycle <= 8) {
            plan.globalStrategy = "BREAKDOWN";
            for (PlayerSnapshot s : players) {
                if (MenaceGauge.isInCooldown(s.playerId)) {
                    plan.perPlayerActions.put(s.playerId, new ActionPlan.PlayerAction());
                    continue;
                }
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                if (MenaceGauge.canAfford(s.playerId, MenaceGauge.EventType.FAKE_CHAT)) {
                    ActionPlan.FakeChat chat = new ActionPlan.FakeChat();
                    chat.fromPlayer = "Watcher";
                    chat.message = "are you sure you're the real one?";
                    chat.target = s.playerId == null ? "" : s.playerId.toString();
                    a.fakeChat = chat;
                    MenaceGauge.spend(s.playerId, MenaceGauge.EventType.FAKE_CHAT);
                }
                if (MenaceGauge.canAfford(s.playerId, MenaceGauge.EventType.BLOCK_CORRUPTION)) {
                    ActionPlan.BlockOverride b = new ActionPlan.BlockOverride();
                    b.x = (int) s.position.getX() + 3;
                    b.y = (int) s.position.getY();
                    b.z = (int) s.position.getZ();
                    b.blockId = "minecraft:barrier";
                    a.asymmetricBlocks = java.util.List.of(b);
                    MenaceGauge.spend(s.playerId, MenaceGauge.EventType.BLOCK_CORRUPTION);
                }
                a.desyncDelta = 5;
                a.shaderIntensity = 0.5f;
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: BREAKDOWN - chat + blocks (cycle {})", cycle);
        } else {
            plan.globalStrategy = "CLIMAX";
            for (PlayerSnapshot s : players) {
                ActionPlan.PlayerAction a = new ActionPlan.PlayerAction();
                if (MenaceGauge.canAfford(s.playerId, MenaceGauge.EventType.HUNT)) {
                    a.desyncDelta = 20;
                    a.shaderIntensity = 0.8f;
                    MenaceGauge.spend(s.playerId, MenaceGauge.EventType.HUNT);
                } else {
                    a.shaderIntensity = 0.4f;
                }
                plan.perPlayerActions.put(s.playerId, a);
            }
            Devorcification.LOGGER.info("[Devorcification AI] Procedural fallback: CLIMAX - hunt (cycle {})", cycle);
        }
        return plan;
    }
}
