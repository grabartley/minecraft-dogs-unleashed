package com.grahambartley.pet;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class PetData {

  private final UUID petId;
  private final UUID ownerId;
  private final String breedType;
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
      String breedType,
      String name,
      float health,
      float maxHealth,
      BlockPos lastKnownPosition,
      String dimension,
      boolean alive) {
    this.petId = petId;
    this.ownerId = ownerId;
    this.breedType = breedType;
    this.name = name;
    this.health = health;
    this.maxHealth = maxHealth;
    this.lastKnownPosition = lastKnownPosition;
    this.dimension = dimension;
    this.alive = alive;
    this.baby = false;
    this.collarColor = DyeColor.RED.getId();
    this.coatVariant = -1;
    this.huskyEyeVariant = -1;
  }

  public UUID getPetId() {
    return petId;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getBreedType() {
    return breedType;
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
      this.coatVariant = -1;
    }
    if (dog instanceof HuskyEntity husky) {
      this.huskyEyeVariant = husky.getEyeColorVariant().ordinal();
    } else {
      this.huskyEyeVariant = -1;
    }
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid("PetId", petId);
    nbt.putUuid("OwnerId", ownerId);
    nbt.putString("BreedType", breedType);
    nbt.putString("Name", name);
    nbt.putFloat("Health", health);
    nbt.putFloat("MaxHealth", maxHealth);
    nbt.putInt("PosX", lastKnownPosition.getX());
    nbt.putInt("PosY", lastKnownPosition.getY());
    nbt.putInt("PosZ", lastKnownPosition.getZ());
    nbt.putString("Dimension", dimension);
    nbt.putBoolean("Alive", alive);
    nbt.putBoolean("PortraitBaby", baby);
    nbt.putInt("PortraitCollar", collarColor);
    nbt.putInt("portraitCoatVariant", coatVariant);
    nbt.putInt("PortraitHuskyEye", huskyEyeVariant);
    return nbt;
  }

  public static PetData fromNbt(NbtCompound nbt) {
    final PetData pet =
        new PetData(
            nbt.getUuid("PetId"),
            nbt.getUuid("OwnerId"),
            nbt.getString("BreedType"),
            nbt.getString("Name"),
            nbt.getFloat("Health"),
            nbt.getFloat("MaxHealth"),
            new BlockPos(nbt.getInt("PosX"), nbt.getInt("PosY"), nbt.getInt("PosZ")),
            nbt.getString("Dimension"),
            nbt.getBoolean("Alive"));
    if (nbt.contains("PortraitBaby")) {
      pet.baby = nbt.getBoolean("PortraitBaby");
    }
    if (nbt.contains("PortraitCollar", NbtElement.NUMBER_TYPE)) {
      pet.collarColor = nbt.getInt("PortraitCollar");
    }
    if (nbt.contains("portraitCoatVariant", NbtElement.NUMBER_TYPE)) {
      pet.coatVariant = nbt.getInt("portraitCoatVariant");
    }
    if (nbt.contains("PortraitHuskyEye", NbtElement.NUMBER_TYPE)) {
      pet.huskyEyeVariant = nbt.getInt("PortraitHuskyEye");
    }
    return pet;
  }
}
