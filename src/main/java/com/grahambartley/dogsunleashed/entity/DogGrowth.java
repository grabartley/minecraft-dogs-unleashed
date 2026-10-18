package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.Optional;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
  public static final Identifier JUMP_STRENGTH_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "growth_stage_jump_strength");

  private static final int EVOLVE_PARTICLES = 60;

  private final UnleashedDogEntity dog;

  private int ticksInStage;
  private int battlesWon;
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

  public int battlesWon() {
    return this.battlesWon;
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
    final DireWolfGrowthStage stage = this.stage();
    if (!stage.growsWithTime()) {
      return;
    }
    this.ticksInStage++;
    if (DogGrowthRules.advancesStage(
        stage, this.ticksInStage, settings().growthRateMultiplierPercent())) {
      this.growInto(stage.next());
    }
  }

  public void recordBattle(final LivingEntity defeated) {
    if (!this.applies()
        || !this.dog.isTamed()
        || !this.stage().evolvesThroughBond()
        || !(defeated instanceof Monster)) {
      return;
    }
    final LivingEntity owner = this.dog.getOwner();
    if (owner == null
        || owner.squaredDistanceTo(this.dog)
            > DogGrowthRules.BOND_RANGE_BLOCKS * DogGrowthRules.BOND_RANGE_BLOCKS) {
      return;
    }
    this.battlesWon++;
  }

  @Nullable
  public ActionResult tryEvolve(final PlayerEntity player, final ItemStack stack) {
    if (!this.applies() || !stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
      return null;
    }
    final int battlesToEvolve = settings().battlesToEvolve();
    final Optional<String> refusal =
        DogGrowthRules.evolutionRefusalKey(this.stage(), this.battlesWon, battlesToEvolve);
    if (refusal.isPresent()) {
      player.sendMessage(
          Text.translatable(
              refusal.get(),
              PetNames.tamedNameOf(this.dog),
              DogGrowthRules.battlesStillNeeded(this.battlesWon, battlesToEvolve)),
          true);
      return ActionResult.SUCCESS;
    }
    if (!DogGrowthRules.canEvolve(
        this.stage(), this.dog.isTamed(), this.battlesWon, battlesToEvolve)) {
      return ActionResult.PASS;
    }
    stack.decrementUnlessCreative(1, player);
    this.growInto(DireWolfGrowthStage.ALPHA);
    this.celebrateEvolution();
    return ActionResult.SUCCESS;
  }

  private void growInto(final DireWolfGrowthStage stage) {
    this.setStage(stage);
    if (this.dog.getOwner() instanceof PlayerEntity owner) {
      owner.sendMessage(
          Text.translatable(
              "message.dogs-unleashed.direwolf.grew",
              PetNames.tamedNameOf(this.dog),
              Text.translatable(stage.translationKey())),
          false);
    }
  }

  private void celebrateEvolution() {
    final SoundEvent howl = this.dog.getBreed().howlSound();
    if (howl != null) {
      this.dog.playSound(howl, 2.0f, 0.8f);
    }
    if (this.dog.getWorld() instanceof ServerWorld world) {
      world.spawnParticles(
          ParticleTypes.SNOWFLAKE,
          this.dog.getX(),
          this.dog.getBodyY(0.5),
          this.dog.getZ(),
          EVOLVE_PARTICLES,
          this.dog.getWidth() * 0.6,
          this.dog.getHeight() * 0.5,
          this.dog.getWidth() * 0.6,
          0.05);
      world.spawnParticles(
          ParticleTypes.END_ROD,
          this.dog.getX(),
          this.dog.getBodyY(0.6),
          this.dog.getZ(),
          EVOLVE_PARTICLES / 3,
          this.dog.getWidth() * 0.4,
          this.dog.getHeight() * 0.4,
          this.dog.getWidth() * 0.4,
          0.02);
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
    this.overwrite(
        EntityAttributes.GENERIC_JUMP_STRENGTH,
        JUMP_STRENGTH_MODIFIER_ID,
        stage.jumpStrength() - DogJumpPhysics.VANILLA_JUMP_STRENGTH);
    if (this.dog.getNavigation() instanceof MobNavigation navigation) {
      navigation.setCanWalkOverFences(stage.clearsFences());
    }
    this.dog.setHealth(
        DogGrowthRules.healthAfterStageChange(
            this.dog.getHealth(), previousMaxHealth, this.dog.getMaxHealth()));
    this.dog.calculateDimensions();
  }

  public double pathClimbHeight() {
    return this.applies() && this.stage().leapsHigh() ? this.stage().jumpClearance() : 0.0;
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

  void restore(final int stageNumber, final int battlesWon, final int ticksInStage) {
    if (!this.applies()) {
      return;
    }
    this.battlesWon = Math.max(0, battlesWon);
    if (!this.dog.isBaby()) {
      this.dog.setTrackedGrowthStage(DireWolfGrowthStage.fromNumber(stageNumber).number());
    }
    this.refresh();
    this.ticksInStage = Math.max(0, ticksInStage);
  }

  void writeNbt(final NbtCompound nbt) {
    if (!this.applies()) {
      return;
    }
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, this.dog.getTrackedGrowthStage());
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, this.ticksInStage);
    nbt.putInt(ModNbtKeys.GROWTH_BATTLES_WON, this.battlesWon);
  }

  void readNbt(final NbtCompound nbt) {
    if (!this.applies() || !nbt.contains(ModNbtKeys.GROWTH_STAGE, NbtElement.NUMBER_TYPE)) {
      return;
    }
    this.dog.setTrackedGrowthStage(
        DireWolfGrowthStage.fromNumber(nbt.getInt(ModNbtKeys.GROWTH_STAGE)).number());
    this.ticksInStage = Math.max(0, nbt.getInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE));
    this.battlesWon = Math.max(0, nbt.getInt(ModNbtKeys.GROWTH_BATTLES_WON));
    this.dog.calculateDimensions();
  }

  private static DireWolfSettings settings() {
    return DogsUnleashed.SERVER_CONFIG.direWolf();
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
