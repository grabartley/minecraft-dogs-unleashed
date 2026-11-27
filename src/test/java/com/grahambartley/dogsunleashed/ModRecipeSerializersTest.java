package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModRecipeSerializersTest {

  static Stream<Arguments> serializers() {
    return Stream.of(
        Arguments.of("dog_furniture_dyeing", ModRecipeSerializers.DOG_FURNITURE_DYEING));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0}")
  @MethodSource("serializers")
  @DisplayName("each recipe serializer is registered under the id its recipe files name")
  void serializerIsRegistered(final String path, final RecipeSerializer<?> serializer) {
    assertSame(
        serializer, Registries.RECIPE_SERIALIZER.get(Identifier.of(DogsUnleashed.MOD_ID, path)));
  }
}
