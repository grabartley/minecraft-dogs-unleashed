package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Class-level contract for {@link StickProjectileEntity} after the shared lifecycle moved to {@link
 * AbstractFetchProjectileEntity}. Pure reflection, no Minecraft bootstrap: the concrete {@code
 * FetchTypes.STICK} value is registry-backed and is verified live in the gametest suite (see {@link
 * AbstractFetchProjectileEntityTest} for why the registries cannot init under JUnit).
 */
class StickProjectileEntityTest {

  @Test
  @DisplayName("extends the shared fetch projectile base so it inherits the land/drop lifecycle")
  void extendsAbstractFetchProjectile() {
    assertEquals(AbstractFetchProjectileEntity.class, StickProjectileEntity.class.getSuperclass());
  }

  @Test
  @DisplayName("supplies its own fetch item type and nothing else item-specific")
  void declaresGetFetchItemType() {
    final var method =
        assertDoesNotThrow(() -> StickProjectileEntity.class.getDeclaredMethod("getFetchItemType"));
    assertEquals(FetchItemType.class, method.getReturnType());
  }

  @ParameterizedTest(name = "does not redeclare {0}")
  @ValueSource(strings = {"registerControllers", "getAnimatableInstanceCache", "buildDropStack"})
  @DisplayName("leaves shared behavior on the base instead of duplicating it")
  void doesNotRedeclareSharedBehavior(final String methodName) {
    assertThrows(
        NoSuchMethodException.class,
        () -> findNoArgOrControllerMethod(methodName),
        methodName + " must stay owned by AbstractFetchProjectileEntity");
  }

  private static void findNoArgOrControllerMethod(final String methodName) throws Exception {
    if ("registerControllers".equals(methodName)) {
      StickProjectileEntity.class.getDeclaredMethod(
          methodName,
          software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar.class);
    } else {
      StickProjectileEntity.class.getDeclaredMethod(methodName);
    }
  }
}
