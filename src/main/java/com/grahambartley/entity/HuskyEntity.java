package com.grahambartley.entity;

import static com.grahambartley.ModConstants.MINECRAFT_TICK_RATE;

import com.grahambartley.ModEntities;
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
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HuskyEntity extends TameableEntity implements GeoEntity, Angerable {

  private static final TrackedData<Integer> ANGER_TIME =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> COLLAR_COLOR =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> TAIL_WAG_TIMER =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> SHAKE_PROGRESS =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);

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

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public HuskyEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
    this.setTamed(false, false);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ANGER_TIME, 0);
    builder.add(COLLAR_COLOR, DyeColor.RED.getId());
    builder.add(TAIL_WAG_TIMER, 0);
    builder.add(SHAKE_PROGRESS, 0);
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
    this.goalSelector.add(3, new TemptGoal(this, 1.0, TAMING_INGREDIENT, false));
    this.goalSelector.add(4, new EscapeDangerGoal(this, 1.5));
    this.goalSelector.add(5, new PounceAtTargetGoal(this, 0.4F));
    this.goalSelector.add(6, new MeleeAttackGoal(this, 1.0, true));
    this.goalSelector.add(7, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
    this.goalSelector.add(8, new AnimalMateGoal(this, 1.0));
    this.goalSelector.add(9, new WanderAroundFarGoal(this, 1.0));
    this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    this.goalSelector.add(11, new LookAroundGoal(this));

    this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    this.targetSelector.add(3, new RevengeGoal(this));
    this.targetSelector.add(
        4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
    this.targetSelector.add(5, new UniversalAngerGoal<>(this, true));
  }

  public static DefaultAttributeContainer.Builder createHuskyAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
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
        this.setSitting(!this.isSitting());
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

  @Override
  public boolean canBreedWith(AnimalEntity other) {
    if (other == this) {
      return false;
    }
    if (!this.isTamed()) {
      return false;
    }
    if (!(other instanceof HuskyEntity otherHusky)) {
      return false;
    }
    if (!otherHusky.isTamed()) {
      return false;
    }
    if (otherHusky.isInSittingPose()) {
      return false;
    }
    return this.isInLove() && otherHusky.isInLove();
  }

  @Override
  public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
    final HuskyEntity baby = new HuskyEntity(ModEntities.HUSKY, world);
    baby.setBaby(true);
    if (entity instanceof HuskyEntity otherParent) {
      if (this.isTamed()) {
        baby.setOwnerUuid(this.getOwnerUuid());
        baby.setTamed(true, true);
      }
    }
    return baby;
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient) {
      final int currentTimer = this.dataTracker.get(TAIL_WAG_TIMER);

      if (!this.isInSittingPose() && this.getAngerTime() <= 0) {
        final PlayerEntity nearbyPlayer = this.getWorld().getClosestPlayer(this, 10.0D);
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
    }
    return super.damage(source, amount);
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    this.writeAngerToNbt(nbt);
    nbt.putInt("CollarColor", this.getCollarColor().getId());
    nbt.putInt("ShakeProgress", this.getShakeProgress());
    nbt.putBoolean("WasInWater", this.wasInWater);
    nbt.putInt("TicksSinceLeftWater", this.ticksSinceLeftWater);
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
              if (state.getAnimatable().isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
              }
              if (state.getAnimatable().getVelocity().horizontalLengthSquared() > 0.01) {
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
              final HuskyEntity husky = state.getAnimatable();
              if (husky.dataTracker.get(TAIL_WAG_TIMER) > 0) {
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
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
