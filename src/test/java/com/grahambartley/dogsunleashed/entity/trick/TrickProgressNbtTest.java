package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TrickProgressNbtTest {

  static Stream<TrickProgress> progresses() {
    return Stream.of(
        TrickProgress.NONE,
        TrickProgress.of(Map.of(DogTrick.BEG, 2)),
        TrickProgress.of(Map.of(DogTrick.BEG, 5, DogTrick.ROLL_OVER, 1, DogTrick.PLAY_DEAD, 5)));
  }

  @ParameterizedTest
  @MethodSource("progresses")
  @DisplayName("trick progress survives a save and load")
  void roundTrips(final TrickProgress progress) {
    assertEquals(progress, TrickProgressNbt.read(TrickProgressNbt.write(progress)));
  }

  @Test
  @DisplayName("unknown tricks and non-number entries are ignored on load")
  void ignoresJunk() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt("beg", 3);
    nbt.putInt("fetch_slippers", 5);
    nbt.putString("roll_over", "five");
    assertEquals(TrickProgress.of(Map.of(DogTrick.BEG, 3)), TrickProgressNbt.read(nbt));
  }
}
