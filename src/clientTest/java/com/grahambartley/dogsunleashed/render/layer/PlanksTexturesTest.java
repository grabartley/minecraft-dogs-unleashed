package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.wood.WoodPlanks;
import net.minecraft.block.WoodType;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PlanksTexturesTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "oak, minecraft:textures/block/oak_planks.png",
    "spruce, minecraft:textures/block/spruce_planks.png",
    "birch, minecraft:textures/block/birch_planks.png",
    "jungle, minecraft:textures/block/jungle_planks.png",
    "acacia, minecraft:textures/block/acacia_planks.png",
    "dark_oak, minecraft:textures/block/dark_oak_planks.png",
    "mangrove, minecraft:textures/block/mangrove_planks.png",
    "cherry, minecraft:textures/block/cherry_planks.png",
    "bamboo, minecraft:textures/block/bamboo_planks.png",
    "crimson, minecraft:textures/block/crimson_planks.png",
    "warped, minecraft:textures/block/warped_planks.png"
  })
  @DisplayName("a wood draws vanilla's planks texture, so a resource pack restyles it")
  void textureIsVanillas(final String wood, final String texture) {
    assertEquals(Identifier.of(texture), PlanksTextures.of(WoodPlanks.byName(wood)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"pale_oak", "redwood"})
  @DisplayName("a wood with no vanilla planks draws oak rather than a missing texture")
  void unknownWoodDrawsOak(final String name) {
    assertEquals(
        PlanksTextures.of(WoodType.OAK),
        PlanksTextures.of(new WoodType(name, WoodType.OAK.setType())));
  }
}
