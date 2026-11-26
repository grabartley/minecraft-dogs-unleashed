package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.DogFrame;
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

  public record Crown(float up, float forward, float side) {}

  private PatTarget() {}

  public static Optional<Crown> of(
      final LivingEntity player,
      final UnleashedDogEntity dog,
      final Arm arm,
      final float tickDelta) {
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
                          final Vec3d offset =
                              dog.getLerpedPos(tickDelta)
                                  .add(onDog.x, onDog.y, onDog.z)
                                  .subtract(player.getLerpedPos(tickDelta));
                          return inBodyFrame(
                              offset,
                              MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw),
                              arm);
                        }));
  }

  static Vector3f onDog(final Vector3f entityPoint, final float dogBodyYawDegrees) {
    return new Vector3f(entityPoint).rotateY((float) Math.toRadians(180.0f - dogBodyYawDegrees));
  }

  static Crown inBodyFrame(final Vec3d offset, final float bodyYawDegrees, final Arm arm) {
    final float yaw = (float) Math.toRadians(bodyYawDegrees);
    final float forward =
        (float) (-offset.x * MathHelper.sin(yaw) + offset.z * MathHelper.cos(yaw));
    final float right = (float) (-offset.x * MathHelper.cos(yaw) - offset.z * MathHelper.sin(yaw));
    return new Crown((float) offset.y, forward, arm == Arm.RIGHT ? right : -right);
  }
}
