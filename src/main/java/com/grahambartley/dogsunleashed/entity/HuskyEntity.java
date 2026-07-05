package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.FULL_MOON_PHASE;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_COOLDOWN_TICKS;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_DURATION_TICKS;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_HEARING_RANGE_SQUARED;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_PITCH;
import static com.grahambartley.dogsunleashed.ModConstants.RANDOM_HOWL_CHANCE;
import static com.grahambartley.dogsunleashed.ModEntities.HUSKY;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.advancement.HuskyHowledCriterion;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoatRolls;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class HuskyEntity extends UnleashedDogEntity {
  private static final TrackedData<Boolean> HOWLING =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> EYE_COLOR_VARIANT =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private int howlCooldownTicks = 0;
  private int howlActiveTicks = 0;

  public HuskyEntity(EntityType<? extends UnleashedDogEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(HOWLING, false);
    builder.add(COAT_VARIANT, HuskyCoat.BLACK_WHITE.ordinal());
    builder.add(EYE_COLOR_VARIANT, HuskyEyeColor.HAZEL_HAZEL.ordinal());
  }

  @Override
  protected void rollAppearance(final SpawnReason spawnReason) {
    final int roll = this.random.nextInt(HuskyCoatRolls.ROLL_BOUND);
    final HuskyCoat coat = HuskyCoatRolls.resolveCoatFromRoll(spawnReason, roll);
    this.dataTracker.set(COAT_VARIANT, coat.ordinal());
    this.dataTracker.set(EYE_COLOR_VARIANT, HuskyEyeColor.fromRandom(random).ordinal());
  }

  @Override
  public UnleashedDogCoat getCoatVariant() {
    return HuskyCoat.fromOrdinal(this.dataTracker.get(COAT_VARIANT));
  }

  public HuskyEyeColor getEyeColorVariant() {
    return HuskyEyeColor.fromOrdinal(this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.COAT_VARIANT, this.dataTracker.get(COAT_VARIANT));
    nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains(ModNbtKeys.COAT_VARIANT, net.minecraft.nbt.NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(COAT_VARIANT, nbt.getInt(ModNbtKeys.COAT_VARIANT));
    }
    if (nbt.contains(ModNbtKeys.EYE_COLOR_VARIANT, net.minecraft.nbt.NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(EYE_COLOR_VARIANT, nbt.getInt(ModNbtKeys.EYE_COLOR_VARIANT));
    }
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new HuskyEntity(HUSKY, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof HuskyEntity;
  }

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.HUSKY;
  }

  @Override
  protected String getSleepInBedMovementAnimationName() {
    return DogAnimationKeys.SLEEP;
  }

  @Override
  protected @Nullable SoundEvent getBarkSound() {
    return null;
  }

  @Override
  protected boolean canBark() {
    return false;
  }

  public boolean isHowling() {
    return this.dataTracker.get(HOWLING);
  }

  private void setHowling(boolean howling) {
    this.dataTracker.set(HOWLING, howling);
  }

  public int getHowlCooldownTicks() {
    return this.howlCooldownTicks;
  }

  private boolean isHowlConditionMet() {
    return !this.isDead()
        && !this.isSleepingInBed()
        && !this.getWorld().isDay()
        && this.getWorld().getMoonPhase() == FULL_MOON_PHASE;
  }

  private boolean canHowl() {
    return isHowlConditionMet() && this.howlCooldownTicks <= 0;
  }

  @Override
  protected void tickBreedSpecificSounds() {
    if (this.howlCooldownTicks > 0) this.howlCooldownTicks--;

    if (this.howlActiveTicks > 0) {
      this.howlActiveTicks--;
      if (this.howlActiveTicks == 0) {
        this.setHowling(false);
      }
    }

    boolean willHowl = this.canHowl() && this.random.nextInt(RANDOM_HOWL_CHANCE) == 0;
    if (willHowl) {
      this.setHowling(true);
      this.playSound(ModSounds.HUSKY_HOWL, DogsUnleashed.SERVER_CONFIG.howlVolume(), HOWL_PITCH);
      this.howlCooldownTicks = HOWL_COOLDOWN_TICKS;
      this.howlActiveTicks = HOWL_DURATION_TICKS;
      this.triggerHowlAdvancement();
    }
  }

  private void triggerHowlAdvancement() {
    if (!(this.getWorld() instanceof ServerWorld)) {
      return;
    }

    final Entity owner = this.getOwner();
    if (!(owner instanceof ServerPlayerEntity player) || !player.isAlive()) {
      return;
    }

    if (this.squaredDistanceTo(player) > HOWL_HEARING_RANGE_SQUARED) {
      return;
    }

    HuskyHowledCriterion.INSTANCE.trigger(player);
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    super.registerControllers(controllers);
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
  }
}
