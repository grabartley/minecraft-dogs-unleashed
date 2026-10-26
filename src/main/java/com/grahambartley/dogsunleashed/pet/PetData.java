package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogAgingRules;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.DogTricks;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgressNbt;
import com.grahambartley.dogsunleashed.portrait.DogLook;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class PetData {

  private static final float PERCENT = 100.0f;

  private final UUID petId;
  private final UUID ownerId;
  private String name;
  private float health;
  private float maxHealth;
  private BlockPos lastKnownPosition;
  private String dimension;
  private PetLifeState lifeState;
  private UUID parentAId;
  private UUID parentBId;
  private DogStatueLook look;
  private DogPortraitOutfit outfit = DogPortraitOutfit.NONE;
  private float movementSpeed;
  private float attackDamage;
  private int growthBattlesWon;
  private int growthTicksInStage;
  private int staminaPercent;
  private long adultAgeTicks;
  private boolean seniorParent;
  private int bondPoints;
  private DogTraits personality = DogTraits.NONE;
  private TrickProgress tricks = TrickProgress.NONE;

  public PetData(
      UUID petId,
      UUID ownerId,
      UnleashedDogBreed breed,
      String name,
      float health,
      float maxHealth,
      BlockPos lastKnownPosition,
      String dimension,
      PetLifeState lifeState) {
    this.petId = petId;
    this.ownerId = ownerId;
    this.setLook(DogStatueLook.unseen(breed));
    this.name = name;
    this.health = health;
    this.maxHealth = maxHealth;
    this.lastKnownPosition = lastKnownPosition;
    this.dimension = dimension;
    this.lifeState = lifeState;
  }

  public UUID getPetId() {
    return petId;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public UnleashedDogBreed getBreed() {
    return look.breed();
  }

  public String getBreedType() {
    return look.breed().serializedId();
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

  public PetLifeState getLifeState() {
    return lifeState;
  }

  public void setLifeState(PetLifeState lifeState) {
    this.lifeState = lifeState;
  }

  public boolean isAlive() {
    return lifeState.isAlive();
  }

  public DogStatueLook getLook() {
    return look;
  }

  private void setLook(final DogStatueLook look) {
    this.look = look.withGenes(look.resolvedGenes());
  }

  public DogTraits getPersonality() {
    return personality;
  }

  public TrickProgress getTricks() {
    return tricks;
  }

  public float getMovementSpeed() {
    return movementSpeed;
  }

  public float getAttackDamage() {
    return attackDamage;
  }

  public DogPortraitOutfit getOutfit() {
    return outfit;
  }

  public int getStaminaPercent() {
    return staminaPercent;
  }

  public long getAdultAgeTicks() {
    return adultAgeTicks;
  }

  public boolean hasSeniorParent() {
    return seniorParent;
  }

  public int getBondPoints() {
    return bondPoints;
  }

  public int getGrowthBattlesWon() {
    return growthBattlesWon;
  }

  public int getGrowthTicksInStage() {
    return growthTicksInStage;
  }

  public UUID getParentAId() {
    return parentAId;
  }

  public UUID getParentBId() {
    return parentBId;
  }

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

  public static PetData snapshotOf(
      final UnleashedDogEntity dog,
      final @Nullable UUID ownerId,
      final String name,
      final PetLifeState lifeState) {
    final PetData pet =
        new PetData(
            dog.getUuid(),
            ownerId,
            dog.getBreed(),
            name,
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            dog.getWorld().getRegistryKey().getValue().toString(),
            lifeState);
    pet.syncFrom(dog);
    return pet;
  }

  public void syncFrom(final UnleashedDogEntity dog) {
    this.setLook(DogStatueLook.of(dog));
    this.outfit = DogPortraitOutfits.of(dog);
    this.movementSpeed = (float) dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    this.attackDamage = (float) dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    this.growthBattlesWon = dog.getGrowth().battlesWon();
    this.growthTicksInStage = dog.getGrowth().ticksInStage();
    this.staminaPercent = Math.round(dog.getRiding().staminaFraction() * PERCENT);
    final DogAging aging = dog.getComponents().get(DogAging.class);
    this.adultAgeTicks = aging.adultAgeTicks();
    this.seniorParent = aging.hasSeniorParent();
    this.bondPoints = dog.getComponents().get(DogBond.class).points();
    this.personality = dog.getComponents().get(DogPersonality.class).traits();
    this.tricks = dog.getComponents().get(DogTricks.class).progress();
  }

  public boolean isSenior(final AgingSettings aging) {
    return DogAgingRules.isSenior(
        aging.dogAgingEnabled(), lifeState.isUndead(), adultAgeTicks, aging.seniorAfterDays());
  }

  public DogLook portraitLook(final AgingSettings aging) {
    return new DogLook(look, outfit, lifeState.isUndead(), isSenior(aging));
  }

  public boolean differsFrom(final PetData current, final AgingSettings aging) {
    return Float.compare(this.health, current.health) != 0
        || this.bondPoints != current.bondPoints
        || !this.lastKnownPosition.equals(current.lastKnownPosition)
        || !Objects.equals(this.dimension, current.dimension)
        || !this.look.equals(current.look)
        || !this.outfit.equals(current.outfit)
        || this.isSenior(aging) != current.isSenior(aging);
  }

  public NbtCompound toNbt(final RegistryWrapper.WrapperLookup registries) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.PET_ID, petId);
    nbt.putUuid(ModNbtKeys.OWNER_ID, ownerId);
    nbt.putString(ModNbtKeys.NAME, name);
    nbt.putFloat(ModNbtKeys.HEALTH, health);
    nbt.putFloat(ModNbtKeys.MAX_HEALTH, maxHealth);
    nbt.putInt(ModNbtKeys.POS_X, lastKnownPosition.getX());
    nbt.putInt(ModNbtKeys.POS_Y, lastKnownPosition.getY());
    nbt.putInt(ModNbtKeys.POS_Z, lastKnownPosition.getZ());
    nbt.putString(ModNbtKeys.DIMENSION, dimension);
    nbt.putString(ModNbtKeys.LIFE_STATE, lifeState.serializedName());
    nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, movementSpeed);
    nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, attackDamage);
    nbt.putInt(ModNbtKeys.GROWTH_BATTLES_WON, growthBattlesWon);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, growthTicksInStage);
    nbt.putInt(ModNbtKeys.STAMINA, staminaPercent);
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, adultAgeTicks);
    nbt.putBoolean(ModNbtKeys.SENIOR_PARENT, seniorParent);
    nbt.putInt(ModNbtKeys.BOND_POINTS, bondPoints);
    nbt.put(ModNbtKeys.PERSONALITY, personality.toNbt());
    nbt.put(ModNbtKeys.TRICKS, TrickProgressNbt.write(tricks));
    PetLookNbt.write(nbt, look, outfit, registries);
    if (parentAId != null) {
      nbt.putUuid(ModNbtKeys.PARENT_A_ID, parentAId);
    }
    if (parentBId != null) {
      nbt.putUuid(ModNbtKeys.PARENT_B_ID, parentBId);
    }
    return nbt;
  }

  static PetLifeState lifeStateFrom(final NbtCompound nbt) {
    return nbt.contains(ModNbtKeys.LIFE_STATE, NbtElement.STRING_TYPE)
        ? PetLifeState.fromSerializedName(nbt.getString(ModNbtKeys.LIFE_STATE))
        : PetLifeState.fromLegacyAliveFlag(nbt.getBoolean(ModNbtKeys.ALIVE));
  }

  public static PetData fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final DogStatueLook look = PetLookNbt.readLook(nbt);
    final PetData pet =
        new PetData(
            nbt.getUuid(ModNbtKeys.PET_ID),
            nbt.getUuid(ModNbtKeys.OWNER_ID),
            look.breed(),
            nbt.getString(ModNbtKeys.NAME),
            nbt.getFloat(ModNbtKeys.HEALTH),
            nbt.getFloat(ModNbtKeys.MAX_HEALTH),
            new BlockPos(
                nbt.getInt(ModNbtKeys.POS_X),
                nbt.getInt(ModNbtKeys.POS_Y),
                nbt.getInt(ModNbtKeys.POS_Z)),
            nbt.getString(ModNbtKeys.DIMENSION),
            lifeStateFrom(nbt));
    if (nbt.contains(ModNbtKeys.MOVEMENT_SPEED, NbtElement.NUMBER_TYPE)) {
      pet.movementSpeed = nbt.getFloat(ModNbtKeys.MOVEMENT_SPEED);
    }
    if (nbt.contains(ModNbtKeys.ATTACK_DAMAGE, NbtElement.NUMBER_TYPE)) {
      pet.attackDamage = nbt.getFloat(ModNbtKeys.ATTACK_DAMAGE);
    }
    pet.growthBattlesWon = nbt.getInt(ModNbtKeys.GROWTH_BATTLES_WON);
    pet.growthTicksInStage = nbt.getInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE);
    pet.staminaPercent = nbt.getInt(ModNbtKeys.STAMINA);
    pet.adultAgeTicks = Math.max(0L, nbt.getLong(ModNbtKeys.ADULT_AGE_TICKS));
    pet.seniorParent = nbt.getBoolean(ModNbtKeys.SENIOR_PARENT);
    pet.bondPoints = DogBondRules.clamp(nbt.getInt(ModNbtKeys.BOND_POINTS));
    if (nbt.contains(ModNbtKeys.PERSONALITY, NbtElement.LIST_TYPE)) {
      pet.personality =
          DogTraits.fromNbt(nbt.getList(ModNbtKeys.PERSONALITY, NbtElement.STRING_TYPE));
    }
    if (nbt.contains(ModNbtKeys.TRICKS, NbtElement.COMPOUND_TYPE)) {
      pet.tricks = TrickProgressNbt.read(nbt.getCompound(ModNbtKeys.TRICKS));
    }
    pet.setLook(look);
    pet.outfit = PetLookNbt.readOutfit(nbt, registries);
    if (nbt.containsUuid(ModNbtKeys.PARENT_A_ID)) {
      pet.parentAId = nbt.getUuid(ModNbtKeys.PARENT_A_ID);
    }
    if (nbt.containsUuid(ModNbtKeys.PARENT_B_ID)) {
      pet.parentBId = nbt.getUuid(ModNbtKeys.PARENT_B_ID);
    }
    return pet;
  }
}
