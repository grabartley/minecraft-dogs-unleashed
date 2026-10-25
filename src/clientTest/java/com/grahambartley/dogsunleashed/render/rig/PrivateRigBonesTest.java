package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class PrivateRigBonesTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"goldenretriever", "husky", "direwolf"})
  @DisplayName("each rig gets one private copy of its bones until the copies are cleared")
  void bakesOneCopyPerRigUntilCleared(final String rig) {
    final List<Identifier> baked = new ArrayList<>();
    final PrivateRigBones rigs =
        new PrivateRigBones(
            location -> {
              baked.add(location);
              return ShippedRigs.model(rig);
            });
    final Identifier location = Identifier.of("dogs-unleashed", "geo/" + rig + ".geo.json");

    final BakedGeoModel first = rigs.of(location);
    assertSame(first, rigs.of(location));
    rigs.clear();
    final BakedGeoModel rebuilt = rigs.of(location);

    assertNotSame(first, rebuilt);
    assertEquals(List.of(location, location), baked);
  }
}
