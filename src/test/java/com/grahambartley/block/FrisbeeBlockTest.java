package com.grahambartley.block;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.FallingBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrisbeeBlockTest {

  @Test
  @DisplayName("frisbee block should be a falling block implementing block entity provider")
  void frisbeeBlockShouldBeFallingBlockWithBlockEntity() {
    assertTrue(FrisbeeBlock.class.getSuperclass() == FallingBlock.class);
    assertTrue(
        Arrays.asList(FrisbeeBlock.class.getInterfaces()).contains(BlockEntityProvider.class));
  }
}
