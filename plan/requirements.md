# THE LOOP: CORRUPTED — Requirements & Technical Specification

## 1. FUNCTIONAL REQUIREMENTS

### 1.1 Core Loop System (FR-001 to FR-010)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-001 | The Loop must be a self-contained dimension or isolated structure with no escape except through the End Door | P0 | Player cannot break out with mining, ender pearls, or portals |
| FR-002 | End Door must trigger Cycle increment with 1.5s screen fade (blindness + nausea) | P0 | Smooth transition, no visible chunk loading |
| FR-003 | Each cycle must apply 0-3 environmental mutations per player based on AI Action Plan | P0 | Mutations applied during screen fade, player sees result on wake |
| FR-004 | Branch rooms must unlock progressively based on cycle count and AI decision | P1 | Doorway blocks change from Barrier to Air on unlock |
| FR-005 | The Loop must track session time and cycle count per player in persistent server state | P0 | Data survives server restart within same session |
| FR-006 | Player must spawn at Start Door on first join and after each cycle | P0 | Respawn anchor disabled within Loop dimension |
| FR-007 | Block breaking/placement must be selectively restricted (AI-controlled) | P1 | Builder-type players can place limited blocks, but AI can corrupt them |
| FR-008 | Inventory items must be renamable by AI (client-side display only) | P2 | Item function unchanged, only display name corrupted |
| FR-009 | Chat messages must interceptable for fake chat injection | P2 | Fake messages appear identical to real player messages |
| FR-010 | The Loop must support 1-4 players simultaneously with individual state tracking | P0 | Each player has independent cycle, desync level, and fear profile |

### 1.2 AI Director System (FR-011 to FR-020)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-011 | Server must collect behavioral snapshot every 5 seconds per player | P0 | Yaw delta, position delta, block interact, inventory open, health change |
| FR-012 | Server must send batched context to AI Director every 10 seconds | P0 | JSON payload includes all players + social graph |
| FR-013 | AI Director must return Action Plan within 5 seconds (async) | P0 | Timeout triggers fallback procedural director |
| FR-014 | Action Plan must include per-player asymmetric block overrides | P1 | Block state sent only to target player client |
| FR-015 | Action Plan must include directed audio events | P1 | Sound plays only for target player with custom spatialization |
| FR-016 | Action Plan must include fake entity spawn instructions | P1 | Entity visible only to specified players, server has no physics for it |
| FR-017 | Action Plan must include fake chat messages | P2 | Message appears in target player's chat HUD with spoofed sender |
| FR-018 | AI Director must maintain Menace Gauge and enforce cooldowns | P0 | No more than 8 intense events per 10-minute window per player |
| FR-019 | AI Director must classify player into Fear Archetype by Cycle 2 | P1 | Classification matches observed behavior (Leader/Coward/Builder/Strategist/Skeptic) |
| FR-020 | AI Director must generate unique ending based on session data | P1 | Ending differs per player profile and multiplayer dynamics |

### 1.3 Entity System (FR-021 to FR-030)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-021 | The Watcher entity must have custom model (GeckoLib) with 5 states | P0 | Null, Peripheral, Mirror, Doppelgänger, Hunt |
| FR-022 | The Watcher must be visible only in specific conditions (FOV edge, mirrors, direct) | P0 | Peripheral state vanishes when player looks directly |
| FR-023 | Fake Player entity must render target player's skin and mimic delayed movement | P1 | Delay configurable 2-120 seconds by AI |
| FR-024 | Fake Player entity must have subtle "tells" (no breath, wrong shadow) | P2 | At least 3 visual differences from real player |
| FR-025 | Ceiling Crawler entity must attach to ceiling geometry | P2 | Renders upside-down, moves along ceiling blocks |
| FR-026 | Wall Hands must render as block entities (not mobs) from deepslate walls | P2 | Pure visual, no hitbox, no server entity |
| FR-027 | Mirror Reflection entity must render in glass blocks with pose mismatch | P2 | Reflection does player's pose from 3 seconds ago |
| FR-028 | All custom entities must bypass vanilla mob AI and lighting | P1 | No random wandering, no burning in sunlight |
| FR-029 | Entity spawn/despawn must be client-side controllable for asymmetric system | P1 | Server can send "spawn to Player A only" packet |
| FR-030 | Entity count must not exceed 20 active per player to maintain FPS | P1 | Despawn furthest entities when budget exceeded |

