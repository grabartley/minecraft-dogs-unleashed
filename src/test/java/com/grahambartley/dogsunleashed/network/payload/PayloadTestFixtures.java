package com.grahambartley.dogsunleashed.network.payload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;

final class PayloadTestFixtures {

  private PayloadTestFixtures() {}

  static void assertLeadingUuid(final RegistryByteBuf buf, final UUID expected) {
    assertEquals(expected.getMostSignificantBits(), buf.readLong());
    assertEquals(expected.getLeastSignificantBits(), buf.readLong());
  }

  static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }
}
