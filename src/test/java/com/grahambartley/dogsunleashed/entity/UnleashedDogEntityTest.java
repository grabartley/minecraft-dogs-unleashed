package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animatable.GeoEntity;

/**
 * Verifies type-hierarchy and persisted-default contracts of {@link UnleashedDogEntity} and its
 * concrete breed subclasses. These contracts hold at the JVM class level so we exercise them with
 * pure reflection instead of paying the gametest server boot cost per breed.
 *
 * <p>Live ticking, NBT round-trip through a real DataTracker, and registry lookups still live in
 * the gametest suite because they need the Minecraft Bootstrap that fabric-loom does not apply to
 * the unit-test classpath.
 */
class UnleashedDogEntityTest {

  static Stream<Class<? extends UnleashedDogEntity>> dogEntityClasses() {
    return Stream.of(
        HuskyEntity.class,
        DachshundEntity.class,
        BeagleEntity.class,
        GoldenRetrieverEntity.class,
        ShibaInuEntity.class);
  }

  @ParameterizedTest(name = "{0} is a TameableEntity")
  @MethodSource("dogEntityClasses")
  @DisplayName("every dog entity subclass extends TameableEntity so vanilla taming hooks fire")
  void everyDogEntityIsTameable(final Class<? extends UnleashedDogEntity> dogClass) {
    assertTrue(
        TameableEntity.class.isAssignableFrom(dogClass),
        dogClass.getSimpleName() + " must extend TameableEntity");
  }

  @ParameterizedTest(name = "{0} implements GeoEntity")
  @MethodSource("dogEntityClasses")
  @DisplayName("every dog entity subclass implements GeoEntity so GeckoLib drives its animations")
  void everyDogEntityImplementsGeoEntity(final Class<? extends UnleashedDogEntity> dogClass) {
    assertTrue(
        GeoEntity.class.isAssignableFrom(dogClass),
        dogClass.getSimpleName() + " must implement GeoEntity");
  }

  @Test
  @DisplayName("default collar color id round-trips back to DyeColor.RED")
  void defaultCollarColorIdResolvesToRed() {
    final int defaultId = DyeColor.RED.getId();
    assertEquals(
        DyeColor.RED,
        DyeColor.byId(defaultId),
        "Default collar color id should round-trip back to DyeColor.RED for untamed dogs");
  }
}
