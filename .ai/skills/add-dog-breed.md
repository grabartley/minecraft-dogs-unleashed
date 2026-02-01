---
description: Add a new dog breed to the Dogs Unleashed Minecraft mod with all required files and tests
---

# Add Dog Breed Skill

This skill guides adding new dog breeds to the Dogs Unleashed Minecraft mod.

## Prerequisites

Before starting, ensure you have the following files ready:
- **Animation file**: `{breed_name}.animation.json` - GeckoLib animation file from Blockbench
- **Geo file**: `{breed_name}.geo.json` - GeckoLib model file from Blockbench
- **Texture file**: `{breed_name}.png` - Entity texture (typically 64x32 or custom size)
- **Collar texture file**: `{breed_name}_collar.png` - Collar overlay texture

## File Creation Checklist

When adding a new breed (e.g., `Labrador`), create/modify the following files:

### 1. Entity Class (src/main)

**Path**: `src/main/java/com/grahambartley/entity/{BreedName}Entity.java`

```java
package com.grahambartley.entity;

import static com.grahambartley.ModEntities.{BREED_NAME_UPPER};

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;

public class {BreedName}Entity extends UnleashedDogEntity implements GeoEntity, Angerable {

public static DefaultAttributeContainer.Builder createAttributes() {
	return MobEntity.createMobAttributes()
		.add(EntityAttributes.GENERIC_MAX_HEALTH, {health_value})
		.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, {speed_value})
		.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, {damage_value});
}

public {BreedName}Entity(EntityType<? extends TameableEntity> entityType, World world) {
	super(entityType, world);
}

@Override
protected UnleashedDogEntity createBaby(ServerWorld world) {
	return new {BreedName}Entity({BREED_NAME_UPPER}, world);
}

@Override
protected boolean isSameSpecies(MobEntity entity) {
	return entity instanceof {BreedName}Entity;
}
}
```

**Attribute Guidelines**:
- `GENERIC_MAX_HEALTH`: 15.0-25.0 (vanilla wolf is 20.0)
- `GENERIC_MOVEMENT_SPEED`: 0.25-0.35 (vanilla wolf is 0.30)
- `GENERIC_ATTACK_DAMAGE`: 2.0-6.0 (vanilla wolf is 4.0)

### 2. Constants (src/main)

**Path**: `src/main/java/com/grahambartley/ModConstants.java`

Add the following constants:

```java
public static final int {BREED_NAME_UPPER}_SPAWN_EGG_PRIMARY_COLOR = 0x{HEX_COLOR};
public static final int {BREED_NAME_UPPER}_SPAWN_EGG_SECONDARY_COLOR = 0x{HEX_COLOR};

public static final float {BREED_NAME_UPPER}_WIDTH = 0.8f;
public static final float {BREED_NAME_UPPER}_HEIGHT = 1.1f;

public static final int {BREED_NAME_UPPER}_SPAWN_WEIGHT = 10;
public static final int {BREED_NAME_UPPER}_SPAWN_MIN_GROUP = 1;
public static final int {BREED_NAME_UPPER}_SPAWN_MAX_GROUP = 3;
```

### 3. Entity Registration (src/main)

**Path**: `src/main/java/com/grahambartley/ModEntities.java`

Add the entity type registration:

```java
public static final EntityType<{BreedName}Entity> {BREED_NAME_UPPER} =
	Registry.register(
		Registries.ENTITY_TYPE,
		Identifier.of(DogsUnleashed.MOD_ID, "{breed_name}"),
		EntityType.Builder.create({BreedName}Entity::new, SpawnGroup.CREATURE)
			.dimensions(ModConstants.{BREED_NAME_UPPER}_WIDTH, ModConstants.{BREED_NAME_UPPER}_HEIGHT)
			.build(Identifier.of(DogsUnleashed.MOD_ID, "{breed_name}").toString()));
```

In the `initialize()` method, add:

```java
FabricDefaultAttributeRegistry.register({BREED_NAME_UPPER}, {BreedName}Entity.createAttributes());
```

### 4. Items Registration (src/main)

**Path**: `src/main/java/com/grahambartley/ModItems.java`

Add the spawn egg:

