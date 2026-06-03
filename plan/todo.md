# THE LOOP: CORRUPTED — Development Roadmap & TODO

## Phase 0: Foundation & Tooling (Week 1)
**Goal:** Working development environment with Fabric 1.20.1, custom packet system, and basic Loop structure.

### 0.1 Environment Setup
- [ ] Install Fabric 1.20.1 development kit (Loom 1.5, Gradle 8.5, Java 17)
- [ ] Setup IDE (IntelliJ IDEA recommended) with Fabric run configurations
- [ ] Create mod skeleton: `modid`, `fabric.mod.json`, main entry points (Client/Server)
- [ ] Initialize Git repository with `.gitignore` for Minecraft dev
- [ ] Setup automated build pipeline (GitHub Actions or local Gradle tasks)

### 0.2 Core Architecture
- [ ] Implement mod config system (MidnightConfig or Cloth Config API)
- [ ] Create module packages: `core`, `ai`, `entity`, `render`, `audio`, `network`, `loop`
- [ ] Register custom dimension/structure system for The Loop
- [ ] Build NBT structure exporter: Create Loop corridor in Creative, export to `resources/data/loop/structures/`
- [ ] Implement structure loader: Place Loop at spawn on session start

### 0.3 Networking Foundation
- [ ] Register custom packet channels: `loop:asymmetric`, `loop:director`, `loop:audio`, `loop:entity`
- [ ] Implement packet serialization/deserialization for all custom types
- [ ] Test packet roundtrip: Server → Client → verify receipt
- [ ] Implement packet throttling: Max 50 packets/second per player

### 0.4 Basic Session State
- [ ] Create `LoopSession` class: UUID, cycle counter, player list, global menace budget
- [ ] Create `PlayerSessionData` class: desync level, fear profile, reaction history
- [ ] Implement server-side state persistence (SQLite via JDBC or NBT file)
- [ ] Test state save/load across server restart

---

## Phase 1: The Loop Core (Week 2)
**Goal:** Player can enter Loop, walk corridor, trigger cycle, see mutations.

### 1.1 Spatial System
- [ ] Build Blockout Map v1 in Creative mode (60x12x6 corridor + 5 branch rooms)
- [ ] Export structure NBT and embed in mod resources
- [ ] Implement `LoopStructureManager`: Place, replace, mutate blocks
- [ ] Implement `CycleManager`: Increment counter, trigger screen fade, teleport
- [ ] Block breaking/placement restrictions within Loop bounds
- [ ] Prevent escape: Disable portals, disable ender pearls, void damage below Y=-10

### 1.2 Cycle Mutations (Procedural)
- [ ] Implement `MutationEngine`: Apply block state changes during screen fade
- [ ] Create mutation types: `BlockReplace`, `LightChange`, `DoorUnlock`, `EntitySpawn`
- [ ] Hardcode 10 "test mutations" for Phase 1 (no AI yet)
- [ ] Test mutation application: Player walks End Door → black screen → world changed

### 1.3 Player Observer
- [ ] Implement behavioral tracking: position delta, yaw delta, pitch delta every tick
- [ ] Detect reactions: `RapidLookEvent`, `BackpedalEvent`, `FreezeEvent`, `BlockBreakEvent`
- [ ] Store reaction counts in `PlayerSessionData`
- [ ] Create debug overlay (F3 + custom) showing observer metrics

### 1.4 Basic Audio
- [ ] Register custom sound events in `sounds.json`
- [ ] Add 5 placeholder ambient sounds (footsteps, drip, creak, whisper, drone)
- [ ] Implement positional audio spawn at specific block positions
- [ ] Test audio in corridor: 3D positioning, volume falloff

---

## Phase 2: AI Director Integration (Week 3)
**Goal:** LLM analyzes player behavior and returns action plans.

### 2.1 AI Backend (Python)
- [ ] Setup FastAPI project with `main.py`, `director.py`, `models.py`
- [ ] Implement `POST /director/plan` endpoint with Pydantic models
- [ ] Integrate OpenAI SDK (GPT-4o) with retry logic
- [ ] Implement fallback: Ollama local (`llama3:8b`) if cloud unreachable
- [ ] Add request validation and response JSON schema enforcement
- [ ] Implement `GET /director/health` healthcheck

