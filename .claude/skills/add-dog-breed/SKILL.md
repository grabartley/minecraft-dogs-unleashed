---
name: add-dog-breed
description: Add a new dog breed to the Dogs Unleashed Minecraft mod with all required files and tests. Use when asked to add or create a new dog breed.
---

# Add Dog Breed

## Workflow Requirement

1. Run the `build` skill first with the linked GitHub issue.
2. Let `build` handle `worktree` setup, implementation flow, testing, issue status updates, and `pr` handoff.
3. Use this skill as a breed-specific implementation checklist inside that build flow.

## Prerequisites

Ensure you have these files ready:
- `{breed_name}.animation.json` - GeckoLib animation file
- `{breed_name}.geo.json` - GeckoLib model file
- `{breed_name}.png` - Entity texture
- `{breed_name}_collar.png` - Collar overlay texture

## Files to Create/Modify

1. **Entity**: `src/main/java/com/grahambartley/dogsunleashed/entity/{BreedName}Entity.java`
2. **Constants**: `src/main/java/com/grahambartley/dogsunleashed/ModConstants.java`
3. **Entity Registration**: `src/main/java/com/grahambartley/dogsunleashed/ModEntities.java`
4. **Items**: `src/main/java/com/grahambartley/dogsunleashed/ModItems.java`
5. **Spawns**: `src/main/java/com/grahambartley/dogsunleashed/ModSpawns.java`
6. **Model**: `src/client/java/com/grahambartley/dogsunleashed/model/{BreedName}Model.java`
7. **Collar Layer**: `src/client/java/com/grahambartley/dogsunleashed/render/layer/{BreedName}CollarLayer.java`
8. **Renderer**: `src/client/java/com/grahambartley/dogsunleashed/render/{BreedName}Renderer.java`
9. **Client Init**: `src/client/java/com/grahambartley/dogsunleashed/DogsUnleashedClient.java`
10. **Language**: `src/main/resources/assets/dogs-unleashed/lang/en_us.json`
11. **Spawn Egg Model**: `src/main/resources/assets/dogs-unleashed/models/item/{breed_name}_spawn_egg.json`
12. **fabric.mod.json**: Add game test entrypoint
13. **Game Tests**: `src/main/java/com/grahambartley/dogsunleashed/gametest/{BreedName}EntityGameTest.java`
14. **Common Tests**: Update `UnleashedDogEntityGameTest.java`
15. **Unit Tests**: Update `DogModelTest.java` and `DogRendererTest.java`
