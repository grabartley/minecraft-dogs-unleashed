package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogBedTrip {

  public static final Identifier FOLLOW_RANGE_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "bed_trip_follow_range");

  private static final double SPEED = 1.0;

  private final UnleashedDogEntity dog;
  private long nextRepathAt;

  public DogBedTrip(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public void begin(final BlockPos bedPos) {
    final EntityAttributeInstance followRange =
        this.dog.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
    if (followRange != null) {
      final double distance = Math.sqrt(this.dog.getBlockPos().getSquaredDistance(bedPos));
      final double base = followRange.getBaseValue();
      followRange.removeModifier(FOLLOW_RANGE_MODIFIER_ID);
      followRange.addTemporaryModifier(
          new EntityAttributeModifier(
              FOLLOW_RANGE_MODIFIER_ID,
              BedTripRules.extraFollowRange(distance, base),
              EntityAttributeModifier.Operation.ADD_VALUE));
      this.dog.getNavigation().setRangeMultiplier(BedTripRules.nodeBudgetScale(distance, base));
    }
    this.walkTo(bedPos);
  }

  public boolean hasArrived(final BlockPos bedPos) {
    return BedTripRules.hasArrived(this.dog.getBlockPos().getSquaredDistance(bedPos));
  }

  public void keepWalking(final BlockPos bedPos) {
    if (this.dog.getWorld().getTime() >= this.nextRepathAt && this.dog.getNavigation().isIdle()) {
      this.walkTo(bedPos);
    }
  }

  public void end() {
    final EntityAttributeInstance followRange =
        this.dog.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
    if (followRange != null) {
      followRange.removeModifier(FOLLOW_RANGE_MODIFIER_ID);
    }
    this.dog.getNavigation().resetRangeMultiplier();
  }

  private void walkTo(final BlockPos bedPos) {
    this.nextRepathAt = this.dog.getWorld().getTime() + BedTripRules.REPATH_INTERVAL_TICKS;
    final Vec3d target = Vec3d.ofBottomCenter(bedPos);
    this.dog.getNavigation().startMovingTo(target.x, target.y, target.z, SPEED);
  }
}
