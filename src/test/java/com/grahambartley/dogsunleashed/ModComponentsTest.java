package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModComponentsTest {

  static Stream<Arguments> components() {
    return Stream.of(
        Arguments.of("dog_grave_uuid", ModComponents.DOG_GRAVE_UUID),
        Arguments.of("dog_grave_name", ModComponents.DOG_GRAVE_NAME),
        Arguments.of("dog_statue", ModComponents.DOG_STATUE),
        Arguments.of("dog_portrait", ModComponents.DOG_PORTRAIT),
        Arguments.of("portrait_size", ModComponents.PORTRAIT_SIZE));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0}")
  @MethodSource("components")
  @DisplayName("each item component is registered under its id")
  void componentIsRegistered(final String path, final ComponentType<?> component) {
    assertSame(
        component, Registries.DATA_COMPONENT_TYPE.get(Identifier.of(DogsUnleashed.MOD_ID, path)));
  }
}
