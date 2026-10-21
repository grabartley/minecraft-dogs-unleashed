package com.grahambartley.dogsunleashed.entity.personality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogTraitsTest {

  static Stream<Arguments> keptTraits() {
    return Stream.of(
        Arguments.of(List.of(), List.of()),
        Arguments.of(List.of(DogTrait.BRAVE), List.of(DogTrait.BRAVE)),
        Arguments.of(List.of(DogTrait.BRAVE, DogTrait.BRAVE), List.of(DogTrait.BRAVE)),
        Arguments.of(List.of(DogTrait.BRAVE, DogTrait.TIMID), List.of(DogTrait.BRAVE)),
        Arguments.of(List.of(DogTrait.PLAYFUL, DogTrait.LAZY), List.of(DogTrait.PLAYFUL)),
        Arguments.of(
            List.of(DogTrait.GREEDY, DogTrait.CLINGY, DogTrait.LAZY),
            List.of(DogTrait.GREEDY, DogTrait.CLINGY)),
        Arguments.of(
            List.of(DogTrait.TIMID, DogTrait.BRAVE, DogTrait.LAZY),
            List.of(DogTrait.TIMID, DogTrait.LAZY)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("keptTraits")
  @DisplayName("a dog keeps at most two distinct traits that do not conflict")
  void keepsCompatibleTraits(final List<DogTrait> given, final List<DogTrait> kept) {
    assertEquals(kept, new DogTraits(given).traits());
  }

  static Stream<Arguments> combinedEffects() {
    return Stream.of(
        Arguments.of(DogTraits.NONE, false, 0.0, 0.0, true, 1.0, 1.0),
        Arguments.of(DogTraits.of(DogTrait.BRAVE, DogTrait.CLINGY), true, 0.0, 2.0, true, 1.0, 0.6),
        Arguments.of(
            DogTraits.of(DogTrait.GREEDY, DogTrait.TIMID), false, 0.5, -2.5, false, 1.0, 1.0),
        Arguments.of(
            DogTraits.of(DogTrait.LAZY, DogTrait.CLINGY), false, 0.0, 0.0, true, 0.8, 0.6));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("combinedEffects")
  @DisplayName("two traits combine their effects")
  void combinesEffects(
      final DogTraits traits,
      final boolean holdsGround,
      final double fleeHealth,
      final double stance,
      final boolean joinsFights,
      final double idleSpeed,
      final double followScale) {
    assertEquals(holdsGround, traits.holdsGround());
    assertEquals(fleeHealth, traits.fleeHealthFraction());
    assertEquals(stance, traits.stanceOffset());
    assertEquals(joinsFights, traits.joinsOwnerFights());
    assertEquals(idleSpeed, traits.idleSpeedScale(), 1.0E-9);
    assertEquals(followScale, traits.followDistanceScale(), 1.0E-9);
  }

  static Stream<DogTraits> samples() {
    return Stream.of(
        DogTraits.NONE,
        DogTraits.of(DogTrait.PLAYFUL),
        DogTraits.of(DogTrait.TIMID, DogTrait.GREEDY),
        DogTraits.of(DogTrait.LAZY, DogTrait.CLINGY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("samples")
  @DisplayName("traits round-trip through NBT and the network")
  void roundTrips(final DogTraits traits) {
    assertEquals(traits, DogTraits.fromNbt(traits.toNbt()));
    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    traits.write(buf);
    assertEquals(traits, DogTraits.read(buf));
    assertEquals(0, buf.readableBytes());
  }

  @Test
  @DisplayName("unknown saved ids are dropped")
  void unknownSavedIdsAreDropped() {
    final NbtList list = new NbtList();
    list.add(NbtString.of("fearless"));
    list.add(NbtString.of("clingy"));
    assertEquals(DogTraits.of(DogTrait.CLINGY), DogTraits.fromNbt(list));
    assertTrue(DogTraits.fromNbt(new NbtList()).isEmpty());
  }

  @Test
  @DisplayName("a packet claiming too many traits is rejected")
  void oversizedPacketIsRejected() {
    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    buf.writeVarInt(DogTraits.MAX_TRAITS + 1);
    assertThrows(DecoderException.class, () -> DogTraits.read(buf));
  }
}
