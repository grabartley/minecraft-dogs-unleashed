package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEntityNbtTest {

  @ParameterizedTest(name = "{0} ticks saved reads back as {1}")
  @CsvSource({"0, 0", "1, 1", "600, 600", "-1, 0", "-600, 0"})
  @DisplayName("a negative treat buff on disk is clamped away")
  void aNegativeTreatBuffOnDiskIsClampedAway(final int saved, final int expected) {
    assertEquals(expected, DogEntityNbt.sanitizedTreatBuffTicks(saved));
  }

  @ParameterizedTest(name = "sitting={0} maps to {1}")
  @CsvSource({"true, SIT", "false, FOLLOW"})
  @DisplayName("a save with no command id falls back to the sitting pose")
  void aSaveWithNoCommandIdFallsBackToTheSittingPose(
      final boolean sitting, final DogCommand expected) {
    assertEquals(expected, DogEntityNbt.commandForLegacySave(sitting));
  }

  @Test
  @DisplayName("a stored variant ordinal wins over the fallback")
  void aStoredVariantOrdinalWinsOverTheFallback() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(ModNbtKeys.COAT_VARIANT, 3);
    assertEquals(3, DogEntityNbt.variantOrdinalOr(nbt, ModNbtKeys.COAT_VARIANT, 7));
  }

  @Test
  @DisplayName("a missing variant ordinal keeps the fallback")
  void aMissingVariantOrdinalKeepsTheFallback() {
    assertEquals(7, DogEntityNbt.variantOrdinalOr(new NbtCompound(), ModNbtKeys.COAT_VARIANT, 7));
  }

  @Test
  @DisplayName("a variant ordinal stored as the wrong type keeps the fallback")
  void aVariantOrdinalStoredAsTheWrongTypeKeepsTheFallback() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.COAT_VARIANT, "not a number");
    assertEquals(7, DogEntityNbt.variantOrdinalOr(nbt, ModNbtKeys.COAT_VARIANT, 7));
  }

  @Test
  @DisplayName("a fully written anchor round trips")
  void aFullyWrittenAnchorRoundTrips() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_X, 12);
    nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Y, -60);
    nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Z, 340);
    assertEquals(new BlockPos(12, -60, 340), DogEntityNbt.commandAnchorFrom(nbt));
  }

  static Stream<Arguments> partialAnchors() {
    return Stream.of(
        Arguments.of("no axes at all"),
        Arguments.of(ModNbtKeys.COMMAND_ANCHOR_X),
        Arguments.of(ModNbtKeys.COMMAND_ANCHOR_Y),
        Arguments.of(ModNbtKeys.COMMAND_ANCHOR_Z));
  }

  @ParameterizedTest(name = "only {0} present")
  @MethodSource("partialAnchors")
  @DisplayName("a partially written anchor resolves to no anchor")
  void aPartiallyWrittenAnchorResolvesToNoAnchor(final String presentKey) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(presentKey, 5);
    assertNull(DogEntityNbt.commandAnchorFrom(nbt));
  }

  @Test
  @DisplayName("an anchor missing a single axis resolves to no anchor")
  void anAnchorMissingASingleAxisResolvesToNoAnchor() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_X, 1);
    nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Y, 2);
    assertNull(DogEntityNbt.commandAnchorFrom(nbt));
  }
}
