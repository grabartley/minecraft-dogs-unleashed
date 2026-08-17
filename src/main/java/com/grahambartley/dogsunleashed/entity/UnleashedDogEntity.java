package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.BARK_PITCH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.genome.DogGenomeCombiner;
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
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import com.grahambartley.dogsunleashed.util.BreedingOwnerResolver;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
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
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
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
  private static final int TAME_SUCCESS_CHANCE = 3;
  private static final float BREEDING_ITEM_HEAL_AMOUNT = 2.0F;
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

  private final DogVocalization vocalization = new DogVocalization(this);
  private final DogAmbienceEffects ambience = new DogAmbienceEffects(this);
  private final DogSleepController sleep = new DogSleepController(this);
  private final DogPlaySession play = new DogPlaySession(this);
  private UUID parentDogUuid = null;
  private UUID secondParentDogUuid = null;
  private boolean spawnedByDogSpawner = false;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final UnleashedDogBreed breed;
  private @Nullable DogGenome cachedGenome;
  private boolean genomeCacheValid;

  private static final UnleashedDogBreed FALLBACK_RIG_BREED = UnleashedDogBreed.HUSKY;

  private static final float GUARANTEED_EQUIPMENT_DROP_CHANCE = 2.0f;
  private static final DogEquipmentSlot[] TRACKED_EQUIPMENT_SLOTS = {
    DogEquipmentSlot.PENDANT, DogEquipmentSlot.COSMETIC
  };

  public UnleashedDogEntity(
      EntityType<? extends TameableEntity> entityType, World world, UnleashedDogBreed breed) {
    super(entityType, world);
    this.breed = breed;
    this.setEquipmentDropChance(EquipmentSlot.BODY, GUARANTEED_EQUIPMENT_DROP_CHANCE);
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
        this.applyGenome(this.randomFounderMix());
        this.setHealth(this.getMaxHealth());
      }
      this.rollAppearance(spawnReason);
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

  private DogGenome genomeOrPure() {
    final DogGenome genome = this.getGenome();
    return genome != null ? genome : DogGenome.pure(this.breed);
  }

  private DogGenome randomFounderMix() {
    final List<UnleashedDogBreed> founders =
        Arrays.stream(UnleashedDogBreed.values())
            .filter(UnleashedDogBreed::isNaturallySpawning)
            .toList();
    final int firstIndex = this.random.nextInt(founders.size());
    int secondIndex = this.random.nextInt(founders.size() - 1);
    if (secondIndex >= firstIndex) {
      secondIndex++;
    }
    return DogGenomeCombiner.combine(
        DogGenome.pure(founders.get(firstIndex)),
        DogGenome.pure(founders.get(secondIndex)),
        this.random);
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

  protected void rollAppearance(final SpawnReason spawnReason) {
    final UnleashedDogBreed rigBreed = this.getRigSourceBreed();
    final BiFunction<SpawnReason, Integer, UnleashedDogCoat> rollResolver =
        DogCoats.rollResolverFor(rigBreed);
    final DogTraits current = this.getTraits();
    final int coatVariantOrdinal =
        rollResolver != null
            ? rollResolver.apply(spawnReason, this.random.nextInt(DogCoats.ROLL_BOUND)).getOrdinal()
            : current.coatVariantOrdinal();
    final int eyeColorVariantOrdinal =
        rigBreed.hasEyeColorVariants()
            ? HuskyEyeColor.fromRandom(this.random).ordinal()
            : current.eyeColorVariantOrdinal();
    this.applyTraits(new DogTraits(rigBreed, coatVariantOrdinal, eyeColorVariantOrdinal));
  }

  public int getBarkCooldownTicks() {
    return this.vocalization.barkCooldownTicks();
  }

  public float getBarkPitch() {
    return this.vocalization.barkPitch();
  }

  public boolean isHowling() {
    return this.dataTracker.get(HOWLING);
  }

  void setHowling(final boolean howling) {
    this.dataTracker.set(HOWLING, howling);
  }

  public int getHowlCooldownTicks() {
    return this.vocalization.howlCooldownTicks();
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

  /**
   * Single entry point for switching command modes: keeps the sitting pose, the Stay/Guard anchor,
   * and any in-progress sleep consistent with the new command.
   */
  public void applyCommand(final DogCommand command) {
    if (!this.isTamed()) {
      return;
    }
    if (this.isSleepingInBed() || this.isCommandedToSleep()) {
      this.markManuallyWoken();
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
    this.vocalization.barkIfReady(this.getBarkPitch());
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

  void releaseBirthWakeHearts() {
    this.ambience.releaseBirthWakeHearts();
  }

  public void clearAssignedBed() {
    this.sleep.clearAssignedBed();
  }

  public void commandToSleep(final BlockPos bedPos) {
    this.sleep.commandToSleep(bedPos);
  }

  public boolean isCommandedToSleep() {
    return this.dataTracker.get(COMMANDED_TO_SLEEP);
  }

  public void markManuallyWoken() {
    this.sleep.markManuallyWoken();
  }

  public boolean isAutoSleepSuppressed() {
    return this.sleep.isAutoSleepSuppressed();
  }

  public void startSleepingInBed(final BlockPos bedPos) {
    this.sleep.startSleepingInBed(bedPos);
  }

  public void wakeUp() {
    this.sleep.wakeUp();
  }

  public boolean hasPendingBirthWakeHearts() {
    return this.ambience.hasPendingBirthWakeHearts();
  }

  public boolean hasAssignedBed() {
    return this.getAssignedBedPos().isPresent();
  }

  public boolean isInPlayMode() {
    return this.play.isInPlayMode();
  }

  public @Nullable UUID getPlayPartnerPlayerUuid() {
    return this.dataTracker.get(PLAY_PARTNER_UUID).orElse(null);
  }

  void setPlayPartnerPlayerUuid(final @Nullable UUID playerUuid) {
    this.dataTracker.set(PLAY_PARTNER_UUID, Optional.ofNullable(playerUuid));
  }

  public @Nullable BlockPos getActiveFetchBlockPos() {
    return this.play.activeFetchBlockPos();
  }

  public void setActiveFetchBlockPos(final @Nullable BlockPos pos) {
    this.play.setActiveFetchBlockPos(pos);
  }

  String activeFetchTypeId() {
    return this.dataTracker.get(ACTIVE_FETCH_TYPE_ID);
  }

  void setActiveFetchTypeId(final String activeFetchTypeId) {
    this.dataTracker.set(ACTIVE_FETCH_TYPE_ID, activeFetchTypeId);
  }

  public @Nullable FetchItemType getActiveFetchType() {
    return DogPlaySession.fetchTypeFromId(this.activeFetchTypeId());
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

  public ItemStack getEquipment(final DogEquipmentSlot slot) {
    return switch (slot) {
      case ARMOUR -> this.getEquippedStack(EquipmentSlot.BODY);
      case PENDANT -> this.dataTracker.get(PENDANT_ITEM);
      case COSMETIC -> this.dataTracker.get(COSMETIC_ITEM);
    };
  }

  public void setEquipment(final DogEquipmentSlot slot, final ItemStack stack) {
    switch (slot) {
      case ARMOUR -> this.equipStack(EquipmentSlot.BODY, stack);
      case PENDANT -> this.dataTracker.set(PENDANT_ITEM, stack);
      case COSMETIC -> this.dataTracker.set(COSMETIC_ITEM, stack);
    }
  }

  @Override
  public boolean canUseSlot(final EquipmentSlot slot) {
    return slot == EquipmentSlot.BODY || super.canUseSlot(slot);
  }

  @Override
  public Integer getScreenOpeningData(final ServerPlayerEntity player) {
    return this.getId();
  }

  @Override
  public ScreenHandler createMenu(
      final int syncId, final PlayerInventory playerInventory, final PlayerEntity player) {
    return new DogEquipmentScreenHandler(syncId, playerInventory, this);
  }

  @Override
  protected void dropEquipment(
      final ServerWorld world, final DamageSource source, final boolean causedByPlayer) {
    super.dropEquipment(world, source, causedByPlayer);
    for (final DogEquipmentSlot slot : TRACKED_EQUIPMENT_SLOTS) {
      final ItemStack stack = this.getEquipment(slot);
      if (!stack.isEmpty()) {
        this.dropStack(stack);
        this.setEquipment(slot, ItemStack.EMPTY);
      }
    }
  }

  public void startPlayMode(final PlayerEntity player, final FetchItemType fetchItemType) {
    this.play.startPlayMode(player, fetchItemType);
  }

  public void endPlayMode() {
    this.play.endPlayMode();
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

  public boolean isActivelyFetching() {
    return this.play.isActivelyFetching();
  }

  /**
   * Celebrates the arrival of this dog's owner with a tail wag and a small burst of heart
   * particles. Server-authoritative: the tail wag is driven by the synced {@code TAIL_WAG_TIMER}
   * tracked data so it animates on every tracking client, and the hearts go out via {@code
   * spawnParticles} which the server broadcasts to nearby players. A per-dog cooldown keyed on
   * entity age prevents repeat bursts when an owner relogs or paces in and out of the same chunk.
   */
  public void celebrateOwnerArrival() {
    this.ambience.celebrateOwnerArrival();
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
    this.vocalization.forceBark(this.getBarkPitch());
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
    this.goalSelector.add(9, new TemptGoal(this, DEFAULT_GOAL_SPEED, TAMING_INGREDIENT, false));
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
    final ItemStack itemStack = player.getStackInHand(hand);

    if (this.getWorld().isClient) {
      final boolean shouldInteract = this.isOwner(player) || !this.isTamed() || player.isSneaking();
      return shouldInteract ? ActionResult.CONSUME : ActionResult.PASS;
    }

    if (!this.isOwner(player) && player.isSneaking()) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
        ModNetworking.sendOpenDogInspect(serverPlayer, this);
      }
      return ActionResult.SUCCESS;
    }

    FetchItemType fetchItemType = FetchTypes.forItem(itemStack.getItem());
    if (this.isTamed() && this.isOwner(player) && player.isSneaking() && fetchItemType != null) {
      if (this.isInPlayMode()) {
        this.endPlayMode();
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.play_end", this.getTamedName()), true);
      } else {
        if (this.isLeashed() && DogsUnleashed.SERVER_CONFIG.dropLeashOnPlayMode()) {
          this.detachLeash();
        }
        this.play.endOtherNearbyPlayModes(player);
        this.startPlayMode(player, fetchItemType);
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.play_start", this.getTamedName()), true);
      }
      return ActionResult.SUCCESS;
    }

    if (this.isTamed()) {
      if (this.isOwner(player) && !player.isSneaking() && itemStack.isOf(ModItems.DOG_WHISTLE)) {
        DogWhistleItem.bind(itemStack, this.getUuid(), this.getTamedName());
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.whistle.bound", this.getTamedName()), true);
        return ActionResult.SUCCESS;
      }

      if (this.isOwner(player) && !player.isSneaking() && itemStack.isOf(ModItems.DOG_TREAT)) {
        itemStack.decrementUnlessCreative(1, player);
        this.applyTreatBuff();
        return ActionResult.SUCCESS;
      }

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

      if (this.isOwner(player) && !player.isSneaking()) {
        final ActionResult equipResult = this.tryDirectEquip(player, hand, itemStack);
        if (equipResult != null) {
          return equipResult;
        }
      }

      if (this.isOwner(player) && !this.isTamingItem(itemStack)) {
        if (player.isSneaking()) {
          final String dogName = this.getTamedName();
          DogBedBlock.setPendingAssignment(player.getUuid(), this.getUuid());
          player.sendMessage(
              Text.translatable("message.dogs-unleashed.pending_bed_assignment", dogName), true);
          return ActionResult.SUCCESS;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
          ModNetworking.sendOpenCommandWheel(serverPlayer, this);
        }
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

  private @Nullable ActionResult tryDirectEquip(
      final PlayerEntity player, final Hand hand, final ItemStack heldStack) {
    if (heldStack.isOf(Items.SHEARS)) {
      final ItemStack equippedArmour = this.getEquipment(DogEquipmentSlot.ARMOUR);
      if (equippedArmour.isEmpty()) {
        return null;
      }
      this.setEquipment(DogEquipmentSlot.ARMOUR, ItemStack.EMPTY);
      heldStack.damage(1, player, getSlotForHand(hand));
      player.giveItemStack(equippedArmour);
      this.playSoundIfNotSilent(SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF);
      return ActionResult.SUCCESS;
    }

    final DogEquipmentSlot slot = DogEquipmentSlot.directEquipSlotFor(heldStack);
    if (slot == null) {
      return null;
    }
    final ItemStack previous = this.getEquipment(slot);
    this.setEquipment(slot, heldStack.copyWithCount(1));
    heldStack.decrementUnlessCreative(1, player);
    if (!previous.isEmpty()) {
      player.giveItemStack(previous);
    }
    this.playSoundIfNotSilent(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value());
    return ActionResult.SUCCESS;
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return BREEDING_INGREDIENT.test(stack);
  }

  public boolean isTamingItem(ItemStack stack) {
    return TAMING_INGREDIENT.test(stack);
  }

  public static Ingredient breedingIngredient() {
    return BREEDING_INGREDIENT;
  }

  public static Ingredient tamingIngredient() {
    return TAMING_INGREDIENT;
  }

  private void tame(final @Nullable PlayerEntity player) {
    if (player == null) {
      return;
    }
    this.setOwner(player);
    this.applyCommand(DogCommand.SIT);
    this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);

    final PetData petData = PetRegistrar.registerPetFor(this, player.getUuid());
    if (petData != null && player instanceof ServerPlayerEntity serverPlayer) {
      ModNetworking.sendOpenNamingScreen(
          serverPlayer, this.getUuid(), this.getBreed(), petData.getName());
    }
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
    if (other == this) {
      return false;
    }
    if (!this.isTamed()) {
      return false;
    }
    if (!(other instanceof UnleashedDogEntity otherDog)) {
      return false;
    }
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
    if (!(entity instanceof UnleashedDogEntity partner)) {
      return null;
    }
    final UnleashedDogBreed childBreed =
        this.breed == partner.getBreed() && this.breed != UnleashedDogBreed.CROSS_BREED
            ? this.breed
            : UnleashedDogBreed.CROSS_BREED;
    final UnleashedDogEntity baby = ModEntities.getDogEntityType(childBreed).create(world);
    if (baby == null) {
      return null;
    }
    baby.setBaby(true);
    baby.setParentDogUuid(this.getUuid());
    baby.setSecondParentDogUuid(partner.getUuid());
    if (childBreed == UnleashedDogBreed.CROSS_BREED) {
      baby.applyGenome(
          DogGenomeCombiner.combine(this.genomeOrPure(), partner.genomeOrPure(), this.random));
      baby.setHealth(baby.getMaxHealth());
    }
    baby.rollAppearance(SpawnReason.BREEDING);
    final PlayerEntity lovingPlayer = this.getLovingPlayer();
    if (lovingPlayer != null) {
      baby.tame(lovingPlayer);
    } else {
      final UUID inheritedOwnerUuid =
          BreedingOwnerResolver.resolveInheritedOwnerUuid(
              this.getOwnerUuid(), partner.getOwnerUuid());
      if (inheritedOwnerUuid != null) {
        baby.setOwnerUuid(inheritedOwnerUuid);
        baby.setTamed(true, true);
        // The baby is still unpositioned here; AnimalEntity#breed moves and spawns it right after,
        // and the resulting ENTITY_LOAD makes PetLocationSyncListener write the real position.
        PetRegistrar.registerPetFor(baby, inheritedOwnerUuid);
      }
    }
    return baby;
  }

  @Override
  public void setBaby(final boolean baby) {
    super.setBaby(baby);
    if (baby) {
      this.ambience.armBirthWakeHearts();
    }
  }

  public void setParentDogUuid(final UUID parentDogUuid) {
    this.parentDogUuid = parentDogUuid;
  }

  public @Nullable UUID getParentDogUuid() {
    return this.parentDogUuid;
  }

  public void setSecondParentDogUuid(final UUID secondParentDogUuid) {
    this.secondParentDogUuid = secondParentDogUuid;
  }

  public @Nullable UUID getSecondParentDogUuid() {
    return this.secondParentDogUuid;
  }

  /**
   * Resolves the living parent dog this puppy follows, or {@code null} when there is no recorded
   * parent or it is no longer alive and loaded in the server world.
   */
  public @Nullable UnleashedDogEntity getParentDog() {
    if (this.parentDogUuid == null || !(this.getWorld() instanceof ServerWorld serverWorld)) {
      return null;
    }
    return serverWorld.getEntity(this.parentDogUuid) instanceof UnleashedDogEntity parent
            && parent.isAlive()
        ? parent
        : null;
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
    return this.isTamingItem(player.getMainHandStack())
        || this.isTamingItem(player.getOffHandStack())
        || this.isBreedingItem(player.getMainHandStack())
        || this.isBreedingItem(player.getOffHandStack());
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
      DogGraveSpawner.spawnGrave(serverWorld, this, bedPosToAvoid);
    }
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    this.writeAngerToNbt(nbt);
    final DogGenome genome = this.getGenome();
    if (genome != null) {
      nbt.put(ModNbtKeys.GENOME, genome.toNbt());
    }
    final DogTraits traits = this.getTraits();
    if (DogCoats.hasCoatVariants(traits.rigSourceBreed())) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, traits.coatVariantOrdinal());
    }
    if (traits.rigSourceBreed().hasEyeColorVariants()) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, traits.eyeColorVariantOrdinal());
    }
    nbt.putInt(ModNbtKeys.COLLAR_COLOR, this.getCollarColor().getId());
    this.ambience.writeNbt(nbt);
    this.sleep.writeNbt(nbt);
    this.play.writeNbt(nbt);
    nbt.putInt(ModNbtKeys.TREAT_BUFF_TICKS, this.getTreatBuffTicks());
    nbt.putInt(ModNbtKeys.COMMAND_MODE, this.getCommand().id());
    if (this.commandAnchorPos != null) {
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_X, this.commandAnchorPos.getX());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Y, this.commandAnchorPos.getY());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Z, this.commandAnchorPos.getZ());
    }
    nbt.putBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER, this.spawnedByDogSpawner);
    if (this.parentDogUuid != null) {
      nbt.putUuid(ModNbtKeys.PARENT_DOG_ID, this.parentDogUuid);
    }
    if (this.secondParentDogUuid != null) {
      nbt.putUuid(ModNbtKeys.SECOND_PARENT_DOG_ID, this.secondParentDogUuid);
    }
    this.writeEquipmentToNbt(nbt, DogEquipmentSlot.PENDANT, ModNbtKeys.PENDANT_ITEM);
    this.writeEquipmentToNbt(nbt, DogEquipmentSlot.COSMETIC, ModNbtKeys.COSMETIC_ITEM);
  }

  private void writeEquipmentToNbt(
      final NbtCompound nbt, final DogEquipmentSlot slot, final String key) {
    final ItemStack stack = this.getEquipment(slot);
    if (stack.isEmpty()) {
      return;
    }
    ItemStack.CODEC
        .encodeStart(this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE), stack)
        .result()
        .ifPresent(tag -> nbt.put(key, tag));
  }

  private void readEquipmentFromNbt(
      final NbtCompound nbt, final DogEquipmentSlot slot, final String key) {
    if (!nbt.contains(key)) {
      return;
    }
    ItemStack.CODEC
        .parse(this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE), nbt.get(key))
        .result()
        .ifPresent(stack -> this.setEquipment(slot, stack));
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    this.readAngerFromNbt(this.getWorld(), nbt);
    if (nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)) {
      final DogGenome genome = DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME));
      if (genome != null) {
        this.applyGenome(genome);
      }
    }
    final DogTraits currentTraits = this.getTraits();
    this.applyTraits(
        new DogTraits(
            this.getRigSourceBreed(),
            nbt.contains(ModNbtKeys.COAT_VARIANT, NbtElement.NUMBER_TYPE)
                ? nbt.getInt(ModNbtKeys.COAT_VARIANT)
                : currentTraits.coatVariantOrdinal(),
            nbt.contains(ModNbtKeys.EYE_COLOR_VARIANT, NbtElement.NUMBER_TYPE)
                ? nbt.getInt(ModNbtKeys.EYE_COLOR_VARIANT)
                : currentTraits.eyeColorVariantOrdinal()));
    if (nbt.contains(ModNbtKeys.COLLAR_COLOR, NbtElement.NUMBER_TYPE)) {
      this.setCollarColor(DyeColor.byId(nbt.getInt(ModNbtKeys.COLLAR_COLOR)));
    }
    this.ambience.readNbt(nbt);
    this.sleep.readNbt(nbt);
    this.play.readNbt(nbt);
    if (nbt.contains(ModNbtKeys.TREAT_BUFF_TICKS, NbtElement.NUMBER_TYPE)) {
      final int treatBuffTicks = Math.max(0, nbt.getInt(ModNbtKeys.TREAT_BUFF_TICKS));
      this.dataTracker.set(TREAT_BUFF_TICKS, treatBuffTicks);
      if (treatBuffTicks > 0) {
        DogTreatBuff.apply(this);
      }
    }
    if (nbt.contains(ModNbtKeys.COMMAND_MODE, NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(COMMAND, DogCommand.fromId(nbt.getInt(ModNbtKeys.COMMAND_MODE)).id());
    } else {
      // Pre-command saves: sitting dogs stay seated, everything else keeps today's follow default.
      this.dataTracker.set(
          COMMAND, this.isSitting() ? DogCommand.SIT.id() : DogCommand.FOLLOW.id());
    }
    if (nbt.contains(ModNbtKeys.COMMAND_ANCHOR_X, NbtElement.NUMBER_TYPE)
        && nbt.contains(ModNbtKeys.COMMAND_ANCHOR_Y, NbtElement.NUMBER_TYPE)
        && nbt.contains(ModNbtKeys.COMMAND_ANCHOR_Z, NbtElement.NUMBER_TYPE)) {
      this.commandAnchorPos =
          new BlockPos(
              nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_X),
              nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_Y),
              nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_Z));
    }
    if (nbt.contains(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER)) {
      this.spawnedByDogSpawner = nbt.getBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER);
    }
    if (nbt.containsUuid(ModNbtKeys.PARENT_DOG_ID)) {
      this.parentDogUuid = nbt.getUuid(ModNbtKeys.PARENT_DOG_ID);
    }
    if (nbt.containsUuid(ModNbtKeys.SECOND_PARENT_DOG_ID)) {
      this.secondParentDogUuid = nbt.getUuid(ModNbtKeys.SECOND_PARENT_DOG_ID);
    }
    this.readEquipmentFromNbt(nbt, DogEquipmentSlot.PENDANT, ModNbtKeys.PENDANT_ITEM);
    this.readEquipmentFromNbt(nbt, DogEquipmentSlot.COSMETIC, ModNbtKeys.COSMETIC_ITEM);
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
