package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.List;
import java.util.UUID;

public final class PetSyncDataFixture {

  private String petId = UUID.randomUUID().toString();
  private UnleashedDogBreed breed = UnleashedDogBreed.HUSKY;
  private String name = "Balto";
  private float health = 12.5f;
  private float maxHealth = 25.0f;
  private PetLifeState lifeState = PetLifeState.LIVING;
  private boolean baby;
  private int coatVariant = UnleashedDogEntity.UNSET_VARIANT;
  private int huskyEyeVariant = UnleashedDogEntity.UNSET_VARIANT;
  private int furStage = DogFurStage.NATURAL.ordinal();
  private int growthStage;
  private DogPortraitOutfit outfit = DogPortraitOutfit.NONE;
  private float movementSpeed = 0.29f;
  private float attackDamage = 3.5f;
  private List<BreedShare> composition = List.of();
  private RidingView riding = RidingView.NONE;
  private PetAwayStatus away = PetAwayStatus.HOME;
  private DogBondStep bond = DogBondStep.STRANGER;
  private DogTraits personality = DogTraits.NONE;

  private PetSyncDataFixture() {}

  public static PetSyncDataFixture pet() {
    return new PetSyncDataFixture();
  }

  public PetSyncDataFixture id(final String petId) {
    this.petId = petId;
    return this;
  }

  public PetSyncDataFixture breed(final UnleashedDogBreed breed) {
    this.breed = breed;
    return this;
  }

  public PetSyncDataFixture name(final String name) {
    this.name = name;
    return this;
  }

  public PetSyncDataFixture health(final float health, final float maxHealth) {
    this.health = health;
    this.maxHealth = maxHealth;
    return this;
  }

  public PetSyncDataFixture lifeState(final PetLifeState lifeState) {
    this.lifeState = lifeState;
    return this;
  }

  public PetSyncDataFixture baby(final boolean baby) {
    this.baby = baby;
    return this;
  }

  public PetSyncDataFixture variants(final int coatVariant, final int huskyEyeVariant) {
    this.coatVariant = coatVariant;
    this.huskyEyeVariant = huskyEyeVariant;
    return this;
  }

  public PetSyncDataFixture furStage(final int furStage) {
    this.furStage = furStage;
    return this;
  }

  public PetSyncDataFixture growthStage(final int growthStage) {
    this.growthStage = growthStage;
    return this;
  }

  public PetSyncDataFixture outfit(final DogPortraitOutfit outfit) {
    this.outfit = outfit;
    return this;
  }

  public PetSyncDataFixture senior(final boolean senior) {
    this.outfit = this.outfit.withSenior(senior);
    return this;
  }

  public PetSyncDataFixture stats(final float movementSpeed, final float attackDamage) {
    this.movementSpeed = movementSpeed;
    this.attackDamage = attackDamage;
    return this;
  }

  public PetSyncDataFixture composition(final List<BreedShare> composition) {
    this.composition = composition;
    return this;
  }

  public PetSyncDataFixture away(final PetAwayStatus away) {
    this.away = away;
    return this;
  }

  public PetSyncDataFixture bond(final DogBondStep bond) {
    this.bond = bond;
    return this;
  }

  public PetSyncDataFixture personality(final DogTraits personality) {
    this.personality = personality;
    return this;
  }

  public PetSyncData build() {
    return new PetSyncData(
        this.petId,
        this.breed,
        this.name,
        this.health,
        this.maxHealth,
        10,
        64,
        -20,
        "minecraft:overworld",
        this.lifeState,
        this.baby,
        this.coatVariant,
        this.huskyEyeVariant,
        this.furStage,
        this.growthStage,
        this.outfit,
        this.movementSpeed,
        this.attackDamage,
        this.composition,
        this.riding,
        this.away,
        this.bond,
        this.personality);
  }
}
