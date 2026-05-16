package com.grahambartley.pet;

import com.grahambartley.ModNbtKeys;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public final class PetData {

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
    this.collarColor = UnleashedDogEntity.DEFAULT_COLLAR_COLOR_ID;
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
    if (dog.getCoatVariant() != null) {
      this.coatVariant = dog.getCoatVariant().getOrdinal();
    } else {
      this.coatVariant = UnleashedDogEntity.UNSET_VARIANT;
    }
    if (dog instanceof HuskyEntity husky) {
      this.huskyEyeVariant = husky.getEyeColorVariant().ordinal();
    } else {
      this.huskyEyeVariant = UnleashedDogEntity.UNSET_VARIANT;
    }
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
