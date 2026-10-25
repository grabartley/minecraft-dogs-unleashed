package com.grahambartley.dogsunleashed.screenhandler;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DogEquipmentInventory implements Inventory {

  public static final int MAX_INTERACTION_DISTANCE = 8;
  private static final int SIZE = 1;
  private static final int MAX_STACK_SIZE = 1;

  private final @Nullable UnleashedDogEntity dog;
  private final DogEquipmentSlot slot;

  public DogEquipmentInventory(
      final @Nullable UnleashedDogEntity dog, final DogEquipmentSlot slot) {
    this.dog = dog;
    this.slot = slot;
  }

  public DogEquipmentSlot slot() {
    return this.slot;
  }

  public static boolean canReach(
      final @Nullable UnleashedDogEntity dog, final PlayerEntity player) {
    return dog != null
        && dog.isAlive()
        && dog.isTamed()
        && (dog.isOwner(player) || DogDeliveries.isWaitingFor(dog, player))
        && player.canInteractWithEntity(dog, MAX_INTERACTION_DISTANCE);
  }

  @Override
  public int size() {
    return SIZE;
  }

  @Override
  public boolean isEmpty() {
    return this.stack().isEmpty();
  }

  @Override
  public ItemStack getStack(final int index) {
    return this.stack();
  }

  @Override
  public ItemStack removeStack(final int index, final int amount) {
    final ItemStack remaining = this.stack().copy();
    if (remaining.isEmpty()) {
      return ItemStack.EMPTY;
    }
    final ItemStack removed = remaining.split(amount);
    this.setStack(index, remaining);
    return removed;
  }

  @Override
  public ItemStack removeStack(final int index) {
    final ItemStack removed = this.stack().copy();
    this.setStack(index, ItemStack.EMPTY);
    return removed;
  }

  @Override
  public void setStack(final int index, final ItemStack stack) {
    if (this.dog == null || !stack.isEmpty() && !this.isValid(index, stack)) {
      return;
    }
    this.dog.getEquipmentHolder().setStack(this.slot, stack);
  }

  @Override
  public int getMaxCountPerStack() {
    return MAX_STACK_SIZE;
  }

  @Override
  public void markDirty() {}

  @Override
  public boolean canPlayerUse(final PlayerEntity player) {
    return canReach(this.dog, player);
  }

  @Override
  public boolean isValid(final int index, final ItemStack stack) {
    return this.slot.canHold(stack)
        && (this.dog == null || this.dog.getEquipmentHolder().fits(this.slot));
  }

  public boolean canTake(final PlayerEntity player) {
    return this.dog == null || this.dog.getEquipmentHolder().releases(this.slot, player);
  }

  @Override
  public void clear() {
    this.setStack(0, ItemStack.EMPTY);
  }

  private ItemStack stack() {
    return this.dog == null ? ItemStack.EMPTY : this.dog.getEquipmentHolder().getStack(this.slot);
  }
}
