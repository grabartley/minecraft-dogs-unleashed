package com.grahambartley.dogsunleashed.wood;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class WoodPlanks {

  public static final WoodType DEFAULT = WoodType.OAK;

  public static final Codec<WoodType> CODEC = Codec.STRING.xmap(WoodPlanks::byName, WoodType::name);

  public static final PacketCodec<ByteBuf, WoodType> PACKET_CODEC =
      PacketCodecs.STRING.xmap(WoodPlanks::byName, WoodType::name);

  private static final String NBT_WOOD = "Wood";

  private static final Supplier<List<WoodType>> WOODS =
      Suppliers.memoize(
          () -> WoodType.stream().filter(wood -> planksOf(wood) != Blocks.AIR).toList());

  private static final Supplier<Map<String, WoodType>> BY_NAME =
      Suppliers.memoize(
          () -> all().stream().collect(Collectors.toUnmodifiableMap(WoodType::name, wood -> wood)));

  private WoodPlanks() {}

  public static List<WoodType> all() {
    return WOODS.get();
  }

  public static Block planksOf(final WoodType wood) {
    final Identifier id =
        Identifier.tryParse(Identifier.DEFAULT_NAMESPACE, wood.name() + "_planks");
    return id == null ? Blocks.AIR : Registries.BLOCK.get(id);
  }

  public static WoodType byName(final String name) {
    return BY_NAME.get().getOrDefault(name, DEFAULT);
  }

  public static WoodType read(final NbtCompound nbt) {
    return byName(nbt.getString(NBT_WOOD));
  }

  public static void write(final NbtCompound nbt, final WoodType wood) {
    nbt.putString(NBT_WOOD, wood.name());
  }
}
