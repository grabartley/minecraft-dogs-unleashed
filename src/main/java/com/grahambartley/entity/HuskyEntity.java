package com.grahambartley.entity;

import static com.grahambartley.ModConstants.HOWL_COOLDOWN_TICKS;
import static com.grahambartley.ModConstants.HOWL_PITCH;
import static com.grahambartley.ModConstants.HOWL_VOLUME;
import static com.grahambartley.ModConstants.NEAR_FULL_MOON_THRESHOLD;
import static com.grahambartley.ModEntities.HUSKY;

import com.grahambartley.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class HuskyEntity extends UnleashedDogEntity {

  private int howlCooldownTicks = 0;

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
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new HuskyEntity(HUSKY, world);
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
  protected SoundEvent getBarkSound() {
    return ModSounds.HUSKY_BARK;
  }

  public int getHowlCooldownTicks() {
    return this.howlCooldownTicks;
  }

  private boolean canHowl() {
    return !this.isDead()
        && this.howlCooldownTicks <= 0
        && !this.getWorld().isDay()
        && this.getWorld().getMoonPhase() <= NEAR_FULL_MOON_THRESHOLD;
  }

  @Override
  protected void tickBreedSpecificSounds() {
    if (this.howlCooldownTicks > 0) {
      this.howlCooldownTicks--;
    }
    if (this.canHowl() && this.random.nextInt(600) == 0) {
      this.playSound(ModSounds.HUSKY_HOWL, HOWL_VOLUME, HOWL_PITCH);
      this.howlCooldownTicks = HOWL_COOLDOWN_TICKS;
    }
  }
}
