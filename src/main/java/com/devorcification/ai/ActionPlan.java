package com.devorcification.ai;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActionPlan {
    public int cycle;
    public String globalStrategy;
    public int menaceBudgetSpend;
    public int menaceBudgetReserve;
    public Map<UUID, PlayerAction> perPlayerActions;

    public static class PlayerAction {
        public int desyncDelta;
        public List<BlockOverride> asymmetricBlocks;
        public DirectedAudio directedAudio;
        public FakeChat fakeChat;
        public FakeEntity fakeEntity;
        public float shaderIntensity;
    }

    public static class BlockOverride {
        public int x;
        public int y;
        public int z;
        public String blockId;
    }

    public static class DirectedAudio {
        public String soundId;
        public float volume;
        public float pitch;
        public String target;
    }

    public static class FakeChat {
        public String fromPlayer;
        public String message;
        public String target;
    }

    public static class FakeEntity {
        public String entityType;
        public UUID mimicTargetUuid;
        public String position;
        public float yaw;
        public float pitch;
        public String behavior;
        public int durationTicks;
        public List<String> visibleTo;
    }
}
