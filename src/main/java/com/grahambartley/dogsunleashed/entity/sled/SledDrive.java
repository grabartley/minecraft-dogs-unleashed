package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class SledDrive {

  public static final float TURN_DEGREES_PER_TICK = 3.5f;
  static final float YAW_FOLLOW = 0.25f;
  static final double MIN_FOLLOW_DISTANCE = 0.5;
  static final double GRAVITY = 0.04;

  public record Target(Vec3d position, double speedMultiplier, Vec3d facing) {}

  private final SledEntity sled;
  private float heading;
  private double speed;
  private boolean sprintHeld;
  private boolean sprinting;

  SledDrive(final SledEntity sled) {
    this.sled = sled;
  }

  public float heading() {
    return this.heading;
  }

  public double speed() {
    return this.speed;
  }

  public boolean isSprinting() {
    return this.sprinting;
  }

  public void setSprintHeld(final boolean held) {
    this.sprintHeld = held;
  }

  void face(final float yaw) {
    this.heading = yaw;
  }

  public @Nullable Target targetFor(final UnleashedDogEntity dog) {
    final SledTeam team = this.sled.getTeam();
    final int index = team.slotOf(dog);
    if (index < 0) {
      return null;
    }
    final SledFormation.Slot slot =
        SledFormation.slot(index, team.present().size(), team.widestDog());
    final Vec3d position =
        this.sled
            .getPos()
            .add(SledFormation.offset(slot, this.heading, SledFormation.lead(this.speed)));
    final double attribute = dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    final double catchUp =
        Math.max(0.0, dog.getPos().distanceTo(position) - SledFormation.lead(this.speed));
    return new Target(
        position,
        SledSpeed.dogSpeedMultiplier(this.speed + catchUp * SledFormation.CATCH_UP, attribute),
        SledFormation.forward(this.heading));
  }

  void tick(
      final List<UnleashedDogEntity> dogs,
      @Nullable final PlayerEntity rider,
      final boolean canSprint) {
    final float forward = rider != null ? rider.forwardSpeed : 0.0f;
    final float sideways = rider != null ? rider.sidewaysSpeed : 0.0f;
    if (!dogs.isEmpty()) {
      this.heading = MathHelper.wrapDegrees(this.heading - sideways * TURN_DEGREES_PER_TICK);
    }
    this.sprinting = this.sprintHeld && forward > 0.0f && canSprint && !dogs.isEmpty();
    final double target =
        forward > 0.0f && !dogs.isEmpty()
            ? SledSpeed.cruise(
                dogs.size(),
                SledSurface.averageFactor(dogs.stream().map(SledDrive::surfaceUnder).toList()),
                this.sprinting)
            : 0.0;
    this.speed = SledSpeed.approach(this.speed, target, forward < 0.0f);
    this.move(dogs);
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putFloat(ModNbtKeys.SLED_HEADING, this.heading);
  }

  void readNbt(final NbtCompound nbt) {
    this.heading =
        nbt.contains(ModNbtKeys.SLED_HEADING)
            ? nbt.getFloat(ModNbtKeys.SLED_HEADING)
            : this.sled.getYaw();
  }

  private void move(final List<UnleashedDogEntity> dogs) {
    Vec3d horizontal = Vec3d.ZERO;
    if (!dogs.isEmpty()) {
      final Vec3d centre =
          dogs.stream()
              .map(UnleashedDogEntity::getPos)
              .reduce(Vec3d.ZERO, Vec3d::add)
              .multiply(1.0 / dogs.size());
      final Vec3d toTeam = new Vec3d(centre.x - this.sled.getX(), 0.0, centre.z - this.sled.getZ());
      final double distance = toTeam.length();
      if (distance > MIN_FOLLOW_DISTANCE) {
        final double rest =
            SledFormation.restDistance(dogs.size(), this.sled.getTeam().widestDog());
        horizontal = toTeam.multiply(SledFormation.pull(distance, rest, this.speed) / distance);
        final float towardTeam =
            (float) (MathHelper.atan2(-toTeam.x, toTeam.z) * MathHelper.DEGREES_PER_RADIAN);
        this.sled.setYaw(
            MathHelper.wrapDegrees(
                this.sled.getYaw()
                    + MathHelper.wrapDegrees(towardTeam - this.sled.getYaw()) * YAW_FOLLOW));
      }
    }
    this.sled.setVelocity(horizontal.x, this.sled.getVelocity().y - GRAVITY, horizontal.z);
    this.sled.move(MovementType.SELF, this.sled.getVelocity());
    if (this.sled.isOnGround()) {
      this.sled.setVelocity(this.sled.getVelocity().multiply(1.0, 0.0, 1.0));
    }
  }

  static SledSurface surfaceUnder(final UnleashedDogEntity dog) {
    final BlockState feet = dog.getWorld().getBlockState(dog.getBlockPos());
    final BlockState ground = dog.getSteppingBlockState();
    return SledSurface.classify(
        feet.isIn(BlockTags.SNOW) || ground.isIn(BlockTags.SNOW),
        ground.getBlock().getSlipperiness(),
        ground.isIn(BlockTags.SAND));
  }
}
