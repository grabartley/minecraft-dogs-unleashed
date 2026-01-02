package com.grahambartley;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class ModComponents {

  public static final ComponentType<DyeColor> DOG_BED_COLOR =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed_color"),
          ComponentType.<DyeColor>builder()
              .codec(DyeColor.CODEC)
              .packetCodec(DyeColor.PACKET_CODEC)
              .build());

  public static void initialize() {}
}
