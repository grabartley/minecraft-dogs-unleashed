package com.grahambartley.entity;

import static com.grahambartley.ModEntities.SHIBA_INU;

import com.grahambartley.ModSounds;
import com.grahambartley.entity.variant.ShibaInuCoat;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class ShibaInuEntity extends UnleashedDogEntity {

  private static final TrackedData<Integer> COAT_VARIANT =
      DataTracker.registerData(ShibaInuEntity.class, TrackedDataHandlerRegistry.INTEGER);

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5);
  }

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
    final int roll = this.random.nextInt(100);
    final ShibaInuCoat coat;
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < 65) {
        coat = ShibaInuCoat.RED;
      } else if (roll < 85) {
        coat = ShibaInuCoat.BLACK;
        // sesame is breeding exclusive variant
      } else {
        coat = ShibaInuCoat.SESAME;
      }
    } else {
      if (roll < 65) {
        coat = ShibaInuCoat.RED;
      } else {
        coat = ShibaInuCoat.BLACK;
      }
    }
    this.dataTracker.set(COAT_VARIANT, coat.ordinal());
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt("CoatVariant", this.dataTracker.get(COAT_VARIANT));
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains("CoatVariant", NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(COAT_VARIANT, nbt.getInt("CoatVariant"));
    }
  }

  @Override
  public UnleashedDogCoat getCoatVariant() {
    return ShibaInuCoat.fromOrdinal(this.dataTracker.get(COAT_VARIANT));
  }

  protected String getSleepInBedMovementAnimationName() {
    return "sleep";
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
  public String getBreedId() {
    return "shibainu";
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.SHIBA_INU_BARK;
  }
}
