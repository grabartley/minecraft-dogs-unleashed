package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class DogSaddlebags extends DogComponent {

  public static final int SIZE = 9;

  private final UnleashedDogEntity dog;
  private final SimpleInventory contents = new SimpleInventory(SIZE);

  DogSaddlebags(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public SimpleInventory contents() {
    return this.contents;
  }

  public boolean isWorn() {
    return !this.dog.getEquipmentHolder().getStack(DogEquipmentSlot.SADDLEBAGS).isEmpty();
  }

  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  void dropContents() {
    for (int slot = 0; slot < SIZE; slot++) {
      final ItemStack stack = this.contents.removeStack(slot);
      if (!stack.isEmpty()) {
        this.dog.dropStack(stack);
      }
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.contents.isEmpty()) {
      return;
    }
    nbt.put(
        ModNbtKeys.SADDLEBAG_CONTENTS,
        Inventories.writeNbt(
            new NbtCompound(),
            this.contents.getHeldStacks(),
            this.dog.getWorld().getRegistryManager()));
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.contents.clear();
    if (nbt.contains(ModNbtKeys.SADDLEBAG_CONTENTS, NbtElement.COMPOUND_TYPE)) {
      Inventories.readNbt(
          nbt.getCompound(ModNbtKeys.SADDLEBAG_CONTENTS),
          this.contents.getHeldStacks(),
          this.dog.getWorld().getRegistryManager());
    }
  }
}
