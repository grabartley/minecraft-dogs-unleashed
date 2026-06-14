package com.grahambartley.entity.fetch;

import net.minecraft.item.ItemStack;

public interface FetchCarriedItemProvider {
  void enrichCarriedItemStack(ItemStack stack);

  void restoreFromCarriedItemStack(ItemStack stack);
}
