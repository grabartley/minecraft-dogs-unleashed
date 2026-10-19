package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.BARK_PITCH;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.BOOLEAN;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.ITEM_STACK;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.NBT_COMPOUND;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.OPTIONAL_UUID;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.STRING;

import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class UnleashedDogEntity extends DogMountEntity
    implements GeoEntity, Angerable, ExtendedScreenHandlerFactory<Integer> {

  public static final int DEFAULT_COLLAR_COLOR_ID = DyeColor.RED.getId();
  public static final int UNSET_VARIANT = -1;

  static final TrackedData<Integer> ANGER_TIME = data(INTEGER);
  static final TrackedData<Integer> COAT_VARIANT = data(INTEGER);
  static final TrackedData<Integer> EYE_COLOR_VARIANT = data(INTEGER);
  static final TrackedData<Boolean> HOWLING = data(BOOLEAN);
  static final TrackedData<Boolean> GROWLING = data(BOOLEAN);
  static final TrackedData<NbtCompound> GENOME = data(NBT_COMPOUND);
  static final TrackedData<Integer> COLLAR_COLOR = data(INTEGER);
  static final TrackedData<Integer> TAIL_WAG_TIMER = data(INTEGER);
  static final TrackedData<Integer> SHAKE_PROGRESS = data(INTEGER);
  static final TrackedData<Integer> TREAT_BUFF_TICKS = data(INTEGER);
  static final TrackedData<Boolean> HEAD_TILTING = data(BOOLEAN);
  static final TrackedData<Boolean> SLEEPING_IN_BED = data(BOOLEAN);
  static final TrackedData<Boolean> COMMANDED_TO_SLEEP = data(BOOLEAN);
  static final TrackedData<Optional<BlockPos>> ASSIGNED_BED_POS = data(OPTIONAL_BLOCK_POS);
  static final TrackedData<Boolean> IS_CARRYING_FETCH_ITEM = data(BOOLEAN);
  static final TrackedData<String> ACTIVE_FETCH_TYPE_ID = data(STRING);
  static final TrackedData<ItemStack> CARRIED_FETCH_ITEM_STACK = data(ITEM_STACK);
  static final TrackedData<ItemStack> PENDANT_ITEM = data(ITEM_STACK);
  static final TrackedData<ItemStack> COSMETIC_ITEM = data(ITEM_STACK);

  static final TrackedData<Optional<UUID>> PLAY_PARTNER_UUID = data(OPTIONAL_UUID);

  static final TrackedData<Integer> COMMAND = data(INTEGER);
  static final TrackedData<Integer> FUR_STAGE = data(INTEGER);
  static final TrackedData<Integer> GROWTH_STAGE = data(INTEGER);
  static final TrackedData<Boolean> NAPPING = data(BOOLEAN);

  private static <T> TrackedData<T> data(final TrackedDataHandler<T> handler) {
    return DataTracker.registerData(UnleashedDogEntity.class, handler);
  }

  private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
  private java.util.UUID angryAt;

  private final DogAnimationState animationState = new DogAnimationState(this);
  private final DogVocalization vocalization = new DogVocalization(this);
  private final DogAmbienceEffects ambience = new DogAmbienceEffects(this);
  private final DogSleepController sleep = new DogSleepController(this);
  private final DogPlaySession play = new DogPlaySession(this);
  private final DogEquipmentHolder equipment = new DogEquipmentHolder(this);
  private final DogAppearanceRoller appearance = new DogAppearanceRoller(this);
  private final DogLineage lineage = new DogLineage(this);
  private final DogGeneHolder genes = new DogGeneHolder(this);
  private final DogEntityNbt persistence = new DogEntityNbt(this);
  private final DogInteractions interactions = new DogInteractions(this);
  private final DogCommandController commandController = new DogCommandController(this);
  private final DogComponents components =
      DogComponents.of(new DogTreatBuffState(this), new DogSpawnerOrigin(), new DogCollar(this));
  private final DogGenomeCache genomeCache = new DogGenomeCache(this);
  private final DogUndeadState undeadState = new DogUndeadState(this);
  private final DogCuring curing = new DogCuring(this);
  private final DogPendants pendants = new DogPendants(this);
  private final DogArmour armour = new DogArmour(this);
  private final DogFetchCarry fetchCarry = new DogFetchCarry(this);
  private final DogFur fur = new DogFur(this);
  private final DogGrowth growth = new DogGrowth(this);
  private final DogPack pack = new DogPack(this);
  private final DogGrowl growl = new DogGrowl(this);
  private final DogTrust trust = new DogTrust(this);
  private final DogRiding riding = new DogRiding(this);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final UnleashedDogBreed breed;
  private final boolean undead;

  public UnleashedDogEntity(
      EntityType<? extends TameableEntity> entityType,
      World world,
      UnleashedDogBreed breed,
      boolean undead) {
    super(entityType, world);
    this.breed = breed;
    this.undead = undead;
    this.equipment.guaranteeArmourDrop();
  }

  public static boolean canSpawn(
      final EntityType<? extends UnleashedDogEntity> type,
      final WorldAccess world,
      final SpawnReason spawnReason,
      final BlockPos pos,
      final Random random) {
    final BlockState ground = world.getBlockState(pos.down());
    return (ground.isIn(ModBlockTags.DOGS_SPAWNABLE_ON)
            || ground.isIn(ModBlockTags.spawnableOn(Registries.ENTITY_TYPE.getId(type))))
        && isLightLevelValidForNaturalSpawn(world, pos);
  }

  @Override
  public @Nullable EntityData initialize(
      ServerWorldAccess world,
      LocalDifficulty difficulty,
      SpawnReason spawnReason,
      @Nullable EntityData entityData) {
    if (spawnReason != SpawnReason.BREEDING) {
      if (this.breed == UnleashedDogBreed.CROSS_BREED && this.getGenome() == null) {
        this.applyGenome(this.appearance.randomFounderMix());
        this.setHealth(this.getMaxHealth());
      }
      this.getAppearanceRoller().rollAppearance(spawnReason);
    }
    this.undeadState.applyAttributeScaling();
    final EntityData packData = this.pack.join(spawnReason, entityData);
    this.growth.joinPack(packData);
    final EntityData initialized = super.initialize(world, difficulty, spawnReason, packData);
    this.growth.settleInPack();
    this.growth.refresh();
    this.fur.rollSpawnStage(spawnReason);
    WildPackFounding.bringPack(world, this, spawnReason, entityData, packData);
    return initialized;
  }

  @Override
  public int getLimitPerChunk() {
    return this.breed.nature().livesInPacks() ? 1 : super.getLimitPerChunk();
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return this.components.get(DogSpawnerOrigin.class).isFromSpawner() && !this.isTamed();
  }

  @Override
  public void setTamed(final boolean tamed, final boolean updateAttributes) {
    super.setTamed(tamed, updateAttributes);
    if (tamed) {
      this.components.get(DogSpawnerOrigin.class).setFromSpawner(false);
    }
  }

  public UnleashedDogBreed getBreed() {
    return this.breed;
  }

  public boolean isUndead() {
    return this.undead;
  }

  public DogUndeadState getUndeadState() {
    return this.undeadState;
  }

  public DogCuring getCuring() {
    return this.curing;
  }

  boolean isExposedToDaylight() {
    return this.isAffectedByDaylight();
  }

  public UnleashedDogBreed getRigSourceBreed() {
    final DogGenome genome = this.getGenome();
    return DogGenome.rigBreedOf(this.breed, genome != null ? genome.composition() : List.of());
  }

  public UnleashedDogBreed getVoiceBreed() {
    final DogGenome genome = this.getGenome();
    return genome != null ? genome.voiceBreed() : this.breed;
  }

  public @Nullable DogGenome getGenome() {
    return this.genomeCache.get();
  }

  public void applyGenome(final DogGenome genome) {
    this.genomeCache.apply(genome);
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> trackedData) {
    super.onTrackedDataSet(trackedData);
    if (GENOME.equals(trackedData)) {
      this.genomeCache.invalidate();
    }
    if (GROWTH_STAGE.equals(trackedData)) {
      this.calculateDimensions();
    }
  }

  public DogAppearance getAppearance() {
    return new DogAppearance(
        this.getRigSourceBreed(),
        this.dataTracker.get(COAT_VARIANT),
        this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  public void applyAppearance(final DogAppearance appearance) {
    this.dataTracker.set(COAT_VARIANT, appearance.coatVariantOrdinal());
    this.dataTracker.set(EYE_COLOR_VARIANT, appearance.eyeColorVariantOrdinal());
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ANGER_TIME, 0);
    builder.add(COLLAR_COLOR, DEFAULT_COLLAR_COLOR_ID);
    builder.add(TAIL_WAG_TIMER, 0);
    builder.add(SHAKE_PROGRESS, 0);
    builder.add(TREAT_BUFF_TICKS, 0);
    builder.add(HEAD_TILTING, false);
    builder.add(SLEEPING_IN_BED, false);
    builder.add(COMMANDED_TO_SLEEP, false);
    builder.add(ASSIGNED_BED_POS, Optional.empty());
    builder.add(IS_CARRYING_FETCH_ITEM, false);
    builder.add(ACTIVE_FETCH_TYPE_ID, DogPlaySession.NO_ACTIVE_FETCH_TYPE);
    builder.add(CARRIED_FETCH_ITEM_STACK, ItemStack.EMPTY);
    builder.add(PENDANT_ITEM, ItemStack.EMPTY);
    builder.add(COSMETIC_ITEM, ItemStack.EMPTY);
    builder.add(PLAY_PARTNER_UUID, Optional.empty());
    builder.add(COMMAND, DogCommand.FOLLOW.id());
    builder.add(COAT_VARIANT, 0);
    builder.add(EYE_COLOR_VARIANT, 0);
    builder.add(HOWLING, false);
    builder.add(GROWLING, false);
    builder.add(GENOME, new NbtCompound());
    builder.add(FUR_STAGE, DogFurStage.NATURAL.ordinal());
    builder.add(GROWTH_STAGE, DireWolfGrowthStage.YEARLING.number());
    builder.add(NAPPING, false);
  }

  public DogCommand getCommand() {
    return DogCommand.fromId(this.dataTracker.get(COMMAND));
  }

  void stopJumping() {
    this.jumping = false;
  }

  public DogAnimationState getAnimationState() {
    return this.animationState;
  }

  public DogVocalization getVocalization() {
    return this.vocalization;
  }

  public DogAmbienceEffects getAmbienceEffects() {
    return this.ambience;
  }

  public DogSleepController getSleepController() {
    return this.sleep;
  }

  public DogPlaySession getPlaySession() {
    return this.play;
  }

  DogAppearanceRoller getAppearanceRoller() {
    return this.appearance;
  }

  public DogGeneHolder getGenes() {
    return this.genes;
  }

  public DogLineage getLineage() {
    return this.lineage;
  }

  public DogFetchCarry getFetchCarry() {
    return this.fetchCarry;
  }

  public DogFur getFur() {
    return this.fur;
  }

  public DogGrowth getGrowth() {
    return this.growth;
  }

  public DogPack getPack() {
    return this.pack;
  }

  public DogGrowl getGrowl() {
    return this.growl;
  }

  public DogTrust getTrust() {
    return this.trust;
  }

  @Override
  public DogRiding getRiding() {
    return this.riding;
  }

  @Override
  public EntityDimensions getBaseDimensions(final EntityPose pose) {
    return this.growth.dimensions(super.getBaseDimensions(pose));
  }

  public DogPendants getPendants() {
    return this.pendants;
  }

  public DogEquipmentHolder getEquipmentHolder() {
    return this.equipment;
  }

  public DogInteractions getInteractions() {
    return this.interactions;
  }

  public DogCommandController getCommandController() {
    return this.commandController;
  }

  public DogComponents getComponents() {
    return this.components;
  }

  public @Nullable UnleashedDogCoat getCoatVariant() {
    return DogCoats.coatOf(this.getRigSourceBreed(), this.dataTracker.get(COAT_VARIANT));
  }

  public @Nullable HuskyEyeColor getEyeColorVariant() {
    return this.getRigSourceBreed().hasEyeColorVariants()
        ? HuskyEyeColor.fromOrdinal(this.dataTracker.get(EYE_COLOR_VARIANT))
        : null;
  }

  @Override
  public void wakeUp() {
    this.sleep.wakeUp();
  }

  @Override
  public boolean canUseSlot(final EquipmentSlot slot) {
    return DogEquipmentHolder.isArmourSlot(slot) || super.canUseSlot(slot);
  }

  @Override
  public Integer getScreenOpeningData(final ServerPlayerEntity player) {
    return this.getId();
  }

  @Override
  public ScreenHandler createMenu(
      final int syncId, final PlayerInventory playerInventory, final PlayerEntity player) {
    return this.equipment.createMenu(syncId, playerInventory);
  }

  @Override
  protected void dropEquipment(
      final ServerWorld world, final DamageSource source, final boolean causedByPlayer) {
    super.dropEquipment(world, source, causedByPlayer);
    this.equipment.dropModOwnedSlots();
  }

  @Override
  protected void initGoals() {
    DogGoalRegistrar.registerGoals(this, this.goalSelector, this.targetSelector);
  }

  @Override
  public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
    if (DogFriendlyTargetPolicy.isFriendlyTarget(target)) {
      return false;
    }
    return super.canAttackWithOwner(target, owner);
  }

  @Override
  public ActionResult interactMob(PlayerEntity player, Hand hand) {
    return this.interactions.interact(player, hand);
  }

  ActionResult vanillaInteract(final PlayerEntity player, final Hand hand) {
    return super.interactMob(player, hand);
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return !this.undead && DogFoods.isBreedingItem(stack);
  }

  @Override
  public boolean canBreedWith(AnimalEntity other) {
    return this.lineage.canBreedWith(other);
  }

  @Override
  public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
    return this.lineage.createChild(world, entity);
  }

  @Override
  public void setBaby(final boolean baby) {
    super.setBaby(baby);
    if (baby && !this.undead) {
      this.ambience.armBirthWakeHearts();
    }
    this.growth.refresh();
  }

  @Override
  protected void onGrowUp() {
    super.onGrowUp();
    this.growth.refresh();
  }

  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    if (target != null
        && (this.isBaby() || this.pack.staysOutOfFights() || this.riding.isRidden())) {
      return;
    }
    super.setTarget(target);
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient) {
      DogServerTick.run(this);
    }
  }

  @Override
  public boolean isFireImmune() {
    return super.isFireImmune() || this.pendants.isFireImmune();
  }

  @Override
  public boolean isInvulnerableTo(final DamageSource source) {
    return super.isInvulnerableTo(source) || this.pendants.guardsAgainst(source);
  }

  public DogArmour getArmour() {
    return this.armour;
  }

  @Override
  protected void applyDamage(final DamageSource source, final float amount) {
    if (this.armour.absorbs(source)) {
      this.armour.takeHit(source, amount);
      return;
    }
    super.applyDamage(source, amount);
    this.pendants.saveIfFatal(source);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    final SoundEvent armourSound = this.armour.hurtSound(source);
    return armourSound != null ? armourSound : super.getHurtSound(source);
  }

  @Override
  public boolean damage(DamageSource source, float amount) {
    if (this.isInvulnerableTo(source)) {
      return false;
    }
    if (!this.getWorld().isClient) {
      this.commandController.demoteSitToFollow();
      this.wakeUp();
      this.vocalization.barkIfReady(BARK_PITCH);
    }
    final boolean damaged = super.damage(source, amount);
    if (damaged && !this.getWorld().isClient) {
      this.sleep.recordHurt();
      this.pack.onHurtBy(source.getAttacker());
    }
    return damaged;
  }

  @Override
  public void onStruckByLightning(final ServerWorld world, final LightningEntity lightning) {
    if (this.undead) {
      return;
    }
    super.onStruckByLightning(world, lightning);
  }

  @Override
  public void remove(RemovalReason reason) {
    this.play.endOnRemoval(reason);
    super.remove(reason);
  }

  @Override
  public void onDeath(DamageSource damageSource) {
    this.play.endPlayMode();
    super.onDeath(damageSource);

    if (this.getWorld() instanceof ServerWorld serverWorld && this.isTamed()) {
      DogDeathHandler.onTamedDogDeath(this, serverWorld);
    }
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    this.persistence.writeNbt(nbt);
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    this.persistence.readNbt(nbt);
  }

  @Override
  public int getAngerTime() {
    return this.dataTracker.get(ANGER_TIME);
  }

  @Override
  public void setAngerTime(int angerTime) {
    this.dataTracker.set(ANGER_TIME, angerTime);
  }

  @Override
  public void chooseRandomAngerTime() {
    this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
  }

  @Override
  public java.util.UUID getAngryAt() {
    return this.angryAt;
  }

  @Override
  public void setAngryAt(java.util.UUID uuid) {
    this.angryAt = uuid;
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    DogAnimationControllers.register(this, controllers);
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
