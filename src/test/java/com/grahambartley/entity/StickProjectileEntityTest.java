package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickProjectileEntityTest {
  private static final Path STICK_PROJECTILE_ENTITY_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/StickProjectileEntity.java");

  @Test
  @DisplayName(
      "stick projectile should register as a fetch projectile and resolve stick fetch type")
  void stickProjectileShouldExposeStickFetchType() throws IOException {
    String source = Files.readString(STICK_PROJECTILE_ENTITY_SOURCE);

    assertTrue(source.contains("implements GeoEntity, FetchProjectileEntity"));
    assertTrue(source.contains("super(ModEntities.STICK_PROJECTILE, thrower, world);"));
    assertTrue(source.contains("return FetchTypes.STICK;"));
  }

  @Test
  @DisplayName("stick projectile should place or drop the stick on landing without dealing damage")
  void stickProjectileShouldPlaceOrDropStick() throws IOException {
    String source = Files.readString(STICK_PROJECTILE_ENTITY_SOURCE);

    assertTrue(source.contains("fetchItemType.landedBlock().getDefaultState()"));
    assertTrue(source.contains("new ItemStack(fetchItemType.item())"));
    assertTrue(source.contains("notifyPlayingDogsOfLandedFetchItem"));
    assertTrue(source.contains("notifyPlayingDogsToEndPlayMode"));
    assertTrue(source.contains("it should not damage entities it touches"));
  }

  @Test
  @DisplayName("stick projectile should source its carry type from the generic fetch item type")
  void stickProjectileShouldUseGenericFetchItemTypeFlow() throws IOException {
    String source = Files.readString(STICK_PROJECTILE_ENTITY_SOURCE);

    assertTrue(source.contains("FetchItemType fetchItemType = this.getFetchItemType();"));
  }
}
