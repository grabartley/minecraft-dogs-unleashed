package com.grahambartley.dogsunleashed.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ModNetworkingTest {

  // Both wheel payload codecs only use primitive and string buf operations, so no registry lookup
  // is needed and null is safe here.
  private static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogWheelAction.class)
  @DisplayName("SelectWheelActionPayload codec round-trips every wheel action")
  void selectWheelActionPayloadCodecRoundTrips(final DogWheelAction action) {
    final ModNetworking.SelectWheelActionPayload payload =
        new ModNetworking.SelectWheelActionPayload(UUID.randomUUID(), action.id());
    final RegistryByteBuf buf = newBuf();
    ModNetworking.SelectWheelActionPayload.CODEC.encode(buf, payload);
    assertEquals(payload, ModNetworking.SelectWheelActionPayload.CODEC.decode(buf));
  }

  static Stream<Arguments> openCommandWheelPayloads() {
    return Stream.of(
        Arguments.of("typical", 42, 0, true, "Rex"),
        Arguments.of("no bed", 1, 3, false, "Good Boy"),
        Arguments.of("unicode name", 7, 6, true, "小白"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("openCommandWheelPayloads")
  @DisplayName("OpenCommandWheelPayload codec round-trips its fields")
  void openCommandWheelPayloadCodecRoundTrips(
      final String label,
      final int entityId,
      final int commandId,
      final boolean hasBed,
      final String dogName) {
    final ModNetworking.OpenCommandWheelPayload payload =
        new ModNetworking.OpenCommandWheelPayload(
            entityId, UUID.randomUUID(), commandId, hasBed, dogName);
    final RegistryByteBuf buf = newBuf();
    ModNetworking.OpenCommandWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, ModNetworking.OpenCommandWheelPayload.CODEC.decode(buf));
  }

  static Stream<Arguments> stripControlCharsCases() {
    final String printableAscii =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    return Stream.of(
        Arguments.of("normal lowercase", "hello", "hello"),
        Arguments.of("mixed-case sentence", "Hello World 123", "Hello World 123"),
        Arguments.of("camelCase", "abcDEF", "abcDEF"),
        Arguments.of("null byte in middle", "a\0b", "ab"),
        Arguments.of("null byte at end", "hello\0", "hello"),
        Arguments.of("null byte at start", "\0hello", "hello"),
        Arguments.of("newline in middle", "a\nb", "ab"),
        Arguments.of("tab in middle", "a\tb", "ab"),
        Arguments.of("carriage return in middle", "a\rb", "ab"),
        Arguments.of("low control around text", "hello", "hello"),
        Arguments.of("DEL in middle", "ab", "ab"),
        Arguments.of("DEL at end", "hello", "hello"),
        Arguments.of("empty string", "", ""),
        Arguments.of("only control characters", "\0\n\r\t", ""),
        Arguments.of("mixed control and printable", "a\0b\0c\tX\nY\rZ123", "abcXYZ123"),
        Arguments.of("printable ASCII preserved", printableAscii, printableAscii),
        Arguments.of("accented Latin preserved", "éàüñ", "éàüñ"),
        Arguments.of("CJK preserved", "中国", "中国"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("stripControlCharsCases")
  @DisplayName("stripControlChars removes control bytes and keeps printable characters")
  void stripControlCharsRemovesControlBytes(
      final String label, final String input, final String expected) {
    assertEquals(expected, ModNetworking.stripControlChars(input));
  }

  private static ModNetworking.PetSyncData samplePet(
      final UnleashedDogBreed breed, final String name) {
    return new ModNetworking.PetSyncData(
        UUID.randomUUID().toString(),
        breed,
        name,
        12.5f,
        25.0f,
        10,
        64,
        -20,
        "minecraft:overworld",
        true,
        false,
        1,
        2,
        0);
  }

  static Stream<Arguments> openDogInspectPayloads() {
    return Stream.of(
        Arguments.of(
            "wild dog",
            samplePet(UnleashedDogBreed.HUSKY, ""),
            false,
            "",
            List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f))),
        Arguments.of(
            "tamed dog with owner",
            samplePet(UnleashedDogBreed.BEAGLE, "Rex"),
            true,
            "Steve",
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.25f))),
        Arguments.of(
            "tamed dog with unknown owner",
            samplePet(UnleashedDogBreed.SHIBA_INU, "小白"),
            true,
            "",
            List.of(new BreedShare(UnleashedDogBreed.SHIBA_INU, 1.0f))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("openDogInspectPayloads")
  @DisplayName("OpenDogInspectPayload codec round-trips its fields")
  void openDogInspectPayloadCodecRoundTrips(
      final String label,
      final ModNetworking.PetSyncData pet,
      final boolean tamed,
      final String ownerName,
      final List<BreedShare> composition) {
    final ModNetworking.OpenDogInspectPayload payload =
        new ModNetworking.OpenDogInspectPayload(pet, tamed, ownerName, composition);
    final RegistryByteBuf buf = newBuf();
    ModNetworking.OpenDogInspectPayload.CODEC.encode(buf, payload);
    assertEquals(payload, ModNetworking.OpenDogInspectPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("SyncDogConnectionsPayload codec round-trips the focus composition")
  void syncDogConnectionsPayloadCodecRoundTripsComposition() {
    final ModNetworking.ConnectionDogSyncData self =
        new ModNetworking.ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.HUSKY, "Luna"), UUID.randomUUID().toString(), "Alex");
    final ModNetworking.ConnectionDogSyncData parent =
        new ModNetworking.ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.BEAGLE, "Max"), UUID.randomUUID().toString(), "");
    final ModNetworking.SyncDogConnectionsPayload payload =
        new ModNetworking.SyncDogConnectionsPayload(
            self,
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            List.of(parent),
            List.of(),
            List.of(),
            List.of(),
            false);
    final RegistryByteBuf buf = newBuf();
    ModNetworking.SyncDogConnectionsPayload.CODEC.encode(buf, payload);
    assertEquals(payload, ModNetworking.SyncDogConnectionsPayload.CODEC.decode(buf));
  }
}
