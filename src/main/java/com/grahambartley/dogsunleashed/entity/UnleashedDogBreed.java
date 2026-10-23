package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

public enum UnleashedDogBreed {
  HUSKY(
      "husky",
      new FetchCarryProfiles(new CarryProfile(0.0, -0.173), new CarryProfile(0.02, -0.173)),
      new SpawnEggColors(0xFFFFFF, 0x808080),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(
          10,
          2,
          4,
          BiomeKeys.SNOWY_TAIGA,
          BiomeKeys.SNOWY_PLAINS,
          BiomeKeys.ICE_SPIKES,
          BiomeKeys.FROZEN_PEAKS,
          BiomeKeys.SNOWY_SLOPES,
          BiomeKeys.GROVE),
      new Attributes(25.0, 0.30, 5.0),
      new Voice(true, null, null, () -> ModSounds.HUSKY_HOWL, null, false),
      true,
      new RenderTransforms(1.3f, 0.5f, 0.0f, 1.31f),
      new FurProfile(DogFurStage.OVERGROWN, 48000, 2),
      Nature.ORDINARY),
  DACHSHUND(
      "dachshund",
      new FetchCarryProfiles(new CarryProfile(0.03, 0.09), new CarryProfile(0.03, 0.09)),
      new SpawnEggColors(0xA0673F, 0xDC8847),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.MEADOW),
      new Attributes(10.0, 0.25, 2.0),
      new Voice(
          false,
          () -> ModSounds.DACHSHUND_BARK,
          () -> ModSounds.DACHSHUND_ALERT,
          null,
          null,
          false),
      false,
      new RenderTransforms(1.3f, 0.75f, 180.0f, 0.57f),
      new FurProfile(DogFurStage.SHAGGY, 72000, 1),
      Nature.ORDINARY),
  BEAGLE(
      "beagle",
      new FetchCarryProfiles(new CarryProfile(-0.025, -0.053), new CarryProfile(-0.025, -0.053)),
      new SpawnEggColors(0xFFFFFF, 0x936732),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(
          10,
          1,
          3,
          BiomeKeys.FLOWER_FOREST,
          BiomeKeys.FOREST,
          BiomeKeys.BIRCH_FOREST,
          BiomeKeys.OLD_GROWTH_BIRCH_FOREST,
          BiomeKeys.MEADOW),
      new Attributes(17.0, 0.29, 3.0),
      new Voice(
          false, () -> ModSounds.BEAGLE_BARK, () -> ModSounds.BEAGLE_ALERT, null, null, false),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f, 0.88f),
      new FurProfile(DogFurStage.SHAGGY, 72000, 1),
      Nature.ORDINARY.trackingScentUpTo(64)),
  GOLDEN_RETRIEVER(
      "goldenretriever",
      new FetchCarryProfiles(new CarryProfile(-0.03, -0.075), new CarryProfile(-0.03, -0.075)),
      new SpawnEggColors(0xDAA06D, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 3, BiomeKeys.BEACH),
      new Attributes(24.0, 0.30, 4.0),
      new Voice(
          false,
          () -> ModSounds.GOLDEN_RETRIEVER_BARK,
          () -> ModSounds.GOLDEN_RETRIEVER_ALERT,
          null,
          null,
          false),
      false,
      new RenderTransforms(1.7f, 0.85f, 0.0f, 1.31f),
      new FurProfile(DogFurStage.OVERGROWN, 48000, 2),
      Nature.ORDINARY),
  SHIBA_INU(
      "shibainu",
      new FetchCarryProfiles(new CarryProfile(0.0, -0.048), new CarryProfile(0.0, -0.048)),
      new SpawnEggColors(0xCE8346, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.CHERRY_GROVE),
      new Attributes(18.0, 0.32, 3.5),
      new Voice(
          false,
          () -> ModSounds.SHIBA_INU_BARK,
          () -> ModSounds.SHIBA_INU_ALERT,
          null,
          null,
          false),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f, 0.88f),
      new FurProfile(DogFurStage.OVERGROWN, 60000, 2),
      Nature.ORDINARY),
  DIRE_WOLF(
      "direwolf",
      new FetchCarryProfiles(new CarryProfile(-0.017, -0.183), new CarryProfile(-0.017, -0.183)),
      new SpawnEggColors(0x5E6268, 0xD9D9D3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(
          2,
          1,
          1,
          BiomeKeys.ICE_SPIKES,
          BiomeKeys.FROZEN_PEAKS,
          BiomeKeys.JAGGED_PEAKS,
          BiomeKeys.SNOWY_SLOPES,
          BiomeKeys.SNOWY_TAIGA),
      new Attributes(30.0, 0.32, 6.0),
      new Voice(
          true,
          () -> ModSounds.DIRE_WOLF_BARK,
          () -> ModSounds.DIRE_WOLF_ALERT,
          () -> ModSounds.DIRE_WOLF_HOWL,
          () -> ModSounds.DIRE_WOLF_GROWL,
          true),
      false,
      new RenderTransforms(1.47f, 0.735f, 0.0f, 1.31f),
      new FurProfile(DogFurStage.OVERGROWN, 48000, 2),
      Nature.ORDINARY
          .breedingOnlyWithOwnBreed()
          .growingThroughStages()
          .sleepingOutsideBeds()
          .livingInPacks()
          .tamingThroughTrust()),
  PINSCHER(
      "pinscher",
      new FetchCarryProfiles(new CarryProfile(0.0, -0.048), new CarryProfile(0.0, -0.048)),
      new SpawnEggColors(0x1C1A18, 0xB0622A),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS),
      new Attributes(12.0, 0.31, 2.0),
      new Voice(
          false, () -> ModSounds.PINSCHER_BARK, () -> ModSounds.PINSCHER_ALERT, null, null, false),
      false,
      new RenderTransforms(1.0f, 0.5f, 0.0f, 0.59f),
      new FurProfile(DogFurStage.SHAGGY, 72000, 1),
      Nature.ORDINARY),
  CROSS_BREED(
      "crossbreed",
      null,
      new SpawnEggColors(0x8B7355, 0xC4A484),
      new Dimensions(0.8f, 1.1f),
      null,
      new Attributes(18.0, 0.29, 3.5),
      new Voice(false, null, null, null, null, false),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f, 0.88f),
      new FurProfile(DogFurStage.SHAGGY, 60000, 1),
      Nature.ORDINARY);

  public static final UnleashedDogBreed FALLBACK_RIG = HUSKY;

  private final String serializedId;
  private final @Nullable FetchCarryProfiles fetchCarryProfiles;
  private final SpawnEggColors spawnEggColors;
  private final Dimensions dimensions;
  private final @Nullable SpawnSettings spawnSettings;
  private final Attributes attributes;
  private final Voice voice;
  private final boolean hasEyeColorVariants;
  private final RenderTransforms renderTransforms;
  private final FurProfile furProfile;
  private final Nature nature;

  UnleashedDogBreed(
      final String serializedId,
      final @Nullable FetchCarryProfiles fetchCarryProfiles,
      final SpawnEggColors spawnEggColors,
      final Dimensions dimensions,
      final @Nullable SpawnSettings spawnSettings,
      final Attributes attributes,
      final Voice voice,
      final boolean hasEyeColorVariants,
      final RenderTransforms renderTransforms,
      final FurProfile furProfile,
      final Nature nature) {
    this.serializedId = serializedId;
    this.fetchCarryProfiles = fetchCarryProfiles;
    this.spawnEggColors = spawnEggColors;
    this.dimensions = dimensions;
    this.spawnSettings = spawnSettings;
    this.attributes = attributes;
    this.voice = voice;
    this.hasEyeColorVariants = hasEyeColorVariants;
    this.renderTransforms = renderTransforms;
    this.furProfile = furProfile;
    this.nature = nature;
  }

  public String serializedId() {
    return this.serializedId;
  }

  public String translationKey() {
    return "entity.dogs-unleashed." + this.serializedId;
  }

  public Identifier animationId() {
    return Identifier.of(
        DogsUnleashed.MOD_ID, "animations/" + this.serializedId + ".animation.json");
  }

  public String geoPath() {
    return "geo/" + this.serializedId + ".geo.json";
  }

  public Optional<FetchCarryProfiles> fetchCarryProfiles() {
    return Optional.ofNullable(this.fetchCarryProfiles);
  }

  public Optional<CarryProfile> carryProfileFor(final @Nullable FetchItemType fetchType) {
    return this.fetchCarryProfiles().map(profiles -> profiles.forFetchItem(fetchType));
  }

  public SpawnEggColors spawnEggColors() {
    return this.spawnEggColors;
  }

  public Dimensions dimensions() {
    return this.dimensions;
  }

  public boolean isNaturallySpawning() {
    return this.spawnSettings != null;
  }

  public boolean isCrossBreedFounder() {
    return this.isNaturallySpawning() && !this.nature.breedsOnlyWithOwnBreed();
  }

  public @Nullable SpawnSettings spawnSettings() {
    return this.spawnSettings;
  }

  public Attributes attributes() {
    return this.attributes;
  }

  public boolean howls() {
    return this.voice.howls();
  }

  public boolean hasBarkSound() {
    return this.voice.barkSound() != null;
  }

  public @Nullable SoundEvent barkSound() {
    final Supplier<SoundEvent> barkSound = this.voice.barkSound();
    return barkSound == null ? null : barkSound.get();
  }

  public @Nullable SoundEvent alertSound() {
    final Supplier<SoundEvent> alertSound = this.voice.alertSound();
    return alertSound == null ? null : alertSound.get();
  }

  public @Nullable SoundEvent howlSound() {
    final Supplier<SoundEvent> howlSound = this.voice.howlSound();
    return howlSound == null ? null : howlSound.get();
  }

  public @Nullable SoundEvent growlSound() {
    final Supplier<SoundEvent> growlSound = this.voice.growlSound();
    return growlSound == null ? null : growlSound.get();
  }

  public boolean barkMovesJaw() {
    return this.voice.barkMovesJaw();
  }

  public boolean hasEyeColorVariants() {
    return this.hasEyeColorVariants;
  }

  public RenderTransforms renderTransforms() {
    return this.renderTransforms;
  }

  public FurProfile furProfile() {
    return this.furProfile;
  }

  public Nature nature() {
    return this.nature;
  }

  public DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, this.attributes.maxHealth())
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, this.attributes.movementSpeed())
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, this.attributes.attackDamage());
  }

  public static @Nullable UnleashedDogBreed fromSerializedIdOrNull(final @Nullable String id) {
    if (id == null || id.isEmpty()) {
      return null;
    }

    return switch (id.toLowerCase(Locale.ROOT)) {
      case "husky" -> HUSKY;
      case "dachshund" -> DACHSHUND;
      case "beagle" -> BEAGLE;
      case "goldenretriever", "golden_retriever" -> GOLDEN_RETRIEVER;
      case "shibainu", "shiba_inu" -> SHIBA_INU;
      case "pinscher" -> PINSCHER;
      case "direwolf", "dire_wolf" -> DIRE_WOLF;
      case "crossbreed", "cross_breed" -> CROSS_BREED;
      default -> null;
    };
  }

  public static UnleashedDogBreed fromSerializedId(final String id) {
    final UnleashedDogBreed breed = fromSerializedIdOrNull(id);
    return breed == null ? HUSKY : breed;
  }

  public record SpawnEggColors(int primary, int secondary) {}

  public record Dimensions(float width, float height) {}

  public record SpawnSettings(
      int weight, int minGroupSize, int maxGroupSize, RegistryKey<Biome>... biomes) {}

  public record Attributes(double maxHealth, double movementSpeed, double attackDamage) {}

  public record Voice(
      boolean howls,
      @Nullable Supplier<SoundEvent> barkSound,
      @Nullable Supplier<SoundEvent> alertSound,
      @Nullable Supplier<SoundEvent> howlSound,
      @Nullable Supplier<SoundEvent> growlSound,
      boolean barkMovesJaw) {}

  public record RenderTransforms(
      float adultScale, float babyScale, float bodyYawOffsetDegrees, float portraitHeight) {}

  public record FurProfile(DogFurStage maxStage, int ticksPerStage, int shearYieldPerStage) {}

  public record Nature(
      int tameSuccessChance,
      boolean breedsOnlyWithOwnBreed,
      boolean growsThroughStages,
      boolean sleepsInBeds,
      boolean livesInPacks,
      boolean tamesThroughTrust,
      int scentRange) {
    public static final int ORDINARY_SCENT_RANGE = 32;
    public static final Nature ORDINARY =
        new Nature(3, false, false, true, false, false, ORDINARY_SCENT_RANGE);

    public Nature breedingOnlyWithOwnBreed() {
      return new Nature(
          tameSuccessChance,
          true,
          growsThroughStages,
          sleepsInBeds,
          livesInPacks,
          tamesThroughTrust,
          scentRange);
    }

    public Nature growingThroughStages() {
      return new Nature(
          tameSuccessChance,
          breedsOnlyWithOwnBreed,
          true,
          sleepsInBeds,
          livesInPacks,
          tamesThroughTrust,
          scentRange);
    }

    public Nature sleepingOutsideBeds() {
      return new Nature(
          tameSuccessChance,
          breedsOnlyWithOwnBreed,
          growsThroughStages,
          false,
          livesInPacks,
          tamesThroughTrust,
          scentRange);
    }

    public Nature livingInPacks() {
      return new Nature(
          tameSuccessChance,
          breedsOnlyWithOwnBreed,
          growsThroughStages,
          sleepsInBeds,
          true,
          tamesThroughTrust,
          scentRange);
    }

    public Nature tamingThroughTrust() {
      return new Nature(
          tameSuccessChance,
          breedsOnlyWithOwnBreed,
          growsThroughStages,
          sleepsInBeds,
          livesInPacks,
          true,
          scentRange);
    }

    public Nature trackingScentUpTo(final int blocks) {
      return new Nature(
          tameSuccessChance,
          breedsOnlyWithOwnBreed,
          growsThroughStages,
          sleepsInBeds,
          livesInPacks,
          tamesThroughTrust,
          blocks);
    }
  }
}
