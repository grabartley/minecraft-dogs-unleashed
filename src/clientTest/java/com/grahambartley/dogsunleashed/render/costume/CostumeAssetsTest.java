package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumeAssetsTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "dogs-unleashed:spider_costume, dogs-unleashed:textures/entity/costume/spider_costume.png,"
        + " dogs-unleashed:geo/costume/spider_costume.geo.json,"
        + " dogs-unleashed:textures/entity/costume/spider_costume_parts.png,"
        + " dogs-unleashed:animations/costume/spider_costume.animation.json",
    "minecraft:phantom_membrane, minecraft:textures/entity/costume/phantom_membrane.png,"
        + " minecraft:geo/costume/phantom_membrane.geo.json,"
        + " minecraft:textures/entity/costume/phantom_membrane_parts.png,"
        + " minecraft:animations/costume/phantom_membrane.animation.json"
  })
  @DisplayName(
      "a costume's suit sheet, part model, part texture and part animations are named after its item")
  void assetsAreNamedAfterTheItem(
      final String item,
      final String sheet,
      final String partsModel,
      final String partsTexture,
      final String partsAnimation) {
    final CostumeAssets assets = CostumeAssets.of(Identifier.of(item));

    assertEquals(Identifier.of(sheet), assets.sheet());
    assertEquals(Identifier.of(partsModel), assets.partsModel());
    assertEquals(Identifier.of(partsTexture), assets.partsTexture());
    assertEquals(Identifier.of(partsAnimation), assets.partsAnimation());
  }
}
