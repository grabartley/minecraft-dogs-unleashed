package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that each {@link ModBlocks} field is added to the canonical {@link Registries#BLOCK}
 * under the expected {@code dogs-unleashed:*} identifier. Catches regressions where a field is
 * removed from {@code ModBlocks} or where its registration call is dropped without removing the
 * field, which would silently leave the field non-null but missing from the registry on a live
 * server.
 *
 * <p>Equivalent assertions for {@link ModEntities} and {@link ModItems} would require running
 * {@code EntityType.<clinit>} / {@code Items.<clinit>} on the unit-test classpath, which fails
 * verification under Yarn-mapped jars without the Loom production-runtime widening pass. Those
 * fields are exercised in the gametest suite instead.
 */
@ExtendWith(MinecraftBootstrapExtension.class)
class ModContentRegistrationTest {

  private static Identifier modId(final String path) {
    return Identifier.of(DogsUnleashed.MOD_ID, path);
  }

  static Stream<Arguments> registeredBlocks() {
    return Stream.of(
        Arguments.of("dog_bed", ModBlocks.DOG_BED), Arguments.of("dog_grave", ModBlocks.DOG_GRAVE));
  }

  @ParameterizedTest(name = "block dogs-unleashed:{0} is registered")
  @MethodSource("registeredBlocks")
  @DisplayName("every ModBlocks field is registered under its canonical mod-namespaced id")
  void blockIsRegistered(final String path, final Block expected) {
    final Identifier id = modId(path);
    assertTrue(Registries.BLOCK.containsId(id), id + " must be in Registries.BLOCK");
    assertSame(expected, Registries.BLOCK.get(id), "Registry must return the ModBlocks reference");
    assertNotNull(
        Registries.BLOCK.getId(expected), "ModBlocks reference must round-trip to its id");
  }
}