### 2.2 Context Builder (Java)
- [ ] Implement `ContextBuilder`: Compile `PlayerSnapshot` from observer data
- [ ] Implement `SocialGraphBuilder`: Calculate proximity, voice activity, trust
- [ ] Batch 3 players into single JSON payload
- [ ] HTTP client: Async request using Java 11+ `HttpClient` (non-blocking)
- [ ] Timeout handling: 5s timeout → fallback to `ProceduralDirector`

### 2.3 Action Plan Executor (Java)
- [ ] Parse AI response JSON into `ActionPlan` objects
- [ ] Route actions: `AsymmetricBlockApplier`, `DirectedAudioPlayer`, `FakeEntitySpawner`
- [ ] Implement Menace Gauge: Track budget, enforce cooldowns, prevent spam
- [ ] Create `ProceduralDirector` fallback: Hardcoded scary behavior tree
- [ ] Test end-to-end: Player walks → observer collects → AI returns → mutation applied

### 2.4 Fear Profiling
- [ ] Implement archetype classifier: `Leader`, `Coward`, `Builder`, `Strategist`, `Skeptic`
- [ ] Classification rules based on Phase 1 observer data
- [ ] Store archetype in `PlayerSessionData`, send to AI
- [ ] Test classifier: Walkthrough with different playstyles, verify correct classification

---

## Phase 3: Entity System (Week 4)
**Goal:** The Watcher, Fake Players, and environmental entities are functional.

### 3.1 The Watcher Entity
- [ ] Setup GeckoLib 4 dependency in `build.gradle`
- [ ] Model The Watcher in Blockbench: Humanoid, elongated limbs, oversized head
- [ ] Export as GeckoLib animated model (`.geo.json`, `.animation.json`)
- [ ] Implement `WatcherEntity` class: Custom software entity, no vanilla AI
- [ ] Implement state machine: `NULL → PERIPHERAL → MIRROR → DOPPELGANGER → HUNT`
- [ ] Peripheral detection: Calculate FOV edge (90°-110° from center), spawn/despawn
- [ ] Mirror detection: Raycast to glass blocks tagged `loop:mirror`, render Watcher there
- [ ] Hunt behavior: Direct pathfinding toward player, speed slightly faster than sprint

### 3.2 Fake Player Entity (Multiplayer)
- [ ] Implement `FakePlayerEntity`: Extends LivingEntity, uses player model
- [ ] Skin loading: Download from Mojang API, cache to `/.minecraft/loop_skins/`
- [ ] Movement mimicry: Record target player's position history in `RingBuffer`, replay
- [ ] Delay system: 30-120 second configurable delay
- [ ] Visual tells: Disable armor glint, disable breath particles, wrong shadow offset
- [ ] Asymmetric visibility: Spawn packet sent only to specified UUIDs

### 3.3 Environmental Entities
- [ ] `WallHandEntity`: Block entity renderer, arms reaching from wall blocks
- [ ] `CeilingCrawlerEntity`: Attaches to ceiling, upside-down movement logic
- [ ] `MirrorReflectionEntity`: Renders player model with 3-second pose delay
- [ ] All entities: No server physics, no hitbox, client-side render priority

### 3.4 Entity AI & Spawning
- [ ] `EntityDirector`: Receives AI Action Plan, decides WHAT to spawn WHERE for WHOM
- [ ] Spawn packet: `FakeEntityS2C` with entity type, position, visibility mask
- [ ] Despawn logic: Auto-despawn after duration or distance threshold
- [ ] Performance cap: Max 20 active custom entities per player

---

## Phase 4: Custom Rendering & Shaders (Week 5)
**Goal:** Visual horror pipeline is mindblowing. Silent Hill PT aesthetic achieved.

