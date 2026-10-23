package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.render.costume.CostumePartFit;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.SlotAnchor;
import java.util.Optional;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record MouthGrip(String boneName, Vector3f point, Vector3f forward) {

  static final float GRIP_HEIGHT = -0.25f;
  private static final float FRONT_FACE = -0.5f;

  public MouthGrip {
    point = new Vector3f(point);
    forward = new Vector3f(forward);
  }

  public static Optional<MouthGrip> of(
      final BakedGeoModel model, final RigProfile profile, final float rigYawDegrees) {
    return SlotAnchor.locate(model, profile.bonesIn(Slot.MOUTH))
        .map(
            mouth ->
                new MouthGrip(
                    mouth.boneName(),
                    CostumePartFit.partToBone(mouth, rigYawDegrees)
                        .transformPosition(new Vector3f(0.0f, GRIP_HEIGHT, FRONT_FACE)),
                    new Vector3f(0.0f, 0.0f, FRONT_FACE * 2.0f)
                        .rotateY((float) Math.toRadians(-rigYawDegrees))));
  }

  @Override
  public Vector3f point() {
    return new Vector3f(this.point);
  }

  @Override
  public Vector3f forward() {
    return new Vector3f(this.forward);
  }
}
