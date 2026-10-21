package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PortraitSizeTest {

  @ParameterizedTest(name = "{0} is {1} wide and {2} tall, saved as {3}")
  @CsvSource({"SMALL, 1, 1, 1x1", "WIDE, 2, 1, 2x1", "LARGE, 2, 2, 2x2"})
  @DisplayName("each size has the block dimensions and saved name of its frame art")
  void sizeMatchesItsFrameArt(
      final PortraitSize size, final int width, final int height, final String name) {
    assertEquals(width, size.width());
    assertEquals(height, size.height());
    assertEquals(name, size.serializedName());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"SMALL, WIDE", "WIDE, LARGE", "LARGE, SMALL"})
  @DisplayName("sneak-using a canvas steps through the sizes and wraps back to the smallest")
  void nextCyclesThroughEverySize(final PortraitSize size, final PortraitSize next) {
    assertEquals(next, size.next());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PortraitSize.class)
  @DisplayName("a size survives the save codec and the network codec")
  void sizeRoundTrips(final PortraitSize size) {
    assertEquals(
        size,
        PortraitSize.CODEC
            .parse(
                NbtOps.INSTANCE, PortraitSize.CODEC.encodeStart(NbtOps.INSTANCE, size).getOrThrow())
            .getOrThrow());
    final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
    PortraitSize.PACKET_CODEC.encode(buffer, size);
    assertEquals(size, PortraitSize.PACKET_CODEC.decode(buffer));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"", "3x3", "huge"})
  @DisplayName("an unknown saved size reads as the smallest instead of failing")
  void unknownSizeIsSmall(final String name) {
    assertEquals(PortraitSize.SMALL, PortraitSize.fromSerializedName(name));
    assertEquals(
        PortraitSize.SMALL,
        PortraitSize.CODEC.parse(NbtOps.INSTANCE, NbtString.of(name)).getOrThrow());
  }
}
