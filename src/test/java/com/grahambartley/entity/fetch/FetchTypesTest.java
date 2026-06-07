package com.grahambartley.entity.fetch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchTypesTest {
  private static final Path FETCH_TYPES_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/fetch/FetchTypes.java");

  @Test
  @DisplayName("fetch types should register the tennis ball in the central registry")
  void fetchTypesShouldRegisterTennisBall() throws IOException {
    String source = Files.readString(FETCH_TYPES_SOURCE);

    assertTrue(source.contains("public static final FetchItemType TENNIS_BALL"));
    assertTrue(source.contains("ModItems.TENNIS_BALL"));
    assertTrue(source.contains("ModEntities.TENNIS_BALL_PROJECTILE"));
    assertTrue(source.contains("ModBlocks.TENNIS_BALL"));
    assertTrue(source.contains("ModBlockEntities.TENNIS_BALL"));
  }

  @Test
  @DisplayName("fetch types should expose item, block, entity, and ingredient lookups")
  void fetchTypesShouldExposeLookupMethods() throws IOException {
    String source = Files.readString(FETCH_TYPES_SOURCE);

    assertTrue(source.contains("public static @Nullable FetchItemType forItem(Item item)"));
    assertTrue(source.contains("public static @Nullable FetchItemType forBlock(Block block)"));
    assertTrue(
        source.contains(
            "public static @Nullable FetchItemType forEntityType(EntityType<?> entityType)"));
    assertTrue(source.contains("public static Ingredient asIngredient()"));
  }
}
