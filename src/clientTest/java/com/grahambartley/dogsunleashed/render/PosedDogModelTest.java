package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.render.rig.PrivateRigBones;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class PosedDogModelTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"goldenretriever", "husky", "shibainu"})
  @DisplayName("statues and portraits pose their own copy of a rig, not the bones live dogs share")
  void posesItsOwnCopyOfTheRig(final String rig) {
    final BakedGeoModel own = ShippedRigs.model(rig);
    final PosedDogModel model = new PosedDogModel(new PrivateRigBones(location -> own));
    final Identifier location = Identifier.of("dogs-unleashed", "geo/" + rig + ".geo.json");

    assertSame(own, model.getBakedModel(location));
    for (final GeoBone bone : own.topLevelBones()) {
      assertSame(bone, model.getAnimationProcessor().getBone(bone.getName()));
    }
  }
}
