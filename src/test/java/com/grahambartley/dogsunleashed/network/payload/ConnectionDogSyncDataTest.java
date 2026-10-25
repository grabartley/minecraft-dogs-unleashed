package com.grahambartley.dogsunleashed.network.payload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.TestRegistries;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ConnectionDogSyncDataTest {

  private static final UUID MUM = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID DAD = UUID.fromString("00000000-0000-0000-0000-00000000000b");

  static Stream<Arguments> parentPairs() {
    return Stream.of(
        Arguments.of(MUM, DAD, List.of(MUM.toString(), DAD.toString())),
        Arguments.of(MUM, null, List.of(MUM.toString())),
        Arguments.of(null, DAD, List.of(DAD.toString())),
        Arguments.of(null, null, List.of()));
  }

  @ParameterizedTest(name = "{0} + {1} -> {2}")
  @MethodSource("parentPairs")
  @DisplayName("parent ids keep each recorded parent in order and skip unknown ones")
  void parentIdsSkipUnknownParents(final UUID a, final UUID b, final List<String> expected) {
    assertEquals(expected, ConnectionDogSyncData.parentIdsOf(a, b));
  }

  static Stream<List<String>> parentLists() {
    return Stream.of(List.of(), List.of(MUM.toString()), List.of(MUM.toString(), DAD.toString()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("parentLists")
  @DisplayName("parent ids survive the wire")
  void parentIdsRoundTrip(final List<String> parentIds) {
    final ConnectionDogSyncData dog = dogWith(parentIds);
    final RegistryByteBuf buf = TestRegistries.buf();
    dog.write(buf);
    assertEquals(dog, ConnectionDogSyncData.read(buf));
  }

  @Test
  @DisplayName("a packet claiming more than two parents is rejected")
  void tooManyParentsIsRejected() {
    final RegistryByteBuf buf = TestRegistries.buf();
    dogWith(List.of()).write(buf);
    buf.writerIndex(buf.writerIndex() - 1);
    buf.writeVarInt(3);
    assertThrows(DecoderException.class, () -> ConnectionDogSyncData.read(buf));
  }

  private static ConnectionDogSyncData dogWith(final List<String> parentIds) {
    return new ConnectionDogSyncData(
        PetSyncDataFixture.pet().breed(UnleashedDogBreed.HUSKY).name("Luna").build(),
        DAD.toString(),
        "Alex",
        List.of(),
        true,
        parentIds);
  }
}
