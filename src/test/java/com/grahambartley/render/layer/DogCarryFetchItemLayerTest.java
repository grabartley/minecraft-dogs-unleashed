package com.grahambartley.render.layer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogCarryFetchItemLayerTest {
  private static final Path DOG_CARRY_FETCH_ITEM_LAYER_SOURCE =
      Path.of("src/client/java/com/grahambartley/render/layer/DogCarryFetchItemLayer.java");

  @Test
  @DisplayName(
      "dog carry fetch item layer should render the active fetch item instead of a tennis ball fallback")
  void dogCarryFetchItemLayerShouldRenderActiveFetchItem() throws IOException {
    String source = Files.readString(DOG_CARRY_FETCH_ITEM_LAYER_SOURCE);

    assertTrue(source.contains("var fetchItemType = animatable.getActiveFetchType();"));
    assertTrue(
        source.contains(
            "return fetchItemType != null ? new ItemStack(fetchItemType.item()) : null;"));
  }
}
