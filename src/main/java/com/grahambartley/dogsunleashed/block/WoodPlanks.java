package com.grahambartley.dogsunleashed.block;

import com.google.common.base.Suppliers;
import com.grahambartley.dogsunleashed.ModComponents;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
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

  private static final Supplier<Map<WoodType, Identifier>> TEXTURES =
      Suppliers.memoize(
          () ->
              all().stream()
                  .collect(
                      Collectors.toUnmodifiableMap(Function.identity(), WoodPlanks::textureFor)));

  private WoodPlanks() {}

  public static List<WoodType> all() {
    return WOODS.get();
  }

  public static Block planksOf(final WoodType wood) {
    final Identifier id =
        Identifier.tryParse(Identifier.DEFAULT_NAMESPACE, wood.name() + "_planks");
    return id == null ? Blocks.AIR : Registries.BLOCK.get(id);
  }

  public static Identifier textureOf(final WoodType wood) {
    return TEXTURES.get().getOrDefault(wood, TEXTURES.get().get(DEFAULT));
  }

  public static WoodType byName(final String name) {
    return BY_NAME.get().getOrDefault(name, DEFAULT);
  }

  public static List<ItemStack> stacksOf(final Item item) {
    return all().stream()
        .map(
            wood -> {
              final ItemStack stack = new ItemStack(item);
              stack.set(ModComponents.WOOD, wood);
              return stack;
            })
        .toList();
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

  public static void appendTooltip(final ItemStack stack, final List<Text> tooltip) {
    tooltip.add(tooltipOf(of(stack)));
  }

  public static WoodType read(final NbtCompound nbt) {
    return byName(nbt.getString(NBT_WOOD));
  }

  public static void write(final NbtCompound nbt, final WoodType wood) {
    nbt.putString(NBT_WOOD, wood.name());
  }

  private static Identifier textureFor(final WoodType wood) {
    final Identifier planks = Registries.BLOCK.getId(planksOf(wood));
    return Identifier.of(planks.getNamespace(), "textures/block/" + planks.getPath() + ".png");
  }
}
