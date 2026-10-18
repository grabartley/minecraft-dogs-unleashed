package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DogCollarTest {

  @ParameterizedTest(name = "a saved {0} collar loads as {0}")
  @EnumSource(DyeColor.class)
  @DisplayName("a saved colour id loads as that colour")
  void savedColourLoads(final DyeColor color) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(ModNbtKeys.COLLAR_COLOR, color.getId());
    assertEquals(color, DogCollar.colorFromSave(nbt, DyeColor.RED));
  }

  @Test
  @DisplayName("a save without a collar colour keeps the current colour")
  void missingKeyKeepsTheColour() {
    assertEquals(DyeColor.LIME, DogCollar.colorFromSave(new NbtCompound(), DyeColor.LIME));
  }

  @Test
  @DisplayName("a collar colour saved as text is ignored")
  void nonNumberIsIgnored() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.COLLAR_COLOR, "lime");
    assertEquals(DyeColor.BLUE, DogCollar.colorFromSave(nbt, DyeColor.BLUE));
  }
}
