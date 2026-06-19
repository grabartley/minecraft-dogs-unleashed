package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Class-level contract for {@link FrisbeeProjectileEntity}: the one fetch projectile that
 * customizes the shared lifecycle via hooks (color-carrying block entity and dropped stack, slow
 * glide). Pure reflection, no Minecraft bootstrap: the concrete {@code FetchTypes.FRISBEE} value
 * and live color round-trip are registry-backed and verified in the gametest suite (see {@link
 * AbstractFetchProjectileEntityTest} for why the registries cannot init under JUnit).
 */
class FrisbeeProjectileEntityTest {

  @Test
  @DisplayName("extends the shared fetch projectile base so it inherits the land/drop lifecycle")
  void extendsAbstractFetchProjectile() {
    assertEquals(
        AbstractFetchProjectileEntity.class, FrisbeeProjectileEntity.class.getSuperclass());
  }

  @Test
  @DisplayName("supplies its own fetch item type")
  void declaresGetFetchItemType() {
    final var method =
        assertDoesNotThrow(
            () -> FrisbeeProjectileEntity.class.getDeclaredMethod("getFetchItemType"));
    assertEquals(FetchItemType.class, method.getReturnType());
  }

  @ParameterizedTest(name = "overrides {0} to carry its color / glide")
  @ValueSource(strings = {"enrichLandedBlockEntity", "buildDropStack", "getGravity"})
  @DisplayName("overrides exactly the hooks it needs for color and glide behavior")
  void overridesCustomizationHooks(final String methodName) {
    assertDoesNotThrow(
        () -> findOverriddenHook(methodName),
        methodName + " must be overridden so frisbee color/glide behavior is preserved");
  }

  @ParameterizedTest(name = "does not redeclare {0}")
  @ValueSource(strings = {"registerControllers", "getAnimatableInstanceCache"})
  @DisplayName("leaves the animation plumbing on the base instead of duplicating it")
  void doesNotRedeclareSharedAnimationPlumbing(final String methodName) {
    assertThrows(
        NoSuchMethodException.class,
        () -> {
          if ("registerControllers".equals(methodName)) {
            FrisbeeProjectileEntity.class.getDeclaredMethod(
                methodName,
                software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar.class);
          } else {
            FrisbeeProjectileEntity.class.getDeclaredMethod(methodName);
          }
        },
        methodName + " must stay owned by AbstractFetchProjectileEntity");
  }

  private static void findOverriddenHook(final String methodName) throws Exception {
    switch (methodName) {
      case "enrichLandedBlockEntity" ->
          assertEquals(
              void.class,
              FrisbeeProjectileEntity.class
                  .getDeclaredMethod("enrichLandedBlockEntity", BlockPos.class)
                  .getReturnType());
      case "buildDropStack" ->
          assertEquals(
              ItemStack.class,
              FrisbeeProjectileEntity.class.getDeclaredMethod("buildDropStack").getReturnType());
      case "getGravity" ->
          assertEquals(
              double.class,
              FrisbeeProjectileEntity.class.getDeclaredMethod("getGravity").getReturnType());
      default -> throw new IllegalArgumentException("unknown hook " + methodName);
    }
  }
}
