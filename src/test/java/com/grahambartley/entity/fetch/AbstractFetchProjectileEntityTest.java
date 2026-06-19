package com.grahambartley.entity.fetch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.animatable.GeoEntity;

/**
 * Locks the design contract of {@link AbstractFetchProjectileEntity}: it is the abstract base that
 * owns the shared fetch-projectile lifecycle while exposing protected hooks for per-item state.
 *
 * <p>These are JVM class-level contracts, so they are exercised with pure reflection. The live
 * land-as-block / drop-as-item behaviour and the concrete {@code getFetchItemType()} values need
 * the Minecraft registries, which cannot initialize on the unit-test classpath (the {@code
 * ModEntities} {@code <clinit>} hits a Yarn-mapping {@code VerifyError} under the JUnit
 * classloader), so they live in the gametest suite instead.
 */
class AbstractFetchProjectileEntityTest {

  @Test
  @DisplayName("is abstract so it can only be used through a concrete fetch projectile subclass")
  void isAbstract() {
    assertTrue(
        Modifier.isAbstract(AbstractFetchProjectileEntity.class.getModifiers()),
        "AbstractFetchProjectileEntity must be abstract");
  }

  @Test
  @DisplayName("extends ThrownEntity so vanilla projectile physics drive the throw arc")
  void extendsThrownEntity() {
    assertEquals(ThrownEntity.class, AbstractFetchProjectileEntity.class.getSuperclass());
  }

  @ParameterizedTest(name = "implements {0}")
  @ValueSource(classes = {GeoEntity.class, FetchProjectileEntity.class})
  @DisplayName("implements the GeckoLib and fetch contracts shared by every projectile")
  void implementsSharedContracts(final Class<?> contract) {
    assertTrue(
        contract.isAssignableFrom(AbstractFetchProjectileEntity.class),
        "AbstractFetchProjectileEntity must implement " + contract.getSimpleName());
  }

  @ParameterizedTest(name = "{0} is final so subclasses cannot drift the shared lifecycle")
  @ValueSource(strings = {"onBlockHit", "onEntityHit"})
  @DisplayName("the shared hit handlers are final on the base")
  void sharedHitHandlersAreFinal(final String methodName) {
    final Method method = findDeclaredMethod(methodName);
    assertTrue(
        Modifier.isFinal(method.getModifiers()),
        methodName + " must be final so the land/drop lifecycle stays owned by the base");
  }

  @ParameterizedTest(name = "{0} is an overridable protected hook")
  @ValueSource(strings = {"enrichLandedBlockEntity", "buildDropStack"})
  @DisplayName(
      "per-item customization points are protected and non-final so subclasses can override")
  void customizationHooksAreOverridable(final String methodName) {
    final Method method = findDeclaredMethod(methodName);
    assertTrue(Modifier.isProtected(method.getModifiers()), methodName + " hook must be protected");
    assertFalse(Modifier.isFinal(method.getModifiers()), methodName + " hook must be overridable");
  }

  @Test
  @DisplayName("buildDropStack returns an ItemStack so subclasses can attach components")
  void buildDropStackReturnsItemStack() {
    assertEquals(ItemStack.class, findDeclaredMethod("buildDropStack").getReturnType());
  }

  private static Method findDeclaredMethod(final String methodName) {
    return assertDoesNotThrow(
        () ->
            switch (methodName) {
              case "onBlockHit" ->
                  AbstractFetchProjectileEntity.class.getDeclaredMethod(
                      "onBlockHit", BlockHitResult.class);
              case "onEntityHit" ->
                  AbstractFetchProjectileEntity.class.getDeclaredMethod(
                      "onEntityHit", EntityHitResult.class);
              case "enrichLandedBlockEntity" ->
                  AbstractFetchProjectileEntity.class.getDeclaredMethod(
                      "enrichLandedBlockEntity", BlockPos.class);
              case "buildDropStack" ->
                  AbstractFetchProjectileEntity.class.getDeclaredMethod("buildDropStack");
              default -> throw new IllegalArgumentException("unknown method " + methodName);
            },
        AbstractFetchProjectileEntity.class.getSimpleName() + " must declare " + methodName);
  }
}
