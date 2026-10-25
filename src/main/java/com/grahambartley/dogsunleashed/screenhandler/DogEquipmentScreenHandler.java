package com.grahambartley.dogsunleashed.screenhandler;

import com.grahambartley.dogsunleashed.ModScreenHandlers;
import com.grahambartley.dogsunleashed.advancement.DogMilestone;
import com.grahambartley.dogsunleashed.advancement.DogMilestoneCriterion;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.screenhandler.DogGearSlotRouting.Section;
import com.grahambartley.dogsunleashed.screenhandler.DogGearSlotRouting.Target;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DogEquipmentScreenHandler extends ScreenHandler {

  public static final int MAX_INTERACTION_DISTANCE = 8;
  public static final int SLOT_SPACING = DogSlotLayout.SLOT_SPACING;
  public static final int PLAYER_INVENTORY_X = 8;
  public static final int PLAYER_INVENTORY_Y = 84;
  public static final int HOTBAR_Y = 142;

  private static final int PLAYER_INVENTORY_ROWS = 3;
  private static final int PLAYER_INVENTORY_COLUMNS = 9;

  public static final DogGearSlotRouting ROUTING =
      new DogGearSlotRouting(
          DogSaddlebags.SIZE,
          DogEquipmentSlot.values().length,
          PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS,
          PLAYER_INVENTORY_COLUMNS);
  public static final int BAG_SLOT_START = ROUTING.bagStart();

  private final @Nullable UnleashedDogEntity dog;
  private final boolean ownerView;

  public DogEquipmentScreenHandler(
      final int syncId, final PlayerInventory playerInventory, final int dogEntityId) {
    this(syncId, playerInventory, resolveDog(playerInventory, dogEntityId));
  }

  public DogEquipmentScreenHandler(
      final int syncId,
      final PlayerInventory playerInventory,
      final @Nullable UnleashedDogEntity dog) {
    super(ModScreenHandlers.DOG_EQUIPMENT, syncId);
    this.dog = dog;
    this.ownerView = dog != null && dog.isOwner(playerInventory.player);

    final Inventory bags =
        dog == null
            ? new SimpleInventory(DogSaddlebags.SIZE)
            : dog.getComponents().get(DogSaddlebags.class).contents();
    for (int index = 0; index < DogSaddlebags.SIZE; index++) {
      this.addSlot(
          new BagSlot(
              bags,
              index,
              DogSlotLayout.bagX(index),
              DogSlotLayout.bagY(index),
              dog,
              this.ownerView));
    }
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      this.addSlot(
          new DogEquipmentItemSlot(
              new DogEquipmentInventory(dog, slot),
              DogSlotLayout.x(slot.ordinal()),
              DogSlotLayout.y(slot.ordinal()),
              this.ownerView,
              playerInventory.player));
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

  public static int gearSlotIndex(final DogEquipmentSlot slot) {
    return ROUTING.gearStart() + slot.ordinal();
  }

  public @Nullable UnleashedDogEntity dog() {
    return this.dog;
  }

  public boolean isOwnerView() {
    return this.ownerView;
  }

  public boolean bagsOpen() {
    final UnleashedDogEntity dog = this.dog();
    return dog != null && dog.getComponents().get(DogSaddlebags.class).isWorn();
  }

  public static @Nullable DogEquipmentSlot gearSlotAt(final int index) {
    return ROUTING.sectionOf(index) == Section.GEAR
        ? DogEquipmentSlot.values()[index - ROUTING.gearStart()]
        : null;
  }

  @Override
  public boolean canUse(final PlayerEntity player) {
    return this.dog != null
        && this.dog.isAlive()
        && this.dog.isTamed()
        && (this.dog.isOwner(player) || DogDeliveries.isWaitingFor(this.dog, player))
        && player.canInteractWithEntity(this.dog, MAX_INTERACTION_DISTANCE);
  }

  @Override
  public void onClosed(final PlayerEntity player) {
    super.onClosed(player);
    final UnleashedDogEntity dog = this.dog();
    if (dog != null && !this.ownerView) {
      DogDeliveries.handedOver(dog, player);
    }
  }

  @Override
  public ItemStack quickMove(final PlayerEntity player, final int index) {
    final Slot source = this.slots.get(index);
    if (source == null || !source.hasStack()) {
      return ItemStack.EMPTY;
    }

    final ItemStack moving = source.getStack();
    final ItemStack original = moving.copy();
    boolean moved = false;
    for (final Target target :
        DogGearSlotRouting.targetsFor(ROUTING.sectionOf(index), this.ownerView, this.bagsOpen())) {
      if (this.moveInto(target, moving)) {
        moved = true;
        break;
      }
    }
    if (!moved) {
      return ItemStack.EMPTY;
    }

    if (moving.isEmpty()) {
      source.setStack(ItemStack.EMPTY);
    } else {
      source.markDirty();
    }
    return original;
  }

  private boolean moveInto(final Target target, final ItemStack stack) {
    return switch (target) {
      case PLAYER -> this.insertItem(stack, ROUTING.mainStart(), ROUTING.total(), true);
      case GEAR -> this.insertIntoDogSlots(stack);
      case BAGS -> this.insertItem(stack, ROUTING.bagStart(), ROUTING.gearStart(), false);
      case HOTBAR -> this.insertItem(stack, ROUTING.hotbarStart(), ROUTING.total(), false);
      case MAIN -> this.insertItem(stack, ROUTING.mainStart(), ROUTING.hotbarStart(), false);
    };
  }

  private boolean insertIntoDogSlots(final ItemStack stack) {
    for (int index = ROUTING.gearStart(); index < ROUTING.mainStart(); index++) {
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
    private final boolean ownerView;
    private final PlayerEntity viewer;

    private DogEquipmentItemSlot(
        final DogEquipmentInventory equipment,
        final int x,
        final int y,
        final boolean ownerView,
        final PlayerEntity viewer) {
      super(equipment, 0, x, y);
      this.equipment = equipment;
      this.ownerView = ownerView;
      this.viewer = viewer;
    }

    @Override
    public void setStack(final ItemStack stack, final ItemStack previousStack) {
      super.setStack(stack, previousStack);
      if (!stack.isEmpty()) {
        DogMilestoneCriterion.INSTANCE.trigger(this.viewer, DogMilestone.GEAR_EQUIPPED);
      }
    }

    @Override
    public boolean isEnabled() {
      return this.ownerView;
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
      return this.ownerView && this.equipment.isValid(this.getIndex(), stack);
    }

    @Override
    public boolean canTakeItems(final PlayerEntity player) {
      return this.ownerView && this.equipment.canTake(player);
    }

    @Override
    public @Nullable Pair<Identifier, Identifier> getBackgroundSprite() {
      final Identifier icon = this.equipment.slot().emptyIcon();
      return icon == null ? null : Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, icon);
    }
  }

  private static final class BagSlot extends Slot {

    private final @Nullable UnleashedDogEntity dog;
    private final boolean ownerView;

    private BagSlot(
        final Inventory bags,
        final int index,
        final int x,
        final int y,
        final @Nullable UnleashedDogEntity dog,
        final boolean ownerView) {
      super(bags, index, x, y);
      this.dog = dog;
      this.ownerView = ownerView;
    }

    private boolean worn() {
      return this.dog != null && this.dog.getComponents().get(DogSaddlebags.class).isWorn();
    }

    @Override
    public boolean isEnabled() {
      return this.worn();
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
      return this.ownerView && this.worn();
    }

    @Override
    public boolean canTakeItems(final PlayerEntity player) {
      return this.worn();
    }
  }
}
