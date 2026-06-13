package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.entity.fetch.FetchProjectileEntity;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animatable.GeoEntity;

class FrisbeeProjectileEntityTest {

  @Test
  @DisplayName("frisbee projectile should implement GeoEntity and FetchProjectileEntity")
  void frisbeeProjectileShouldImplementRequiredInterfaces() {
    assertTrue(
        Arrays.asList(FrisbeeProjectileEntity.class.getInterfaces()).contains(GeoEntity.class));
    assertTrue(
        Arrays.asList(FrisbeeProjectileEntity.class.getInterfaces())
            .contains(FetchProjectileEntity.class));
  }
}
