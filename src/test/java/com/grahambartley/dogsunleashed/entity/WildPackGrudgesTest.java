package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WildPackGrudgesTest {

  private static final UUID PACK = UUID.randomUUID();
  private static final UUID OTHER_PACK = UUID.randomUUID();
  private static final UUID PLAYER = UUID.randomUUID();
  private static final UUID OTHER_PLAYER = UUID.randomUUID();

  @Test
  @DisplayName("a pack holds a grudge only against the player who hurt it")
  void grudgeIsPerPackAndPlayer() {
    final WildPackGrudges grudges = new WildPackGrudges();
    grudges.record(PACK, PLAYER);

    assertTrue(grudges.holds(PACK, PLAYER));
    assertFalse(grudges.holds(PACK, OTHER_PLAYER));
    assertFalse(grudges.holds(OTHER_PACK, PLAYER));
  }

  @Test
  @DisplayName("a new grudge marks the save dirty and a repeated one does not")
  void onlyNewGrudgesDirtyTheSave() {
    final WildPackGrudges grudges = new WildPackGrudges();
    grudges.record(PACK, PLAYER);
    assertTrue(grudges.isDirty());

    grudges.setDirty(false);
    grudges.record(PACK, PLAYER);
    assertFalse(grudges.isDirty());
  }

  @Test
  @DisplayName("grudges survive a save and load")
  void grudgesSurviveSaveAndLoad() {
    final WildPackGrudges grudges = new WildPackGrudges();
    grudges.record(PACK, PLAYER);
    grudges.record(PACK, OTHER_PLAYER);
    grudges.record(OTHER_PACK, OTHER_PLAYER);

    final WildPackGrudges loaded =
        WildPackGrudges.fromNbt(grudges.writeNbt(new NbtCompound(), null), null);

    assertTrue(loaded.holds(PACK, PLAYER));
    assertTrue(loaded.holds(PACK, OTHER_PLAYER));
    assertTrue(loaded.holds(OTHER_PACK, OTHER_PLAYER));
    assertFalse(loaded.holds(OTHER_PACK, PLAYER));
  }
}
