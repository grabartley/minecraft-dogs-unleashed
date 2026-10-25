package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogWheelIconsTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "follow, dogs-unleashed:command_wheel/follow",
    "free_roam, dogs-unleashed:command_wheel/free_roam",
    "delivery, dogs-unleashed:command_wheel/delivery"
  })
  @DisplayName("a wheel icon is a mod sprite in the command wheel folder")
  void spriteLivesInTheWheelFolder(final String name, final String expected) {
    assertEquals(Identifier.of(expected), DogWheelIcons.sprite(name));
  }

  @Test
  @DisplayName("the Actions sector has its own icon")
  void actionsSectorHasItsOwnIcon() {
    assertEquals(DogWheelIcons.sprite("actions"), DogWheelIcons.ACTIONS);
  }
}
