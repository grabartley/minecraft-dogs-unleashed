package com.grahambartley.dogsunleashed.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.config.DireWolfPackSettings;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.config.FurSettings;
import com.grahambartley.dogsunleashed.config.GameplaySettings;
import com.grahambartley.dogsunleashed.config.SleepSettings;
import com.grahambartley.dogsunleashed.config.SoundSettings;
import com.grahambartley.dogsunleashed.config.SpawningSettings;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServerConfigPayloadsTest {

  private static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  static Stream<Arguments> configs() {
    return Stream.of(
        Arguments.of("defaults", DogsUnleashedConfig.defaults()),
        Arguments.of(
            "names hidden",
            DogsUnleashedConfig.defaults()
                .withGameplay(DogsUnleashedConfig.defaults().gameplay().withShowDogNames(false))),
        Arguments.of(
            "muddy dogs off",
            DogsUnleashedConfig.defaults()
                .withGameplay(DogsUnleashedConfig.defaults().gameplay().withMuddyDogs(false))),
        Arguments.of(
            "every field flipped off the default",
            new DogsUnleashedConfig(
                new SpawningSettings(false, 250, Map.of("husky", 0, "beagle", 40), false),
                new GameplaySettings(false, false, false, false),
                new SleepSettings(false, 64),
                new FurSettings(false, 250, 0),
                new SoundSettings(0.5f, 0.25f),
                new DireWolfSettings(
                    175,
                    8,
                    6,
                    9,
                    new DireWolfPackSettings(false, true),
                    new DireWolfStaminaSettings(false, 150, 60)),
                new AgingSettings(true, 42))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("configs")
  @DisplayName("the sync payload round-trips every config field")
  void syncPayloadRoundTripsEveryField(final String label, final DogsUnleashedConfig config) {
    final SyncServerConfigS2CPayload payload = new SyncServerConfigS2CPayload(config);
    final RegistryByteBuf buf = newBuf();

    SyncServerConfigS2CPayload.CODEC.encode(buf, payload);

    assertEquals(payload, SyncServerConfigS2CPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("configs")
  @DisplayName("the edit payload round-trips every config field")
  void editPayloadRoundTripsEveryField(final String label, final DogsUnleashedConfig config) {
    final EditServerConfigC2SPayload payload = new EditServerConfigC2SPayload(config);
    final RegistryByteBuf buf = newBuf();

    EditServerConfigC2SPayload.CODEC.encode(buf, payload);

    assertEquals(payload, EditServerConfigC2SPayload.CODEC.decode(buf));
  }

  private static RegistryByteBuf breedRatesWithEntries(final int entries) {
    final RegistryByteBuf buf = newBuf();
    buf.writeVarInt(entries);
    for (int i = 0; i < entries; i++) {
      buf.writeString("breed_" + i);
      buf.writeVarInt(100);
    }
    return buf;
  }

  static IntStream entriesWithinCap() {
    return IntStream.of(0, 1, ServerConfigPayloads.BREED_RATES_MAX_SIZE);
  }

  static IntStream entriesOverCap() {
    return IntStream.of(ServerConfigPayloads.BREED_RATES_MAX_SIZE + 1, Integer.MAX_VALUE);
  }

  @ParameterizedTest(name = "{0} entries")
  @MethodSource("entriesWithinCap")
  @DisplayName("a breed map within the cap decodes every entry")
  void breedRatesWithinCapDecode(final int entries) {
    assertEquals(
        entries, ServerConfigPayloads.readBreedRates(breedRatesWithEntries(entries)).size());
  }

  @ParameterizedTest(name = "{0} entries")
  @MethodSource("entriesOverCap")
  @DisplayName("a breed map over the cap is refused before any entry is read")
  void breedRatesOverCapAreRefused(final int entries) {
    final RegistryByteBuf buf = newBuf();
    buf.writeVarInt(entries);
    assertThrows(DecoderException.class, () -> ServerConfigPayloads.readBreedRates(buf));
  }
}
