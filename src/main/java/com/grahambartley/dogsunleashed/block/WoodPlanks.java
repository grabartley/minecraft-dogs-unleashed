package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModComponents;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public final class WoodPlanks {

  public static final WoodType DEFAULT = WoodType.OAK;

  public static final PacketCodec<ByteBuf, WoodType> PACKET_CODEC =
      PacketCodecs.STRING.xmap(WoodPlanks::byName, WoodType::name);

  private static final String NBT_WOOD = "Wood";

  private WoodPlanks() {}

  public static List<WoodType> all() {
    return WoodType.stream().filter(wood -> planksOf(wood) != Blocks.AIR).toList();
  }

  public static Block planksOf(final WoodType wood) {
    final Identifier id =
        Identifier.tryParse(Identifier.DEFAULT_NAMESPACE, wood.name() + "_planks");
    return id == null ? Blocks.AIR : Registries.BLOCK.get(id);
  }

  public static Identifier textureOf(final WoodType wood) {
    final Identifier planks = Registries.BLOCK.getId(planksOf(wood));
    return Identifier.of(planks.getNamespace(), "textures/block/" + planks.getPath() + ".png");
  }

  public static WoodType byName(final String name) {
    return WoodType.stream()
        .filter(wood -> wood.name().equals(name) && planksOf(wood) != Blocks.AIR)
        .findFirst()
        .orElse(DEFAULT);
  }

  public static WoodType of(final ItemStack stack) {
    return stack.getOrDefault(ModComponents.WOOD, DEFAULT);
  }

  public static BlockState appearanceOf(
      final BlockState state, final BlockView world, final BlockPos pos) {
    return state.getBlock() instanceof WoodenBlock wooden
        ? planksOf(wooden.woodAt(world, pos, state)).getDefaultState()
        : state;
  }

  public static Text tooltipOf(final WoodType wood) {
    return Text.translatable(planksOf(wood).getTranslationKey()).formatted(Formatting.GRAY);
  }

  public static WoodType read(final NbtCompound nbt) {
    return byName(nbt.getString(NBT_WOOD));
  }

  public static void write(final NbtCompound nbt, final WoodType wood) {
    nbt.putString(NBT_WOOD, wood.name());
  }
}