### 4.1 Core Shader System
- [ ] Create `assets/minecraft/shaders/post/` override directory
- [ ] Implement `corruption_post.json`: Vignette, chromatic aberration, film grain, VHS lines
- [ ] Implement vertex/fragment shaders in `.vsh` / `.fsh` files
- [ ] Uniform injection: Pass `desyncLevel`, `time`, `heartbeatPhase` from Java to shader
- [ ] Test shader compilation: Verify no crash on Intel/AMD/NVIDIA

### 4.2 Mirror Shader
- [ ] Tag glass blocks as `loop:mirror` via block entity or block state
- [ ] Implement delayed reflection: Render scene to framebuffer, delay by 2-5 seconds
- [ ] Color grading: Desaturate + slight red tint in mirror view
- [ ] Entity visibility: Show `MirrorReflectionEntity` and `Watcher` in mirror only

### 4.3 Entity Glow & Fog
- [ ] `entity_glow.json`: Watcher ignores lighting, self-illuminated white/red
- [ ] `fog_corruption.json`: Override vanilla fog, density 0.0-1.0 controlled by server
- [ ] Fog color: Deep purple/black, animated noise
- [ ] Near-player clear bubble: 2-block radius clarity for claustrophobia

### 4.4 Iris/Canvas Compatibility
- [ ] Test with Iris 1.6.x installed: Verify shaders layer correctly
- [ ] Test with Sodium 0.5.x: No chunk culling conflicts
- [ ] Fallback: If shader compilation fails, disable custom shaders, use vanilla + warning
- [ ] Config option: `shader_compatibility_mode: [native, iris, vanilla_fallback]`

### 4.5 Custom Textures & UI
- [ ] Resource pack structure: `assets/loop/textures/entity/`, `assets/loop/textures/block/`
- [ ] Dynamic painting: CPU algorithm to distort player skin into corrupted portrait
- [ ] UI corruption: Heart icon cracking (texture swap based on health %)
- [ ] Hunger bar transformation: Eyeball icons instead of drumsticks
- [ ] Font glitch: Custom font renderer with 5% character swap chance

### 4.6 Rendering Hooks (Mixin)
- [ ] `MixinGameRenderer`: Inject post-processing pipeline after world render
- [ ] `MixinWorldRenderer`: Override entity render for Watcher (bypass lighting)
- [ ] `MixinInGameHud`: Inject vignette, heartbeat sync, desync indicators
- [ ] `MixinClientPlayNetworkHandler`: Handle asymmetric packets before vanilla processing

---

## Phase 5: Asymmetric Multiplayer (Week 6)
**Goal:** Each player sees a different reality. Social paranoia is achievable.

### 5.1 Asymmetric Block System
- [ ] `AsymmetricBlockManager`: Store per-player block overrides in server state
- [ ] Override packet: Send `BlockUpdateS2C` with fake state to target player only
- [ ] Physics consistency: Server keeps true state, client sees fake state
- [ ] Phantom bridge test: Player A sees bridge (walks, falls), Player B sees air (wonders why A fell)
- [ ] Batch updates: Send block overrides in 16-block radius chunks

### 5.2 Directed Audio System
- [ ] `DirectedAudioEngine`: Play sound to specific player UUID only
- [ ] Spatial override: Bypass normal distance attenuation for "inside head" sounds
- [ ] Asymmetric audio test: Player A hears scream, Player B hears silence
- [ ] Sync with visual: Audio cue triggers exactly when asymmetric entity appears

### 5.3 Fake Chat System
- [ ] `FakeChatInjector`: Intercept chat HUD rendering
- [ ] Packet: `FakeChatS2C` with spoofed sender UUID, message, timestamp
- [ ] Visual match: Identical to real chat (font, color, hover text)
- [ ] Test: Player A sees message from "PlayerB: I'm behind you", Player B never sent it

### 5.4 Social Dynamics Engine
- [ ] `SocialGraph`: Track proximity matrix (distance between all player pairs)
- [ ] `TrustIndex`: Decay metric when asymmetric events cause confusion
- [ ] `LeadershipDetector`: Identify who walks first, who follows
- [ ] Forced separation: Door lock, teleport to different room instances
- [ ] Test 3-player session: Verify different experiences per player

