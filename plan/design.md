# THE LOOP: CORRUPTED — Design Document
## AI-Driven Psychological Horror Mod for Minecraft Fabric

**Version:** Fabric 1.20.1 (LTS Stable)  
**Mod Loader:** Fabric Loader 0.15+  
**Fabric API:** 0.92.0+  
**Architecture:** Server-Authoritative with Client-Side Rendering Layer  
**AI Backend:** Cloud LLM (OpenAI/Claude) + Local Fallback (Ollama)  
**Renderer:** Custom Core Shaders + Iris/Canvas Compatibility Layer  
**Entity Models:** GeckoLib 4 + Custom Software Entity Renderer  

---

## 1. CORE DESIGN PILLARS

### 1.1 The Unpredictable Creator Doctrine
> *"The developer must fear their own creation. If the developer knows every jump scare, every corridor shift, every audio cue — the mod is dead."*

**Implementation:** The AI Director (LLM) is the FINAL AUTHORITY on ALL horror events. No hardcoded scare sequences. No deterministic spawn tables. The developer builds the STAGE, the AI writes the PLAY in real-time.

### 1.2 The PT Loop Transmuted
A 40-60 block looping corridor that is NOT a level — it is a **living psychological instrument**. The corridor learns the player's fears and reconfigures itself every cycle.

### 1.3 Asymmetric Multiplayer Reality
Each player sees a DIFFERENT version of the same space. The monster wears your friend's face. The bridge exists for you but not for them. Trust is the first victim.

---

## 2. PSYCHOLOGICAL ARCHITECTURE

### 2.1 Fear Hierarchy (Implemented)
| Tier | Fear Type | Mechanic |
|------|-----------|----------|
| 1 | Startle | Rare, AI-budgeted, never repeated |
| 2 | Atmospheric | Dynamic fog, audio, light corruption |
| 3 | Predator Surveillance | Ghost entity in peripheral vision |
| 4 | Control Erosion | Torch failure, block replacement |
| 5 | Cognitive Gaslighting | Inventory rename, fake chat, structure shift |
| 6 | Adaptive Intelligence | LLM profiles and exploits player weakness |
| 7 | Identity Threat | Doppelgänger, mirror delay, self-mockery |
| 8 | Social Contagion | Multiplayer impersonation, trust destruction |

### 2.2 The SOR Framework Integration
Based on Stimulus-Organism-Response research:
- **Cognitive Processing > Physiological Shock**: Gaslighting mechanics produce longer-lasting dread than jump scares.
- **Perceived Control Maintenance**: Player must ALWAYS feel they have a strategy, even when doomed.
- **Menace Gauge**: AI has a "terror budget" per cycle. Over-budget = player exhaustion/quit.

---

## 3. THE LOOP — SPATIAL DESIGN

### 3.1 Physical Blueprint (NBT Structure)
```
[Start Door: Iron Door + Heavy Pressure Plate]
    ↓
[Corridor A: 20 blocks]
    • Left Wall: Polished Deepslate (reflective-ish via shader)
    • Right Wall: Crying Obsidian (subtle animation)
    • Floor: Dark Oak Planks (footstep audio priority)
    • Ceiling: Black Concrete (absorbs light)
    • Lighting: Redstone Lamps (AI-controllable flicker)
    ↓
[The Turn: 90° Right, 5 blocks, tight squeeze]
    ↓
[Corridor B: 20 blocks]
    • Identical to A, but AI can asymmetrically modify
    • 5 Branch Doorways (currently sealed with Barrier blocks)
    ↓
[End Door: Iron Door]
    → Screen Black (1.5s Blindness + Nausea)
    → Cycle Counter +1
    → AI Director evaluates
    → Teleport to Start Door
    → Environment mutations applied
```

### 3.2 Branch Rooms (Unlock per Cycle)
| Room | Cycle Unlock | Purpose | AI Controllable Elements |
|------|-------------|---------|------------------------|
| Bathroom | 0 | Profiling: Does player check mirror? | Water color, mirror reflection delay, bathtub fill level |
| Radio Room | 1 | Audio narrative delivery | Broadcast text (LLM-generated), volume, static level |
| Phone Room | 2 | Direct communication | Ring trigger, voice line (TTS or pre-recorded), caller ID |
| Gallery | 3 | Visual hallucination seed | Painting swap (map art → player face → distorted face) |
| Nursery | 4 | Primal fear trigger | Crib entity, lullaby audio, toy movement |
| The Extra Room | 5+ | Impossible architecture | Room that shouldn't exist, generates from sealed wall |

