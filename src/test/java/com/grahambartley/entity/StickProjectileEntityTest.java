package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.entity.fetch.FetchProjectileEntity;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animatable.GeoEntity;

class StickProjectileEntityTest {

  @Test
  @DisplayName(
      "stick projectile should implement the fetch projectile interface and geo entity interface")
  void stickProjectileShouldImplementFetchProjectileInterface() {
    assertTrue(
        Arrays.asList(StickProjectileEntity.class.getInterfaces())
            .contains(FetchProjectileEntity.class));
    assertTrue(
        Arrays.asList(StickProjectileEntity.class.getInterfaces()).contains(GeoEntity.class));
  }
}
