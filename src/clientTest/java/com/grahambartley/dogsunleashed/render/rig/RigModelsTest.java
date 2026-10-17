package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RigModelsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a rig's geometry id is its geo path in the mod namespace")
  void idIsTheGeoPathInTheModNamespace(final UnleashedDogBreed rig) {
    assertEquals(Identifier.of("dogs-unleashed", rig.geoPath()), RigModels.idOf(rig));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("no rig has a model before GeckoLib has loaded any")
  void nothingIsLoadedBeforeGeckoLibLoads(final UnleashedDogBreed rig) {
    assertTrue(RigModels.loaded(rig).isEmpty());
  }
}