### 3.3 The Recursive Basement (Secret)
A hidden vertical shaft that opens in Cycle 6+. Descending reveals the "underneath" of the loop — a non-Euclidean space where the corridor is seen from below, twisted. Used for existential dread climax.

---

## 4. ENTITY DESIGN — THE WITNESS

### 4.1 The Ghost Entity ("The Watcher")
**Base:** Custom Software Entity (not extending Mob) to bypass vanilla AI.

**Model Architecture:**
- **Base:** Humanoid, 1.8 blocks tall, semi-transparent (shader-based, not potion)
- **Head:** Slightly oversized (uncanny valley), no facial features (smooth skin)
- **Limbs:** Longer than player, jointed wrong (elbows bend backward slightly)
- **Texture:** Procedural — can be tinted per-player based on fear profile

**States:**
1. **Null State**: Invisible, no hitbox, pure observer. Tracks player yaw/pitch.
2. **Peripheral State**: Visible only in corner of screen. Vanishes if player turns directly. Uses FOV calculation.
3. **Mirror State**: Visible in reflective surfaces (custom shader) but NOT in direct line of sight.
4. **Doppelgänger State**: Wears player skin (downloaded from Mojang API + cached). Mimics movement with 2-5 second delay.
5. **Hunt State**: Direct chase. Only triggered when Menace Gauge allows AND player is isolated.

### 4.2 Fake Player Entity (Multiplayer Sabotage)
- **Model:** Exact player model using target's skin
- **Nametag:** Identical to target player
- **Behavior:** Repeats target's movement history from 30-120 seconds ago
- **Visibility:** Client-side only, sent to SPECIFIC players via asymmetric packet
- **Tell:** Subtle differences — no armor glint, no breath particle in cold, shadow slightly wrong

### 4.3 The Corrupted (Environmental Entities)
- **Hands from Walls**: Block entities that render arms reaching from deepslate walls. Pure visual, no hitbox.
- **Ceiling Crawler**: Entity that crawls on ceiling geometry. Only visible when player looks up suddenly.
- **The Reflection**: Entity in mirror/glass that doesn't match player pose. Uses camera matrix inversion.

---

## 5. CUSTOM RENDERING PIPELINE

### 5.1 Shader Architecture
**Compatibility Target:** Minecraft 1.20.1 Core Shaders + Iris/Canvas Optional Layer

**Custom Core Shaders:**
1. **corruption_post.json** (Post-Processing)
   - Vignette: Black radial gradient that PULSES with heartbeat audio
   - Chromatic Aberration: RGB split that increases with "Desync Level"
   - Film Grain: Subtle noise that makes darkness feel "alive"
   - VHS Tracking Lines: Horizontal distortion during ghost events

2. **mirror_distortion.json** (World Shader)
   - Applied to glass blocks tagged as "loop_mirror"
   - Delayed reflection: Renders scene from 2-5 seconds ago
   - Color desaturation + slight red tint
   - Can show entities (The Reflection) not visible in main view

3. **entity_glow.json** (Entity Shader)
   - The Watcher entity ignores vanilla lighting
   - Self-illuminated with color based on AI state (white=observing, red=hunting)
   - Transparent edges that "smoke" via vertex displacement

4. **fog_corruption.json** (Fog Shader)
   - Distance-based fog that doesn't obey biome rules
   - Density controlled by AI Director (0.0 to 1.0)
   - Color: Deep purple/black, slightly animated
   - Near-player fog: 2-block radius "clear bubble" that feels claustrophobic

### 5.2 Custom Texture Pipeline
**Texture Types:**
- **Dynamic Paintings**: Map art generated at runtime from player skin (distorted via CPU algorithm)
- **Procedural Blocks**: Crying Obsidian that "bleeds" more when ghost is near (texture animation via sprite sheet)
- **UI Corruption**: Heart icons that crack, hunger bar that turns into eyeballs (resource pack override with conditional loading)
- **Font Corruption**: Custom font renderer that occasionally "glitches" chat text (character swap, kerning disruption)

### 5.3 Rendering Hooks (Fabric API + Mixin)
- `WorldRenderEvents.BEFORE_ENTITIES`: Inject fog and post-processing setup
- `HudRenderCallback`: Custom vignette, heartbeat sync, desync indicators
- `ClientPlayConnectionEvents.JOIN`: Initialize asymmetric rendering state
- `MixinGameRenderer`: Inject post-processing pipeline
- `MixinWorldRenderer`: Override entity rendering for The Watcher (bypass vanilla lighting)

