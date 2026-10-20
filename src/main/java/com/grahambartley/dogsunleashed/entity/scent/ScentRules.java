package com.grahambartley.dogsunleashed.entity.scent;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class ScentRules {

  public static final RegistryKey<Registry<ScentRule>> REGISTRY_KEY =
      RegistryKey.ofRegistry(Identifier.of(DogsUnleashed.MOD_ID, "scent_rule"));

  private ScentRules() {}

  public static void register() {
    DynamicRegistries.register(REGISTRY_KEY, ScentRule.CODEC);
  }

  public static Optional<RegistryEntry.Reference<ScentRule>> forItem(
      final World world, final ItemStack stack) {
    return world
        .getRegistryManager()
        .getOptional(REGISTRY_KEY)
        .flatMap(
            registry ->
                registry
                    .streamEntries()
                    .filter(entry -> entry.value().matchesItem(stack))
                    .sorted(
                        (left, right) ->
                            left.registryKey().getValue().compareTo(right.registryKey().getValue()))
                    .findFirst());
  }

  public static Optional<RegistryEntry.Reference<ScentRule>> byId(
      final World world, final Identifier id) {
    return world
        .getRegistryManager()
        .getOptional(REGISTRY_KEY)
        .flatMap(registry -> registry.getEntry(RegistryKey.of(REGISTRY_KEY, id)));
  }
}
