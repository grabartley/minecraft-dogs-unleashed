package com.grahambartley.block.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickBlockEntityTest {
  private static final Path STICK_BLOCK_ENTITY_SOURCE =
      Path.of("src/main/java/com/grahambartley/block/entity/StickBlockEntity.java");

  @Test
  @DisplayName("stick block entity should bind to the registered stick block entity type")
  void stickBlockEntityShouldBindToStickType() throws IOException {
    String source = Files.readString(STICK_BLOCK_ENTITY_SOURCE);

    assertTrue(
        source.contains(
            "public class StickBlockEntity extends BlockEntity implements GeoBlockEntity"));
    assertTrue(source.contains("super(ModBlockEntities.STICK, pos, state);"));
    assertTrue(source.contains("BlockEntityUpdateS2CPacket.create(this)"));
  }
}