---

## 6. AUDIO DESIGN — THE INVISIBLE MONSTER

### 6.1 3D Audio System
Using Minecraft's sound engine with custom attenuation:
- **Behind-Head Priority**: Sounds from rear 180° have 1.5x volume and reverb
- **Whisper Channel**: Mono, close-mic'd, spatialized to feel "inside ear"
- **Sub-bass**: 20-60Hz rumble during ghost proximity (requires good headphones)

### 6.2 Dynamic Music/Soundscape
**No traditional music.** Instead:
- **Drone Layer**: Constant low tone that shifts pitch based on Menace Gauge
- **Environmental Layer**: Creaking, dripping, wind — all spatialized and AI-triggered
- **Reactive Layer**: Sudden silence when ghost enters FOV (inverse scare)
- **Narrative Layer**: LLM-triggered broadcasts (radio), phone calls, whispered names

### 6.3 Multiplayer Audio Asymmetry
- Player A hears baby crying from Bathroom
- Player B hears nothing — but sees Player A suddenly orient toward Bathroom
- Creates social paranoia without either knowing who is "hallucinating"

---

## 7. AI DIRECTOR — THE BRAIN

### 7.1 Architecture
```
Minecraft Server (Fabric)
    ↓ HTTP/JSON (every 10s, or event-driven)
AI Director Server (Python/FastAPI)
    ├─ OpenAI GPT-4o / Claude 3.5 Sonnet (Primary)
    ├─ Ollama + Llama 3 8B (Local Fallback)
    └─ Redis/Memory Store (Session State)
    ↓ JSON Action Plan
Minecraft Server applies changes
```

### 7.2 Context Payload (Server → AI)
```json
{
  "session_id": "uuid",
  "cycle": 4,
  "players": [
    {
      "uuid": "...",
      "name": "PlayerName",
      "position": [x, y, z],
      "yaw_pitch": [y, p],
      "velocity": [vx, vy, vz],
      "health": 20,
      "inventory_snapshot": ["item_id", ...],
      "looking_at": "block_id",
      "light_level": 5,
      "biome": "the_loop",
      "reactions": {
        "rapid_look_count": 3,
        "backpedal_count": 1,
        "freeze_duration_ms": 4500,
        "block_break_count": 0,
        "chat_messages": 2
      },
      "fear_profile": "PARANOID_BUILDER",
      "desync_level": 35
    }
  ],
  "social_graph": {
    "proximity_matrix": {"uuid1_uuid2": 5.2},
    "voice_activity": {"uuid1": 45},
    "trust_index": {"uuid1_uuid2": 0.7}
  },
  "global_state": {
    "menace_budget_remaining": 4,
    "current_act": 2,
    "time_in_loop_ms": 720000
  }
}
```

### 7.3 Action Plan (AI → Server)
```json
{
  "cycle": 4,
  "global_strategy": "ESCALATE_PARANOIA",
  "menace_budget": {"spend": 2, "reserve": 2},
  "per_player": {
    "uuid1": {
      "desync_delta": +15,
      "asymmetric_blocks": [
        {"pos": "12 64 -5", "fake_state": "minecraft:air", "target": "uuid1"}
      ],
      "directed_audio": {
        "sound": "loop:whisper_name",
        "target": "uuid1",
        "volume": 0.4,
        "pitch": 0.9
      },
      "fake_chat": null,
      "fake_entity": {
        "type": "doppelganger",
        "mimic_target": "uuid2",
        "visible_to": ["uuid1"],
        "position": "20 64 -10",
        "behavior": "stand_stare"
      },
      "shader_intensity": 0.6
    }
  }
}
```

### 7.4 Fear Profiling Taxonomy
AI classifies each player into dynamic archetype:
- **The Leader**: Walks ahead, checks corners. Strategy: Isolate them. Make followers doubt.
- **The Coward**: Stays back, rapid camera movement. Strategy: Peripheral threats. Never direct.
- **The Builder**: Places blocks defensively. Strategy: Corrupt their structures. Make walls unsafe.
- **The Strategist**: Analyzes patterns. Strategy: Break patterns. Introduce mathematical impossibilities.
- **The Skeptic**: Ignores audio cues. Strategy: Physical reality manipulation. Make them doubt their eyes.