### 1.4 Rendering & Shader System (FR-031 to FR-045)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-031 | Custom post-processing shader must support vignette, chromatic aberration, film grain | P0 | Effects intensity controlled by server packet |
| FR-032 | Mirror shader must delay reflection by 2-5 seconds | P1 | Glass blocks tagged as mirrors show delayed scene |
| FR-033 | Entity glow shader must make Watcher self-illuminated | P1 | Ignores scene lighting, color changes per state |
| FR-034 | Fog shader must override vanilla fog with AI-controlled density | P0 | Fog can be 0 (clear) to 1 (zero visibility) |
| FR-035 | All shaders must be compatible with Iris Shaders mod | P1 | Works when Iris is installed, gracefully degrades if not |
| FR-036 | All shaders must be compatible with Sodium | P1 | No chunk rendering conflicts |
| FR-037 | Custom textures must support dynamic painting generation from player skin | P2 | CPU algorithm distorts skin into "corrupted portrait" |
| FR-038 | UI corruption must support heart icon cracking and hunger bar transformation | P2 | Resource pack override with conditional loading |
| FR-039 | Font renderer must support "glitch" mode for occasional character swap | P2 | 5% chance per chat message in high desync |
| FR-040 | Screen effects must sync with audio heartbeat | P2 | Vignette pulse matches BPM of ambient heartbeat track |
| FR-041 | Rendering hooks must not break vanilla GUI or other mod HUDs | P0 | Compatibility with minimap mods, inventory mods |
| FR-042 | Shader intensity must be controllable per-player in multiplayer | P1 | Player A sees heavy distortion, Player B sees clear |
| FR-043 | Asymmetric block rendering must not affect server physics | P0 | Player A sees air, Player B sees wall, server sees wall |
| FR-044 | Block override packet must support all block states and block entities | P1 | Chests, signs, doors all overridable |
| FR-045 | Night vision and brightness settings must be forcibly overridden in Loop | P1 | Gamma clamp to 0.5, night vision potions ineffective |

### 1.5 Audio System (FR-046 to FR-055)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-046 | Custom sound engine must support 3D spatialization with behind-head boost | P1 | Rear 180° sounds 1.5x louder with reverb |
| FR-047 | Whisper channel must feel "inside player's head" | P1 | Mono, close-mic, no distance attenuation |
| FR-048 | Dynamic ambient drone must shift pitch based on Menace Gauge | P2 | Pitch lowers as tension rises (infrasound effect) |
| FR-049 | Audio must be asymmetric — different players hear different things | P1 | Same position can have different sound events per player |
| FR-050 | Radio broadcasts must support LLM-generated text-to-speech or subtitle | P2 | Text displayed as subtitle, optional TTS if resource available |
| FR-051 | Footstep audio must be enhanced and context-sensitive | P2 | Different block types, echo in corridors |
| FR-052 | Silence detection must trigger AI (sudden audio cut) | P2 | AI notified when ambient audio drops for > 5 seconds |
| FR-053 | Phone ring must be locational and interactable | P2 | Ring originates from specific block, answering triggers event |
| FR-054 | Audio must respect Minecraft's sound category sliders | P0 | Master/Weather/Blocks hostile etc. categories respected |
| FR-055 | Resource pack must include 50+ custom sounds (ambience, whispers, impacts) | P1 | Ogg format, mono for positional, stereo for ambient |

### 1.6 Multiplayer & Networking (FR-056 to FR-065)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-056 | Server must track social graph: proximity, voice activity, trust index | P1 | Updated every 5 seconds |
| FR-057 | Asymmetric block packets must be sent only to target player | P0 | Other players receive no packet, see original block |
| FR-058 | Fake entity packets must include visibility mask (which players see it) | P1 | Bitmask or UUID list in packet |
| FR-059 | Fake chat must be indistinguishable from real chat in HUD | P2 | Same font, color, timestamp format |
| FR-060 | Forced separation mechanic must physically split players | P1 | Doors lock, teleport to different instances of same room |
| FR-061 | Trust index must decay when asymmetric events create confusion | P2 | Metric tracked, affects AI social sabotage strategy |
| FR-062 | Player reconnection must restore session state (desync, cycle, profile) | P1 | SQLite or NBT persistence |
| FR-063 | Server must handle player disconnect mid-session gracefully | P1 | AI notified, remaining players experience "absence" events |
| FR-064 | Maximum 4 players per Loop session for optimal AI performance | P2 | 5+ players split into separate Loop instances |
| FR-065 | LAN and dedicated server both supported | P1 | Tested on both environments |

### 1.7 Safety & Fallback (FR-066 to FR-075)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-066 | `/loop status` command must show current reality anchor | P2 | Displays desync level, cycle, last AI interaction |
| FR-067 | `/loop reset` command must be available for server operators | P1 | Clears session, resets player to Cycle 0 |
| FR-068 | Auto-pause must trigger if rapid disconnect/reconnect detected | P2 | AI stops escalation, sends "calm" event |
| FR-069 | Procedural Director must activate if AI backend is unreachable | P0 | Hardcoded behavior tree takes over, still scary |
| FR-070 | Client must not crash if shader compilation fails | P0 | Fallback to vanilla rendering with warning |
| FR-071 | Mod must not corrupt player data in vanilla worlds | P0 | Loop effects contained within Loop dimension/structure only |
| FR-072 | Resource pack must be optional — mod works without it (vanilla textures) | P1 | Degraded but functional experience |
| FR-073 | Config file must allow server operators to disable AI and use procedural only | P1 | `enable_ai_director: false` in config |
| FR-074 | Config must allow adjusting Menace Gauge sensitivity | P2 | `menace_budget_multiplier: 0.5 to 2.0` |
| FR-075 | Config must allow custom AI backend URL and API key | P1 | Support OpenAI, Claude, local Ollama endpoints |

