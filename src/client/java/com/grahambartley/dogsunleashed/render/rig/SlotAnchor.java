package com.grahambartley.dogsunleashed.render.rig;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public record SlotAnchor(
    String boneName, Vector3f centre, Quaternionf restRotation, Vector3f size) {

  private static final float MIN_EXTENT = 1.0e-3f;

  public SlotAnchor {
    centre = new Vector3f(centre);
    restRotation = new Quaternionf(restRotation);
    size = new Vector3f(size);
  }

  public static Optional<SlotAnchor> locate(final BakedGeoModel model, final Set<String> bones) {
    final Optional<GeoBone> anchor =
        bones.stream()
            .map(model::getBone)
            .flatMap(Optional::stream)
            .min(Comparator.comparingInt(SlotAnchor::depthOf).thenComparing(GeoBone::getName));
    final CubeGeometry.Bounds bounds = SlotBounds.of(model, bones);
    if (anchor.isEmpty() || bounds.isEmpty()) {
      return Optional.empty();
    }
    final RestPose pose = RestPose.of(anchor.get());
    final Vector3f size = bounds.max().sub(bounds.min()).max(new Vector3f(MIN_EXTENT));
    return Optional.of(
        new SlotAnchor(anchor.get().getName(), pose.local(bounds.center()), pose.rotation(), size));
  }

  private static int depthOf(final GeoBone bone) {
    int depth = 0;
    for (GeoBone parent = bone.getParent(); parent != null; parent = parent.getParent()) {
      depth++;
    }
    return depth;
  }

  @Override
  public Vector3f centre() {
    return new Vector3f(this.centre);
  }

  @Override
  public Quaternionf restRotation() {
    return new Quaternionf(this.restRotation);
  }

  @Override
  public Vector3f size() {
    return new Vector3f(this.size);
  }
}
