package com.grahambartley.entity.fetch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchProjectileEntityTest {

  @Test
  @DisplayName("fetch projectile interface should be an interface")
  void fetchProjectileEntityShouldBeInterface() {
    assertTrue(FetchProjectileEntity.class.isInterface());
  }

  @Test
  @DisplayName("fetch projectile interface should have the expected method signatures")
  void fetchProjectileInterfaceShouldHaveRequiredMethods() {
    boolean hasGetFetchItemType =
        Arrays.stream(FetchProjectileEntity.class.getMethods())
            .anyMatch(m -> m.getName().equals("getFetchItemType"));
    assertTrue(hasGetFetchItemType, "should declare getFetchItemType");

    boolean hasNotifyLanded =
        Arrays.stream(FetchProjectileEntity.class.getMethods())
            .anyMatch(
                m ->
                    m.getName().equals("notifyPlayingDogsOfLandedFetchItem")
                        && java.lang.reflect.Modifier.isStatic(m.getModifiers()));
    assertTrue(hasNotifyLanded, "should declare static notifyPlayingDogsOfLandedFetchItem");

    boolean hasNotifyEnd =
        Arrays.stream(FetchProjectileEntity.class.getMethods())
            .anyMatch(
                m ->
                    m.getName().equals("notifyPlayingDogsToEndPlayMode")
                        && java.lang.reflect.Modifier.isStatic(m.getModifiers()));
    assertTrue(hasNotifyEnd, "should declare static notifyPlayingDogsToEndPlayMode");
  }
}
