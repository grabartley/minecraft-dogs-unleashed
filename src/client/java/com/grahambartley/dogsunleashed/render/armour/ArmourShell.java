package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.rig.PoseClearance;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class ArmourShell {

  private ArmourShell() {}

  public static List<PoseClearance.Box> boxes(
      final BakedGeoModel model, final UnleashedDogEntity animatable, final Set<String> headSide) {
    if (animatable.getArmour().worn().isEmpty()) {
      return List.of();
    }
    final Set<String> armoured =
        new HashSet<>(ArmourFit.torsoBones(animatable.getRigSourceBreed()));
    armoured.removeAll(headSide);
    final Vector3f inflation = ArmourClearance.inflationFor(model, animatable.getRigSourceBreed());
    return PoseClearance.liveBoxesOf(model, armoured).stream()
        .map(box -> box.grown(inflation))
        .toList();
  }
}
