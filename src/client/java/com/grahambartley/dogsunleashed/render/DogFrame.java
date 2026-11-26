package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.RenderTransforms;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.cross.CrossBreedRendering;
import com.grahambartley.dogsunleashed.render.growth.GrowthRigScale;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record DogFrame(float scale, float yawOffsetDegrees, @Nullable GrowthRigScale growth) {

  public static DogFrame of(
      final UnleashedDogEntity dog, final BakedGeoModel model, final RigProfile profile) {
    final GrowthRigScale growth =
        dog.getGrowth().applies()
            ? GrowthRigScale.of(model, profile, dog.getGrowth().stage())
            : CrossBreedRendering.shapeOf(dog, model, profile);
    final RenderTransforms transforms = dog.getRigSourceBreed().renderTransforms();
    final float scale =
        DogFrameScale.of(
            transforms,
            dog.isBaby(),
            dog.getGrowth().applies() ? dog.getGrowth().stage().renderScaleMultiplier() : null,
            growth == null ? 1.0f : growth.heightScale());
    return new DogFrame(scale, transforms.bodyYawOffsetDegrees(), growth);
  }

  public float lift() {
    return this.growth == null ? 0.0f : this.growth.lift();
  }

  public Vector3f toEntity(final Vector3f modelPoint) {
    return new Vector3f(modelPoint)
        .rotateY((float) Math.toRadians(this.yawOffsetDegrees))
        .add(0.0f, this.lift(), 0.0f)
        .mul(this.scale);
  }
}
