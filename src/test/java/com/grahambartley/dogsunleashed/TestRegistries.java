package com.grahambartley.dogsunleashed;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;

public final class TestRegistries {

  public static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private TestRegistries() {}
}