```java
public static final Item {BREED_NAME_UPPER}_SPAWN_EGG =
	Registry.register(
		Registries.ITEM,
		Identifier.of(DogsUnleashed.MOD_ID, "{breed_name}_spawn_egg"),
		new SpawnEggItem(
			ModEntities.{BREED_NAME_UPPER},
			ModConstants.{BREED_NAME_UPPER}_SPAWN_EGG_PRIMARY_COLOR,
			ModConstants.{BREED_NAME_UPPER}_SPAWN_EGG_SECONDARY_COLOR,
			new Item.Settings()));
```

In the `initialize()` method, add:

```java
ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
	.register(entries -> entries.add({BREED_NAME_UPPER}_SPAWN_EGG));
```

### 5. Spawn Configuration (src/main)

**Path**: `src/main/java/com/grahambartley/ModSpawns.java`

Add biome spawning (choose appropriate biomes):

```java
BiomeModifications.addSpawn(
	BiomeSelectors.includeByKey(BiomeKeys.{BIOME_KEY}),
	SpawnGroup.CREATURE,
	ModEntities.{BREED_NAME_UPPER},
	ModConstants.{BREED_NAME_UPPER}_SPAWN_WEIGHT,
	ModConstants.{BREED_NAME_UPPER}_SPAWN_MIN_GROUP,
	ModConstants.{BREED_NAME_UPPER}_SPAWN_MAX_GROUP);

SpawnRestriction.register(
	ModEntities.{BREED_NAME_UPPER},
	SpawnLocationTypes.ON_GROUND,
	Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
	AnimalEntity::isValidNaturalSpawn);
```

### 6. Model Class (src/client)

**Path**: `src/client/java/com/grahambartley/model/{BreedName}Model.java`

```java
package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.{BreedName}Entity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class {BreedName}Model extends GeoModel<{BreedName}Entity> {

@Override
public Identifier getModelResource({BreedName}Entity animatable) {
	return Identifier.of(MOD_ID, "geo/{breed_name}.geo.json");
}

@Override
public Identifier getTextureResource({BreedName}Entity animatable) {
	return Identifier.of(MOD_ID, "textures/entity/{breed_name}.png");
}

@Override
public Identifier getAnimationResource({BreedName}Entity animatable) {
	return Identifier.of(MOD_ID, "animations/{breed_name}.animation.json");
}
}
```

### 7. Collar Layer (src/client)

**Path**: `src/client/java/com/grahambartley/render/layer/{BreedName}CollarLayer.java`

```java
package com.grahambartley.render.layer;

import com.grahambartley.entity.{BreedName}Entity;
import com.grahambartley.render.{BreedName}Renderer;
import net.minecraft.util.Identifier;

public class {BreedName}CollarLayer extends CollarLayer<{BreedName}Entity> {

private final Identifier collarTexture =
	Identifier.of("dogs-unleashed", "textures/entity/{breed_name}_collar.png");

public {BreedName}CollarLayer(final {BreedName}Renderer {breedName}Renderer) {
	super({breedName}Renderer);
}

public Identifier getCollarTexture() {
	return collarTexture;
}
}
```

### 8. Renderer Class (src/client)

**Path**: `src/client/java/com/grahambartley/render/{BreedName}Renderer.java`

```java
package com.grahambartley.render;

import com.grahambartley.entity.{BreedName}Entity;
import com.grahambartley.model.{BreedName}Model;
import com.grahambartley.render.layer.{BreedName}CollarLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class {BreedName}Renderer extends GeoEntityRenderer<{BreedName}Entity> {
public {BreedName}Renderer(EntityRendererFactory.Context context) {
	super(context, new {BreedName}Model());
	this.addRenderLayer(new {BreedName}CollarLayer(this));
}

@Override
public float getMotionAnimThreshold({BreedName}Entity animatable) {
	return 0.005f;
}

@Override
public void preRender(
	MatrixStack poseStack,
	{BreedName}Entity animatable,
	BakedGeoModel model,
	VertexConsumerProvider bufferSource,
	VertexConsumer buffer,
	boolean isReRender,
	float partialTick,
	int packedLight,
	int packedOverlay,
	int colour) {
	if (!isReRender) {
	final float scale = animatable.isBaby() ? 0.5f : 1.3f;
	poseStack.scale(scale, scale, scale);
	}
	super.preRender(
		poseStack,
		animatable,
		model,
		bufferSource,
		buffer,
		isReRender,
		partialTick,
		packedLight,
		packedOverlay,
		colour);
}
}
```

