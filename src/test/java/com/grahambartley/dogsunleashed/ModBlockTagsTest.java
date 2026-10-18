package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModBlockTagsTest {

  static Stream<Arguments> entityTypesAndTheirSpawnGroundTags() {
    return Stream.of(
        Arguments.of("goldenretriever", "spawnable_on/goldenretriever"),
        Arguments.of("husky", "spawnable_on/husky"),
        Arguments.of("direwolf", "spawnable_on/direwolf"));
  }

  @ParameterizedTest(name = "{0} spawns on #dogs-unleashed:{1}")
  @MethodSource("entityTypesAndTheirSpawnGroundTags")
  void spawnableOnNamesABlockTagAfterTheEntityType(
      final String entityPath, final String expectedTagPath) {
    final var tag = ModBlockTags.spawnableOn(Identifier.of(DogsUnleashed.MOD_ID, entityPath));

    assertEquals(RegistryKeys.BLOCK, tag.registry());
    assertEquals(Identifier.of(DogsUnleashed.MOD_ID, expectedTagPath), tag.id());
  }
}
