package com.grahambartley.block;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickBlockTest {
  private static final Path STICK_BLOCK_SOURCE =
      Path.of("src/main/java/com/grahambartley/block/StickBlock.java");

  @Test
  @DisplayName("stick block should be a falling block with a thin grounded fetch shape")
  void stickBlockShouldExposeThinGroundShape() throws IOException {
    String source = Files.readString(STICK_BLOCK_SOURCE);

    assertTrue(
        source.contains(
            "public class StickBlock extends FallingBlock implements BlockEntityProvider"));
    assertTrue(source.contains("VoxelShapes.cuboid(0.0625, 0.0, 0.375, 0.9375, 0.125, 0.625)"));
    assertTrue(source.contains("return SHAPE;"));
  }

  @Test
  @DisplayName("stick block should create a stick block entity for rendering")
  void stickBlockShouldCreateStickBlockEntity() throws IOException {
    String source = Files.readString(STICK_BLOCK_SOURCE);

    assertTrue(source.contains("return new StickBlockEntity(pos, state);"));
  }
}
