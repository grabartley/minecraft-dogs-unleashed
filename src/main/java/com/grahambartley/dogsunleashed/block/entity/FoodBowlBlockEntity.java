package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.block.DogBowlRules;
import com.grahambartley.dogsunleashed.block.FoodBowlBlock;
import com.grahambartley.dogsunleashed.entity.DogFoods;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class FoodBowlBlockEntity extends BlockEntity implements SidedInventory {

  private static final int[] SLOTS = IntStream.range(0, DogBowlRules.FOOD_CAPACITY).toArray();

  private final DefaultedList<ItemStack> stacks =
      DefaultedList.ofSize(DogBowlRules.FOOD_CAPACITY, ItemStack.EMPTY);

  public FoodBowlBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.FOOD_BOWL, pos, state);
  }

  public int foodCount() {
    return this.stacks.stream().mapToInt(ItemStack::getCount).sum();
  }

  public int addFood(final ItemStack food) {
    if (!DogFoods.isBreedingItem(food)) {
      return 0;
    }
    int moved = 0;
    while (moved < food.getCount() && DogBowlRules.foodRoom(this.foodCount()) > 0) {
      this.insertOne(food.copyWithCount(1));
      moved++;
    }
    if (moved > 0) {
      this.markDirty();
    }
    return moved;
  }

  public ItemStack takeOne(final Predicate<ItemStack> edible) {
    for (final ItemStack stack : this.stacks) {
      if (!stack.isEmpty() && edible.test(stack)) {
        final ItemStack taken = stack.split(1);
        this.markDirty();
        return taken;
      }
    }
    return ItemStack.EMPTY;
  }

  public boolean hasFood(final Predicate<ItemStack> edible) {
    return this.stacks.stream().anyMatch(stack -> !stack.isEmpty() && edible.test(stack));
  }

  private void insertOne(final ItemStack one) {
    for (final ItemStack stack : this.stacks) {
      if (!stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, one)) {
        stack.increment(1);
        return;
      }
    }
    for (int slot = 0; slot < this.stacks.size(); slot++) {
      if (this.stacks.get(slot).isEmpty()) {
        this.stacks.set(slot, one);
        return;
      }
    }
  }

  @Override
  public void markDirty() {
    super.markDirty();
    if (this.world != null && !this.world.isClient) {
      final BlockState state = this.getCachedState();
      if (state.contains(FoodBowlBlock.FILL)) {
        final BlockState filled =
            state.with(FoodBowlBlock.FILL, DogBowlRules.foodFill(this.foodCount()));
        if (filled != state) {
          this.world.setBlockState(this.pos, filled);
        }
      }
    }
  }

  @Override
  public int[] getAvailableSlots(final Direction side) {
    return SLOTS;
  }

  @Override
  public boolean canInsert(final int slot, final ItemStack stack, final @Nullable Direction dir) {
    return this.isValid(slot, stack);
  }

  @Override
  public boolean canExtract(final int slot, final ItemStack stack, final Direction dir) {
    return false;
  }

  @Override
  public boolean isValid(final int slot, final ItemStack stack) {
    return DogFoods.isBreedingItem(stack) && DogBowlRules.foodRoom(this.foodCount()) > 0;
  }

  @Override
  public int size() {
    return this.stacks.size();
  }

  @Override
  public boolean isEmpty() {
    return this.stacks.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getStack(final int slot) {
    return this.stacks.get(slot);
  }

  @Override
  public ItemStack removeStack(final int slot, final int amount) {
    final ItemStack removed = Inventories.splitStack(this.stacks, slot, amount);
    if (!removed.isEmpty()) {
      this.markDirty();
    }
    return removed;
  }

  @Override
  public ItemStack removeStack(final int slot) {
    return Inventories.removeStack(this.stacks, slot);
  }

  @Override
  public void setStack(final int slot, final ItemStack stack) {
    this.stacks.set(slot, stack);
    this.markDirty();
  }

  @Override
  public boolean canPlayerUse(final PlayerEntity player) {
    return false;
  }

  @Override
  public void clear() {
    this.stacks.clear();
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup lookup) {
    super.writeNbt(nbt, lookup);
    Inventories.writeNbt(nbt, this.stacks, lookup);
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup lookup) {
    super.readNbt(nbt, lookup);
    this.stacks.clear();
    Inventories.readNbt(nbt, this.stacks, lookup);
  }
}
