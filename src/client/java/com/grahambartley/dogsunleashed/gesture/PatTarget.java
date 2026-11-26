package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogHeadReach;
import com.grahambartley.dogsunleashed.entity.PetStance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.DogFrame;
import com.grahambartley.dogsunleashed.render.pat.DogCrowns;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class PatTarget {

  static final float PIXELS_PER_BLOCK = 16.0f / 0.9375f;
  static final float FEET_Y = 1.501f * 16.0f;
  static final float NECK_BACK = 0.35f;
  static final float NECK_DOWN = 0.8f;
  static final float NECK_SIDE = 0.9f;
  static final float KNEEL_AUTHORED_HALF_WIDTH = authoredHalfWidth(UnleashedDogBreed.BEAGLE, 1.0f);
  static final float STAND_AUTHORED_HALF_WIDTH =
      authoredHalfWidth(
          UnleashedDogBreed.DIRE_WOLF, DireWolfGrowthStage.ADULT.renderScaleMultiplier());
  static final float MIN_STROKE_SCALE = 0.5f;
  static final float MAX_STROKE_SCALE = 1.6f;

  public record Aim(Vector3f crown, Vector3f neck, float kneel, float strokeScale) {}

  public record Frame(Vec3d feet, float bodyYawDegrees, float mirror) {

    public Vector3f toModel(final Vec3d world) {
      final Vec3d offset = world.subtract(this.feet);
      final float yaw = this.bodyYawDegrees * MathHelper.RADIANS_PER_DEGREE;
      final float left = (float) (offset.x * MathHelper.cos(yaw) + offset.z * MathHelper.sin(yaw));
      final float forward =
          (float) (-offset.x * MathHelper.sin(yaw) + offset.z * MathHelper.cos(yaw));
      return new Vector3f(
          left * PIXELS_PER_BLOCK * this.mirror,
          FEET_Y - (float) offset.y * PIXELS_PER_BLOCK,
          -forward * PIXELS_PER_BLOCK);
    }

    public Vec3d toWorld(final Vector3f model) {
      final float yaw = this.bodyYawDegrees * MathHelper.RADIANS_PER_DEGREE;
      final double left = model.x * this.mirror / PIXELS_PER_BLOCK;
      final double forward = -model.z / PIXELS_PER_BLOCK;
      final double up = (FEET_Y - model.y) / PIXELS_PER_BLOCK;
      return this.feet.add(
          left * MathHelper.cos(yaw) - forward * MathHelper.sin(yaw),
          up,
          left * MathHelper.sin(yaw) + forward * MathHelper.cos(yaw));
    }
  }

  private PatTarget() {}

  public static Frame frameOf(final LivingEntity player, final Arm arm, final float tickDelta) {
    return new Frame(
        player.getLerpedPos(tickDelta),
        MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw),
        arm == Arm.RIGHT ? 1.0f : -1.0f);
  }

  public static Optional<Aim> of(
      final Frame frame, final UnleashedDogEntity dog, final float tickDelta) {
    return crownOf(dog, tickDelta)
        .map(
            crown -> {
              final DogHeadReach head = DogHeadReach.of(dog);
              final float yaw =
                  MathHelper.lerp(tickDelta, dog.prevBodyYaw, dog.bodyYaw)
                      * MathHelper.RADIANS_PER_DEGREE;
              final Vec3d facing = new Vec3d(-MathHelper.sin(yaw), 0.0, MathHelper.cos(yaw));
              final Vec3d neck = neckOf(crown, facing, frame.feet(), head);
              final float kneel = PetStance.kneelWeight((float) (crown.y - frame.feet().y));
              return new Aim(
                  frame.toModel(crown),
                  frame.toModel(neck),
                  kneel,
                  strokeScale(head.halfWidth(), kneel));
            });
  }

  static Vec3d neckOf(
      final Vec3d crown, final Vec3d facing, final Vec3d player, final DogHeadReach head) {
    final Vec3d across = new Vec3d(facing.z, 0.0, -facing.x);
    final Vec3d towardPlayer =
        player.subtract(crown).dotProduct(across) >= 0.0 ? across : across.negate();
    return crown
        .subtract(facing.multiply(NECK_BACK * (head.snout() - head.neck())))
        .subtract(0.0, NECK_DOWN * head.halfWidth(), 0.0)
        .add(towardPlayer.multiply(NECK_SIDE * head.halfWidth()));
  }

  private static float authoredHalfWidth(final UnleashedDogBreed rig, final float stage) {
    return DogHeadReach.rigUnits(rig)
        .scaled(rig.renderTransforms().adultScale() * stage / 16.0f)
        .halfWidth();
  }

  static float strokeScale(final float halfWidth, final float kneel) {
    final float authored =
        MathHelper.lerp(kneel, STAND_AUTHORED_HALF_WIDTH, KNEEL_AUTHORED_HALF_WIDTH);
    return MathHelper.clamp(halfWidth / authored, MIN_STROKE_SCALE, MAX_STROKE_SCALE);
  }

  public static Optional<Vec3d> crownOf(final UnleashedDogEntity dog, final float tickDelta) {
    final Optional<Vec3d> live = DogCrowns.SHARED.of(dog.getId(), dog.getWorld().getTime());
    return live.isPresent() ? live : restCrownOf(dog, tickDelta);
  }

  private static Optional<Vec3d> restCrownOf(final UnleashedDogEntity dog, final float tickDelta) {
    final UnleashedDogBreed rig = dog.getRigSourceBreed();
    final RigProfile profile = RigProfiles.of(rig);
    return RigModels.loaded(rig)
        .flatMap(
            model ->
                HeadTopLocator.locate(model, profile)
                    .map(
                        top -> {
                          final Vector3f onDog =
                              onDog(
                                  DogFrame.of(dog, model, profile).toEntity(top.crown()),
                                  MathHelper.lerp(tickDelta, dog.prevBodyYaw, dog.bodyYaw));
                          return dog.getLerpedPos(tickDelta).add(onDog.x, onDog.y, onDog.z);
                        }));
  }

  static Vector3f onDog(final Vector3f entityPoint, final float dogBodyYawDegrees) {
    return new Vector3f(entityPoint).rotateY((float) Math.toRadians(180.0f - dogBodyYawDegrees));
  }
}