---

## 2. NON-FUNCTIONAL REQUIREMENTS

### 2.1 Performance
- **NFR-001**: Server tick time impact < 5ms for AI observation + packet handling
- **NFR-002**: Client FPS impact < 10% with all shaders active (target 60 FPS on GTX 1060 / RX 580)
- **NFR-003**: AI HTTP request must be async, non-blocking server thread
- **NFR-004**: Packet size for asymmetric block must be < 1KB per block batch
- **NFR-005**: Entity spawn packet must be processed by client within 50ms

### 2.2 Compatibility
- **NFR-006**: Minecraft 1.20.1 Fabric only (no Forge, no Quilt initially)
- **NFR-007**: Java 17+ required
- **NFR-008**: Compatible with Sodium 0.5.x (rendering optimization)
- **NFR-009**: Compatible with Iris 1.6.x (shader pipeline sharing)
- **NFR-010**: Compatible with Simple Voice Chat 2.4.x (optional panic detection)
- **NFR-011**: NOT compatible with OptiFine (shader conflict expected)
- **NFR-012**: Singleplayer LAN and dedicated server supported

### 2.3 Security
- **NFR-013**: AI backend API key must be stored server-side only, never sent to client
- **NFR-014**: Asymmetric packets must be validated to prevent client spoofing
- **NFR-015**: Fake chat must be clearly marked in server logs to prevent moderation confusion
- **NFR-016**: Mod must not execute arbitrary code from AI response (strict JSON schema validation)

### 2.4 Maintainability
- **NFR-017**: Codebase organized in modules: Core, AI, Entity, Render, Audio, Network, Config
- **NFR-018**: Mixins documented with @Reason annotation
- **NFR-019**: All custom packets versioned for backward compatibility
- **NFR-020**: AI prompt templates externalized to config/prompts/ directory

---

## 3. HARDWARE REQUIREMENTS

### 3.1 Client (Minimum)
- CPU: Intel i5-8400 / AMD Ryzen 5 2600
- GPU: GTX 1060 6GB / RX 580 8GB
- RAM: 8GB total, 4GB allocated to Minecraft
- Storage: 500MB for mod + resource pack

### 3.2 Client (Recommended)
- CPU: Intel i7-10700 / AMD Ryzen 7 3700X
- GPU: RTX 3060 / RX 6700 XT (for advanced shaders)
- RAM: 16GB total, 8GB allocated
- Headphones: REQUIRED (asymmetric audio design)

### 3.3 Server (Dedicated)
- CPU: 4 cores minimum, high single-thread performance
- RAM: 4GB base + 1GB per concurrent Loop session
- Network: Stable connection to AI backend (< 100ms latency)
- Storage: 1GB for SQLite session logs

---

## 4. DEPENDENCIES

### 4.1 Minecraft Mod Dependencies
```
Fabric Loader >= 0.15.0
Fabric API >= 0.92.0+1.20.1
GeckoLib 4 >= 4.4.0
(optional) Sodium >= 0.5.0
(optional) Iris >= 1.6.0
(optional) Simple Voice Chat >= 2.4.0
```

### 4.2 AI Backend Dependencies
```
Python 3.10+
FastAPI >= 0.110.0
Uvicorn >= 0.27.0
Redis >= 7.0 (optional, for session caching)
OpenAI Python SDK >= 1.0.0
Anthropic Python SDK >= 0.20.0
Ollama Python SDK >= 0.1.0 (fallback)
```

### 4.3 Build Dependencies
```
Gradle 8.5+
Loom 1.5+ (Fabric plugin)
Java 17
```

---

## 5. API SPECIFICATION (AI Backend)

### 5.1 Endpoint: POST /director/plan
**Request:**
```json
{
  "session_id": "uuid",
  "cycle": 4,
  "players": [...],
  "social_graph": {...},
  "global_state": {...}
}
```

**Response:**
```json
{
  "cycle": 4,
  "global_strategy": "ESCALATE_PARANOIA",
  "menace_budget": {"spend": 2, "reserve": 2},
  "per_player": {
    "uuid1": {
      "desync_delta": 15,
      "asymmetric_blocks": [...],
      "directed_audio": {...},
      "fake_chat": null,
      "fake_entity": {...},
      "shader_intensity": 0.6
    }
  }
}
```

**Timeout:** 5000ms  
**Retry:** 2x with exponential backoff  
**Fallback:** Procedural Director activation

### 5.2 Endpoint: GET /director/health
**Response:** `{"status": "ok", "model": "gpt-4o", "latency_ms": 120}`

---

*Document Version: 1.0*  
*Target Platform: Minecraft 1.20.1 Fabric*  
*Last Updated: 2026-06-03*
