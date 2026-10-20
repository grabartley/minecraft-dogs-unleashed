package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CostumeAnimationInstanceTest {

  private static final Identifier SPIDER = Identifier.of("dogs-unleashed", "spider_costume");
  private static final Identifier BEE = Identifier.of("dogs-unleashed", "bee_costume");

  @ParameterizedTest(name = "entity {0}")
  @ValueSource(ints = {0, 1, 42, Integer.MAX_VALUE})
  @DisplayName("a costume's animations never share the dog's own animation instance")
  void neverCollidesWithTheDogsOwnInstance(final int entityId) {
    final long id = CostumeAnimationInstance.idOf(entityId, SPIDER);

    assertTrue(id < 0);
    assertNotEquals((long) entityId, id);
  }

  @Test
  @DisplayName("each dog and costume pair keeps its own stable instance")
  void stablePerDogAndCostume() {
    assertEquals(
        CostumeAnimationInstance.idOf(7, SPIDER), CostumeAnimationInstance.idOf(7, SPIDER));
    assertNotEquals(
        CostumeAnimationInstance.idOf(7, SPIDER), CostumeAnimationInstance.idOf(8, SPIDER));
    assertNotEquals(
        CostumeAnimationInstance.idOf(7, SPIDER), CostumeAnimationInstance.idOf(7, BEE));
  }
}
