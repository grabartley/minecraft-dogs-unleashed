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

  static final List<ComponentType<?>> UNSEEN_COMPONENTS =
      List.of(
          DataComponentTypes.CUSTOM_NAME,
          DataComponentTypes.LORE,
          DataComponentTypes.CUSTOM_DATA,
          DataComponentTypes.ENCHANTMENTS,
          DataComponentTypes.STORED_ENCHANTMENTS,
          DataComponentTypes.REPAIR_COST,
          DataComponentTypes.ATTRIBUTE_MODIFIERS,
          DataComponentTypes.CAN_PLACE_ON,
          DataComponentTypes.CAN_BREAK,
          DataComponentTypes.CONTAINER,
          DataComponentTypes.CONTAINER_LOOT,
          DataComponentTypes.BUNDLE_CONTENTS,
          DataComponentTypes.CHARGED_PROJECTILES,
          DataComponentTypes.WRITABLE_BOOK_CONTENT,
          DataComponentTypes.WRITTEN_BOOK_CONTENT,
          DataComponentTypes.BLOCK_ENTITY_DATA,
          DataComponentTypes.ENTITY_DATA,
          DataComponentTypes.BUCKET_ENTITY_DATA,
          DataComponentTypes.BEES,
          DataComponentTypes.RECIPES,
          DataComponentTypes.LODESTONE_TRACKER,
          DataComponentTypes.DEBUG_STICK_STATE,
          DataComponentTypes.LOCK);

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
    final boolean glints = stack.hasGlint();
    final ItemStack shown = stack.copyWithCount(1);
    UNSEEN_COMPONENTS.forEach(shown::remove);
    if (glints && !shown.hasGlint()) {
      shown.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    return shown;
  }
}
