package com.grahambartley.resources;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrisbeeRecipeTest {

  private static final Path RECIPE_DIR = Path.of("src/main/resources/data/dogs-unleashed/recipe");

  @Test
  @DisplayName("every dye color should have a frisbee recipe file")
  void everyDyeColorShouldHaveRecipeFile() {
    for (DyeColor color : DyeColor.values()) {
      Path recipe = RECIPE_DIR.resolve(color.getName() + "_frisbee.json");
      assertTrue(Files.exists(recipe), "Missing recipe file: " + recipe.getFileName());
    }
  }

  @Test
  @DisplayName(
      "every frisbee recipe should use the matching dye and set the correct frisbee_color component")
  void everyRecipeShouldUseMatchingDyeAndSetColor() throws IOException {
    for (DyeColor color : DyeColor.values()) {
      Path recipe = RECIPE_DIR.resolve(color.getName() + "_frisbee.json");
      JsonObject json = JsonParser.parseString(Files.readString(recipe)).getAsJsonObject();

      String expectedDye = "minecraft:" + color.getName() + "_dye";
      String dKey = json.getAsJsonObject("key").getAsJsonObject("D").get("item").getAsString();
      assertTrue(
          expectedDye.equals(dKey),
          color.getName() + " recipe should use " + expectedDye + " but found " + dKey);

      String componentColor =
          json.getAsJsonObject("result")
              .getAsJsonObject("components")
              .get("dogs-unleashed:frisbee_color")
              .getAsString();
      assertTrue(
          color.getName().equals(componentColor),
          color.getName()
              + " recipe component should be \""
              + color.getName()
              + "\" but found \""
              + componentColor
              + "\"");
    }
  }

  @Test
  @DisplayName("every frisbee recipe should use honeycomb as the H ingredient")
  void everyRecipeShouldUseHoneycomb() throws IOException {
    for (DyeColor color : DyeColor.values()) {
      Path recipe = RECIPE_DIR.resolve(color.getName() + "_frisbee.json");
      JsonObject json = JsonParser.parseString(Files.readString(recipe)).getAsJsonObject();
      String hItem = json.getAsJsonObject("key").getAsJsonObject("H").get("item").getAsString();
      assertTrue(
          "minecraft:honeycomb".equals(hItem),
          color.getName() + " recipe H ingredient should be minecraft:honeycomb");
    }
  }
}
