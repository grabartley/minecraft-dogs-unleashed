---
name: Run Game Client
description: Runs the Minecraft game client locally with the mod for manual testing.
---
# Run Game Client

Runs the Minecraft client with the Dogs Unleashed mod for manual testing.

## Quick Start

```bash
./gradlew runClient
```

## Entity IDs for Testing

- `dogs-unleashed:husky` - spawns in snowy biomes
- `dogs-unleashed:dachshund` - spawns in plains
- `dogs-unleashed:beagle` - spawns in flower forest
- `dogs-unleashed:goldenretriever` - spawns on beaches
- `dogs-unleashed:shibainu` - spawns in cherry groves

## Useful Commands

- `/gamemode creative` - access spawn eggs
- `/summon dogs-unleashed:goldenretriever` - spawn entity directly
- `/locate biome minecraft:beach` - find biomes

## Hot Reload

Press **F3+T** to reload textures/models without restarting.

See `.ai/skills/run-game-client.md` for full documentation.
