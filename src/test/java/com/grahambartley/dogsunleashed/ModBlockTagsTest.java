package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModBlockTagsTest {

  static Stream<Arguments> entityTypesAndTheirSpawnGroundTags() {
    return Stream.of(
        Arguments.of(
            UnleashedDogBreed.GOLDEN_RETRIEVER.serializedId(), "spawnable_on/goldenretriever"),
        Arguments.of(UnleashedDogBreed.HUSKY.serializedId(), "spawnable_on/husky"),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF.serializedId(), "spawnable_on/direwolf"));
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
