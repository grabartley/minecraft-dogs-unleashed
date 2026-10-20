package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogBondTest {

  @ParameterizedTest(name = "a saved time of {0} loads as {0}")
  @ValueSource(longs = {0L, 1L, 24_000L, 9_000_000_000L})
  @DisplayName("a saved time loads as saved")
  void savedTimeLoads(final long time) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putLong(ModNbtKeys.BOND_LAST_NEAR_OWNER, time);
    assertEquals(time, DogBond.longOr(nbt, ModNbtKeys.BOND_LAST_NEAR_OWNER, DogBondRules.NEVER));
  }

  @ParameterizedTest(name = "saved text \"{0}\"")
  @ValueSource(strings = {"", "noon"})
  @DisplayName("a missing or non-number time falls back to never, so no greeting is backdated")
  void missingTimeIsNever(final String text) {
    final NbtCompound nbt = new NbtCompound();
    if (!text.isEmpty()) {
      nbt.putString(ModNbtKeys.BOND_LAST_NEAR_OWNER, text);
    }
    assertEquals(
        DogBondRules.NEVER,
        DogBond.longOr(nbt, ModNbtKeys.BOND_LAST_NEAR_OWNER, DogBondRules.NEVER));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogBondRules.Source.class)
  @DisplayName("each source's bond earned today survives a reload")
  void earnedLoadsPerSource(final DogBondRules.Source source) {
    final NbtCompound earned = new NbtCompound();
    earned.putInt(source.name(), 7);
    assertEquals(Map.of(source, 7), DogBond.earnedFromSave(earned));
  }

  @Test
  @DisplayName("unknown, text and negative entries are dropped or floored when loading")
  void earnedIgnoresBadEntries() {
    final NbtCompound earned = new NbtCompound();
    earned.putInt("SWIM", 5);
    earned.putString(DogBondRules.Source.WALK.name(), "lots");
    earned.putInt(DogBondRules.Source.PLAY.name(), -3);
    assertEquals(Map.of(DogBondRules.Source.PLAY, 0), DogBond.earnedFromSave(earned));
  }
}
