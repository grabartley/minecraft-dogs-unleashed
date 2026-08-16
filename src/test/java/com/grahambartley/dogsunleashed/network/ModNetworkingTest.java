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
    return samplePet(breed, name, List.of());
  }

  private static ModNetworking.PetSyncData samplePet(
      final UnleashedDogBreed breed, final String name, final List<BreedShare> composition) {
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
        0,
        0.29f,
        3.5f,
        composition);
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
  @DisplayName("PetSyncData codec round-trips a cross-breed pet with genome fields")
  void petSyncDataCodecRoundTripsCrossBreedGenomeFields() {
    final ModNetworking.PetSyncData pet =
        samplePet(
            UnleashedDogBreed.CROSS_BREED,
            "Pixel",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)));
    final RegistryByteBuf buf = newBuf();
    ModNetworking.PetSyncData.CODEC.encode(buf, pet);
    assertEquals(pet, ModNetworking.PetSyncData.CODEC.decode(buf));
  }

  static Stream<Arguments> displayBreedCases() {
    return Stream.of(
        Arguments.of(
            "pure breed ignores composition",
            samplePet(UnleashedDogBreed.BEAGLE, "Rex"),
            UnleashedDogBreed.BEAGLE),
        Arguments.of(
            "cross-breed without composition falls back to its own breed",
            samplePet(UnleashedDogBreed.CROSS_BREED, "Pixel"),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "cross-breed resolves the dominant composition breed",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                    new BreedShare(UnleashedDogBreed.HUSKY, 0.75f))),
            UnleashedDogBreed.HUSKY),
        Arguments.of(
            "cross-breed breaks share ties by serialized id",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f),
                    new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f))),
            UnleashedDogBreed.BEAGLE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("displayBreedCases")
  @DisplayName("displayBreed resolves the breed the client should render")
  void displayBreedResolvesRenderBreed(
      final String label, final ModNetworking.PetSyncData pet, final UnleashedDogBreed expected) {
    assertEquals(expected, pet.displayBreed());
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
