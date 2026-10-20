package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogMudTest {

  @ParameterizedTest(name = "saved muddy for {0} ticks loads as muddy for {1} ticks")
  @CsvSource({"0, 0", "1200, 1200", "-5, 0"})
  @DisplayName("a muddy dog loads with its mud age kept")
  void muddySaveLoads(final int saved, final int loaded) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putBoolean(ModNbtKeys.MUDDY, true);
    nbt.putInt(ModNbtKeys.MUDDY_TICKS, saved);
    assertEquals(new DogMudRules.MudState(true, 0, loaded, 0), DogMud.fromSave(nbt));
  }

  @Test
  @DisplayName("a save from before muddy dogs loads clean")
  void oldSaveLoadsClean() {
    assertEquals(DogMudRules.CLEAN, DogMud.fromSave(new NbtCompound()));
  }

  @Test
  @DisplayName("a clean save ignores a stray mud age")
  void cleanSaveIgnoresAge() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putBoolean(ModNbtKeys.MUDDY, false);
    nbt.putInt(ModNbtKeys.MUDDY_TICKS, 500);
    assertEquals(DogMudRules.CLEAN, DogMud.fromSave(nbt));
  }
}