---

## 8. MULTIPLAYER ASYMMETRIC SYSTEM

### 8.1 Reality Layers
Each player has a **Desync Level** (0-100):
- 0-20: Normal. Minor audio differences.
- 21-50: Mild. Block state overrides. Fake distant sounds.
- 51-75: Moderate. Fake entities. Mirror anomalies. Chat hallucinations.
- 76-100: Severe. Structural changes. Fake player companions. Complete reality breakdown.

### 8.2 Packet System (Custom Fabric Networking)
```java
// Server → Client packets
ASYMMETRIC_BLOCK_S2C      // Override block state for specific player only
FAKE_ENTITY_S2C           // Spawn client-side entity visible to target only
DIRECTED_AUDIO_S2C        // Play sound to specific player with custom attenuation
FAKE_CHAT_S2C             // Inject chat message appearing from another player
SHADER_OVERRIDE_S2C      // Set desync level, trigger shader intensity
REALITY_ANCHOR_S2C        // Momentary "ground truth" pulse to prevent total confusion

// Client → Server packets
BEHAVIOR_SNAPSHOT_C2S     // Player reaction data (yaw delta, etc.)
HALLUCINATION_REPORT_C2S  // Player reports seeing something (for AI learning)
```

### 8.3 Social Sabotage Events
| Event | Trigger | Effect |
|-------|---------|--------|
| **The Double** | Player separated > 30s | Fake player appears near group wearing separated player's skin |
| **The Lie** | Player checks on friend | Fake chat from friend: "I'm behind you" when they aren't |
| **The Vote** | Cycle 7+ | UI forces "sacrifice" choice. AI reveals fake votes to destroy trust |
| **The Merge** | One player desync > 90 | Their screen shows other players as Watcher entities. Others see them acting erratic |

---

## 9. NARRATIVE DESIGN — THE CORRUPTION

### 9.1 Lore (Justification for Mechanics)
> "The Loop is not a place. It is a memory error. A fragment of corrupted consciousness that has infected the save file. You are not exploring a dungeon — you are trapped inside a recursive psychological construct that learns from its occupants."

### 9.2 Environmental Storytelling
- **Blood Writing**: Appears on walls in Cycle 3+. Text is LLM-generated based on player's observed fears.
- **The Journal**: Book items found in Radio Room. Entries describe previous "occupants" whose behavior matches the current player. (AI generates these from behavior patterns.)
- **Phone Calls**: In Cycle 4+, the phone rings. Answering plays a voice (TTS or phoneme-assembled) reading the player's own chat log from earlier in the session.

### 9.3 Endings (AI-Generated)
Based on fear profile and session behavior:
1. **The Escape**: Player "wakes up" in vanilla world. But the Loop structure has been saved to their spawn chunks. (Fourth wall break)
2. **The Merge**: Player becomes the new Watcher. Their skin is added to the entity pool for future sessions.
3. **The Sacrifice**: Multiplayer only. One player remains. Others "escape" but their player data is corrupted on next login.
4. **The Infinite**: No ending. Loop continues. AI generates "fake credits" that roll while the player is still walking.

---

## 10. TECHNICAL SPECIFICATIONS

### 10.1 Target Platform
- **Minecraft:** 1.20.1 (Fabric LTS)
- **Java:** 17+
- **Fabric Loader:** 0.15.0+
- **Fabric API:** 0.92.0+
- **Required Dependencies:**
  - GeckoLib 4 (Entity animation)
  - Fabric Language Kotlin (if using Kotlin utilities)
- **Optional Compatibility:**
  - Iris Shaders (for advanced users who want to layer OUR shaders with their shaderpack)
  - Simple Voice Chat (for AI pitch/panic detection)
  - Sodium (Rendering optimization — MUST test compatibility)

### 10.2 Performance Budget
- Server: Max 5ms per tick for AI observation + packet processing
- Client: Max 2ms per frame for custom rendering
- AI Call: Async, batched every 10s, timeout 5s with fallback
- Memory: Max 50MB session state per player

### 10.3 Security & Safety
- AI backend is OPTIONAL — mod falls back to procedural director if cloud unavailable
- All asymmetric packets are signed to prevent anti-cheat false positives
- `/loop status` command available (cooldown 5 min) to check "reality anchor"
- Auto-pause if all players send panic signal (rapid disconnect/reconnect pattern)

---

*Document Version: 1.0 — The Recursive Design*  
*Last Updated: 2026-06-03*