### 5.5 Multiplayer Ending
- [ ] Implement "The Vote" GUI: 3 levers, sacrifice choice
- [ ] AI sends private fake vote results to each player
- [ ] Trust destruction event: Reveal "who voted for whom" (all fake, AI-generated)
- [ ] Ending divergence: One player "escapes", others trapped (cosmetic, all get credits)

---

## Phase 6: Narrative & Polish (Week 7)
**Goal:** Environmental storytelling, lore integration, PT-quality pacing.

### 6.1 Environmental Storytelling
- [ ] Blood writing system: Text appears on walls in Cycle 3+, LLM-generated from fear profile
- [ ] Journal items: Books with entries describing previous "occupants" matching player behavior
- [ ] Radio broadcasts: LLM-generated text, displayed as subtitles or TTS
- [ ] Phone calls: Ring event, answering plays voice reading player's own chat history

### 6.2 Pacing & Menace Gauge
- [ ] Implement `MenaceGauge`: Counter per player, max 8 events per 10 minutes
- [ ] `PacingDirector`: Enforce cooldowns, false relief moments, escalation curves
- [ ] Act structure: Act 1 (Profiling) → Act 2 (Haunting) → Act 3 (Breakdown) → Act 4 (Climax)
- [ ] Cycle-to-Act mapping: Cycles 0-2 = Act 1, 3-5 = Act 2, 6-8 = Act 3, 9+ = Act 4

### 6.3 Ending System
- [ ] `EndingDirector`: AI selects ending type based on session data
- [ ] Ending types: `ESCAPE`, `MERGE`, `SACRIFICE`, `INFINITE`
- [ ] Escape: Player wakes in vanilla world, Loop structure appears in spawn chunks
- [ ] Merge: Player becomes Watcher skin for future sessions
- [ ] Infinite: Fake credits roll, player keeps walking in endless corridor
- [ ] Credits: Custom shader with scrolling text, distorted faces of players

### 6.4 Audio Polish
- [ ] Record/procure 50+ custom sounds: ambients, impacts, whispers, voices
- [ ] Implement dynamic mixing: Drone layer + Environmental + Reactive + Narrative
- [ ] Heartbeat sync: Vignette pulse matches audio BPM
- [ ] Silence as weapon: 5-second complete audio cut after intense event

### 6.5 Resource Pack
- [ ] Complete texture set: Blocks, entities, UI, paintings, items
- [ ] Custom models: Radio, phone, crib, mirror frame
- [ ] Sound pack: All custom sounds in Ogg format, proper attenuation
- [ ] Language files: English, Indonesian (user request), possibly others

---

## Phase 7: Testing & Optimization (Week 8)
**Goal:** Stable, performant, terrifying. Developer fears their own mod.

### 7.1 Internal Testing
- [ ] Solo playthrough: 10 sessions, different playstyles, verify fear profiling accuracy
- [ ] 2-player sessions: 10 sessions, test asymmetric events, verify social paranoia
- [ ] 3-4 player sessions: 5 sessions, test group dynamics, ending divergence
- [ ] Stress test: 4 players, 20 cycles, monitor server tick time and FPS

### 7.2 Performance Optimization
- [ ] Profile with Spark: Identify tick lag sources
- [ ] Optimize packet batching: Reduce network overhead
- [ ] Entity culling: Don't render custom entities outside FOV
- [ ] Shader optimization: Reduce fragment shader complexity if FPS < 60
- [ ] Memory leak check: Ensure session state cleans up on player quit

### 7.3 Compatibility Testing
- [ ] Test with Sodium: Verify chunk rendering
- [ ] Test with Iris: Verify shader layering
- [ ] Test with Simple Voice Chat: Verify no audio conflicts
- [ ] Test WITHOUT dependencies: Verify graceful degradation
- [ ] Test on Intel iGPU: Verify shader fallback works

### 7.4 Bug Fixes & Polish
- [ ] Fix desync level not resetting between sessions
- [ ] Fix fake entity skin not loading on first spawn
- [ ] Fix asymmetric block physics edge cases
- [ ] Fix audio cutoff not triggering correctly
- [ ] Fix mirror shader delay inconsistency
- [ ] Add `/loop debug` command for server operators

