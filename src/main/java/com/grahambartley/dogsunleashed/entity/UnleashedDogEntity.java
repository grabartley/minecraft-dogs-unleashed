package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.BARK_PITCH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.goal.AutoSleepGoal;
import com.grahambartley.dogsunleashed.entity.goal.CommandFollowOwnerGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchChaseGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchRetrieveGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchReturnGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchTemptGoal;
import com.grahambartley.dogsunleashed.entity.goal.FollowParentDogGoal;
import com.grahambartley.dogsunleashed.entity.goal.GuardTargetGoal;
import com.grahambartley.dogsunleashed.entity.goal.HuntTargetGoal;
import com.grahambartley.dogsunleashed.entity.goal.PuppyAwareWanderGoal;
import com.grahambartley.dogsunleashed.entity.goal.ReturnToAnchorGoal;
import com.grahambartley.dogsunleashed.entity.goal.SleepInBedGoal;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class UnleashedDogEntity extends TameableEntity
    implements GeoEntity, Angerable, ExtendedScreenHandlerFactory<Integer> {

  // Keep in sync with PetData.DEFAULT_COLLAR_COLOR_ID. PetData mirrors this value from DyeColor
  // directly because referencing this constant would class-load UnleashedDogEntity, a MobEntity
  // subclass that fails bytecode verification on the unit-test classpath.
  public static final int DEFAULT_COLLAR_COLOR_ID = DyeColor.RED.getId();
  public static final int UNSET_VARIANT = -1;

  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final double ESCAPE_DANGER_SPEED = 1.5;
  private static final float POUNCE_STRENGTH = 0.4F;
  private static final double DEFAULT_GOAL_SPEED = 1.0;
  private static final float FOLLOW_OWNER_MAX_DISTANCE = 10.0F;
  private static final float FOLLOW_OWNER_MIN_DISTANCE = 2.0F;
  private static final float HEEL_MAX_DISTANCE = 4.0F;
  private static final float HEEL_MIN_DISTANCE = 1.5F;
  private static final float LOOK_AT_PLAYER_RANGE = 8.0F;
  private static final int PLAYER_ANGER_TARGET_CHANCE = 10;
  private static final double MOVEMENT_THRESHOLD = 0.001;
  private static final double NEARBY_PLAYER_RANGE = 10.0D;

  private static final TrackedData<Integer> ANGER_TIME =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> EYE_COLOR_VARIANT =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Boolean> HOWLING =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<NbtCompound> GENOME =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
  private static final TrackedData<Integer> COLLAR_COLOR =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> TAIL_WAG_TIMER =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> SHAKE_PROGRESS =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> TREAT_BUFF_TICKS =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Boolean> HEAD_TILTING =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Boolean> SLEEPING_IN_BED =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Boolean> COMMANDED_TO_SLEEP =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Optional<BlockPos>> ASSIGNED_BED_POS =
      DataTracker.registerData(
          UnleashedDogEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
  private static final TrackedData<Boolean> IS_CARRYING_FETCH_ITEM =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> ACTIVE_FETCH_TYPE_ID =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<ItemStack> CARRIED_FETCH_ITEM_STACK =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
  private static final TrackedData<ItemStack> PENDANT_ITEM =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
  private static final TrackedData<ItemStack> COSMETIC_ITEM =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
  // String over Identifier because 1.21.1 lacks native Identifier TrackedDataHandler.
  // Syncs the active fetch type id (e.g. "dogs-unleashed:stick") for client-side carry rendering.

  // Synced so the client can mirror the play-mode gate: ACTIVE_PLAY_SESSIONS only exists on the
  // logical server, so on a dedicated server the client predicts stick throws off this instead.
  private static final TrackedData<Optional<UUID>> PLAY_PARTNER_UUID =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

  // Int over enum because 1.21.1 has no enum TrackedDataHandler; DogCommand.fromId round-trips it.
  private static final TrackedData<Integer> COMMAND =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private BlockPos commandAnchorPos = null;

  private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
  private java.util.UUID angryAt;

  private final DogVocalization vocalization = new DogVocalization(this);
  private final DogAmbienceEffects ambience = new DogAmbienceEffects(this);
  private final DogSleepController sleep = new DogSleepController(this);
  private final DogPlaySession play = new DogPlaySession(this);
  private final DogEquipmentHolder equipment = new DogEquipmentHolder(this);
  private final DogAppearanceRoller appearance = new DogAppearanceRoller(this);
  private final DogLineage lineage = new DogLineage(this);
  private final DogEntityNbt persistence = new DogEntityNbt(this);
  private final DogInteractions interactions = new DogInteractions(this);
  private boolean spawnedByDogSpawner = false;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final UnleashedDogBreed breed;
  private @Nullable DogGenome cachedGenome;
  private boolean genomeCacheValid;

  private static final UnleashedDogBreed FALLBACK_RIG_BREED = UnleashedDogBreed.HUSKY;

  public UnleashedDogEntity(
      EntityType<? extends TameableEntity> entityType, World world, UnleashedDogBreed breed) {
    super(entityType, world);
    this.breed = breed;
    this.equipment.guaranteeArmourDrop();
  }

  // Mirrors WolfEntity.canSpawn: the vanilla AnimalEntity predicate only allows grass_block below,
  // which excludes the snow surfaces most husky spawn biomes are made of.
  public static boolean canSpawn(
      final EntityType<? extends UnleashedDogEntity> type,
      final WorldAccess world,
      final SpawnReason spawnReason,
      final BlockPos pos,
      final Random random) {
    return world.getBlockState(pos.down()).isIn(ModBlockTags.DOGS_SPAWNABLE_ON)
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
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  public boolean isSpawnedByDogSpawner() {
    return this.spawnedByDogSpawner;
  }

  public void setSpawnedByDogSpawner(final boolean spawnedByDogSpawner) {
    this.spawnedByDogSpawner = spawnedByDogSpawner;
  }

  /**
   * Wild animals never despawn ({@code AnimalEntity} hard-codes false), which is fine for
   * chunk-generation dogs but would let the cap-independent {@code DogSpawner} monotonically fill
   * the world. Untamed spawner-spawned dogs are therefore despawnable like ambient mobs; taming
   * clears the flag and restores permanent persistence.
   */
  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return this.spawnedByDogSpawner && !this.isTamed();
  }

  @Override
  public void setTamed(final boolean tamed, final boolean updateAttributes) {
    super.setTamed(tamed, updateAttributes);
    if (tamed) {
      this.spawnedByDogSpawner = false;
    }
  }

  public UnleashedDogBreed getBreed() {
    return this.breed;
  }

  public UnleashedDogBreed getRigSourceBreed() {
    if (this.breed != UnleashedDogBreed.CROSS_BREED) {
      return this.breed;
    }
    final DogGenome genome = this.getGenome();
    return genome != null ? genome.dominantBreed() : FALLBACK_RIG_BREED;
  }

  public UnleashedDogBreed getVoiceBreed() {
    final DogGenome genome = this.getGenome();
    return genome != null ? genome.voiceBreed() : this.breed;
  }

  public @Nullable DogGenome getGenome() {
    if (!this.genomeCacheValid) {
      final NbtCompound genomeNbt = this.dataTracker.get(GENOME);
      this.cachedGenome = genomeNbt.isEmpty() ? null : DogGenome.fromNbt(genomeNbt);
      this.genomeCacheValid = true;
    }
    return this.cachedGenome;
  }

  public void applyGenome(final DogGenome genome) {
    this.dataTracker.set(GENOME, genome.toNbt());
    this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(genome.maxHealth());
    this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
        .setBaseValue(genome.movementSpeed());
    this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
        .setBaseValue(genome.attackDamage());
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> data) {
    super.onTrackedDataSet(data);
    if (GENOME.equals(data)) {
      this.genomeCacheValid = false;
    }
  }

  public DogTraits getTraits() {
    return new DogTraits(
        this.getRigSourceBreed(),
        this.dataTracker.get(COAT_VARIANT),
        this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  public void applyTraits(final DogTraits traits) {
    this.dataTracker.set(COAT_VARIANT, traits.coatVariantOrdinal());
    this.dataTracker.set(EYE_COLOR_VARIANT, traits.eyeColorVariantOrdinal());
  }

  public boolean isHowling() {
    return this.dataTracker.get(HOWLING);
  }

  void setHowling(final boolean howling) {
    this.dataTracker.set(HOWLING, howling);
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
    builder.add(GENOME, new NbtCompound());
  }

  public DogCommand getCommand() {
    return DogCommand.fromId(this.dataTracker.get(COMMAND));
  }

  public @Nullable BlockPos getCommandAnchorPos() {
    return this.commandAnchorPos;
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

  public DogLineage getLineage() {
    return this.lineage;
  }

  public DogEquipmentHolder getEquipmentHolder() {
    return this.equipment;
  }

  public DogInteractions getInteractions() {
    return this.interactions;
  }

  void setCommandAnchorPosFromSave(final BlockPos anchorPos) {
    this.commandAnchorPos = anchorPos;
  }

  void setCommandFromSave(final DogCommand command) {
    this.dataTracker.set(COMMAND, command.id());
  }

  void setTreatBuffTicksFromSave(final int treatBuffTicks) {
    this.dataTracker.set(TREAT_BUFF_TICKS, treatBuffTicks);
  }

  /**
   * Single entry point for switching command modes: keeps the sitting pose, the Stay/Guard anchor,
   * and any in-progress sleep consistent with the new command.
   */
  public void applyCommand(final DogCommand command) {
    if (!this.isTamed()) {
      return;
    }
    if (this.isSleepingInBed() || this.isCommandedToSleep()) {
      this.getSleepController().markManuallyWoken();
      this.wakeUp();
    }
    this.dataTracker.set(COMMAND, command.id());
    this.commandAnchorPos = command.isAnchored() ? this.getBlockPos() : null;
    this.setSitting(command == DogCommand.SIT);
    this.jumping = false;
    this.navigation.stop();
    this.setTarget(null);
  }

  /** Bark-and-wag feedback for a command issued in person, separate from silent state changes. */
  public void acknowledgeCommand() {
    this.ambience.startTailWag();
    this.vocalization.barkIfReady(this.getVocalization().getBarkPitch());
  }

  /**
   * For code paths that force a sitting dog to stand (damage, play mode, bed-block sleep): the
   * command must stop being Sit or the pose and command would disagree, but a full {@link
   * #applyCommand} would also clear the attack target these paths may have just set.
   */
  void demoteSitToFollow() {
    if (this.getCommand() == DogCommand.SIT) {
      this.dataTracker.set(COMMAND, DogCommand.FOLLOW.id());
    }
    this.setSitting(false);
  }

  public DyeColor getCollarColor() {
    return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
  }

  public void setCollarColor(final DyeColor color) {
    this.dataTracker.set(COLLAR_COLOR, color.getId());
  }

  public @Nullable UnleashedDogCoat getCoatVariant() {
    return DogCoats.coatOf(this.getRigSourceBreed(), this.dataTracker.get(COAT_VARIANT));
  }

  public @Nullable HuskyEyeColor getEyeColorVariant() {
    return this.getRigSourceBreed().hasEyeColorVariants()
        ? HuskyEyeColor.fromOrdinal(this.dataTracker.get(EYE_COLOR_VARIANT))
        : null;
  }

  public int getShakeProgress() {
    return this.dataTracker.get(SHAKE_PROGRESS);
  }

  public boolean isShaking() {
    return this.getShakeProgress() > 0;
  }

  public boolean isHeadTilting() {
    return this.dataTracker.get(HEAD_TILTING);
  }

  void setHeadTilting(final boolean tilting) {
    this.dataTracker.set(HEAD_TILTING, tilting);
  }

  void setShakeProgress(final int progress) {
    this.dataTracker.set(SHAKE_PROGRESS, progress);
  }

  void setTailWagTimer(final int ticks) {
    this.dataTracker.set(TAIL_WAG_TIMER, ticks);
  }

  public boolean isSleepingInBed() {
    return this.dataTracker.get(SLEEPING_IN_BED);
  }

  public Optional<BlockPos> getAssignedBedPos() {
    return this.dataTracker.get(ASSIGNED_BED_POS);
  }

  public void setAssignedBedPos(final @Nullable BlockPos pos) {
    this.dataTracker.set(ASSIGNED_BED_POS, Optional.ofNullable(pos));
  }

  void setSleepingInBed(final boolean sleeping) {
    this.dataTracker.set(SLEEPING_IN_BED, sleeping);
  }

  void setCommandedToSleep(final boolean commanded) {
    this.dataTracker.set(COMMANDED_TO_SLEEP, commanded);
  }

  public boolean isCommandedToSleep() {
    return this.dataTracker.get(COMMANDED_TO_SLEEP);
  }

  @Override
  public void wakeUp() {
    this.sleep.wakeUp();
  }

  public boolean hasAssignedBed() {
    return this.getAssignedBedPos().isPresent();
  }

  public @Nullable UUID getPlayPartnerPlayerUuid() {
    return this.dataTracker.get(PLAY_PARTNER_UUID).orElse(null);
  }

  void setPlayPartnerPlayerUuid(final @Nullable UUID playerUuid) {
    this.dataTracker.set(PLAY_PARTNER_UUID, Optional.ofNullable(playerUuid));
  }

  String getActiveFetchTypeId() {
    return this.dataTracker.get(ACTIVE_FETCH_TYPE_ID);
  }

  void setActiveFetchTypeId(final String activeFetchTypeId) {
    this.dataTracker.set(ACTIVE_FETCH_TYPE_ID, activeFetchTypeId);
  }

  public @Nullable FetchItemType getActiveFetchType() {
    return DogPlaySession.fetchTypeFromId(this.getActiveFetchTypeId());
  }

  public void setActiveFetchType(final @Nullable FetchItemType activeFetchType) {
    this.setActiveFetchTypeId(DogPlaySession.fetchTypeIdOf(activeFetchType));
  }

  public boolean isCarryingFetchItem() {
    return this.dataTracker.get(IS_CARRYING_FETCH_ITEM);
  }

  public void setCarryingFetchItem(boolean carrying) {
    this.dataTracker.set(IS_CARRYING_FETCH_ITEM, carrying);
    if (!carrying) {
      this.dataTracker.set(CARRIED_FETCH_ITEM_STACK, ItemStack.EMPTY);
    }
  }

  public ItemStack getCarriedFetchItemStack() {
    return this.dataTracker.get(CARRIED_FETCH_ITEM_STACK);
  }

  public void setCarriedFetchItemStack(ItemStack stack) {
    this.dataTracker.set(CARRIED_FETCH_ITEM_STACK, stack);
  }

  ItemStack getPendantItem() {
    return this.dataTracker.get(PENDANT_ITEM);
  }

  void setPendantItem(final ItemStack stack) {
    this.dataTracker.set(PENDANT_ITEM, stack);
  }

  ItemStack getCosmeticItem() {
    return this.dataTracker.get(COSMETIC_ITEM);
  }

  void setCosmeticItem(final ItemStack stack) {
    this.dataTracker.set(COSMETIC_ITEM, stack);
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

  public static boolean isAnyDogInPlayModeFor(final UUID playerUuid) {
    return DogPlaySession.isAnyDogInPlayModeFor(playerUuid);
  }

  public static boolean isAnyNearbyDogInPlayModeFor(final PlayerEntity player) {
    return DogPlaySession.isAnyNearbyDogInPlayModeFor(player);
  }

  public static boolean isAnyDogInPlayMode() {
    return DogPlaySession.isAnyDogInPlayMode();
  }

  public static void clearActivePlaySessions() {
    DogPlaySession.clearActivePlaySessions();
  }

  public int getTailWagTimerTicks() {
    return this.dataTracker.get(TAIL_WAG_TIMER);
  }

  public int getTreatBuffTicks() {
    return this.dataTracker.get(TREAT_BUFF_TICKS);
  }

  public boolean hasTreatBuff() {
    return this.getTreatBuffTicks() > 0;
  }

  /**
   * Starts (or refreshes) the Dog Treat buff and plays the reaction: a tail wag, a heart burst and
   * a single bark. Refreshing resets the full duration rather than stacking, matching how vanilla
   * handles a re-applied status effect of equal strength.
   */
  public void applyTreatBuff() {
    this.dataTracker.set(TREAT_BUFF_TICKS, DogTreatBuff.DURATION_TICKS);
    DogTreatBuff.apply(this);
    this.ambience.startTailWag();
    this.ambience.burstHearts();
    this.vocalization.forceBark(this.getVocalization().getBarkPitch());
  }

  private void tickTreatBuff() {
    final int remaining = this.dataTracker.get(TREAT_BUFF_TICKS);
    if (remaining <= 0) {
      return;
    }
    if (remaining == 1) {
      DogTreatBuff.clear(this);
    }
    this.dataTracker.set(TREAT_BUFF_TICKS, remaining - 1);
  }

  @Override
  protected void initGoals() {
    this.goalSelector.add(1, new SwimGoal(this));
    this.goalSelector.add(2, new SitGoal(this));
    this.goalSelector.add(3, new SleepInBedGoal(this));
    this.goalSelector.add(3, new FetchChaseGoal(this));
    this.goalSelector.add(3, new FetchRetrieveGoal(this));
    this.goalSelector.add(3, new FetchReturnGoal(this));
    this.goalSelector.add(4, new AutoSleepGoal(this));
    this.goalSelector.add(5, new EscapeDangerGoal(this, ESCAPE_DANGER_SPEED));
    this.goalSelector.add(6, new PounceAtTargetGoal(this, POUNCE_STRENGTH));
    this.goalSelector.add(7, new MeleeAttackGoal(this, DEFAULT_GOAL_SPEED, true));
    this.goalSelector.add(8, new AnimalMateGoal(this, DEFAULT_GOAL_SPEED));
    this.goalSelector.add(
        9, new TemptGoal(this, DEFAULT_GOAL_SPEED, DogFoods.tamingIngredient(), false));
    this.goalSelector.add(
        9, new FetchTemptGoal(this, DEFAULT_GOAL_SPEED, FetchTypes.asIngredient(), false));
    // The three goals below share a priority; their command gates keep them mutually exclusive.
    this.goalSelector.add(10, new ReturnToAnchorGoal(this));
    this.goalSelector.add(
        10,
        new CommandFollowOwnerGoal(
            this,
            DEFAULT_GOAL_SPEED,
            FOLLOW_OWNER_MAX_DISTANCE,
            FOLLOW_OWNER_MIN_DISTANCE,
            EnumSet.of(DogCommand.FOLLOW, DogCommand.HUNT)));
    this.goalSelector.add(
        10,
        new CommandFollowOwnerGoal(
            this,
            DEFAULT_GOAL_SPEED,
            HEEL_MAX_DISTANCE,
            HEEL_MIN_DISTANCE,
            EnumSet.of(DogCommand.HEEL)));
    this.goalSelector.add(11, new FollowParentDogGoal(this, DEFAULT_GOAL_SPEED));
    this.goalSelector.add(12, new PuppyAwareWanderGoal(this, DEFAULT_GOAL_SPEED));
    this.goalSelector.add(13, new LookAtEntityGoal(this, PlayerEntity.class, LOOK_AT_PLAYER_RANGE));
    this.goalSelector.add(14, new LookAroundGoal(this));

    this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    this.targetSelector.add(
        3, new RevengeGoal(this, DogFriendlyTargetPolicy.FRIENDLY_TARGET_TYPES));
    this.targetSelector.add(
        4,
        new ActiveTargetGoal<>(
            this,
            PlayerEntity.class,
            PLAYER_ANGER_TARGET_CHANCE,
            true,
            false,
            this::shouldAngerAt));
    this.targetSelector.add(5, new UniversalAngerGoal<>(this, true));
    this.targetSelector.add(6, new HuntTargetGoal(this));
    this.targetSelector.add(6, new GuardTargetGoal(this));
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

  /** Lets {@link DogInteractions} reach the vanilla interaction it defers to. */
  ActionResult vanillaInteract(final PlayerEntity player, final Hand hand) {
    return super.interactMob(player, hand);
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return DogFoods.isBreedingItem(stack);
  }

  public boolean isTamingItem(ItemStack stack) {
    return DogFoods.isTamingItem(stack);
  }

  public String getTamedName() {
    if (this.getWorld() instanceof ServerWorld serverWorld) {
      final PetManager petManager = PetManager.get(serverWorld.getServer());
      final PetData petData = petManager.getPetByEntityId(this.getUuid());
      if (petData != null) {
        return petData.getName();
      }
    }
    return Text.translatable(this.getBreed().translationKey()).getString();
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
    if (baby) {
      this.ambience.armBirthWakeHearts();
    }
  }

  /**
   * Puppies are non-combatants until they grow up. Refusing to accept a target while {@code
   * isBaby()} keeps {@code PounceAtTargetGoal} and {@code MeleeAttackGoal} inert (both require a
   * target to start) and naturally restores adult combat AI the moment the dog stops being a baby.
   */
  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    if (target != null && this.isBaby()) {
      return;
    }
    super.setTarget(target);
  }

  boolean isPlayerHoldingTamingOrBreedingItem(final PlayerEntity player) {
    return DogFoods.isHoldingTamingOrBreedingItem(player);
  }

  protected boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared()
        > MOVEMENT_THRESHOLD;
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient) {
      if (this.isLeashed() && (this.isSleepingInBed() || this.isCommandedToSleep())) {
        this.wakeUp();
      }

      final PlayerEntity nearbyPlayer = this.getWorld().getClosestPlayer(this, NEARBY_PLAYER_RANGE);

      this.ambience.updateSocialCues(nearbyPlayer);

      this.vocalization.tick(nearbyPlayer);
      this.tickTreatBuff();

      this.ambience.tickShakeOff();
    }
  }

  @Override
  public boolean damage(DamageSource source, float amount) {
    if (this.isInvulnerableTo(source)) {
      return false;
    }
    if (!this.getWorld().isClient) {
      this.demoteSitToFollow();
      this.wakeUp();
      this.vocalization.barkIfReady(BARK_PITCH);
    }
    return super.damage(source, amount);
  }

  /**
   * Recreates this dog in the destination world at the given position, which may be the current
   * world. Recreation, rather than an in-place teleport, guarantees clients receive a fresh spawn
   * at the correct position: in-place long-range teleports of entities streamed in from
   * ticket-loaded chunks leave stale tracker state behind, making the dog invisible until relog.
   *
   * @return the dog entity in the destination world (may be a different instance from {@code this})
   */
  public UnleashedDogEntity teleportToWorld(ServerWorld destination, Vec3d pos) {
    final NbtCompound nbt = this.writeNbt(new NbtCompound());
    nbt.remove("Dimension");

    final ServerWorld currentWorld = (ServerWorld) this.getWorld();
    final UnleashedDogEntity newDog = (UnleashedDogEntity) this.getType().create(destination);
    if (newDog == null) {
      DogsUnleashed.log.warn(
          "[Dog] teleportToWorld: failed to create entity in {}",
          destination.getRegistryKey().getValue());
      return this;
    }

    newDog.readNbt(nbt);
    newDog.setPos(pos.x, pos.y, pos.z);
    newDog.setYaw(this.getYaw());
    newDog.setPitch(this.getPitch());

    this.remove(RemovalReason.CHANGED_DIMENSION);
    currentWorld
        .getChunk(this.getBlockPos().getX() >> 4, this.getBlockPos().getZ() >> 4)
        .setNeedsSaving(true);

    final Entity existing = destination.getEntity(this.getUuid());
    if (existing != null && existing != this) {
      DogsUnleashed.log.warn(
          "[Dog] teleportToWorld: removing stale entity {} from {} (UUID collision)",
          existing.getUuid(),
          destination.getRegistryKey().getValue());
      existing.remove(RemovalReason.DISCARDED);
    }

    destination.spawnEntity(newDog);

    DogsUnleashed.log.info(
        "[Dog] teleportToWorld: new dog {} spawned in {}",
        newDog.getUuid(),
        destination.getRegistryKey().getValue());
    return newDog;
  }

  @Override
  public void remove(RemovalReason reason) {
    this.play.endOnRemoval(reason);
    super.remove(reason);
  }

  @Override
  public void onDeath(DamageSource damageSource) {
    this.getPlaySession().endPlayMode();
    super.onDeath(damageSource);

    // CRITICAL: Only spawn graves for tamed dogs
    if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
      return;
    }

    if (!this.isTamed()) {
      return;
    }

    final PetManager petManager = PetManager.get(serverWorld.getServer());
    final PetData petData = petManager.getPetByEntityId(this.getUuid());
    if (petData != null) {
      petData.syncAppearanceFrom(this);
      petManager.updatePet(petData);
    }
    petManager.markPetDeceased(this.getUuid());

    // Get bed position before clearing it (needed to avoid spawning grave on bed)
    final BlockPos bedPosToAvoid = this.getAssignedBedPos().orElse(null);

    this.getAssignedBedPos()
        .ifPresent(
            bedPos -> {
              if (serverWorld.getBlockEntity(bedPos) instanceof DogBedBlockEntity bedEntity) {
                bedEntity.clearAssignedDog(serverWorld);
              }
            });

    if (DogsUnleashed.SERVER_CONFIG.gravesEnabled()) {
      DogGraveSpawner.spawnGrave(serverWorld, this, bedPosToAvoid);
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
    controllers.add(
        new AnimationController<>(
            this,
            "movement",
            0,
            state -> {
              if (state.getAnimatable().isSleepingInBed()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.SLEEP));
              }
              if (state.getAnimatable().isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
              }
              if (this.isMoving(state)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
              }
              return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }));

    controllers.add(
        new AnimationController<>(
            this,
            "tail",
            0,
            state -> {
              final UnleashedDogEntity dog = state.getAnimatable();
              if (dog.getTailWagTimerTicks() > 0 && !dog.isSleepingInBed()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("tail_wag"));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            this,
            "shake",
            0,
            state -> {
              if (state.getAnimatable().isShaking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("shake"));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            this,
            "howl",
            0,
            state -> {
              if (this.isHowling()) {
                if (this.isInSittingPose()) {
                  return state.setAndContinue(
                      RawAnimation.begin().thenLoop(DogAnimationKeys.HOWL_SIT));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.HOWL));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            this,
            "head_tilt",
            5,
            state -> {
              final UnleashedDogEntity dog = state.getAnimatable();
              if (dog.isHeadTilting()) {
                if (state.getController().getAnimationState()
                    == AnimationController.State.STOPPED) {
                  state.getController().forceAnimationReset();
                }
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("head_tilt"));
              }
              return PlayState.STOP;
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
