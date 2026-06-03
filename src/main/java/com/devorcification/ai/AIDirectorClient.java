package com.devorcification.ai;

import com.devorcification.Devorcification;
import com.devorcification.config.DevorcificationConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AIDirectorClient {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build();
    private static final ScheduledExecutorService SCHEDULER =
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "devorcification-ai-client");
            t.setDaemon(true);
            return t;
        });

    public static CompletableFuture<Optional<ActionPlan>> requestPlan(int cycle, List<PlayerSnapshot> snapshots) {
        if (!DevorcificationConfig.enableAIDirector) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        if (DevorcificationConfig.aiBackendUrl == null || DevorcificationConfig.aiBackendUrl.isBlank()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        JsonObject body = new JsonObject();
        body.addProperty("sessionId", UUID.randomUUID().toString());
        body.addProperty("cycle", cycle);
        body.addProperty("playerCount", snapshots.size());
        body.add("snapshots", GSON.toJsonTree(snapshots));
        String payload = GSON.toJson(body);

        return sendWithRetry(payload, 0);
    }

    private static CompletableFuture<Optional<ActionPlan>> sendWithRetry(String payload, int attempt) {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(DevorcificationConfig.aiBackendUrl))
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .header("X-Devorcification-Key", DevorcificationConfig.aiApiKey == null ? "" : DevorcificationConfig.aiApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        CompletableFuture<Optional<ActionPlan>> future = new CompletableFuture<>();
        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() / 100 == 2) {
                    try {
                        JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                        ActionPlan plan = GSON.fromJson(obj, ActionPlan.class);
                        Devorcification.LOGGER.info("[Devorcification AI] Received plan from backend (cycle {})", plan.cycle);
                        future.complete(Optional.ofNullable(plan));
                    } catch (Exception ex) {
                        Devorcification.LOGGER.warn("[Devorcification AI] Failed to parse response: {}", ex.getMessage());
                        future.complete(Optional.empty());
                    }
                } else {
                    Devorcification.LOGGER.warn("[Devorcification AI] HTTP {} from backend", response.statusCode());
                    if (attempt < 1) {
                        scheduleRetry(payload, attempt + 1).thenAccept(future::complete);
                    } else {
                        future.complete(Optional.empty());
                    }
                }
            })
            .exceptionally(ex -> {
                Devorcification.LOGGER.warn("[Devorcification AI] HTTP error (attempt {}): {}", attempt, ex.getMessage());
                if (attempt < 1) {
                    scheduleRetry(payload, attempt + 1).thenAccept(future::complete);
                } else {
                    future.complete(Optional.empty());
                }
                return null;
            });
        return future;
    }

    private static CompletableFuture<Optional<ActionPlan>> scheduleRetry(String payload, int attempt) {
        long delaySec = (long) Math.pow(2, attempt);
        CompletableFuture<Optional<ActionPlan>> next = new CompletableFuture<>();
        SCHEDULER.schedule(() -> sendWithRetry(payload, attempt).thenAccept(next::complete), delaySec, TimeUnit.SECONDS);
        return next;
    }
}
