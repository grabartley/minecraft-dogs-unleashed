package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OverlayEffectsTest {

  private static final Identifier ROT = Identifier.of("dogs-unleashed", "rot");
  private static final Identifier MUD = Identifier.of("dogs-unleashed", "mud");

  @Test
  @DisplayName("with nothing registered no effect is active")
  void nothingRegistered() {
    assertEquals(List.of(), new OverlayEffects<String>().activeOn("dog"));
  }

  @Test
  @DisplayName("only effects whose condition holds are active, in the order they were registered")
  void activeEffectsFollowTheirConditions() {
    final OverlayEffects<String> effects = new OverlayEffects<>();
    effects.register(MUD, dog -> dog.contains("wet"));
    effects.register(ROT, dog -> dog.contains("undead"));

    assertEquals(List.of(ROT), effects.activeOn("undead"));
    assertEquals(List.of(MUD, ROT), effects.activeOn("wet undead"));
    assertEquals(List.of(), effects.activeOn("clean"));
  }

  @Test
  @DisplayName("registering a sheet again replaces its condition")
  void reRegisteringReplaces() {
    final OverlayEffects<String> effects = new OverlayEffects<>();
    effects.register(ROT, dog -> true);
    effects.register(ROT, dog -> false);

    assertEquals(List.of(), effects.activeOn("dog"));
  }
}
