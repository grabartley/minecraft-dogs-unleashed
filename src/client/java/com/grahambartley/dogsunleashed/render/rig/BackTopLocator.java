package com.grahambartley.dogsunleashed.render.rig;

import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Optional;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class BackTopLocator {

  public record BackTop(String boneName, Vector3f point, Quaternionf restRotation, float width) {

    @Override
    public Vector3f point() {
      return new Vector3f(this.point);
    }

    @Override
    public Quaternionf restRotation() {
      return new Quaternionf(this.restRotation);
    }
  }

  private BackTopLocator() {}

  public static Optional<BackTop> locate(final BakedGeoModel model, final RigProfile profile) {
    final List<CubeGeometry.Face> faces = SlotBounds.facesIn(model, profile.bonesIn(Slot.TORSO));
    if (faces.isEmpty()) {
      return Optional.empty();
    }
    final CubeGeometry.Bounds torso = SlotBounds.boundsOf(faces);
    final Vector3f centre = torso.center();
    return HeadTopLocator.surfaceTop(faces, centre.x, centre.z)
        .map(
            hit -> {
              final RestPose pose = RestPose.of(model.getBone(hit.face().boneName()).orElseThrow());
              return new BackTop(
                  hit.face().boneName(),
                  pose.local(new Vector3f(centre.x, hit.y(), centre.z)),
                  pose.rotation(),
                  torso.max().x - torso.min().x);
            });
  }
}
