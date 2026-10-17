package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.Set;
import software.bernie.geckolib.cache.object.GeoCube;

public final class ArmourFit {

  private ArmourFit() {}

  public static Set<String> torsoBones(final UnleashedDogBreed breed) {
    return RigProfiles.of(breed).bonesIn(RigProfile.Slot.TORSO);
  }

  public static boolean covers(
      final UnleashedDogBreed breed, final String bone, final GeoCube cube) {
    return torsoBones(breed).contains(bone) && !CubeGeometry.isDetail(cube);
  }
}
