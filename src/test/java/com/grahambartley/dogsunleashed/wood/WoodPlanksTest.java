package com.grahambartley.dogsunleashed.wood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodPlanksTest {

  static Stream<Arguments> woods() {
    return Stream.of(
        Arguments.of(WoodType.OAK, Blocks.OAK_PLANKS),
        Arguments.of(WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
        Arguments.of(WoodType.BIRCH, Blocks.BIRCH_PLANKS),
        Arguments.of(WoodType.ACACIA, Blocks.ACACIA_PLANKS),
        Arguments.of(WoodType.CHERRY, Blocks.CHERRY_PLANKS),
        Arguments.of(WoodType.JUNGLE, Blocks.JUNGLE_PLANKS),
        Arguments.of(WoodType.DARK_OAK, Blocks.DARK_OAK_PLANKS),
        Arguments.of(WoodType.CRIMSON, Blocks.CRIMSON_PLANKS),
        Arguments.of(WoodType.WARPED, Blocks.WARPED_PLANKS),
        Arguments.of(WoodType.MANGROVE, Blocks.MANGROVE_PLANKS),
        Arguments.of(WoodType.BAMBOO, Blocks.BAMBOO_PLANKS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood resolves to vanilla's own planks block")
  void planksAreVanillas(final WoodType wood, final Block planks) {
    assertSame(planks, WoodPlanks.planksOf(wood));
  }

  @Test
  @DisplayName("exactly the vanilla woods are offered, in vanilla's registration order")
  void onlyVanillaWoodsAreOffered() {
    assertEquals(woods().map(arguments -> arguments.get()[0]).toList(), WoodPlanks.all());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood survives being saved and loaded by name")
  void woodSurvivesSaving(final WoodType wood, final Block planks) {
    final NbtCompound nbt = new NbtCompound();
    WoodPlanks.write(nbt, wood);

    assertSame(wood, WoodPlanks.read(nbt));
    assertSame(wood, WoodPlanks.byName(wood.name()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood saved in an item component loads back as the same wood")
  void codecRoundTrips(final WoodType wood, final Block planks) {
    final JsonElement json = WoodPlanks.CODEC.encodeStart(JsonOps.INSTANCE, wood).getOrThrow();

    assertEquals(new JsonPrimitive(wood.name()), json);
    assertSame(wood, WoodPlanks.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }

  @ParameterizedTest(name = "\"{0}\" loads as oak")
  @ValueSource(strings = {"", "pale_oak", "redwood", "not a wood"})
  @DisplayName("a missing or unknown wood loads as oak, so old saves and removed mods still load")
  void unknownWoodIsOak(final String name) {
    assertSame(WoodType.OAK, WoodPlanks.byName(name));
    assertSame(WoodType.OAK, WoodPlanks.read(new NbtCompound()));
    assertSame(
        WoodType.OAK,
        WoodPlanks.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(name)).getOrThrow());
  }
}
