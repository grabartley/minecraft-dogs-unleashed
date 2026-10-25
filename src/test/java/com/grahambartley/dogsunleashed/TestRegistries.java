package com.grahambartley.dogsunleashed;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;

public final class TestRegistries {

  public static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private TestRegistries() {}

  public static RegistryByteBuf buf() {
    return new RegistryByteBuf(Unpooled.buffer(), REGISTRIES);
  }
}
