package com.grahambartley.entity.fetch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchProjectileEntityTest {
  private static final Path FETCH_PROJECTILE_ENTITY_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/fetch/FetchProjectileEntity.java");

  @Test
  @DisplayName(
      "fetch projectile entities should expose fetch type access and landed-item callbacks")
  void fetchProjectileEntityShouldExposeFetchCallbacks() throws IOException {
    String source = Files.readString(FETCH_PROJECTILE_ENTITY_SOURCE);

    assertTrue(source.contains("FetchItemType getFetchItemType();"));
    assertTrue(source.contains("notifyPlayingDogsOfLandedFetchItem"));
    assertTrue(
        source.contains("dog.setActiveFetchType(fetchProjectileEntity.getFetchItemType());"));
    assertTrue(source.contains("setActiveFetchBlockPos(fetchItemPos);"));
  }

  @Test
  @DisplayName("fetch projectile entities should expose play-end callbacks for blocked throws")
  void fetchProjectileEntityShouldExposePlayEndCallback() throws IOException {
    String source = Files.readString(FETCH_PROJECTILE_ENTITY_SOURCE);

    assertTrue(source.contains("notifyPlayingDogsToEndPlayMode"));
    assertTrue(source.contains("dog.endPlayMode();"));
  }
}
