package com.grahambartley.dogsunleashed.portrait;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public final class PortraitGear {

  static final List<ComponentType<?>> SHOWN_COMPONENTS =
      List.of(
          DataComponentTypes.DYED_COLOR,
          DataComponentTypes.TRIM,
          DataComponentTypes.PROFILE,
          DataComponentTypes.BANNER_PATTERNS,
          DataComponentTypes.BASE_COLOR,
          DataComponentTypes.DAMAGE,
          DataComponentTypes.MAX_DAMAGE);

  public static final Codec<ItemStack> LENIENT_CODEC =
      new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStack, T>> decode(final DynamicOps<T> ops, final T input) {
          final DataResult<Pair<ItemStack, T>> read = ItemStack.OPTIONAL_CODEC.decode(ops, input);
          return read.result().isPresent()
              ? read
              : DataResult.success(Pair.of(ItemStack.EMPTY, input), Lifecycle.stable());
        }

        @Override
        public <T> DataResult<T> encode(
            final ItemStack stack, final DynamicOps<T> ops, final T prefix) {
          return ItemStack.OPTIONAL_CODEC.encode(stack, ops, prefix);
        }
      };

  private PortraitGear() {}

  public static ItemStack shownPart(final ItemStack stack) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    final ItemStack shown = new ItemStack(stack.getItem());
    SHOWN_COMPONENTS.forEach(type -> copy(stack, shown, type));
    if (stack.hasGlint()) {
      shown.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    return shown;
  }

  private static <T> void copy(
      final ItemStack from, final ItemStack to, final ComponentType<T> type) {
    final T value = from.get(type);
    if (value != null) {
      to.set(type, value);
    }
  }
}
