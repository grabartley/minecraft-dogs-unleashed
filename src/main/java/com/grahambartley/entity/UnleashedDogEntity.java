package com.grahambartley.entity;

import static com.grahambartley.ModConstants.BARK_COOLDOWN_TICKS;
import static com.grahambartley.ModConstants.BARK_PITCH;
import static com.grahambartley.ModConstants.BARK_VOLUME;
import static com.grahambartley.ModConstants.LOW_HEALTH_THRESHOLD;
import static com.grahambartley.ModConstants.MINECRAFT_TICK_RATE;
import static com.grahambartley.ModConstants.RANDOM_BARK_CHANCE;

import com.grahambartley.ModBlocks;
import com.grahambartley.block.DogBedBlock;
import com.grahambartley.block.DogGraveBlock;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.block.entity.DogGraveBlockEntity;
import com.grahambartley.entity.goal.AutoSleepGoal;
import com.grahambartley.entity.goal.SleepInBedGoal;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import com.grahambartley.util.DogNames;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
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
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class UnleashedDogEntity extends TameableEntity implements GeoEntity, Angerable {

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

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public UnleashedDogEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
  }

  protected abstract UnleashedDogEntity createBaby(ServerWorld world);

  protected abstract boolean isSameSpecies(MobEntity entity);

  public abstract String getBreedId();

  protected abstract SoundEvent getBarkSound();

  protected String getSleepInBedMovementAnimationName() {
    return "sit";
  }

  protected void tickBreedSpecificSounds() {}

  public int getBarkCooldownTicks() {
    return this.barkCooldownTicks;
  }

  private boolean canBark() {
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
      this.playSound(this.getBarkSound(), BARK_VOLUME, BARK_PITCH);
      this.barkCooldownTicks = BARK_COOLDOWN_TICKS;
    }
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ANGER_TIME, 0);
    builder.add(COLLAR_COLOR, DyeColor.RED.getId());
    builder.add(TAIL_WAG_TIMER, 0);
    builder.add(SHAKE_PROGRESS, 0);
    builder.add(HEAD_TILTING, false);
    builder.add(SLEEPING_IN_BED, false);
    builder.add(COMMANDED_TO_SLEEP, false);
    builder.add(ASSIGNED_BED_POS, Optional.empty());
  }

  public DyeColor getCollarColor() {
    return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
  }

  public void setCollarColor(final DyeColor color) {
    this.dataTracker.set(COLLAR_COLOR, color.getId());
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
    this.dataTracker.set(COMMANDED_TO_SLEEP, true);
    this.navigation.startMovingTo(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, 1.0);
  }

  public boolean isCommandedToSleep() {
    return this.dataTracker.get(COMMANDED_TO_SLEEP);
  }

  public void startSleepingInBed(BlockPos bedPos) {
    this.dataTracker.set(SLEEPING_IN_BED, true);
    this.refreshPositionAndAngles(
        bedPos.getX() + 0.5,
        bedPos.getY() + 0.1,
        bedPos.getZ() + 0.5,
        this.getYaw(),
        this.getPitch());
    this.setVelocity(0, 0, 0);
    this.navigation.stop();
  }

  public void wakeUp() {
    this.dataTracker.set(SLEEPING_IN_BED, false);
    this.dataTracker.set(COMMANDED_TO_SLEEP, false);
  }

  public boolean hasAssignedBed() {
    return this.getAssignedBedPos().isPresent();
  }

  private void startShaking() {
    if (!this.isShaking() && !this.isInSittingPose()) {
      this.dataTracker.set(SHAKE_PROGRESS, SHAKE_DURATION_TICKS);
    }
  }

  public void spawnShakeParticles() {
    if (this.getWorld() instanceof ServerWorld serverWorld) {
      for (int i = 0; i < 20; i++) {
        final double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
        final double offsetY = this.random.nextDouble() * 0.5;
        final double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
        serverWorld.spawnParticles(
            ParticleTypes.SPLASH,
            this.getX() + offsetX,
            this.getY() + offsetY + 0.5,
            this.getZ() + offsetZ,
            1,
            (this.random.nextDouble() - 0.5) * 0.3,
            this.random.nextDouble() * 0.1,
            (this.random.nextDouble() - 0.5) * 0.3,
            0.1);
      }
    }
  }

  @Override
  protected void initGoals() {
    this.goalSelector.add(1, new SwimGoal(this));
    this.goalSelector.add(2, new SitGoal(this));
    this.goalSelector.add(3, new SleepInBedGoal(this));
    this.goalSelector.add(4, new AutoSleepGoal(this));
    this.goalSelector.add(5, new TemptGoal(this, 1.0, TAMING_INGREDIENT, false));
    this.goalSelector.add(6, new EscapeDangerGoal(this, 1.5));
    this.goalSelector.add(7, new PounceAtTargetGoal(this, 0.4F));
    this.goalSelector.add(8, new MeleeAttackGoal(this, 1.0, true));
    this.goalSelector.add(9, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
    this.goalSelector.add(10, new AnimalMateGoal(this, 1.0));
    this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0));
    this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    this.goalSelector.add(13, new LookAroundGoal(this));

    this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    this.targetSelector.add(3, new RevengeGoal(this));
    this.targetSelector.add(
        4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
    this.targetSelector.add(5, new UniversalAngerGoal<>(this, true));
  }

  @Override
  public ActionResult interactMob(PlayerEntity player, Hand hand) {
    final ItemStack itemStack = player.getStackInHand(hand);

    if (this.getWorld().isClient) {
      final boolean shouldInteract = this.isOwner(player) || !this.isTamed();
      return shouldInteract ? ActionResult.CONSUME : ActionResult.PASS;
    }

    if (this.isTamed()) {
      if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
        itemStack.decrementUnlessCreative(1, player);
        this.heal(2.0F);
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
          final String dogName = this.getDogNameForMessage();
          DogBedBlock.setPendingAssignment(player.getUuid(), this.getUuid());
          player.sendMessage(
              Text.translatable("message.dogs-unleashed.pending_bed_assignment", dogName), true);
        } else if (this.isSleepingInBed()) {
          this.wakeUp();
          final String dogName = this.getDogNameForMessage();
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
      if (this.random.nextInt(3) == 0) {
        this.setOwner(player);
        this.navigation.stop();
        this.setTarget(null);
        this.setSitting(true);
        this.getWorld().sendEntityStatus(this, (byte) 7);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
          final PetManager petManager = PetManager.get(serverWorld.getServer());
          final PetData petData =
              new PetData(
                  this.getUuid(),
                  player.getUuid(),
                  this.getBreedId(),
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
                serverPlayer, this.getUuid(), this.getBreedId(), petData.getName());
          }
        }
      } else {
        this.getWorld().sendEntityStatus(this, (byte) 6);
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

  private String getDogNameForMessage() {
    if (this.getWorld() instanceof ServerWorld serverWorld) {
      final PetManager petManager = PetManager.get(serverWorld.getServer());
      final PetData petData = petManager.getPetByEntityId(this.getUuid());
      if (petData != null) {
        return petData.getName();
      }
    }
    return Text.translatable("entity.dogs-unleashed." + this.getBreedId()).getString();
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
    if (this.isTamed() || ((UnleashedDogEntity) entity).isTamed()) {
      baby.setOwnerUuid(this.getOwnerUuid());
      baby.setTamed(true, true);
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
      } else if (this.isTamed() && this.random.nextInt(200) == 0) {
        this.dataTracker.set(TAIL_WAG_TIMER, TAIL_WAG_DURATION_TICKS);
      }
    } else if (currentTimer > 0) {
      this.dataTracker.set(TAIL_WAG_TIMER, currentTimer - 1);
    }
  }

  protected boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared() > 0.001;
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient) {
      final PlayerEntity nearbyPlayer = this.getWorld().getClosestPlayer(this, 10.0D);

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
        this.playSound(this.getBarkSound(), BARK_VOLUME, BARK_PITCH);
        this.barkCooldownTicks = BARK_COOLDOWN_TICKS;
      }
    }
    return super.damage(source, amount);
  }

  @Override
  public void onDeath(DamageSource damageSource) {
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

    spawnGrave(serverWorld, bedPosToAvoid);
  }

  private void spawnGrave(ServerWorld world, BlockPos bedPosToAvoid) {
    final BlockPos deathPos = this.getBlockPos();

    final BlockPos gravePos = findValidGravePosition(world, deathPos, bedPosToAvoid);

    if (gravePos != null) {
      final Direction facing =
          Direction.Type.HORIZONTAL.stream().toList().get(world.getRandom().nextInt(4));
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
    for (int radius = 0; radius <= 3; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
            continue;
          }

          for (int dy = -2; dy <= 2; dy++) {
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
    for (int radius = 0; radius <= 3; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
            continue;
          }

          for (int dy = -2; dy <= 2; dy++) {
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
    nbt.putInt("CollarColor", this.getCollarColor().getId());
    nbt.putInt("ShakeProgress", this.getShakeProgress());
    nbt.putBoolean("WasInWater", this.wasInWater);
    nbt.putInt("TicksSinceLeftWater", this.ticksSinceLeftWater);
    nbt.putBoolean("SleepingInBed", this.isSleepingInBed());
    this.getAssignedBedPos()
        .ifPresent(
            pos -> {
              nbt.putInt("BedPosX", pos.getX());
              nbt.putInt("BedPosY", pos.getY());
              nbt.putInt("BedPosZ", pos.getZ());
            });
    com.grahambartley.DogsUnleashed.log.info(
        "[SAVE] {} (UUID={}) - isTamed={}, ownerUuid={}",
        this.getBreedId(),
        this.getUuid(),
        this.isTamed(),
        this.getOwnerUuid());
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    this.readAngerFromNbt(this.getWorld(), nbt);
    if (nbt.contains("CollarColor", 99)) {
      this.setCollarColor(DyeColor.byId(nbt.getInt("CollarColor")));
    }
    if (nbt.contains("ShakeProgress", 99)) {
      this.dataTracker.set(SHAKE_PROGRESS, nbt.getInt("ShakeProgress"));
    }
    if (nbt.contains("WasInWater")) {
      this.wasInWater = nbt.getBoolean("WasInWater");
    }
    if (nbt.contains("TicksSinceLeftWater", 99)) {
      this.ticksSinceLeftWater = nbt.getInt("TicksSinceLeftWater");
    }
    if (nbt.contains("SleepingInBed")) {
      this.dataTracker.set(SLEEPING_IN_BED, nbt.getBoolean("SleepingInBed"));
    }
    if (nbt.contains("BedPosX") && nbt.contains("BedPosY") && nbt.contains("BedPosZ")) {
      this.setAssignedBedPos(
          new BlockPos(nbt.getInt("BedPosX"), nbt.getInt("BedPosY"), nbt.getInt("BedPosZ")));
    }
    com.grahambartley.DogsUnleashed.log.info(
        "[LOAD] {} (UUID={}) - isTamed={}, ownerUuid={}, nbt.hasOwner={}",
        this.getBreedId(),
        this.getUuid(),
        this.isTamed(),
        this.getOwnerUuid(),
        nbt.containsUuid("Owner"));
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
