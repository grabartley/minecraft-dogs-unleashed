package com.grahambartley.dogsunleashed.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServerConfigPayloadsTest {

  // The config codec only uses primitive, string, and map buf operations, so no registry lookup is
  // needed and a null registry is safe here.
  private static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  static Stream<Arguments> configs() {
    return Stream.of(
        Arguments.of("defaults", DogsUnleashedConfig.defaults()),
        Arguments.of("names hidden", DogsUnleashedConfig.defaults().withShowDogNames(false)),
        Arguments.of(
            "every field flipped off the default",
            new DogsUnleashedConfig(
                false,
                250,
                Map.of("husky", 0, "beagle", 40),
                false,
                false,
                false,
                false,
                64,
                0.5f,
                0.25f,
                false)));
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
}
