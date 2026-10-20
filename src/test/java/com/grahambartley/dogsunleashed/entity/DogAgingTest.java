package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogAgingTest {

  @ParameterizedTest(name = "a saved last seen time of {0} loads as {0}")
  @ValueSource(longs = {0L, 1L, 24_000L, 9_000_000_000L})
  @DisplayName("a saved last seen time loads as saved")
  void savedTimeLoads(final long time) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putLong(ModNbtKeys.AGE_LAST_SEEN_TIME, time);
    assertEquals(time, DogAging.lastSeenFromSave(nbt));
  }

  @Test
  @DisplayName("a save from before aging has never been seen, so no time is backdated")
  void missingTimeIsNeverSeen() {
    assertEquals(DogAgingRules.NEVER_SEEN, DogAging.lastSeenFromSave(new NbtCompound()));
  }

  @Test
  @DisplayName("a last seen time saved as text is ignored")
  void nonNumberIsIgnored() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.AGE_LAST_SEEN_TIME, "noon");
    assertEquals(DogAgingRules.NEVER_SEEN, DogAging.lastSeenFromSave(nbt));
  }
}
