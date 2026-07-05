package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class PetData {

  // Keep in sync with UnleashedDogEntity.DEFAULT_COLLAR_COLOR_ID (DyeColor.RED), sourced directly
  // from DyeColor so seeding a new pet record never class-loads the entity. UnleashedDogEntity is a
  // MobEntity subclass that fails bytecode verification under the unit-test classpath, which would
  // otherwise make PetData impossible to construct in plain JUnit tests.
  private static final int DEFAULT_COLLAR_COLOR_ID = DyeColor.RED.getId();

  private final UUID petId;
  private final UUID ownerId;
  private final UnleashedDogBreed breed;
  private String name;
  private float health;
  private float maxHealth;
  private BlockPos lastKnownPosition;
  private String dimension;
  private boolean alive;
  private boolean baby;
  private int collarColor;
  private int coatVariant;
  private int huskyEyeVariant;

  public PetData(
      UUID petId,
      UUID ownerId,
      UnleashedDogBreed breed,
      String name,
      float health,
      float maxHealth,
      BlockPos lastKnownPosition,
      String dimension,
      boolean alive) {
    this.petId = petId;
    this.ownerId = ownerId;
    this.breed = breed;
    this.name = name;
    this.health = health;
    this.maxHealth = maxHealth;
    this.lastKnownPosition = lastKnownPosition;
    this.dimension = dimension;
    this.alive = alive;
    this.baby = false;
    this.collarColor = DEFAULT_COLLAR_COLOR_ID;
    this.coatVariant = UnleashedDogEntity.UNSET_VARIANT;
    this.huskyEyeVariant = UnleashedDogEntity.UNSET_VARIANT;
  }

  public UUID getPetId() {
    return petId;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public UnleashedDogBreed getBreed() {
    return breed;
  }

  public String getBreedType() {
    return breed.serializedId();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public float getHealth() {
    return health;
  }

  public void setHealth(float health) {
    this.health = health;
  }

  public float getMaxHealth() {
    return maxHealth;
  }

  public void setMaxHealth(float maxHealth) {
    this.maxHealth = maxHealth;
  }

  public BlockPos getLastKnownPosition() {
    return lastKnownPosition;
  }

  public void setLastKnownPosition(BlockPos pos) {
    this.lastKnownPosition = pos;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public boolean isBaby() {
    return baby;
  }

  public int getCollarColorId() {
    return collarColor;
  }

  public int getCoatVariant() {
    return coatVariant;
  }

  public int getHuskyEyeVariant() {
    return huskyEyeVariant;
  }

  public void syncAppearanceFrom(final UnleashedDogEntity dog) {
    this.baby = dog.isBaby();
    this.collarColor = dog.getCollarColor().getId();
    this.coatVariant = coatVariantOf(dog);
    this.huskyEyeVariant = huskyEyeVariantOf(dog);
  }

  public static int coatVariantOf(final UnleashedDogEntity dog) {
    return dog.getCoatVariant() != null
        ? dog.getCoatVariant().getOrdinal()
        : UnleashedDogEntity.UNSET_VARIANT;
  }

  public static int huskyEyeVariantOf(final UnleashedDogEntity dog) {
    return dog instanceof HuskyEntity husky
        ? husky.getEyeColorVariant().ordinal()
        : UnleashedDogEntity.UNSET_VARIANT;
  }

  /**
   * Returns whether any field that {@code syncPetData} writes back differs from the supplied live
   * values. Lets callers skip {@link PetManager#updatePet} (and its {@code markDirty()}) when a pet
   * has not changed since the last sync. The arguments mirror the fields set by {@link #setHealth},
   * {@link #setLastKnownPosition}, {@link #setDimension}, and {@link #syncAppearanceFrom}.
   */
  public boolean differsFrom(
      final float health,
      final BlockPos pos,
      final String dimension,
      final boolean baby,
      final int collarColor,
      final int coatVariant,
      final int huskyEyeVariant) {
    return Float.compare(this.health, health) != 0
        || !this.lastKnownPosition.equals(pos)
        || !Objects.equals(this.dimension, dimension)
        || this.baby != baby
        || this.collarColor != collarColor
        || this.coatVariant != coatVariant
        || this.huskyEyeVariant != huskyEyeVariant;
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.PET_ID, petId);
    nbt.putUuid(ModNbtKeys.OWNER_ID, ownerId);
    nbt.putString(ModNbtKeys.BREED_TYPE, breed.serializedId());
    nbt.putString(ModNbtKeys.NAME, name);
    nbt.putFloat(ModNbtKeys.HEALTH, health);
    nbt.putFloat(ModNbtKeys.MAX_HEALTH, maxHealth);
    nbt.putInt(ModNbtKeys.POS_X, lastKnownPosition.getX());
    nbt.putInt(ModNbtKeys.POS_Y, lastKnownPosition.getY());
    nbt.putInt(ModNbtKeys.POS_Z, lastKnownPosition.getZ());
    nbt.putString(ModNbtKeys.DIMENSION, dimension);
    nbt.putBoolean(ModNbtKeys.ALIVE, alive);
    nbt.putBoolean(ModNbtKeys.PORTRAIT_BABY, baby);
    nbt.putInt(ModNbtKeys.PORTRAIT_COLLAR, collarColor);
    nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, coatVariant);
    nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, huskyEyeVariant);
    return nbt;
  }

  public static PetData fromNbt(NbtCompound nbt) {
    final PetData pet =
        new PetData(
            nbt.getUuid(ModNbtKeys.PET_ID),
            nbt.getUuid(ModNbtKeys.OWNER_ID),
            UnleashedDogBreed.fromSerializedId(nbt.getString(ModNbtKeys.BREED_TYPE)),
            nbt.getString(ModNbtKeys.NAME),
            nbt.getFloat(ModNbtKeys.HEALTH),
            nbt.getFloat(ModNbtKeys.MAX_HEALTH),
            new BlockPos(
                nbt.getInt(ModNbtKeys.POS_X),
                nbt.getInt(ModNbtKeys.POS_Y),
                nbt.getInt(ModNbtKeys.POS_Z)),
            nbt.getString(ModNbtKeys.DIMENSION),
            nbt.getBoolean(ModNbtKeys.ALIVE));
    if (nbt.contains(ModNbtKeys.PORTRAIT_BABY)) {
      pet.baby = nbt.getBoolean(ModNbtKeys.PORTRAIT_BABY);
    }
    if (nbt.contains(ModNbtKeys.PORTRAIT_COLLAR, NbtElement.NUMBER_TYPE)) {
      pet.collarColor = nbt.getInt(ModNbtKeys.PORTRAIT_COLLAR);
    }
    if (nbt.contains(ModNbtKeys.PORTRAIT_COAT_VARIANT, NbtElement.NUMBER_TYPE)) {
      pet.coatVariant = nbt.getInt(ModNbtKeys.PORTRAIT_COAT_VARIANT);
    }
    if (nbt.contains(ModNbtKeys.PORTRAIT_HUSKY_EYE, NbtElement.NUMBER_TYPE)) {
      pet.huskyEyeVariant = nbt.getInt(ModNbtKeys.PORTRAIT_HUSKY_EYE);
    }
    return pet;
  }
}
