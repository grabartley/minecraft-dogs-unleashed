package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CosmeticAssetsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCosmetic.class)
  @DisplayName("every cosmetic ships the geometry and texture its ids point at")
  void assetsExist(final DogCosmetic cosmetic) {
    for (final Identifier id :
        new Identifier[] {CosmeticAssets.model(cosmetic), CosmeticAssets.texture(cosmetic)}) {
      assertNotNull(
          CosmeticAssetsTest.class.getResource("/assets/" + id.getNamespace() + "/" + id.getPath()),
          id + " is missing");
    }
  }
}