### 9. Client Initializer (src/client)

**Path**: `src/client/java/com/grahambartley/DogsUnleashedClient.java`

Add renderer registration:

```java
EntityRendererRegistry.register(ModEntities.{BREED_NAME_UPPER}, {BreedName}Renderer::new);
```

### 10. Resource Files

#### Language File
**Path**: `src/main/resources/assets/dogs-unleashed/lang/en_us.json`

Add entries:
```json
"entity.dogs-unleashed.{breed_name}": "{Breed Name}",
"item.dogs-unleashed.{breed_name}_spawn_egg": "{Breed Name} Spawn Egg"
```

#### Spawn Egg Model
**Path**: `src/main/resources/assets/dogs-unleashed/models/item/{breed_name}_spawn_egg.json`

```json
{
"parent": "minecraft:item/template_spawn_egg"
}
```

#### Asset Files (provided by user)
- `src/client/resources/assets/dogs-unleashed/geo/{breed_name}.geo.json`
- `src/client/resources/assets/dogs-unleashed/animations/{breed_name}.animation.json`
- `src/client/resources/assets/dogs-unleashed/textures/entity/{breed_name}.png`
- `src/client/resources/assets/dogs-unleashed/textures/entity/{breed_name}_collar.png`

### 11. fabric.mod.json

**Path**: `src/main/resources/fabric.mod.json`

Add the game test entrypoint:
```json
"fabric-gametest": [
...existing entries...,
"com.grahambartley.gametest.{BreedName}EntityGameTest"
]
```

## Testing Requirements

### Unit Tests

Update the following parameterized tests to include the new breed:

**Path**: `src/test/java/com/grahambartley/model/DogModelTest.java`

Add to `@ValueSource`:
```java
@ValueSource(strings = {"HuskyModel", "DachshundModel", "BeagleModel", "{BreedName}Model"})
```

**Path**: `src/test/java/com/grahambartley/render/DogRendererTest.java`

Add to `@ValueSource`:
```java
@ValueSource(strings = {"HuskyRenderer", "DachshundRenderer", "BeagleRenderer", "{BreedName}Renderer"})
```

### Game Tests

#### Breed-Specific Game Test

**Path**: `src/main/java/com/grahambartley/gametest/{BreedName}EntityGameTest.java`

```java
package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.{BreedName}Entity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class {BreedName}EntityGameTest implements FabricGameTest {

@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
public void {breedName}HasCorrectAttributes(final TestContext context) {
	final BlockPos spawnPos = new BlockPos(0, 1, 0);
	final ServerWorld world = context.getWorld();

	final {BreedName}Entity {breedName} = new {BreedName}Entity(ModEntities.{BREED_NAME_UPPER}, world);
	{breedName}.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
	world.spawnEntity({breedName});

	context.assertTrue(
		{breedName}.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == {health_value},
		"{BreedName} max health should be {health_value}");
	context.assertTrue(
		{breedName}.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == {speed_value},
		"{BreedName} movement speed should be {speed_value}");
	context.assertTrue(
		{breedName}.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == {damage_value},
		"{BreedName} attack damage should be {damage_value}");
	context.complete();
}

@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
public void {breedName}Creates{BreedName}Baby(final TestContext context) {
	final BlockPos spawnPos = new BlockPos(0, 1, 0);
	final ServerWorld world = context.getWorld();

	final {BreedName}Entity parent1 = new {BreedName}Entity(ModEntities.{BREED_NAME_UPPER}, world);
	parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
	parent1.setTamed(true, true);
	world.spawnEntity(parent1);

	final {BreedName}Entity parent2 = new {BreedName}Entity(ModEntities.{BREED_NAME_UPPER}, world);
	parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
	parent2.setTamed(true, true);
	world.spawnEntity(parent2);

	context.runAtTick(
		10,
		() -> {
		final {BreedName}Entity baby = ({BreedName}Entity) parent1.createChild(world, parent2);

		context.assertTrue(baby != null, "Baby should be created from two {breedName}s");
		context.assertTrue(baby instanceof {BreedName}Entity, "Baby should be a {BreedName}Entity");
		context.assertTrue(baby.isBaby(), "Created entity should be a baby");
		context.complete();
		});
}
}
```

