---
name: add-dog-breed
description: Add a new dog breed to the Dogs Unleashed Minecraft mod with all required files and tests. Use when asked to add or create a new dog breed.
---

# Add Dog Breed

## Workflow Requirement

1. Run the `build` skill first with the linked GitHub issue.
2. Let `build` handle `worktree` setup, implementation flow, testing, issue status updates, and `pr` handoff.
3. Use this skill as a breed-specific implementation checklist inside that build flow.

## Architecture

There are NO per-breed Java classes. One concrete `UnleashedDogEntity` backs every breed, one
`DogRenderer` / `DogModel` / `DogCollarLayer` renders every breed, and breed identity is data on
the `UnleashedDogBreed` preset enum. Adding a breed means adding a preset constant, assets, and a
handful of switch cases; the shared entity, renderer, spawn wiring, config plumbing, and per-breed
gametest generators pick the new breed up from there.

## Prerequisites

Ensure you have these assets ready (all under `src/main/resources/assets/dogs-unleashed/`):
- `geo/{breed_id}.geo.json` - GeckoLib model file
- `animations/{breed_id}.animation.json` - GeckoLib animation file
- `textures/entity/{breed_id}_{coat_prefix}.png` - one texture per coat variant, or a single
  `textures/entity/{breed_id}.png` if the breed has no coat variants (texture naming is derived in
  `DogModel`: base id, then `_{coatPrefix}` if the breed has coats, then `_{eyeSuffix}` if it has
  eye color variants)
- `textures/entity/{breed_id}_collar.png` - collar overlay texture
- `sounds/` bark audio plus a `sounds.json` entry, unless the breed howls instead

`{breed_id}` is the lowercase serialized id with no separators (e.g. `goldenretriever`).

## Files to Create/Modify

1. **Breed preset**: `src/main/java/com/grahambartley/dogsunleashed/entity/UnleashedDogBreed.java`
   - New enum constant carrying: serialized id, mouth anchor bone name, `FetchCarryProfiles`,
     `SpawnEggColors`, `Dimensions`, `SpawnSettings` (or `null` for a non-spawnable breed; every
     `values()` consumer already filters through `isNaturallySpawning()`), `Attributes`, `Voice`
     (howl flag plus a lazy `() -> ModSounds.X_BARK` supplier, or `null` supplier for a howler),
     eye-color-variants flag, and `RenderTransforms` (adult scale, baby scale, body yaw offset).
   - Add the id (and any legacy aliases) to the `fromSerializedIdOrNull` switch.
2. **Sounds**: `src/main/java/com/grahambartley/dogsunleashed/ModSounds.java` - register the bark
   sound referenced by the preset `Voice`.
3. **Entity type**: `src/main/java/com/grahambartley/dogsunleashed/ModEntities.java` - new
   `EntityType<UnleashedDogEntity>` constant via `registerDog(breed)` plus a `getDogEntityType`
   switch case. Attribute registration is the existing `values()` loop; no further wiring.
4. **Spawn egg**: `src/main/java/com/grahambartley/dogsunleashed/ModItems.java` plus
   `src/main/resources/assets/dogs-unleashed/models/item/{breed_id}_spawn_egg.json`.
5. **Coat variants** (skip if the breed has a single fixed texture):
   - `src/main/java/com/grahambartley/dogsunleashed/entity/variant/{BreedName}Coat.java`
     implementing `UnleashedDogCoat`
   - `src/main/java/com/grahambartley/dogsunleashed/entity/variant/{BreedName}CoatRolls.java`
     with natural and breeding roll thresholds over `DogCoats.ROLL_BOUND`
   - Wire both switches in `entity/variant/DogCoats.java`: `coatOf` and `rollResolverFor`
     (rarity derivation in `DogRarityClassifier` delegates to these automatically)
6. **Client registration**: `src/client/java/com/grahambartley/dogsunleashed/DogsUnleashedClient.java` -
   one `EntityRendererRegistry.register(ModEntities.{BREED}, DogRenderer::new)` line. Do NOT
   create model, renderer, or collar-layer classes.
7. **Breed filter**: `src/client/java/com/grahambartley/dogsunleashed/screen/PetManagerScreen.java` -
   new `BreedFilterOption` entry.
8. **Language**: `src/main/resources/assets/dogs-unleashed/lang/en_us.json` - entity name, spawn
   egg name, and `coat.dogs-unleashed.{breed_id}.*` keys for every coat.
9. **Test data**: `src/main/java/com/grahambartley/dogsunleashed/gametest/util/DogTestData.java` -
   new constant via `fromPreset` plus a `getAllBreeds()` entry. Every per-breed
   `@CustomTestProvider` generator across the gametest suite fans out to the new breed from this
   single list; no new gametest class and no `fabric.mod.json` change.
10. **Unit tests**:
    - `UnleashedDogBreedTest` - extend the expected-value data providers (serialized id aliases,
      dimensions, attributes, voices, render transforms)
    - `DogCoatsTest` - extend `coatLookups` and `rollTables` when the breed has coats
