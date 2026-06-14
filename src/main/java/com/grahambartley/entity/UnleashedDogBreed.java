package com.grahambartley.entity;

import com.grahambartley.entity.fetch.FetchItemType;
import java.util.Locale;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

public enum UnleashedDogBreed {
  HUSKY(
      "husky",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.41f),
          new CarryProfile(0.0, 0.0, 0.40f),
          new CarryProfile(0.0, 0.0, 0.50f)),
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
      new Attributes(25.0, 0.30, 5.0)),
  DACHSHUND(
      "dachshund",
      "mouth",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.31f),
          new CarryProfile(0.0, 0.0, 0.32f),
          new CarryProfile(0.0, 0.0, 0.40f)),
      new SpawnEggColors(0xA0673F, 0xDC8847),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.MEADOW),
      new Attributes(10.0, 0.25, 2.0)),
  BEAGLE(
      "beagle",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.36f),
          new CarryProfile(0.0, 0.0, 0.38f),
          new CarryProfile(0.0, 0.0, 0.45f)),
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
      new Attributes(17.0, 0.29, 3.0)),
  GOLDEN_RETRIEVER(
      "goldenretriever",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.41f),
          new CarryProfile(0.0, 0.0, 0.40f),
          new CarryProfile(0.0, 0.0, 0.50f)),
      new SpawnEggColors(0xDAA06D, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 3, BiomeKeys.BEACH),
      new Attributes(24.0, 0.30, 4.0)),
  SHIBA_INU(
      "shibainu",
      "snout",
      new FetchCarryProfiles(
          new CarryProfile(0.0, 0.0, 0.36f),
          new CarryProfile(0.0, 0.0, 0.38f),
          new CarryProfile(0.0, 0.0, 0.45f)),
      new SpawnEggColors(0xCE8346, 0xF5DEB3),
      new Dimensions(0.8f, 1.1f),
      new SpawnSettings(10, 1, 2, BiomeKeys.CHERRY_GROVE),
      new Attributes(18.0, 0.32, 3.5));

  private final String serializedId;
  private final String mouthAnchorBoneName;
  private final FetchCarryProfiles fetchCarryProfiles;
  private final SpawnEggColors spawnEggColors;
  private final Dimensions dimensions;
  private final SpawnSettings spawnSettings;
  private final Attributes attributes;

  UnleashedDogBreed(
      final String serializedId,
      final String mouthAnchorBoneName,
      final FetchCarryProfiles fetchCarryProfiles,
      final SpawnEggColors spawnEggColors,
      final Dimensions dimensions,
      final SpawnSettings spawnSettings,
      final Attributes attributes) {
    this.serializedId = serializedId;
    this.mouthAnchorBoneName = mouthAnchorBoneName;
    this.fetchCarryProfiles = fetchCarryProfiles;
    this.spawnEggColors = spawnEggColors;
    this.dimensions = dimensions;
    this.spawnSettings = spawnSettings;
    this.attributes = attributes;
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

  public String defaultTexturePath() {
    return "textures/entity/" + this.serializedId + ".png";
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

  public SpawnSettings spawnSettings() {
    return this.spawnSettings;
  }

  public Attributes attributes() {
    return this.attributes;
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
}
