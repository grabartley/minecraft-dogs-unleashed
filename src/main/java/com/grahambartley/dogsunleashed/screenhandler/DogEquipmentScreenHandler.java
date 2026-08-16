package com.grahambartley.dogsunleashed.screenhandler;

import com.grahambartley.dogsunleashed.ModScreenHandlers;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class DogEquipmentScreenHandler extends ScreenHandler {

  public static final int DOG_SLOT_COUNT = DogEquipmentInventory.SLOTS.length;
  public static final int SLOT_SPACING = 18;
  public static final int DOG_SLOT_X = 8;
  public static final int FIRST_DOG_SLOT_Y = 18;
  public static final int PLAYER_INVENTORY_X = 8;
  public static final int PLAYER_INVENTORY_Y = 84;
  public static final int HOTBAR_Y = 142;

  private static final int PLAYER_INVENTORY_ROWS = 3;
  private static final int PLAYER_INVENTORY_COLUMNS = 9;
  private static final int MAIN_INVENTORY_START = DOG_SLOT_COUNT;
  private static final int HOTBAR_START =
      MAIN_INVENTORY_START + PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;
  private static final int TOTAL_SLOT_COUNT = HOTBAR_START + PLAYER_INVENTORY_COLUMNS;

  private final DogEquipmentInventory equipment;

  public DogEquipmentScreenHandler(
      final int syncId, final PlayerInventory playerInventory, final int dogEntityId) {
    this(syncId, playerInventory, resolveDog(playerInventory, dogEntityId));
  }

  public DogEquipmentScreenHandler(
      final int syncId,
      final PlayerInventory playerInventory,
      final @Nullable UnleashedDogEntity dog) {
    super(ModScreenHandlers.DOG_EQUIPMENT, syncId);
    this.equipment = new DogEquipmentInventory(dog);

    for (int index = 0; index < DOG_SLOT_COUNT; index++) {
      this.addSlot(
          new DogEquipmentItemSlot(
              this.equipment, index, DOG_SLOT_X, FIRST_DOG_SLOT_Y + index * SLOT_SPACING));
    }
    for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
      for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
        this.addSlot(
            new Slot(
                playerInventory,
                column + row * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS,
                PLAYER_INVENTORY_X + column * SLOT_SPACING,
                PLAYER_INVENTORY_Y + row * SLOT_SPACING));
      }
    }
    for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
      this.addSlot(
          new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * SLOT_SPACING, HOTBAR_Y));
    }
  }

  public @Nullable UnleashedDogEntity dog() {
    return this.equipment.dog();
  }

  public DogEquipmentSlot dogSlotAt(final int index) {
    return this.equipment.slotAt(index);
  }

  @Override
  public boolean canUse(final PlayerEntity player) {
    return this.equipment.canPlayerUse(player);
  }

  @Override
  public ItemStack quickMove(final PlayerEntity player, final int index) {
    final Slot source = this.slots.get(index);
    if (source == null || !source.hasStack()) {
      return ItemStack.EMPTY;
    }

    final ItemStack moving = source.getStack();
    final ItemStack original = moving.copy();

    if (index < DOG_SLOT_COUNT) {
      if (!this.insertItem(moving, MAIN_INVENTORY_START, TOTAL_SLOT_COUNT, true)) {
        return ItemStack.EMPTY;
      }
    } else if (!this.insertIntoDogSlots(moving)) {
      final boolean fromMainInventory = index < HOTBAR_START;
      final int targetStart = fromMainInventory ? HOTBAR_START : MAIN_INVENTORY_START;
      final int targetEnd = fromMainInventory ? TOTAL_SLOT_COUNT : HOTBAR_START;
      if (!this.insertItem(moving, targetStart, targetEnd, false)) {
        return ItemStack.EMPTY;
      }
    }

    if (moving.isEmpty()) {
      source.setStack(ItemStack.EMPTY);
    } else {
      source.markDirty();
    }
    return original;
  }

  private boolean insertIntoDogSlots(final ItemStack stack) {
    for (int index = 0; index < DOG_SLOT_COUNT; index++) {
      final Slot dogSlot = this.getSlot(index);
      if (!dogSlot.hasStack()
          && dogSlot.canInsert(stack)
          && this.insertItem(stack, index, index + 1, false)) {
        return true;
      }
    }
    return false;
  }

  private static @Nullable UnleashedDogEntity resolveDog(
      final PlayerInventory playerInventory, final int dogEntityId) {
    return playerInventory.player.getWorld().getEntityById(dogEntityId)
            instanceof UnleashedDogEntity dog
        ? dog
        : null;
  }

  private static final class DogEquipmentItemSlot extends Slot {

    private final DogEquipmentInventory equipment;

    private DogEquipmentItemSlot(
        final DogEquipmentInventory equipment, final int index, final int x, final int y) {
      super(equipment, index, x, y);
      this.equipment = equipment;
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
      return this.equipment.isValid(this.getIndex(), stack);
    }
  }
}
