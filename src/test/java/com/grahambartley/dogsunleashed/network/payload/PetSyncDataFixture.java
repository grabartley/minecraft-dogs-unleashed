package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PortraitLook;
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
  private PortraitLook look = PortraitLook.DEFAULT;
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

  public PetSyncDataFixture look(final PortraitLook look) {
    this.look = look;
    return this;
  }

  public PetSyncDataFixture baby(final boolean baby) {
    return this.look(
        new PortraitLook(
            baby,
            this.look.coatVariant(),
            this.look.huskyEyeVariant(),
            this.look.furStage(),
            this.look.growthStage(),
            this.look.outfit()));
  }

  public PetSyncDataFixture variants(final int coatVariant, final int huskyEyeVariant) {
    return this.look(
        new PortraitLook(
            this.look.baby(),
            coatVariant,
            huskyEyeVariant,
            this.look.furStage(),
            this.look.growthStage(),
            this.look.outfit()));
  }

  public PetSyncDataFixture furStage(final int furStage) {
    return this.look(
        new PortraitLook(
            this.look.baby(),
            this.look.coatVariant(),
            this.look.huskyEyeVariant(),
            furStage,
            this.look.growthStage(),
            this.look.outfit()));
  }

  public PetSyncDataFixture growth(final int growthStage, final DogPortraitOutfit outfit) {
    return this.look(
        new PortraitLook(
            this.look.baby(),
            this.look.coatVariant(),
            this.look.huskyEyeVariant(),
            this.look.furStage(),
            growthStage,
            outfit));
  }

  public PetSyncDataFixture senior(final boolean senior) {
    return this.look(this.look.withSenior(senior));
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
        this.look,
        this.movementSpeed,
        this.attackDamage,
        this.composition,
        this.riding,
        this.away,
        this.bond,
        this.personality);
  }
}
