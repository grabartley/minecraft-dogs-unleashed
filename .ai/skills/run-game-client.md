---
name: Run Game Client
description: Runs the Minecraft game client locally with the mod for manual testing.
tags:
- minecraft
- testing
- gradle
- client
---
# Run Game Client

This skill runs the Minecraft game client with the Dogs Unleashed mod loaded for manual testing.

## Prerequisites

Before running the client:

1. **Java 21** must be installed and active
2. **Build must pass** - run `./gradlew build` first if you've made changes

## Running the Client

```bash
./gradlew runClient
```

This will:
- Download Minecraft assets if needed (first run only)
- Launch the Minecraft client with the mod loaded
- Open a game window for manual testing

## Common Testing Tasks

### Spawning Dogs

Use creative mode or spawn eggs:
- `/gamemode creative` - switch to creative mode
- Find spawn eggs in the creative inventory under "Spawn Eggs" tab
- Or use commands like `/summon dogs-unleashed:goldenretriever`

### Available Entity IDs

- `dogs-unleashed:husky`
- `dogs-unleashed:dachshund`
- `dogs-unleashed:beagle`
- `dogs-unleashed:goldenretriever`
- `dogs-unleashed:shibainu`

### Testing Biome Spawns

Travel to specific biomes to test natural spawning:
- **Husky**: Snowy biomes (snowy taiga, snowy plains, ice spikes)
- **Dachshund**: Plains
- **Beagle**: Flower forest
- **Golden Retriever**: Beach
- **Shiba Inu**: Cherry grove

Use `/locate biome minecraft:beach` to find biomes quickly.

### Hot Reloading

- Press **F3+T** to reload resource packs (textures, models, animations)
- Code changes require restarting the client

## Running the Server

For server-side testing:

```bash
./gradlew runServer
```

Then connect with a Minecraft client to `localhost`.

## Troubleshooting

### Client won't start

1. Check Java version: `java -version` (needs 21+)
2. Try a clean build: `./gradlew clean build`
3. Check for port conflicts if running multiple instances

### Mod not loading

1. Check `fabric.mod.json` for errors
2. Look for errors in the console output
3. Verify all dependencies are present

### Entity invisible or broken

1. Check renderer registration in `DogsUnleashedClient.java`
2. Verify asset paths match (geo, texture, animation files)
3. Press F3+T to reload textures
