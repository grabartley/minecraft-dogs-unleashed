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
gametest generators pick the new breed up from there. Cross-breeding (`CROSS_BREED` +
`DogGenome`) is fully generic over the founding presets: a new founding breed automatically joins
breed compositions, genome stat blending, mix names, and dominant-breed rendering with no extra
wiring. `CROSS_BREED` itself is NOT a template for new breeds; it has null spawn settings, no
spawn egg item, no coat table, and no assets of its own.

## Prerequisites

The rig starts as a Blockbench project in `~/blockbench/<breed_id>/<breed_id>.bbmodel`, edited
with the `bbmodel` MCP and exported with `bbmodel_export_bedrock_geometry` to the geo path below.
Author and review the geo, the coat and collar textures and the rig profile with the `dog-art`
skill: it renders the rig headless, maps every texture face to its rig profile slot and checks the
sheet before the game is launched. Commit the exports, never the project file.
Ensure you have these assets ready. Client-only assets live under
`src/client/resources/assets/dogs-unleashed/`:
- `geo/{breed_id}.geo.json` - GeckoLib model file
- `animations/{breed_id}.animation.json` - GeckoLib animation file
- `textures/entity/{breed_id}_{coat_prefix}.png` - one texture per coat variant, or a single
  `textures/entity/{breed_id}.png` if the breed has no coat variants (texture naming is derived in
  `DogModel`: base id, then `_{coatPrefix}` if the breed has coats, then `_{eyeSuffix}` if it has
  eye color variants)
- `textures/entity/{breed_id}_collar.png` - collar overlay texture
- `rig_profiles/{breed_id}.json` - rig profile naming the rig's bones by role (see step 7)

Shared assets live under `src/main/resources/assets/dogs-unleashed/`:
- `sounds/` bark audio plus a `sounds.json` entry, unless the breed howls instead
- `lang/en_us.json` entries and the spawn egg item model under `models/item/`

`{breed_id}` is the lowercase serialized id with no separators (e.g. `goldenretriever`).

## Files to Create/Modify

1. **Breed preset**: `src/main/java/com/grahambartley/dogsunleashed/entity/UnleashedDogBreed.java`
   - New enum constant carrying: serialized id, `FetchCarryProfiles`,
     `SpawnEggColors`, `Dimensions`, `SpawnSettings` (or `null` for a non-spawnable breed; every
     `values()` consumer already filters through `isNaturallySpawning()`), `Attributes`, `Voice`
     (howl flag plus a lazy `() -> ModSounds.X_BARK` supplier; a `null` supplier means no bark,
     used by howlers and the voiceless `CROSS_BREED` preset),
     eye-color-variants flag, and `RenderTransforms` (adult scale, baby scale, body yaw offset).
   - Add the id (and any legacy aliases) to the `fromSerializedIdOrNull` switch.
2. **Sounds**: `src/main/java/com/grahambartley/dogsunleashed/ModSounds.java` - register the bark
   sound referenced by the preset `Voice`.
3. **Entity type**: `src/main/java/com/grahambartley/dogsunleashed/ModEntities.java` - new
   `EntityType<UnleashedDogEntity>` constant via `registerDog(breed)` plus a `getDogEntityType`
   switch case. Attribute registration is the existing `values()` loop; no further wiring.
4. **Spawn egg**: `src/main/java/com/grahambartley/dogsunleashed/ModItems.java` plus
   `src/main/resources/assets/dogs-unleashed/models/item/{breed_id}_spawn_egg.json`, parented to
   vanilla's `minecraft:item/template_spawn_egg` with no `display` overrides, so it is held and
   shown exactly like a vanilla spawn egg.
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
7. **Rig profile**: `src/client/resources/assets/dogs-unleashed/rig_profiles/{breed_id}.json` -
   names the rig's bones by role so armour, pendants, carried items and fur find them without any
   bone names in Java. Read the `.geo.json` and fill every body slot: `torso`, `mouth`, `head`,
   `eyes`, `neck`, `tail`, `ear_left`, `ear_right`, `leg_front_left`, `leg_front_right`,
   `leg_back_left`, `leg_back_right`. A slot lists only the bones of its own part, so `head` is the
   skull without the snout or ears. A slot may list several bones; list a cube-less parent bone
   together with the child bones that carry its cubes. Left and right are the dog's own left and
   right, not the viewer's. The `eyes` slot lists the eyelid bones, set up the way ADR 0011
   describes: children of the head, named `eyelid_left` and `eyelid_right` (or `eyelids` for one
   cube), hidden inside the head at rest, held closed by `sleep` and blinked closed once in the
   `idle` loop with the same pose. Add `fur_shaggy`, `fur_overgrown`, `trimmed_overlay` and
   `botched_overlay` once the breed's fur art exists. Never rename bones in the rig to fit a slot.
   `RigProfilesTest` checks every breed and fails when a slot is missing, names a bone the rig does
   not have, or puts a left part on the right. Also add `pupils`: the two texels, as `[x, y]`
   pairs, where the eyes are painted on the head's face texture. Undead dogs glow on exactly
   those texels. `UndeadDogEyesTest` fails when a pupil is off the head or not under the closed
   eyelids. Add a couple of the breed's coats to its `TEXTURES_BY_RIG` so the pupils are also
   checked against dark eye pixels in the art. The body slots are also where shared overlay sheets
   land, so a new breed picks up every sheet effect without new art. If a sheet looks wrong on one
   slot of the new breed, add a hand-made atlas-sized texture under `sheet_overrides`, keyed by
   the sheet and then the slot (see ADR 0006).
8. **Breed filter**: `src/client/java/com/grahambartley/dogsunleashed/screen/PetManagerScreen.java` -
   new `BreedFilterOption` entry.
9. **Language**: `src/main/resources/assets/dogs-unleashed/lang/en_us.json` - entity name, spawn
   egg name, and `coat.dogs-unleashed.{breed_id}.*` keys for every coat.
10. **Test data**: `src/main/java/com/grahambartley/dogsunleashed/gametest/util/DogTestData.java` -
   new constant via `fromPreset` plus a `getAllBreeds()` entry. Every per-breed
   `@CustomTestProvider` generator across the gametest suite fans out to the new breed from this
   single list; no new gametest class and no `fabric.mod.json` change.
11. **Unit tests**:
    - `UnleashedDogBreedTest` - extend the expected-value data providers (serialized id aliases,
      dimensions, attributes, voices, render transforms)
    - `DogCoatsTest` - extend `coatLookups` and `rollTables` when the breed has coats
