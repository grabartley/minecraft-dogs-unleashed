package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModEntities.SHIBA_INU;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.variant.ShibaInuCoat;
import com.grahambartley.dogsunleashed.entity.variant.ShibaInuCoatRolls;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class ShibaInuEntity extends UnleashedDogEntity {
  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(ShibaInuEntity.class, TrackedDataHandlerRegistry.INTEGER);

  public ShibaInuEntity(EntityType<? extends UnleashedDogEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(COAT_VARIANT, ShibaInuCoat.RED.ordinal());
  }

  @Override
  protected void rollAppearance(final SpawnReason spawnReason) {
    final int roll = this.random.nextInt(ShibaInuCoatRolls.ROLL_BOUND);
    final ShibaInuCoat coat = ShibaInuCoatRolls.resolveCoatFromRoll(spawnReason, roll);
    this.dataTracker.set(COAT_VARIANT, coat.ordinal());
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.COAT_VARIANT, this.dataTracker.get(COAT_VARIANT));
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains(ModNbtKeys.COAT_VARIANT, NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(COAT_VARIANT, nbt.getInt(ModNbtKeys.COAT_VARIANT));
    }
  }

  @Override
  public UnleashedDogCoat getCoatVariant() {
    return ShibaInuCoat.fromOrdinal(this.dataTracker.get(COAT_VARIANT));
  }

  protected String getSleepInBedMovementAnimationName() {
    return DogAnimationKeys.SLEEP;
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new ShibaInuEntity(SHIBA_INU, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof ShibaInuEntity;
  }

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.SHIBA_INU;
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.SHIBA_INU_BARK;
  }
}
