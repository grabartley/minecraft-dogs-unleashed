package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogScentTest {

  @Test
  @DisplayName("a saved scent id reads back as the same id")
  void savedIdReadsBack() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString("ScentRule", "dogs-unleashed:leather");
    assertEquals(Identifier.of("dogs-unleashed", "leather"), DogScent.savedId(nbt, "ScentRule"));
  }

  @Test
  @DisplayName("a missing, mistyped or malformed id reads as no scent")
  void badIdsReadAsNothing() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt("Number", 3);
    nbt.putString("Broken", "Not An Id!");
    assertNull(DogScent.savedId(nbt, "Missing"));
    assertNull(DogScent.savedId(nbt, "Number"));
    assertNull(DogScent.savedId(nbt, "Broken"));
  }
}
