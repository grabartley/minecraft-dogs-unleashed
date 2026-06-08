package com.grahambartley.block.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.block.entity.BlockEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animatable.GeoBlockEntity;

class StickBlockEntityTest {

  @Test
  @DisplayName("stick block entity should extend block entity and implement geo block entity")
  void stickBlockEntityShouldExtendBlockEntity() {
    assertTrue(StickBlockEntity.class.getSuperclass() == BlockEntity.class);
    assertTrue(
        Arrays.asList(StickBlockEntity.class.getInterfaces()).contains(GeoBlockEntity.class));
  }
}
