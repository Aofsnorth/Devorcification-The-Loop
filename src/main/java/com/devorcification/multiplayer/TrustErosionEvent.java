package com.devorcification.multiplayer;

import com.devorcification.Devorcification;

import java.util.UUID;

public class TrustErosionEvent {
    public final UUID playerA;
    public final UUID playerB;
    public final double trustDelta;
    public final long timestamp;
    public final String reason;

    public TrustErosionEvent(UUID playerA, UUID playerB, double trustDelta, String reason) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.trustDelta = trustDelta;
        this.timestamp = System.currentTimeMillis();
        this.reason = reason == null ? "" : reason;
        Devorcification.LOGGER.info("[Devorcification Multiplayer] TrustErosionEvent: {}<->{} delta={} reason={}",
            playerA, playerB, trustDelta, this.reason);
    }
}
