package com.grahambartley.dogsunleashed.render.fur;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;

public final class FurLook {

  private FurLook() {}

  public static boolean shows(
      final RigProfile profile, final String bone, final DogFurStage stage) {
    if (profile.bonesIn(Slot.FUR_OVERGROWN).contains(bone)) {
      return stage == DogFurStage.OVERGROWN;
    }
    if (profile.bonesIn(Slot.FUR_SHAGGY).contains(bone)) {
      return stage.ordinal() >= DogFurStage.SHAGGY.ordinal();
    }
    return true;
  }

  public static List<Identifier> overlays(final RigProfile profile, final DogFurStage stage) {
    final Stream<Overlay> layers =
        switch (stage) {
          case BOTCHED -> Stream.of(Overlay.TRIMMED, Overlay.BOTCHED);
          case TRIMMED -> Stream.of(Overlay.TRIMMED);
          default -> Stream.empty();
        };
    return layers.map(profile::overlay).flatMap(Optional::stream).toList();
  }
}
