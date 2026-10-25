package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.DogTricks;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgressNbt;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class PetData {

  private static final int DEFAULT_COLLAR_COLOR_ID = DyeColor.RED.getId();
  private static final float PERCENT = 100.0f;

  private final UUID petId;
  private final UUID ownerId;
  private final UnleashedDogBreed breed;
  private String name;
  private float health;
  private float maxHealth;
  private BlockPos lastKnownPosition;
  private String dimension;
  private PetLifeState lifeState;
  private boolean baby;
  private int collarColor;
  private int coatVariant;
  private int huskyEyeVariant;
  private int furStage;
  private UUID parentAId;
  private UUID parentBId;
  private List<BreedShare> composition;
  private DogGenome genome;
  private @Nullable DogGenes genes;
  private float movementSpeed;
  private float attackDamage;
  private int growthStage;
  private int growthBattlesWon;
  private int growthTicksInStage;
  private int staminaPercent;
  private long adultAgeTicks;
  private boolean senior;
  private boolean seniorParent;
  private int bondPoints;
  private DogTraits personality = DogTraits.NONE;
  private TrickProgress tricks = TrickProgress.NONE;
  private DogPortraitOutfit outfit = DogPortraitOutfit.NONE;

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
    this.breed = breed;
    this.name = name;
    this.health = health;
    this.maxHealth = maxHealth;
    this.lastKnownPosition = lastKnownPosition;
    this.dimension = dimension;
    this.lifeState = lifeState;
    this.baby = false;
    this.collarColor = DEFAULT_COLLAR_COLOR_ID;
    this.coatVariant = UnleashedDogEntity.UNSET_VARIANT;
    this.huskyEyeVariant = UnleashedDogEntity.UNSET_VARIANT;
    this.furStage = DogFurStage.NATURAL.ordinal();
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

  public PetLifeState getLifeState() {
    return lifeState;
  }

  public void setLifeState(PetLifeState lifeState) {
    this.lifeState = lifeState;
  }

  public boolean isAlive() {
    return lifeState.isAlive();
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

  public int getFurStage() {
    return furStage;
  }

  public List<BreedShare> getComposition() {
    return composition;
  }

  public DogGenome getGenome() {
    return genome;
  }

  public UnleashedDogBreed getRigSourceBreed() {
    return DogGenome.rigBreedOf(breed, composition);
  }

  public DogTraits getPersonality() {
    return personality;
  }

  public TrickProgress getTricks() {
    return tricks;
  }

  public DogGenes getGenes() {
    if (genes != null) {
      return genes;
    }
    final UnleashedDogBreed rigBreed = getRigSourceBreed();
    return DogGenes.matching(
        new DogAppearance(rigBreed, coatVariant, huskyEyeVariant),
        DogGenes.lociFor(rigBreed, composition));
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

  public int getGrowthStage() {
    return growthStage;
  }

  public int getStaminaPercent() {
    return staminaPercent;
  }

  public long getAdultAgeTicks() {
    return adultAgeTicks;
  }

  public boolean isSenior() {
    return senior;
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

  public void syncFrom(final UnleashedDogEntity dog) {
    final PortraitLook look = PortraitLook.of(dog);
    this.baby = look.baby();
    this.coatVariant = look.coatVariant();
    this.huskyEyeVariant = look.huskyEyeVariant();
    this.furStage = look.furStage();
    this.growthStage = look.growthStage();
    this.outfit = look.outfit();
    this.collarColor = dog.getComponents().get(DogCollar.class).getColor().getId();
    this.movementSpeed = (float) dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    this.attackDamage = (float) dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    this.growthBattlesWon = dog.getGrowth().battlesWon();
    this.growthTicksInStage = dog.getGrowth().ticksInStage();
    this.staminaPercent = Math.round(dog.getRiding().staminaFraction() * PERCENT);
    final DogAging aging = dog.getComponents().get(DogAging.class);
    this.adultAgeTicks = aging.adultAgeTicks();
    this.senior = aging.isSenior();
    this.seniorParent = aging.hasSeniorParent();
    this.bondPoints = dog.getComponents().get(DogBond.class).points();
    final DogGenome dogGenome = dog.getGenome();
    this.genome = dogGenome;
    this.composition = dogGenome != null ? dogGenome.composition() : List.of();
    this.genes = dog.getGenes().genes();
    this.personality = dog.getComponents().get(DogPersonality.class).traits();
    this.tricks = dog.getComponents().get(DogTricks.class).progress();
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

  public PortraitLook portraitLook() {
    return new PortraitLook(baby, coatVariant, huskyEyeVariant, furStage, growthStage, outfit);
  }

  public boolean differsFrom(
      final float health,
      final BlockPos pos,
      final String dimension,
      final PortraitLook look,
      final int bondPoints) {
    return Float.compare(this.health, health) != 0
        || this.bondPoints != bondPoints
        || !this.lastKnownPosition.equals(pos)
        || !Objects.equals(this.dimension, dimension)
        || !this.portraitLook().equals(look);
  }

  public NbtCompound toNbt(final RegistryWrapper.WrapperLookup registries) {
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
    nbt.putString(ModNbtKeys.LIFE_STATE, lifeState.serializedName());
    nbt.putBoolean(ModNbtKeys.PORTRAIT_BABY, baby);
    nbt.putInt(ModNbtKeys.PORTRAIT_COLLAR, collarColor);
    nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, coatVariant);
    nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, huskyEyeVariant);
    nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, furStage);
    nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, movementSpeed);
    nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, attackDamage);
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, growthStage);
    nbt.putInt(ModNbtKeys.GROWTH_BATTLES_WON, growthBattlesWon);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, growthTicksInStage);
    nbt.putInt(ModNbtKeys.STAMINA, staminaPercent);
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, adultAgeTicks);
    nbt.putBoolean(ModNbtKeys.SENIOR, senior);
    nbt.putBoolean(ModNbtKeys.SENIOR_PARENT, seniorParent);
    nbt.putInt(ModNbtKeys.BOND_POINTS, bondPoints);
    if (!composition.isEmpty()) {
      nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(composition));
    }
    if (genome != null) {
      nbt.put(ModNbtKeys.GENOME, genome.toNbt());
    }
    if (genes != null) {
      nbt.put(ModNbtKeys.GENES, genes.toNbt());
    }
    nbt.put(ModNbtKeys.PERSONALITY, personality.toNbt());
    nbt.put(ModNbtKeys.TRICKS, TrickProgressNbt.write(tricks));
    DogPortraitOutfit.CODEC
        .encodeStart(registries.getOps(NbtOps.INSTANCE), outfit)
        .resultOrPartial(DogsUnleashed.log::warn)
        .ifPresent(saved -> nbt.put(ModNbtKeys.PORTRAIT_OUTFIT, saved));
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
            lifeStateFrom(nbt));
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
    if (nbt.contains(ModNbtKeys.PORTRAIT_FUR_STAGE, NbtElement.NUMBER_TYPE)) {
      pet.furStage = DogFurStage.fromOrdinal(nbt.getInt(ModNbtKeys.PORTRAIT_FUR_STAGE)).ordinal();
    }
    if (nbt.contains(ModNbtKeys.MOVEMENT_SPEED, NbtElement.NUMBER_TYPE)) {
      pet.movementSpeed = nbt.getFloat(ModNbtKeys.MOVEMENT_SPEED);
    }
    if (nbt.contains(ModNbtKeys.ATTACK_DAMAGE, NbtElement.NUMBER_TYPE)) {
      pet.attackDamage = nbt.getFloat(ModNbtKeys.ATTACK_DAMAGE);
    }
    pet.growthStage = nbt.getInt(ModNbtKeys.GROWTH_STAGE);
    pet.growthBattlesWon = nbt.getInt(ModNbtKeys.GROWTH_BATTLES_WON);
    pet.growthTicksInStage = nbt.getInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE);
    pet.staminaPercent = nbt.getInt(ModNbtKeys.STAMINA);
    pet.adultAgeTicks = Math.max(0L, nbt.getLong(ModNbtKeys.ADULT_AGE_TICKS));
    pet.senior = nbt.getBoolean(ModNbtKeys.SENIOR);
    pet.seniorParent = nbt.getBoolean(ModNbtKeys.SENIOR_PARENT);
    pet.bondPoints = DogBondRules.clamp(nbt.getInt(ModNbtKeys.BOND_POINTS));
    if (nbt.contains(ModNbtKeys.COMPOSITION, NbtElement.COMPOUND_TYPE)) {
      pet.composition = DogGenome.compositionFromNbt(nbt.getCompound(ModNbtKeys.COMPOSITION));
    }
    if (nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)) {
      pet.genome = DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME));
    }
    if (nbt.contains(ModNbtKeys.GENES, NbtElement.COMPOUND_TYPE)) {
      pet.genes = DogGenes.fromNbt(nbt.getCompound(ModNbtKeys.GENES));
    }
    if (nbt.contains(ModNbtKeys.PERSONALITY, NbtElement.LIST_TYPE)) {
      pet.personality =
          DogTraits.fromNbt(nbt.getList(ModNbtKeys.PERSONALITY, NbtElement.STRING_TYPE));
    }
    if (nbt.contains(ModNbtKeys.TRICKS, NbtElement.COMPOUND_TYPE)) {
      pet.tricks = TrickProgressNbt.read(nbt.getCompound(ModNbtKeys.TRICKS));
    }
    if (nbt.contains(ModNbtKeys.PORTRAIT_OUTFIT, NbtElement.COMPOUND_TYPE)) {
      pet.outfit =
          DogPortraitOutfit.CODEC
              .parse(registries.getOps(NbtOps.INSTANCE), nbt.get(ModNbtKeys.PORTRAIT_OUTFIT))
              .resultOrPartial(DogsUnleashed.log::warn)
              .orElse(DogPortraitOutfit.NONE);
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