#### Common Game Tests (UnleashedDogEntityGameTest.java)

Add the new breed's TestData and test methods to `UnleashedDogEntityGameTest.java`:

1. Add TestData constant:
```java
private static final TestData<{BreedName}Entity> {BREED_NAME_UPPER}_DATA =
	new TestData<>(
		ModEntities.{BREED_NAME_UPPER},
		(type, world) -> new {BreedName}Entity((EntityType<? extends UnleashedDogEntity>) type, world),
		0.8f,
		1.1f);
```

2. Add all standard test methods for the new breed (follow existing pattern for Husky/Dachshund/Beagle):
- `{breedName}SpawnsCorrectly`
- `{breedName}IsTameable`
- `{breedName}CanBeTamed`
- `{breedName}HasCorrectDimensions`
- `{breedName}HasAnimatableInstanceCache`
- `untamed{BreedName}HasDefaultCollarColor`
- `tamed{BreedName}CollarColorCanBeChanged`
- `{breedName}CollarColorPersistsInNbt`
- `baby{BreedName}HasCollarWhenTamed`
- `{breedName}AllDyeColorsWorkOnCollar`
- `{breedName}BredBabyInheritsParentTamedStatus`
- `{breedName}BoneIsTamingItem`
- `{breedName}MeatItemsAreBothTamingAndBreeding`
- `{breedName}ShakesOnceWhenLeavingWater`
- `{breedName}ShakeProgressDecrementsEachTick`
- `{breedName}ShakeProgressPersistsInNbt`
- `{breedName}HeadTiltingStateCanBeTracked`
- `{breedName}OnlyBreedsSameSpecies`

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run all checks (lint + tests)
./gradlew check

# Run game tests locally
./gradlew runGametest

# Run game tests headless (CI/CD style)
./gradlew runGametestServer
```

## Naming Conventions

| Type | Format | Example |
|------|--------|---------|
| Entity class | `{BreedName}Entity` | `LabradorEntity` |
| Model class | `{BreedName}Model` | `LabradorModel` |
| Renderer class | `{BreedName}Renderer` | `LabradorRenderer` |
| Collar layer | `{BreedName}CollarLayer` | `LabradorCollarLayer` |
| Constant prefix | `{BREED_NAME_UPPER}_` | `LABRADOR_` |
| Resource files | `{breed_name}.*` | `labrador.geo.json` |
| Entity ID | `{breed_name}` | `labrador` |

## Biome Selection Guide

Choose biomes appropriate for the breed's real-world habitat:

- **Arctic/Cold breeds** (Husky, Malamute): `SNOWY_TAIGA`, `SNOWY_PLAINS`, `ICE_SPIKES`, `FROZEN_PEAKS`
- **Forest breeds** (Beagle, Hound): `FLOWER_FOREST`, `FOREST`, `BIRCH_FOREST`
- **Plains breeds** (Dachshund, Retriever): `PLAINS`, `SUNFLOWER_PLAINS`, `MEADOW`
- **Mountain breeds** (Bernese, St. Bernard): `MEADOW`, `GROVE`, `SNOWY_SLOPES`
- **Desert breeds** (Basenji): `DESERT`, `BADLANDS`
- **Beach breeds** (Golden Retriever): `BEACH`

## Validation Checklist

Before submitting:

- [ ] All Java files compile without errors
- [ ] Linting passes: `./gradlew spotlessApply`
- [ ] Unit tests pass: `./gradlew test`
- [ ] Game tests pass: `./gradlew runGametestServer`
- [ ] All resource files are in correct locations
- [ ] Language entries added
- [ ] Spawn egg model created
- [ ] Entity spawns correctly in-game
- [ ] Collar renders and dye colors work
- [ ] Baby entities spawn correctly from breeding
- [ ] Animations play correctly (idle, walk, sit, tail_wag, shake, head_tilt)
