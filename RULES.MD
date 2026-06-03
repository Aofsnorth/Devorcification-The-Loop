# Devorcification: The Loop

## Stack
- Minecraft 1.20.1 Fabric
- Java 17
- ModID: devorcification
- Package: com.devorcification

## Concept
Psychological horror PT-style. The Loop divorces player from reality.
Server-authoritative. AI Director controls pacing. Client only renders.

## Rules
- Jangan pakai OptiFine/Forge code
- Jangan hardcode coordinate, pakai BlockPos atau config
- Jangan blocking server thread untuk I/O (HTTP async)
- Mixin class prefix: DevorcificationMixin atau LoopMixin
- Entity ID format: devorcification:watcher, devorcification:fake_player
- Config keys: snake_case di JSON, camelCase di Java
- Semua horror mechanic harus punya fallback jika AI backend offline