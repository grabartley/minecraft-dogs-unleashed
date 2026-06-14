package com.grahambartley.entity;

import static com.grahambartley.ModEntities.DACHSHUND;

import com.grahambartley.ModNbtKeys;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.variant.DachshundCoat;
import com.grahambartley.entity.variant.DachshundCoatRolls;
import com.grahambartley.entity.variant.UnleashedDogCoat;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class DachshundEntity extends UnleashedDogEntity {
  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(DachshundEntity.class, TrackedDataHandlerRegistry.INTEGER);

  public DachshundEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(COAT_VARIANT, DachshundCoat.BLACK_TAN.ordinal());
  }

  @Override
  protected void rollAppearance(final SpawnReason spawnReason) {
    final int roll = this.random.nextInt(DachshundCoatRolls.ROLL_BOUND);
    final DachshundCoat coat = DachshundCoatRolls.resolveCoatFromRoll(spawnReason, roll);
    this.dataTracker.set(COAT_VARIANT, coat.ordinal());
  }

  @Override
  public UnleashedDogCoat getCoatVariant() {
    return DachshundCoat.fromOrdinal(this.dataTracker.get(COAT_VARIANT));
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
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new DachshundEntity(DACHSHUND, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof DachshundEntity;
  }

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.DACHSHUND;
  }

  @Override
  protected String getSleepInBedMovementAnimationName() {
    return DogAnimationKeys.SLEEP;
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.DACHSHUND_BARK;
  }
}
