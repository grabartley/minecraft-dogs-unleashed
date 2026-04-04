package com.grahambartley.entity;

import static com.grahambartley.ModConstants.FULL_MOON_PHASE;
import static com.grahambartley.ModConstants.HOWL_COOLDOWN_TICKS;
import static com.grahambartley.ModConstants.HOWL_DURATION_TICKS;
import static com.grahambartley.ModConstants.HOWL_PITCH;
import static com.grahambartley.ModConstants.HOWL_VOLUME;
import static com.grahambartley.ModConstants.RANDOM_HOWL_CHANCE;
import static com.grahambartley.ModEntities.HUSKY;

import com.grahambartley.ModSounds;
import com.grahambartley.entity.variant.HuskyCoat;
import com.grahambartley.entity.variant.HuskyEyeColor;
import net.minecraft.entity.EntityData;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
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
  public @Nullable EntityData initialize(
      ServerWorldAccess world,
      LocalDifficulty difficulty,
      SpawnReason spawnReason,
      @Nullable EntityData entityData) {
    if (spawnReason != SpawnReason.BREEDING) {
      this.rollAppearance(this.random);
    }
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  private void rollAppearance(final Random random) {
    final int roll = random.nextInt(100);
    final HuskyCoat coat;
    if (roll < 40) {
      coat = HuskyCoat.BLACK_WHITE;
    } else if (roll < 72) {
      coat = HuskyCoat.GREY_WHITE;
    } else if (roll < 87) {
      coat = HuskyCoat.RED_WHITE;
    } else if (roll < 93) {
      coat = HuskyCoat.SABLE;
    } else if (roll < 97) {
      coat = HuskyCoat.AGOUTI;
    } else {
      coat = HuskyCoat.WHITE;
    }
    this.dataTracker.set(COAT_VARIANT, coat.ordinal());
    this.dataTracker.set(EYE_COLOR_VARIANT, HuskyEyeColor.fromRandom(random).ordinal());
  }

  public HuskyCoat getCoatVariant() {
    return HuskyCoat.fromOrdinal(this.dataTracker.get(COAT_VARIANT));
  }

  public HuskyEyeColor getEyeColorVariant() {
    return HuskyEyeColor.fromOrdinal(this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt("CoatVariant", this.dataTracker.get(COAT_VARIANT));
    nbt.putInt("EyeColorVariant", this.dataTracker.get(EYE_COLOR_VARIANT));
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains("CoatVariant", 99)) {
      this.dataTracker.set(COAT_VARIANT, nbt.getInt("CoatVariant"));
    }
    if (nbt.contains("EyeColorVariant", 99)) {
      this.dataTracker.set(EYE_COLOR_VARIANT, nbt.getInt("EyeColorVariant"));
    }
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    final HuskyEntity baby = new HuskyEntity(HUSKY, world);
    baby.rollAppearance(baby.getRandom());
    return baby;
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof HuskyEntity;
  }

  @Override
  public String getBreedId() {
    return "husky";
  }

  @Override
  protected String getSleepInBedMovementAnimationName() {
    return "sleep";
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
                  return state.setAndContinue(RawAnimation.begin().thenLoop("howl_sit"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("howl"));
              }
              return PlayState.STOP;
            }));
  }
}
