package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class WildTrustViewTest {

  static Stream<WildTrustView> views() {
    return Stream.of(
        WildTrustView.NONE,
        WildTrustView.TOO_WILD,
        WildTrustView.building(0),
        WildTrustView.building(73));
  }

  @ParameterizedTest
  @MethodSource("views")
  @DisplayName("the view survives a write and read")
  void roundTrips(final WildTrustView view) {
    final RegistryByteBuf buf = newBuf();
    view.write(buf);
    assertEquals(view, WildTrustView.read(buf));
  }

  @Test
  @DisplayName("building carries the trust value")
  void buildingCarriesTrust() {
    assertEquals(new WildTrustView(WildTrustView.Kind.BUILDING, 40), WildTrustView.building(40));
  }
}
