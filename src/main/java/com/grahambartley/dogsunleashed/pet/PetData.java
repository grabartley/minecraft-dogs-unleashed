package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.attribute.EntityAttributes;
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
  private UUID parentAId;
  private UUID parentBId;
  private List<BreedShare> composition;
  private float movementSpeed;
  private float attackDamage;

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
    this.composition = List.of();
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

  public List<BreedShare> getComposition() {
    return composition;
  }

  public float getMovementSpeed() {
    return movementSpeed;
  }

  public float getAttackDamage() {
    return attackDamage;
  }

  public UUID getParentAId() {
    return parentAId;
  }

  public UUID getParentBId() {
    return parentBId;
  }

  /**
   * Records parentage with set-once semantics: each slot is written only while empty, so lineage
   * can never be rewritten once known. The same call seeds newly registered puppies and backfills
   * records from before parentage was persisted (where the entity remembers at most one parent).
   * Returns whether anything changed so callers can skip persisting an unchanged record.
   */
  public boolean recordParents(final UUID parentAId, final UUID parentBId) {
    boolean changed = false;
    if (this.parentAId == null && parentAId != null) {
      this.parentAId = parentAId;
      changed = true;
    }
    if (this.parentBId == null && parentBId != null && !parentBId.equals(this.parentAId)) {
      this.parentBId = parentBId;
      changed = true;
    }
    return changed;
  }

  public void syncAppearanceFrom(final UnleashedDogEntity dog) {
    this.baby = dog.isBaby();
    this.collarColor = dog.getCollarColor().getId();
    this.coatVariant = coatVariantOf(dog);
    this.huskyEyeVariant = huskyEyeVariantOf(dog);
    this.movementSpeed = (float) dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    this.attackDamage = (float) dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    final DogGenome genome = dog.getGenome();
    this.composition = genome != null ? genome.composition() : List.of();
  }

  public static int coatVariantOf(final UnleashedDogEntity dog) {
    return dog.getCoatVariant() != null
        ? dog.getCoatVariant().getOrdinal()
        : UnleashedDogEntity.UNSET_VARIANT;
  }

  public static int huskyEyeVariantOf(final UnleashedDogEntity dog) {
    final HuskyEyeColor eyeColor = dog.getEyeColorVariant();
    return eyeColor != null ? eyeColor.ordinal() : UnleashedDogEntity.UNSET_VARIANT;
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
    nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, movementSpeed);
    nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, attackDamage);
    if (!composition.isEmpty()) {
      nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(composition));
    }
    if (parentAId != null) {
      nbt.putUuid(ModNbtKeys.PARENT_A_ID, parentAId);
    }
    if (parentBId != null) {
      nbt.putUuid(ModNbtKeys.PARENT_B_ID, parentBId);
    }
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
    if (nbt.contains(ModNbtKeys.MOVEMENT_SPEED, NbtElement.NUMBER_TYPE)) {
      pet.movementSpeed = nbt.getFloat(ModNbtKeys.MOVEMENT_SPEED);
    }
    if (nbt.contains(ModNbtKeys.ATTACK_DAMAGE, NbtElement.NUMBER_TYPE)) {
      pet.attackDamage = nbt.getFloat(ModNbtKeys.ATTACK_DAMAGE);
    }
    if (nbt.contains(ModNbtKeys.COMPOSITION, NbtElement.COMPOUND_TYPE)) {
      pet.composition = DogGenome.compositionFromNbt(nbt.getCompound(ModNbtKeys.COMPOSITION));
    }
    if (nbt.containsUuid(ModNbtKeys.PARENT_A_ID)) {
      pet.parentAId = nbt.getUuid(ModNbtKeys.PARENT_A_ID);
    }
    if (nbt.containsUuid(ModNbtKeys.PARENT_B_ID)) {
      pet.parentBId = nbt.getUuid(ModNbtKeys.PARENT_B_ID);
    }
    return pet;
  }
}
