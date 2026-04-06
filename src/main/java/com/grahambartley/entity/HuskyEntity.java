package com.grahambartley.entity;

import static com.grahambartley.ModConstants.FULL_MOON_PHASE;
import static com.grahambartley.ModConstants.HOWL_COOLDOWN_TICKS;
import static com.grahambartley.ModConstants.HOWL_DURATION_TICKS;
import static com.grahambartley.ModConstants.HOWL_PITCH;
import static com.grahambartley.ModConstants.HOWL_VOLUME;
import static com.grahambartley.ModConstants.RANDOM_HOWL_CHANCE;
import static com.grahambartley.ModEntities.HUSKY;

import com.grahambartley.ModNbtKeys;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.variant.HuskyCoat;
import com.grahambartley.entity.variant.HuskyEyeColor;
import com.grahambartley.entity.variant.UnleashedDogCoat;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class HuskyEntity extends UnleashedDogEntity {
  private static final int ROLL_BOUND = 100;
  private static final int NATURAL_BLACK_WHITE_THRESHOLD = 40;
  private static final int NATURAL_GREY_WHITE_THRESHOLD = 72;
  private static final int NATURAL_RED_WHITE_THRESHOLD = 87;
  private static final int BREEDING_SABLE_THRESHOLD = 93;
  private static final int BREEDING_AGOUTI_THRESHOLD = 97;

  private static final TrackedData<Boolean> HOWLING =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Integer> EYE_COLOR_VARIANT =
      DataTracker.registerData(HuskyEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private int howlCooldownTicks = 0;
  private int howlActiveTicks = 0;

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
  }

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
    final int roll = this.random.nextInt(ROLL_BOUND);
    final HuskyCoat coat;
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < NATURAL_BLACK_WHITE_THRESHOLD) {
        coat = HuskyCoat.BLACK_WHITE;
      } else if (roll < NATURAL_GREY_WHITE_THRESHOLD) {
        coat = HuskyCoat.GREY_WHITE;
      } else if (roll < NATURAL_RED_WHITE_THRESHOLD) {
        coat = HuskyCoat.RED_WHITE;
      } else if (roll < BREEDING_SABLE_THRESHOLD) {
        coat = HuskyCoat.SABLE;
        // Agouti and white variants unique to breeding
      } else if (roll < BREEDING_AGOUTI_THRESHOLD) {
        coat = HuskyCoat.AGOUTI;
      } else {
        coat = HuskyCoat.WHITE;
      }
    } else {
      if (roll < NATURAL_BLACK_WHITE_THRESHOLD) {
        coat = HuskyCoat.BLACK_WHITE;
      } else if (roll < NATURAL_GREY_WHITE_THRESHOLD) {
        coat = HuskyCoat.GREY_WHITE;
      } else if (roll < NATURAL_RED_WHITE_THRESHOLD) {
        coat = HuskyCoat.RED_WHITE;
      } else {
        coat = HuskyCoat.SABLE;
      }
    }
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
  protected SoundEvent getBarkSound() {
    return ModSounds.HUSKY_BARK;
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
      this.playSound(ModSounds.HUSKY_HOWL, HOWL_VOLUME, HOWL_PITCH);
      this.howlCooldownTicks = HOWL_COOLDOWN_TICKS;
      this.howlActiveTicks = HOWL_DURATION_TICKS;
    }
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
