---
name: Run Game Client
description: Runs the Minecraft game client locally with the mod for manual testing.
---
# Run Game Client

Runs the Minecraft client with the Dogs Unleashed mod for manual testing.

## Quick Start

Ensure Java 21 is active (`jenv local 21` or `sdk use java 21-amzn`), then:

```bash
./gradlew runClient
```

## Testing Commands

- `/gamemode creative` - access spawn eggs
- `/summon dogs-unleashed:<entity_id>` - spawn entity directly (see `ModEntities.java` for IDs)
- `/locate biome minecraft:<biome>` - find biomes (see `ModSpawns.java` for spawn locations)

## Hot Reload

Press **F3+T** to reload textures/models without restarting.

See `.ai/skills/run-game-client.md` for full documentation.
