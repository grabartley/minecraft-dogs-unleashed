package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

public enum UnleashedDogBreed {
  HUSKY(
      "husky",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.54f),
          new CarryProfile(0.0, 0.0, 0.40f),
          new CarryProfile(0.0, -0.06, 0.66f)),
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
      new Voice(true, null),
      true,
      new RenderTransforms(1.3f, 0.5f, 0.0f)),
  DACHSHUND(
      "dachshund",
      "mouth",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.31f),
          new CarryProfile(0.0, 0.0, 0.32f),
          new CarryProfile(0.0, 0.05, 0.40f)),
      new SpawnEggColors(0xA0673F, 0xDC8847),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.MEADOW),
      new Attributes(10.0, 0.25, 2.0),
      new Voice(false, () -> ModSounds.DACHSHUND_BARK),
      false,
      new RenderTransforms(1.3f, 0.75f, 180.0f)),
  BEAGLE(
      "beagle",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.36f),
          new CarryProfile(0.0, 0.0, 0.38f),
          new CarryProfile(0.0, -0.05, 0.45f)),
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
      new Voice(false, () -> ModSounds.BEAGLE_BARK),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f)),
  GOLDEN_RETRIEVER(
      "goldenretriever",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.41f),
          new CarryProfile(0.0, 0.0, 0.40f),
          new CarryProfile(0.0, -0.06, 0.50f)),
      new SpawnEggColors(0xDAA06D, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 3, BiomeKeys.BEACH),
      new Attributes(24.0, 0.30, 4.0),
      new Voice(false, () -> ModSounds.GOLDEN_RETRIEVER_BARK),
      false,
      new RenderTransforms(1.7f, 0.85f, 0.0f)),
  SHIBA_INU(
      "shibainu",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.36f),
          new CarryProfile(0.0, 0.0, 0.38f),
          new CarryProfile(0.0, -0.05, 0.45f)),
      new SpawnEggColors(0xCE8346, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.CHERRY_GROVE),
      new Attributes(18.0, 0.32, 3.5),
      new Voice(false, () -> ModSounds.SHIBA_INU_BARK),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f)),
  CROSS_BREED(
      "crossbreed",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.36f),
          new CarryProfile(0.0, 0.0, 0.38f),
          new CarryProfile(0.0, -0.05, 0.45f)),
      new SpawnEggColors(0x8B7355, 0xC4A484),
      new Dimensions(0.8f, 1.1f),
      null,
      new Attributes(18.0, 0.29, 3.5),
      new Voice(false, null),
      false,
      new RenderTransforms(1.5f, 0.75f, 0.0f));

  public static final UnleashedDogBreed FALLBACK_RIG = HUSKY;

  private final String serializedId;
  private final String mouthAnchorBoneName;
  private final FetchCarryProfiles fetchCarryProfiles;
  private final SpawnEggColors spawnEggColors;
  private final Dimensions dimensions;
  private final @Nullable SpawnSettings spawnSettings;
  private final Attributes attributes;
  private final Voice voice;
  private final boolean hasEyeColorVariants;
  private final RenderTransforms renderTransforms;

  UnleashedDogBreed(
      final String serializedId,
      final String mouthAnchorBoneName,
      final FetchCarryProfiles fetchCarryProfiles,
      final SpawnEggColors spawnEggColors,
      final Dimensions dimensions,
      final @Nullable SpawnSettings spawnSettings,
      final Attributes attributes,
      final Voice voice,
      final boolean hasEyeColorVariants,
      final RenderTransforms renderTransforms) {
    this.serializedId = serializedId;
    this.mouthAnchorBoneName = mouthAnchorBoneName;
    this.fetchCarryProfiles = fetchCarryProfiles;
    this.spawnEggColors = spawnEggColors;
    this.dimensions = dimensions;
    this.spawnSettings = spawnSettings;
    this.attributes = attributes;
    this.voice = voice;
    this.hasEyeColorVariants = hasEyeColorVariants;
    this.renderTransforms = renderTransforms;
  }

  public String serializedId() {
    return this.serializedId;
  }

  public String translationKey() {
    return "entity.dogs-unleashed." + this.serializedId;
  }

  public String animationPath() {
    return "animations/" + this.serializedId + ".animation.json";
  }

  public String mouthAnchorBoneName() {
    return this.mouthAnchorBoneName;
  }

  public FetchCarryProfiles fetchCarryProfiles() {
    return this.fetchCarryProfiles;
  }

  public CarryProfile carryProfileFor(final FetchItemType fetchType) {
    return this.fetchCarryProfiles.forFetchItem(fetchType);
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

  public boolean hasEyeColorVariants() {
    return this.hasEyeColorVariants;
  }

  public RenderTransforms renderTransforms() {
    return this.renderTransforms;
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

  public record Voice(boolean howls, @Nullable Supplier<SoundEvent> barkSound) {}

  public record RenderTransforms(float adultScale, float babyScale, float bodyYawOffsetDegrees) {}
}
