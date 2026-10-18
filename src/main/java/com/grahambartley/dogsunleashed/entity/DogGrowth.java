package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogGrowth {

  public static final Identifier MAX_HEALTH_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "growth_stage_max_health");
  public static final Identifier MOVEMENT_SPEED_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "growth_stage_movement_speed");
  public static final Identifier ATTACK_DAMAGE_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "growth_stage_attack_damage");
  public static final Identifier ARMOR_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "growth_stage_armor");

  private final UnleashedDogEntity dog;
  private int ticksInStage;
  private int packMemberIndex = -1;

  DogGrowth(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static boolean assignsPackStage(final SpawnReason reason) {
    return reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION;
  }

  public boolean applies() {
    return this.dog.getBreed().nature().growsThroughStages();
  }

  public DireWolfGrowthStage stage() {
    return this.dog.isBaby()
        ? DireWolfGrowthStage.PUP
        : DireWolfGrowthStage.fromNumber(this.dog.getTrackedGrowthStage());
  }

  public int ticksInStage() {
    return this.ticksInStage;
  }

  public void setStage(final DireWolfGrowthStage stage) {
    if (stage.isPup()) {
      this.dog.setBaby(true);
      return;
    }
    if (this.dog.isBaby()) {
      this.dog.setBaby(false);
    }
    this.dog.setTrackedGrowthStage(stage.number());
    this.ticksInStage = 0;
    this.refresh();
  }

  void tick() {
    if (!this.applies() || this.dog.isBaby()) {
      return;
    }
    final DireWolfGrowthStage next = this.stage().next();
    if (next == null) {
      return;
    }
    this.ticksInStage++;
    if (DogGrowthRules.advancesStage(this.ticksInStage)) {
      this.setStage(next);
    }
  }

  void refresh() {
    if (!this.applies()) {
      return;
    }
    final DireWolfGrowthStage stage = this.stage();
    final UnleashedDogBreed.Attributes base = this.dog.getBreed().attributes();
    final float previousMaxHealth = this.dog.getMaxHealth();
    this.overwrite(
        EntityAttributes.GENERIC_MAX_HEALTH,
        MAX_HEALTH_MODIFIER_ID,
        stage.maxHealth() - base.maxHealth());
    this.overwrite(
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        MOVEMENT_SPEED_MODIFIER_ID,
        stage.movementSpeed() - base.movementSpeed());
    this.overwrite(
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        ATTACK_DAMAGE_MODIFIER_ID,
        stage.attackDamage() - base.attackDamage());
    this.overwrite(EntityAttributes.GENERIC_ARMOR, ARMOR_MODIFIER_ID, stage.armor());
    this.dog.setHealth(
        DogGrowthRules.healthAfterStageChange(
            this.dog.getHealth(), previousMaxHealth, this.dog.getMaxHealth()));
    this.dog.calculateDimensions();
  }

  EntityDimensions dimensions(final EntityDimensions base) {
    if (!this.applies() || this.dog.isBaby()) {
      return base;
    }
    final DireWolfGrowthStage stage = this.stage();
    return EntityDimensions.changing(stage.width(), stage.height());
  }

  @Nullable
  EntityData joinPack(final SpawnReason reason, final @Nullable EntityData entityData) {
    if (!this.applies() || !assignsPackStage(reason)) {
      return entityData;
    }
    final PassiveEntity.PassiveData pack =
        entityData instanceof PassiveEntity.PassiveData passiveData
            ? passiveData
            : new PassiveEntity.PassiveData(false);
    this.packMemberIndex = pack.getSpawnedCount();
    return pack;
  }

  void settleInPack() {
    if (this.packMemberIndex < 0) {
      return;
    }
    this.setStage(
        DogGrowthRules.packStage(
            this.packMemberIndex, this.dog.getRandom().nextInt(DogGrowthRules.PERCENT)));
    this.packMemberIndex = -1;
  }

  void writeNbt(final NbtCompound nbt) {
    if (!this.applies()) {
      return;
    }
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, this.dog.getTrackedGrowthStage());
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, this.ticksInStage);
  }

  void readNbt(final NbtCompound nbt) {
    if (!this.applies() || !nbt.contains(ModNbtKeys.GROWTH_STAGE, NbtElement.NUMBER_TYPE)) {
      return;
    }
    this.dog.setTrackedGrowthStage(
        DireWolfGrowthStage.fromNumber(nbt.getInt(ModNbtKeys.GROWTH_STAGE)).number());
    this.ticksInStage = Math.max(0, nbt.getInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE));
    this.dog.calculateDimensions();
  }

  private void overwrite(
      final RegistryEntry<EntityAttribute> attribute, final Identifier id, final double amount) {
    final EntityAttributeInstance instance = this.dog.getAttributeInstance(attribute);
    if (instance == null) {
      return;
    }
    instance.overwritePersistentModifier(
        new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE));
  }
}
