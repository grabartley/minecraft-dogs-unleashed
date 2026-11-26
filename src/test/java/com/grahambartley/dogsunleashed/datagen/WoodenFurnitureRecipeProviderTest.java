package com.grahambartley.dogsunleashed.datagen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.block.WoodPlanks;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.WoodType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodenFurnitureRecipeProviderTest {

  static Stream<Arguments> recipes() {
    return WoodenFurnitureRecipeProvider.furniture().stream()
        .flatMap(piece -> WoodPlanks.all().stream().map(wood -> Arguments.of(piece, wood)));
  }

  @Test
  @DisplayName("every wood gets a dog house, dog bed and sled recipe")
  void threeRecipesPerWood() {
    assertEquals(33, recipes().count());
  }

  @ParameterizedTest(name = "{1} {0}")
  @MethodSource("recipes")
  @DisplayName("each wood's recipe uses that wood's planks, shares its item's group and makes it")
  void recipeIsGeneratedForTheWood(final WoodenFurniture piece, final WoodType wood)
      throws IOException {
    final Identifier id = piece.recipeId(wood);
    final JsonObject recipe = read("recipe/" + id.getPath());

    assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
    assertEquals(piece.group(), recipe.get("group").getAsString());
    assertEquals(piece.pattern(), strings(recipe.getAsJsonArray("pattern")));
    assertEquals(
        Registries.BLOCK.getId(WoodPlanks.planksOf(wood)).toString(),
        recipe
            .getAsJsonObject("key")
            .getAsJsonObject(String.valueOf(WoodenFurniture.PLANKS_KEY))
            .get("item")
            .getAsString());
    final JsonObject result = recipe.getAsJsonObject("result");
    assertEquals(Registries.ITEM.getId(piece.item()).toString(), result.get("id").getAsString());
    assertEquals(
        wood.name(),
        result.getAsJsonObject("components").get(DogsUnleashed.MOD_ID + ":wood").getAsString());
  }

  @ParameterizedTest(name = "{1} {0}")
  @MethodSource("recipes")
  @DisplayName("each wood's recipe unlocks through its own advancement")
  void recipeHasAnUnlockAdvancement(final WoodenFurniture piece, final WoodType wood)
      throws IOException {
    final JsonObject advancement = read("advancement/" + piece.advancementId(wood).getPath());

    assertEquals(
        List.of(piece.recipeId(wood).toString()),
        strings(advancement.getAsJsonObject("rewards").getAsJsonArray("recipes")));
  }

  @Test
  @DisplayName("the dyeing recipe ships under its serializer's type")
  void dyeingRecipeShips() throws IOException {
    assertEquals(
        DogsUnleashed.MOD_ID + ":" + WoodenFurnitureRecipeProvider.DYEING_RECIPE,
        read("recipe/" + WoodenFurnitureRecipeProvider.DYEING_RECIPE).get("type").getAsString());
  }

  private static JsonObject read(final String path) throws IOException {
    final String resource = "/data/" + DogsUnleashed.MOD_ID + "/" + path + ".json";
    try (InputStream stream =
        WoodenFurnitureRecipeProviderTest.class.getResourceAsStream(resource)) {
      assertNotNull(stream, resource + " was not generated; run ./gradlew runDatagen");
      return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
          .getAsJsonObject();
    }
  }

  private static List<String> strings(final JsonArray array) {
    return array.asList().stream().map(element -> element.getAsString()).toList();
  }
}
