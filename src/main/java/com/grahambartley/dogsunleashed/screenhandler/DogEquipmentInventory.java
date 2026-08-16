package com.grahambartley.dogsunleashed.screenhandler;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DogEquipmentInventory implements Inventory {

  public static final DogEquipmentSlot[] SLOTS = DogEquipmentSlot.values();
  public static final int MAX_INTERACTION_DISTANCE = 8;
  private static final int MAX_STACK_SIZE = 1;

  private final @Nullable UnleashedDogEntity dog;

  public DogEquipmentInventory(final @Nullable UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable UnleashedDogEntity dog() {
    return this.dog;
  }

  public DogEquipmentSlot slotAt(final int index) {
    return SLOTS[index];
  }

  @Override
  public int size() {
    return SLOTS.length;
  }

  @Override
  public boolean isEmpty() {
    for (final DogEquipmentSlot slot : SLOTS) {
      if (!this.stackIn(slot).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getStack(final int index) {
    return this.stackIn(SLOTS[index]);
  }

  @Override
  public ItemStack removeStack(final int index, final int amount) {
    final ItemStack remaining = this.getStack(index).copy();
    if (remaining.isEmpty()) {
      return ItemStack.EMPTY;
    }
    final ItemStack removed = remaining.split(amount);
    this.setStack(index, remaining);
    return removed;
  }

  @Override
  public ItemStack removeStack(final int index) {
    final ItemStack removed = this.getStack(index).copy();
    this.setStack(index, ItemStack.EMPTY);
    return removed;
  }

  @Override
  public void setStack(final int index, final ItemStack stack) {
    if (this.dog == null) {
      return;
    }
    this.dog.setEquipment(SLOTS[index], stack);
  }

  @Override
  public int getMaxCountPerStack() {
    return MAX_STACK_SIZE;
  }

  @Override
  public void markDirty() {}

  @Override
  public boolean canPlayerUse(final PlayerEntity player) {
    return this.dog != null
        && this.dog.isAlive()
        && this.dog.isTamed()
        && this.dog.isOwner(player)
        && player.canInteractWithEntity(this.dog, MAX_INTERACTION_DISTANCE);
  }

  @Override
  public boolean isValid(final int index, final ItemStack stack) {
    return SLOTS[index].canHold(stack);
  }

  @Override
  public void clear() {
    for (int index = 0; index < SLOTS.length; index++) {
      this.setStack(index, ItemStack.EMPTY);
    }
  }

  private ItemStack stackIn(final DogEquipmentSlot slot) {
    return this.dog == null ? ItemStack.EMPTY : this.dog.getEquipment(slot);
  }
}