### 7.5 Release Preparation
- [ ] Write `README.md` with installation, config, and warnings
- [ ] Create CurseForge/Modrinth page with screenshots and trailer
- [ ] Build release JAR with all dependencies shaded correctly
- [ ] Tag version 1.0.0
- [ ] Post to communities, gather feedback for v1.1

---

## Bonus: Advanced Features (Post-v1.0)

### B.1 Voice Panic Detection (Simple Voice Chat Integration)
- [ ] Detect pitch/volume spikes in voice chat
- [ ] Feed panic data to AI Director
- [ ] AI escalates when it hears player genuinely scared

### B.2 Desktop Fourth-Wall Break
- [ ] Fake "Minecraft crashed" screen that is actually in-game GUI
- [ ] Fake Windows BSOD texture on full-screen overlay
- [ ] Requires careful implementation to avoid actual user panic/quitting

### B.3 Persistent Corruption (Cross-Session)
- [ ] Loop structure appears in player's vanilla singleplayer world after playing
- [ ] Requires world gen mixin to inject structure near spawn
- [ ] Extreme fourth-wall break — use with caution

### B.4 Streaming Mode
- [ ] AI detects if player is streaming (OBS window detection? Manual toggle?)
- [ ] AI performs "for audience" scares vs "private" scares
- [ ] Chat integration: Viewers can vote on "hint" or "curse"

---

## Daily Micro-Goals (Example Week 1)

### Day 1
- [ ] Fabric dev env installed
- [ ] `gradle runClient` launches Minecraft with mod loaded
- [ ] Basic mod class prints "The Loop initialized" to console

### Day 2
- [ ] Config system working
- [ ] Can toggle `enable_ai_director` in config file
- [ ] Custom packet channel registered

### Day 3
- [ ] Built 60-block corridor in Creative
- [ ] Exported NBT structure
- [ ] Mod can place structure at world spawn

### Day 4
- [ ] Player enters structure, cannot break out
- [ ] End Door triggers teleport back to start
- [ ] Cycle counter increments

### Day 5
- [ ] Screen fade (blindness + nausea) during teleport
- [ ] 1 test mutation applies (torch replaced with soul torch)
- [ ] Player sees world changed after fade

### Day 6-7
- [ ] 5 hardcoded mutations working
- [ ] Basic observer tracking yaw/pitch
- [ ] Debug overlay shows metrics

---

## Definition of Done (Per Phase)

**Phase 0 Done:** `gradle build` produces JAR. Server starts. Client connects. No crashes.

**Phase 1 Done:** Player can walk Loop, trigger 10 cycles, see mutations, cannot escape.

**Phase 2 Done:** AI backend responds in < 5s. Action Plan applies mutations. Fallback works.

**Phase 3 Done:** Watcher appears in FOV edge. Fake Player mimics movement. Entities render.

**Phase 4 Done:** Vignette pulses. Mirror delays. Fog thickens. FPS stays > 60 on mid-tier GPU.

**Phase 5 Done:** 3 players have different experiences. Fake chat works. Trust is destroyed.

**Phase 6 Done:** Blood writing appears. Radio speaks. Phone rings. Ending plays.

**Phase 7 Done:** 20 playtest sessions completed. No game-breaking bugs. Developer is scared.

---

## Metrics of Terror (Success Criteria)

| Metric | Target | Measurement |
|--------|--------|-------------|
| Player Freeze Duration | > 5 seconds per session | Observer tracking |
| Rapid Camera Movement | > 10 events per session | Yaw delta detection |
| Backpedal Count | > 5 events per session | Movement vector |
| Voice Chat Panic | Detectable in 30% of sessions | Simple Voice Chat pitch |
| Session Completion | 70% reach Cycle 7+ | Server logs |
| Social Accusations | 1+ "you're lying" per multiplayer session | Chat log analysis |
| Developer Fear | Developer cannot predict own playthrough | Subjective |

---

*Roadmap Version: 1.0*  
*Total Estimated Duration: 8 Weeks (Full-time equivalent)*  
*Team Size: 1 Developer (you)*  
*Last Updated: 2026-06-03*
