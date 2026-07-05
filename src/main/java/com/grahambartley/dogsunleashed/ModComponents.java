package com.grahambartley.dogsunleashed;

import com.mojang.serialization.Codec;
import java.util.UUID;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public class ModComponents {

  public static final ComponentType<DyeColor> DOG_BED_COLOR =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed_color"),
          ComponentType.<DyeColor>builder()
              .codec(DyeColor.CODEC)
              .packetCodec(DyeColor.PACKET_CODEC)
              .build());

  public static final ComponentType<UUID> DOG_GRAVE_UUID =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave_uuid"),
          ComponentType.<UUID>builder().codec(Uuids.CODEC).packetCodec(Uuids.PACKET_CODEC).build());

  public static final ComponentType<String> DOG_GRAVE_NAME =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave_name"),
          ComponentType.<String>builder()
              .codec(Codec.STRING)
              .packetCodec(PacketCodecs.STRING)
              .build());

  public static final ComponentType<DyeColor> DOG_GRAVE_FLOWER_COLOR =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave_flower_color"),
          ComponentType.<DyeColor>builder()
              .codec(DyeColor.CODEC)
              .packetCodec(DyeColor.PACKET_CODEC)
              .build());

  public static final ComponentType<DyeColor> FRISBEE_COLOR =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "frisbee_color"),
          ComponentType.<DyeColor>builder()
              .codec(DyeColor.CODEC)
              .packetCodec(DyeColor.PACKET_CODEC)
              .build());

  public static void initialize() {}
}
