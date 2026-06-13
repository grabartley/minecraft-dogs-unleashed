package com.grahambartley.block.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.block.entity.BlockEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animatable.GeoBlockEntity;

class FrisbeeBlockEntityTest {

  @Test
  @DisplayName("frisbee block entity should extend block entity and implement geo block entity")
  void frisbeeBlockEntityShouldExtendBlockEntity() {
    assertTrue(FrisbeeBlockEntity.class.getSuperclass() == BlockEntity.class);
    assertTrue(
        Arrays.asList(FrisbeeBlockEntity.class.getInterfaces()).contains(GeoBlockEntity.class));
  }

  @Test
  @DisplayName("frisbee block entity should expose color getter and setter")
  void frisbeeBlockEntityShouldHaveColorMethods() throws NoSuchMethodException {
    FrisbeeBlockEntity.class.getMethod("getColor");
    FrisbeeBlockEntity.class.getMethod("setColor", net.minecraft.util.DyeColor.class);
  }
}
