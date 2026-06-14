package com.grahambartley.entity;

import static com.grahambartley.ModConstants.BARK_COOLDOWN_TICKS;
import static com.grahambartley.ModConstants.BARK_PITCH;
import static com.grahambartley.ModConstants.LOW_HEALTH_THRESHOLD;
import static com.grahambartley.ModConstants.MINECRAFT_TICK_RATE;
import static com.grahambartley.ModConstants.RANDOM_BARK_CHANCE;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.ModBlocks;
import com.grahambartley.ModNbtKeys;
import com.grahambartley.block.DogBedBlock;
import com.grahambartley.block.DogGraveBlock;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.block.entity.DogGraveBlockEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import com.grahambartley.entity.goal.AutoSleepGoal;
import com.grahambartley.entity.goal.FetchChaseGoal;
import com.grahambartley.entity.goal.FetchRetrieveGoal;
import com.grahambartley.entity.goal.FetchReturnGoal;
import com.grahambartley.entity.goal.FetchTemptGoal;
import com.grahambartley.entity.goal.SleepInBedGoal;
import com.grahambartley.entity.variant.UnleashedDogCoat;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import com.grahambartley.util.BreedingOwnerResolver;
import com.grahambartley.util.DogNames;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
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
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class UnleashedDogEntity extends TameableEntity implements GeoEntity, Angerable {

  public static final int DEFAULT_COLLAR_COLOR_ID = DyeColor.RED.getId();
  public static final int UNSET_VARIANT = -1;

  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final double SLEEP_POSITION_Y_OFFSET = 0.1;
  private static final int FETCH_DETECTION_XZ_RANGE = 128;
  private static final int FETCH_DETECTION_Y_RANGE = 64;
  private static final int SHAKE_PARTICLE_COUNT = 20;
  private static final double SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE = 0.5;
  private static final double SHAKE_PARTICLE_VERTICAL_OFFSET_RANGE = 0.5;
  private static final double SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE = 0.3;
  private static final double SHAKE_PARTICLE_VERTICAL_VELOCITY_RANGE = 0.1;
  private static final double SHAKE_PARTICLE_SPEED = 0.1;
  private static final double ESCAPE_DANGER_SPEED = 1.5;
  private static final float POUNCE_STRENGTH = 0.4F;
  private static final double DEFAULT_GOAL_SPEED = 1.0;
  private static final float FOLLOW_OWNER_MAX_DISTANCE = 10.0F;
  private static final float FOLLOW_OWNER_MIN_DISTANCE = 2.0F;
  private static final float LOOK_AT_PLAYER_RANGE = 8.0F;
  private static final int PLAYER_ANGER_TARGET_CHANCE = 10;
  private static final int TAME_SUCCESS_CHANCE = 3;
  private static final long DAY_LENGTH_TICKS = 24000;
  private static final long NIGHT_START_TICK = 13000;
  private static final float BREEDING_ITEM_HEAL_AMOUNT = 2.0F;
  private static final int RANDOM_TAIL_WAG_CHANCE = 200;
  private static final double MOVEMENT_THRESHOLD = 0.001;
  private static final double NEARBY_PLAYER_RANGE = 10.0D;
  private static final int GRAVE_SEARCH_RADIUS = 3;
  private static final int GRAVE_SEARCH_MIN_Y = -2;
  private static final int GRAVE_SEARCH_MAX_Y = 2;
  private static final int HORIZONTAL_DIRECTION_COUNT = 4;

  private static final TrackedData<Integer> ANGER_TIME =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> COLLAR_COLOR =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> TAIL_WAG_TIMER =
      DataTracker.registerData(UnleashedDogEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> SHAKE_PROGRESS =
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
  // String over Identifier because 1.21.1 lacks native Identifier TrackedDataHandler.
  // Syncs the active fetch type id (e.g. "dogs-unleashed:stick") for client-side carry rendering.

  private static final String NO_ACTIVE_FETCH_TYPE = "";

  private static final Map<UUID, UUID> ACTIVE_PLAY_SESSIONS = new HashMap<>();

  private boolean inPlayMode = false;
  private UUID playPartnerPlayerUuid = null;
  private BlockPos activeFetchBlockPos = null;

  private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
  private java.util.UUID angryAt;
  private static final float TAIL_WAG_DURATION_SECONDS = 3.75f;
  private static final int TAIL_WAG_DURATION_TICKS =
      (int) (TAIL_WAG_DURATION_SECONDS * MINECRAFT_TICK_RATE);
  private static final int SHAKE_DURATION_TICKS = 22;
  private static final int SHAKE_PARTICLE_START_TICK = 10;
  private static final int SHAKE_DELAY_TICKS = 20;

  private boolean wasInWater = false;
  private int ticksSinceLeftWater = 0;
  private static final Ingredient BREEDING_INGREDIENT =
      Ingredient.ofItems(
          Items.CHICKEN,
          Items.COOKED_CHICKEN,
          Items.BEEF,
          Items.COOKED_BEEF,
          Items.PORKCHOP,
          Items.COOKED_PORKCHOP,
          Items.MUTTON,
          Items.COOKED_MUTTON,
          Items.RABBIT,
          Items.COOKED_RABBIT,
          Items.ROTTEN_FLESH);
  private static final Ingredient TAMING_INGREDIENT =
      Ingredient.ofItems(
          Items.CHICKEN,
          Items.COOKED_CHICKEN,
          Items.BEEF,
          Items.COOKED_BEEF,
          Items.PORKCHOP,
          Items.COOKED_PORKCHOP,
          Items.MUTTON,
          Items.COOKED_MUTTON,
          Items.RABBIT,
          Items.COOKED_RABBIT,
          Items.ROTTEN_FLESH,
          Items.BONE);

  private int barkCooldownTicks = 0;
  private int manuallyWokenAge = -1;
  private boolean manuallyWokenAtNight = false;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public UnleashedDogEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  public @Nullable EntityData initialize(
      ServerWorldAccess world,
      LocalDifficulty difficulty,
      SpawnReason spawnReason,
      @Nullable EntityData entityData) {
    if (spawnReason != SpawnReason.BREEDING) {
      this.rollAppearance(spawnReason);
    }
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  protected abstract UnleashedDogEntity createBaby(ServerWorld world);

  protected abstract boolean isSameSpecies(MobEntity entity);

  public abstract UnleashedDogBreed getBreed();

  public final String getBreedId() {
    return this.getBreed().serializedId();
  }

  protected abstract @Nullable SoundEvent getBarkSound();

  protected String getSleepInBedMovementAnimationName() {
    return DogAnimationKeys.SIT;
  }

  protected void rollAppearance(final SpawnReason spawnReason) {}

  protected void tickBreedSpecificSounds() {}

  public int getBarkCooldownTicks() {
    return this.barkCooldownTicks;
  }

  protected boolean canBark() {
    return !this.isDead() && !this.isSleepingInBed() && this.barkCooldownTicks <= 0;
  }

  private boolean shouldBark(final PlayerEntity nearbyPlayer) {
    if (nearbyPlayer != null && this.isPlayerHoldingTamingOrBreedingItem(nearbyPlayer)) {
      return true;
    }
    if (this.getHealth() < this.getMaxHealth() * LOW_HEALTH_THRESHOLD) {
      return true;
    }
    if (this.getTarget() != null) {
      return true;
    }
    return this.random.nextInt(RANDOM_BARK_CHANCE) == 0;
  }

  private void tryBark(final PlayerEntity nearbyPlayer) {
    if (this.canBark() && this.shouldBark(nearbyPlayer)) {
      this.playSound(this.getBarkSound(), DogsUnleashed.SERVER_CONFIG.barkVolume(), BARK_PITCH);
      this.barkCooldownTicks = BARK_COOLDOWN_TICKS;
    }
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ANGER_TIME, 0);
    builder.add(COLLAR_COLOR, DEFAULT_COLLAR_COLOR_ID);
    builder.add(TAIL_WAG_TIMER, 0);
    builder.add(SHAKE_PROGRESS, 0);
    builder.add(HEAD_TILTING, false);
    builder.add(SLEEPING_IN_BED, false);
    builder.add(COMMANDED_TO_SLEEP, false);
    builder.add(ASSIGNED_BED_POS, Optional.empty());
    builder.add(IS_CARRYING_FETCH_ITEM, false);
    builder.add(ACTIVE_FETCH_TYPE_ID, NO_ACTIVE_FETCH_TYPE);
    builder.add(CARRIED_FETCH_ITEM_STACK, ItemStack.EMPTY);
  }

  public DyeColor getCollarColor() {
    return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
  }

  public void setCollarColor(final DyeColor color) {
    this.dataTracker.set(COLLAR_COLOR, color.getId());
  }

  public UnleashedDogCoat getCoatVariant() {
    return null;
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

  public boolean isSleepingInBed() {
    return this.dataTracker.get(SLEEPING_IN_BED);
  }

  public Optional<BlockPos> getAssignedBedPos() {
    return this.dataTracker.get(ASSIGNED_BED_POS);
  }

  public void setAssignedBedPos(BlockPos pos) {
    this.dataTracker.set(ASSIGNED_BED_POS, Optional.ofNullable(pos));
  }

  public void clearAssignedBed() {
    this.dataTracker.set(ASSIGNED_BED_POS, Optional.empty());
    this.wakeUp();
  }

  public void commandToSleep(BlockPos bedPos) {
    if (!this.isTamed()) {
      return;
    }
    this.setAssignedBedPos(bedPos);
    this.setSitting(false);
    this.manuallyWokenAge = -1;
    this.manuallyWokenAtNight = false;
    this.dataTracker.set(COMMANDED_TO_SLEEP, true);
    this.navigation.startMovingTo(
        bedPos.getX() + POSITION_CENTER_OFFSET,
        bedPos.getY(),
        bedPos.getZ() + POSITION_CENTER_OFFSET,
        DEFAULT_GOAL_SPEED);
  }

  public boolean isCommandedToSleep() {
    return this.dataTracker.get(COMMANDED_TO_SLEEP);
  }

  public void markManuallyWoken() {
    this.manuallyWokenAge = this.age;
    final long timeOfDay = this.getWorld().getTimeOfDay() % DAY_LENGTH_TICKS;
    this.manuallyWokenAtNight = timeOfDay >= NIGHT_START_TICK;
  }

  public boolean isAutoSleepSuppressed() {
    if (!this.manuallyWokenAtNight) {
      return false;
    }

    final int elapsed = this.age - this.manuallyWokenAge;
    final long currentTimeOfDay = this.getWorld().getTimeOfDay() % DAY_LENGTH_TICKS;

    if (!shouldKeepAutoSleepSuppressed(elapsed, currentTimeOfDay)) {
      this.manuallyWokenAtNight = false;
      this.manuallyWokenAge = -1;
      return false;
    }

    return true;
  }

  static boolean shouldKeepAutoSleepSuppressed(int elapsed, long currentTimeOfDay) {
    if (elapsed >= DAY_LENGTH_TICKS) {
      return false;
    }

    return currentTimeOfDay >= NIGHT_START_TICK;
  }

  public void startSleepingInBed(BlockPos bedPos) {
    this.dataTracker.set(SLEEPING_IN_BED, true);
    this.dataTracker.set(COMMANDED_TO_SLEEP, false);
    this.refreshPositionAndAngles(
        bedPos.getX() + POSITION_CENTER_OFFSET,
        bedPos.getY() + SLEEP_POSITION_Y_OFFSET,
        bedPos.getZ() + POSITION_CENTER_OFFSET,
        this.getYaw(),
        this.getPitch());
    this.setVelocity(0, 0, 0);
    this.setNoGravity(true);
    this.navigation.stop();
  }

  public void wakeUp() {
    this.setNoGravity(false);
    this.dataTracker.set(SLEEPING_IN_BED, false);
    this.dataTracker.set(COMMANDED_TO_SLEEP, false);
  }

  public boolean hasAssignedBed() {
    return this.getAssignedBedPos().isPresent();
  }

  public boolean isInPlayMode() {
    return this.inPlayMode;
  }

  public UUID getPlayPartnerPlayerUuid() {
    return this.playPartnerPlayerUuid;
  }

  public BlockPos getActiveFetchBlockPos() {
    return this.activeFetchBlockPos;
  }

  public void setActiveFetchBlockPos(BlockPos pos) {
    this.activeFetchBlockPos = pos;
  }

  public @Nullable FetchItemType getActiveFetchType() {
    String activeFetchTypeId = this.dataTracker.get(ACTIVE_FETCH_TYPE_ID);
    if (activeFetchTypeId.isEmpty()) {
      return null;
    }
    return FetchTypes.forId(Identifier.of(activeFetchTypeId));
  }

  public void setActiveFetchType(@Nullable FetchItemType activeFetchType) {
    this.dataTracker.set(
        ACTIVE_FETCH_TYPE_ID,
        activeFetchType != null ? activeFetchType.id().toString() : NO_ACTIVE_FETCH_TYPE);
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

  public void startPlayMode(PlayerEntity player, FetchItemType fetchItemType) {
    if (ACTIVE_PLAY_SESSIONS.containsKey(player.getUuid())) {
      return;
    }
    this.inPlayMode = true;
    this.playPartnerPlayerUuid = player.getUuid();
    this.activeFetchBlockPos = null;
    this.setActiveFetchType(fetchItemType);
    this.setSitting(false);
    ACTIVE_PLAY_SESSIONS.put(player.getUuid(), this.getUuid());
  }

  public void endPlayMode() {
    ActivePlaySessions.clear(
        ACTIVE_PLAY_SESSIONS, this.inPlayMode, this.playPartnerPlayerUuid, this.getUuid());
    this.inPlayMode = false;
    this.playPartnerPlayerUuid = null;
    this.activeFetchBlockPos = null;
    this.setActiveFetchType(null);
    this.setCarryingFetchItem(false);
  }

  public static boolean isAnyDogInPlayModeFor(UUID playerUuid) {
    return ACTIVE_PLAY_SESSIONS.containsKey(playerUuid);
  }

  public static boolean isAnyDogInPlayMode() {
    return !ACTIVE_PLAY_SESSIONS.isEmpty();
  }

  public boolean isActivelyFetching() {
    if (!this.isInPlayMode()) {
      return false;
    }
    if (this.isCarryingFetchItem() || this.activeFetchBlockPos != null) {
      return true;
    }
    if (this.playPartnerPlayerUuid == null) {
      return false;
    }

    return !this.getWorld()
        .getEntitiesByClass(
            Entity.class,
            this.getBoundingBox()
                .expand(
                    FETCH_DETECTION_XZ_RANGE, FETCH_DETECTION_Y_RANGE, FETCH_DETECTION_XZ_RANGE),
            entity ->
                entity instanceof FetchProjectileEntity
                    && entity instanceof ProjectileEntity projectile
                    && projectile.getOwner() instanceof PlayerEntity player
                    && this.playPartnerPlayerUuid.equals(player.getUuid()))
        .isEmpty();
  }

  private void startShaking() {
    if (!this.isShaking() && !this.isInSittingPose()) {
      this.dataTracker.set(SHAKE_PROGRESS, SHAKE_DURATION_TICKS);
    }
  }

  public void spawnShakeParticles() {
    if (this.getWorld() instanceof ServerWorld serverWorld) {
      for (int i = 0; i < SHAKE_PARTICLE_COUNT; i++) {
        final double offsetX =
            (this.random.nextDouble() - POSITION_CENTER_OFFSET)
                * SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE;
        final double offsetY = this.random.nextDouble() * SHAKE_PARTICLE_VERTICAL_OFFSET_RANGE;
        final double offsetZ =
            (this.random.nextDouble() - POSITION_CENTER_OFFSET)
                * SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE;
        serverWorld.spawnParticles(
            ParticleTypes.SPLASH,
            this.getX() + offsetX,
            this.getY() + offsetY + POSITION_CENTER_OFFSET,
            this.getZ() + offsetZ,
            1,
            (this.random.nextDouble() - POSITION_CENTER_OFFSET)
                * SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE,
            this.random.nextDouble() * SHAKE_PARTICLE_VERTICAL_VELOCITY_RANGE,
            (this.random.nextDouble() - POSITION_CENTER_OFFSET)
                * SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE,
            SHAKE_PARTICLE_SPEED);
      }
    }
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
    this.goalSelector.add(9, new TemptGoal(this, DEFAULT_GOAL_SPEED, TAMING_INGREDIENT, false));
    this.goalSelector.add(
        9, new FetchTemptGoal(this, DEFAULT_GOAL_SPEED, FetchTypes.asIngredient(), false));
    this.goalSelector.add(
        10,
        new FollowOwnerGoal(
            this, DEFAULT_GOAL_SPEED, FOLLOW_OWNER_MAX_DISTANCE, FOLLOW_OWNER_MIN_DISTANCE));
    this.goalSelector.add(11, new WanderAroundFarGoal(this, DEFAULT_GOAL_SPEED));
    this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, LOOK_AT_PLAYER_RANGE));
    this.goalSelector.add(13, new LookAroundGoal(this));

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
    final ItemStack itemStack = player.getStackInHand(hand);

    if (this.getWorld().isClient) {
      final boolean shouldInteract = this.isOwner(player) || !this.isTamed();
      return shouldInteract ? ActionResult.CONSUME : ActionResult.PASS;
    }

    FetchItemType fetchItemType = FetchTypes.forItem(itemStack.getItem());
    if (this.isTamed() && this.isOwner(player) && player.isSneaking() && fetchItemType != null) {
      if (this.isInPlayMode()) {
        this.endPlayMode();
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.play_end", this.getTamedName()), true);
      } else {
        endOtherNearbyPlayModes(player);
        this.startPlayMode(player, fetchItemType);
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.play_start", this.getTamedName()), true);
      }
      return ActionResult.SUCCESS;
    }

    if (this.isTamed()) {
      if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
        itemStack.decrementUnlessCreative(1, player);
        this.heal(BREEDING_ITEM_HEAL_AMOUNT);
        return ActionResult.SUCCESS;
      }

      if (itemStack.getItem() instanceof DyeItem dyeItem) {
        final DyeColor dyeColor = dyeItem.getColor();
        this.setCollarColor(dyeColor);
        itemStack.decrementUnlessCreative(1, player);
        return ActionResult.SUCCESS;
      }

      if (this.isOwner(player) && !this.isTamingItem(itemStack)) {
        if (player.isSneaking()) {
          final String dogName = this.getTamedName();
          DogBedBlock.setPendingAssignment(player.getUuid(), this.getUuid());
          player.sendMessage(
              Text.translatable("message.dogs-unleashed.pending_bed_assignment", dogName), true);
        } else if (this.isSleepingInBed()) {
          this.markManuallyWoken();
          this.wakeUp();
          final String dogName = this.getTamedName();
          player.sendMessage(
              Text.translatable("block.dogs-unleashed.dog_bed.wake_command", dogName), true);
        } else {
          this.setSitting(!this.isSitting());
        }
        this.jumping = false;
        this.navigation.stop();
        this.setTarget(null);
        this.dataTracker.set(TAIL_WAG_TIMER, 0);
        return ActionResult.SUCCESS;
      }

      ActionResult actionResult = super.interactMob(player, hand);
      if (actionResult.isAccepted() || this.isBreedingItem(itemStack)) {
        return actionResult;
      }
    } else if (this.isTamingItem(itemStack)) {
      itemStack.decrementUnlessCreative(1, player);
      if (this.random.nextInt(TAME_SUCCESS_CHANCE) == 0) {
        this.tame(player);
      } else {
        this.getWorld()
            .sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
      }
      return ActionResult.SUCCESS;
    }

    return super.interactMob(player, hand);
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return BREEDING_INGREDIENT.test(stack);
  }

  public boolean isTamingItem(ItemStack stack) {
    return TAMING_INGREDIENT.test(stack);
  }

  private void tame(final @Nullable PlayerEntity player) {
    if (player == null) {
      return;
    }
    this.setOwner(player);
    this.navigation.stop();
    this.setTarget(null);
    this.setSitting(true);
    this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);

    if (this.getWorld() instanceof ServerWorld serverWorld) {
      final PetManager petManager = PetManager.get(serverWorld.getServer());
      final PetData petData =
          new PetData(
              this.getUuid(),
              player.getUuid(),
              this.getBreed(),
              DogNames.getRandomName(),
              this.getHealth(),
              this.getMaxHealth(),
              this.getBlockPos(),
              serverWorld.getRegistryKey().getValue().toString(),
              true);
      petData.syncAppearanceFrom(this);
      petManager.registerPet(petData);

      if (player instanceof ServerPlayerEntity serverPlayer) {
        ModNetworking.sendOpenNamingScreen(
            serverPlayer, this.getUuid(), this.getBreed(), petData.getName());
      }
    }
  }

  private String getTamedName() {
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
    if (other == this) {
      return false;
    }
    if (!this.isTamed()) {
      return false;
    }
    if (!this.isSameSpecies(other)) {
      return false;
    }
    final UnleashedDogEntity otherDog = (UnleashedDogEntity) other;
    if (!otherDog.isTamed()) {
      return false;
    }
    if (otherDog.isInSittingPose()) {
      return false;
    }
    return this.isInLove() && otherDog.isInLove();
  }

  @Override
  public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
    if (!this.isSameSpecies(entity)) {
      return null;
    }
    final UnleashedDogEntity baby = this.createBaby(world);
    baby.setBaby(true);
    baby.rollAppearance(SpawnReason.BREEDING);
    final PlayerEntity lovingPlayer = this.getLovingPlayer();
    if (lovingPlayer != null) {
      baby.tame(lovingPlayer);
    } else {
      final UUID inheritedOwnerUuid =
          BreedingOwnerResolver.resolveInheritedOwnerUuid(
              this.getOwnerUuid(),
              entity instanceof UnleashedDogEntity otherDog ? otherDog.getOwnerUuid() : null);
      if (inheritedOwnerUuid != null) {
        baby.setOwnerUuid(inheritedOwnerUuid);
        baby.setTamed(true, true);
      }
    }
    return baby;
  }

  private boolean isPlayerHoldingTamingOrBreedingItem(final PlayerEntity player) {
    return this.isTamingItem(player.getMainHandStack())
        || this.isTamingItem(player.getOffHandStack())
        || this.isBreedingItem(player.getMainHandStack())
        || this.isBreedingItem(player.getOffHandStack());
  }

  private void updateHeadTilt(final PlayerEntity nearbyPlayer) {
    final boolean shouldTilt =
        nearbyPlayer != null && this.isPlayerHoldingTamingOrBreedingItem(nearbyPlayer);
    this.dataTracker.set(HEAD_TILTING, shouldTilt);
  }

  private void updateTailWag(final PlayerEntity nearbyPlayer) {
    final int currentTimer = this.dataTracker.get(TAIL_WAG_TIMER);

    if (!this.isInSittingPose() && this.getAngerTime() <= 0) {
      boolean shouldWag = false;

      if (nearbyPlayer != null) {
        final boolean holdingTamingItem =
            this.isTamingItem(nearbyPlayer.getMainHandStack())
                || this.isTamingItem(nearbyPlayer.getOffHandStack());
        final boolean holdingBreedingItem =
            this.isBreedingItem(nearbyPlayer.getMainHandStack())
                || this.isBreedingItem(nearbyPlayer.getOffHandStack());

        shouldWag =
            (!this.isTamed() && holdingTamingItem) || (this.isTamed() && holdingBreedingItem);
      }

      if (shouldWag) {
        this.dataTracker.set(TAIL_WAG_TIMER, TAIL_WAG_DURATION_TICKS);
      } else if (currentTimer > 0) {
        this.dataTracker.set(TAIL_WAG_TIMER, currentTimer - 1);
      } else if (this.isTamed() && this.random.nextInt(RANDOM_TAIL_WAG_CHANCE) == 0) {
        this.dataTracker.set(TAIL_WAG_TIMER, TAIL_WAG_DURATION_TICKS);
      }
    } else if (currentTimer > 0) {
      this.dataTracker.set(TAIL_WAG_TIMER, currentTimer - 1);
    }
  }

  protected boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared()
        > MOVEMENT_THRESHOLD;
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient) {
      final PlayerEntity nearbyPlayer = this.getWorld().getClosestPlayer(this, NEARBY_PLAYER_RANGE);

      this.updateHeadTilt(nearbyPlayer);
      this.updateTailWag(nearbyPlayer);

      if (this.barkCooldownTicks > 0) {
        this.barkCooldownTicks--;
      }
      this.tryBark(nearbyPlayer);
      this.tickBreedSpecificSounds();

      final boolean inWater = this.isTouchingWater();

      if (inWater) {
        this.ticksSinceLeftWater = 0;
      } else {
        if (this.wasInWater) {
          this.ticksSinceLeftWater = 1;
        } else if (this.ticksSinceLeftWater > 0) {
          this.ticksSinceLeftWater++;
        }

        if (this.ticksSinceLeftWater == SHAKE_DELAY_TICKS && !this.isShaking()) {
          this.startShaking();
        }
      }

      this.wasInWater = inWater;

      final int shakeProgress = this.getShakeProgress();
      if (shakeProgress > 0) {
        final int ticksElapsed = SHAKE_DURATION_TICKS - shakeProgress;
        if (ticksElapsed == SHAKE_PARTICLE_START_TICK) {
          this.spawnShakeParticles();
        }
        this.dataTracker.set(SHAKE_PROGRESS, shakeProgress - 1);
      }
    }
  }

  @Override
  public boolean damage(DamageSource source, float amount) {
    if (this.isInvulnerableTo(source)) {
      return false;
    }
    if (!this.getWorld().isClient) {
      this.setSitting(false);
      this.wakeUp();
      if (this.canBark()) {
        this.playSound(this.getBarkSound(), DogsUnleashed.SERVER_CONFIG.barkVolume(), BARK_PITCH);
        this.barkCooldownTicks = BARK_COOLDOWN_TICKS;
      }
    }
    return super.damage(source, amount);
  }

  /**
   * Moves this dog to the destination world at the owner's position. Used for cross-dimension
   * summons and following the owner through portals.
   *
   * @return the dog entity in the destination world (may be a different instance from {@code this})
   */
  public UnleashedDogEntity followOwnerToDimension(
      ServerPlayerEntity owner, ServerWorld destination) {
    this.wakeUp();
    this.setSitting(false);

    final NbtCompound nbt = this.writeNbt(new NbtCompound());
    nbt.remove("Dimension");

    final ServerWorld currentWorld = (ServerWorld) this.getWorld();
    final UnleashedDogEntity newDog = (UnleashedDogEntity) this.getType().create(destination);
    if (newDog == null) {
      DogsUnleashed.log.warn(
          "[Dog] followOwnerToDimension: failed to create entity in {}",
          destination.getRegistryKey().getValue());
      return this;
    }

    newDog.readNbt(nbt);
    newDog.setPos(owner.getX(), owner.getY(), owner.getZ());
    newDog.setYaw(this.getYaw());
    newDog.setPitch(this.getPitch());

    this.remove(RemovalReason.CHANGED_DIMENSION);
    currentWorld
        .getChunk(this.getBlockPos().getX() >> 4, this.getBlockPos().getZ() >> 4)
        .setNeedsSaving(true);

    final Entity existing = destination.getEntity(this.getUuid());
    if (existing != null && existing != this) {
      DogsUnleashed.log.warn(
          "[Dog] followOwnerToDimension: removing stale entity {} from {} (UUID collision)",
          existing.getUuid(),
          destination.getRegistryKey().getValue());
      existing.remove(RemovalReason.DISCARDED);
    }

    destination.spawnEntity(newDog);

    DogsUnleashed.log.info(
        "[Dog] followOwnerToDimension: new dog {} spawned in {}",
        newDog.getUuid(),
        destination.getRegistryKey().getValue());
    return newDog;
  }

  @Override
  public void remove(RemovalReason reason) {
    if (ActivePlaySessions.shouldEndOnRemoval(reason)) {
      this.endPlayMode();
    }
    super.remove(reason);
  }

  private void endOtherNearbyPlayModes(PlayerEntity player) {
    for (UnleashedDogEntity dog :
        this.getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                this.getBoundingBox()
                    .expand(
                        FETCH_DETECTION_XZ_RANGE,
                        FETCH_DETECTION_Y_RANGE,
                        FETCH_DETECTION_XZ_RANGE),
                candidate ->
                    candidate != this
                        && candidate.isInPlayMode()
                        && player.getUuid().equals(candidate.getPlayPartnerPlayerUuid()))) {
      dog.endPlayMode();
    }
  }

  @Override
  public void onDeath(DamageSource damageSource) {
    this.endPlayMode();
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
      spawnGrave(serverWorld, bedPosToAvoid);
    }
  }

  private void spawnGrave(ServerWorld world, BlockPos bedPosToAvoid) {
    final BlockPos deathPos = this.getBlockPos();

    final BlockPos gravePos = findValidGravePosition(world, deathPos, bedPosToAvoid);

    if (gravePos != null) {
      final Direction facing =
          Direction.Type.HORIZONTAL.stream()
              .toList()
              .get(world.getRandom().nextInt(HORIZONTAL_DIRECTION_COUNT));
      world.setBlockState(
          gravePos, ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.FACING, facing));

      if (world.getBlockEntity(gravePos) instanceof DogGraveBlockEntity graveEntity) {
        graveEntity.setDogUuid(this.getUuid());
        graveEntity.setFlowerColor(this.getCollarColor());

        final PetManager petManager = PetManager.get(world.getServer());
        final PetData petData = petManager.getPetByEntityId(this.getUuid());
        final String dogName = petData != null ? petData.getName() : this.getName().getString();

        graveEntity.setDogName(dogName);
      }
    }
  }

  private BlockPos findValidGravePosition(
      ServerWorld world, BlockPos center, BlockPos bedPosToAvoid) {
    // First pass: look for air blocks (preferred)
    for (int radius = 0; radius <= GRAVE_SEARCH_RADIUS; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
            continue;
          }

          for (int dy = GRAVE_SEARCH_MIN_Y; dy <= GRAVE_SEARCH_MAX_Y; dy++) {
            final BlockPos testPos = center.add(dx, dy, dz);

            // Skip bed position if provided
            if (bedPosToAvoid != null && testPos.equals(bedPosToAvoid)) {
              continue;
            }

            // Prefer positions that are currently air
            if (world.getBlockState(testPos).isAir() && isValidGravePosition(world, testPos)) {
              return testPos;
            }
          }
        }
      }
    }

    // Second pass: accept replaceable blocks if no air found
    for (int radius = 0; radius <= GRAVE_SEARCH_RADIUS; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
            continue;
          }

          for (int dy = GRAVE_SEARCH_MIN_Y; dy <= GRAVE_SEARCH_MAX_Y; dy++) {
            final BlockPos testPos = center.add(dx, dy, dz);

            // Skip bed position if provided
            if (bedPosToAvoid != null && testPos.equals(bedPosToAvoid)) {
              continue;
            }

            if (isValidGravePosition(world, testPos)) {
              return testPos;
            }
          }
        }
      }
    }

    return null;
  }

  private boolean isValidGravePosition(ServerWorld world, BlockPos pos) {
    final BlockState stateAtPos = world.getBlockState(pos);
    final BlockState stateBelow = world.getBlockState(pos.down());
    final BlockState stateAbove = world.getBlockState(pos.up());

    final boolean hasValidGround =
        !stateBelow.isAir() && stateBelow.isSolidBlock(world, pos.down());
    final boolean canReplace = stateAtPos.isReplaceable() || stateAtPos.isAir();
    final boolean hasAirAbove = stateAbove.isAir() || stateAbove.isReplaceable();
    final boolean notInFluid = !stateAtPos.isOf(Blocks.WATER) && !stateAtPos.isOf(Blocks.LAVA);

    return hasValidGround && canReplace && hasAirAbove && notInFluid;
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    this.writeAngerToNbt(nbt);
    nbt.putInt(ModNbtKeys.COLLAR_COLOR, this.getCollarColor().getId());
    nbt.putInt(ModNbtKeys.SHAKE_PROGRESS, this.getShakeProgress());
    nbt.putBoolean(ModNbtKeys.WAS_IN_WATER, this.wasInWater);
    nbt.putInt(ModNbtKeys.TICKS_SINCE_LEFT_WATER, this.ticksSinceLeftWater);
    nbt.putBoolean(ModNbtKeys.SLEEPING_IN_BED, this.isSleepingInBed());
    this.getAssignedBedPos()
        .ifPresent(
            pos -> {
              nbt.putInt(ModNbtKeys.BED_POS_X, pos.getX());
              nbt.putInt(ModNbtKeys.BED_POS_Y, pos.getY());
              nbt.putInt(ModNbtKeys.BED_POS_Z, pos.getZ());
            });
    nbt.putBoolean(ModNbtKeys.CARRYING_BALL, this.isCarryingFetchItem());
    String activeFetchTypeId = this.dataTracker.get(ACTIVE_FETCH_TYPE_ID);
    if (!activeFetchTypeId.isEmpty()) {
      nbt.putString(ModNbtKeys.ACTIVE_FETCH_TYPE_ID, activeFetchTypeId);
    }
    ItemStack carried = this.getCarriedFetchItemStack();
    if (!carried.isEmpty()) {
      ItemStack.CODEC
          .encodeStart(this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE), carried)
          .result()
          .ifPresent(tag -> nbt.put(ModNbtKeys.CARRIED_FETCH_ITEM_STACK, tag));
    }
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    this.readAngerFromNbt(this.getWorld(), nbt);
    if (nbt.contains(ModNbtKeys.COLLAR_COLOR, NbtElement.NUMBER_TYPE)) {
      this.setCollarColor(DyeColor.byId(nbt.getInt(ModNbtKeys.COLLAR_COLOR)));
    }
    if (nbt.contains(ModNbtKeys.SHAKE_PROGRESS, NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(SHAKE_PROGRESS, nbt.getInt(ModNbtKeys.SHAKE_PROGRESS));
    }
    if (nbt.contains(ModNbtKeys.WAS_IN_WATER)) {
      this.wasInWater = nbt.getBoolean(ModNbtKeys.WAS_IN_WATER);
    }
    if (nbt.contains(ModNbtKeys.TICKS_SINCE_LEFT_WATER, NbtElement.NUMBER_TYPE)) {
      this.ticksSinceLeftWater = nbt.getInt(ModNbtKeys.TICKS_SINCE_LEFT_WATER);
    }
    if (nbt.contains(ModNbtKeys.SLEEPING_IN_BED)) {
      this.dataTracker.set(SLEEPING_IN_BED, nbt.getBoolean(ModNbtKeys.SLEEPING_IN_BED));
    }
    if (nbt.contains(ModNbtKeys.BED_POS_X)
        && nbt.contains(ModNbtKeys.BED_POS_Y)
        && nbt.contains(ModNbtKeys.BED_POS_Z)) {
      this.setAssignedBedPos(
          new BlockPos(
              nbt.getInt(ModNbtKeys.BED_POS_X),
              nbt.getInt(ModNbtKeys.BED_POS_Y),
              nbt.getInt(ModNbtKeys.BED_POS_Z)));
    }
    if (nbt.contains(ModNbtKeys.CARRYING_BALL)) {
      this.setCarryingFetchItem(nbt.getBoolean(ModNbtKeys.CARRYING_BALL));
    }
    if (nbt.contains(ModNbtKeys.ACTIVE_FETCH_TYPE_ID, NbtElement.STRING_TYPE)) {
      this.dataTracker.set(ACTIVE_FETCH_TYPE_ID, nbt.getString(ModNbtKeys.ACTIVE_FETCH_TYPE_ID));
    }
    if (nbt.contains(ModNbtKeys.CARRIED_FETCH_ITEM_STACK)) {
      ItemStack.CODEC
          .parse(
              this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE),
              nbt.get(ModNbtKeys.CARRIED_FETCH_ITEM_STACK))
          .result()
          .ifPresent(this::setCarriedFetchItemStack);
    }
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
                return state.setAndContinue(
                    RawAnimation.begin()
                        .thenLoop(state.getAnimatable().getSleepInBedMovementAnimationName()));
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
              if (dog.dataTracker.get(TAIL_WAG_TIMER) > 0 && !dog.isSleepingInBed()) {
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
