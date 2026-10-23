package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogMilestone;
import com.grahambartley.dogsunleashed.advancement.DogMilestoneCriterion;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public final class DogEquipmentHolder {

  private static final float GUARANTEED_ARMOUR_DROP_CHANCE = 2.0f;

  private static final PersistedSlot[] MOD_OWNED_SLOTS = {
    new PersistedSlot(DogEquipmentSlot.PENDANT, ModNbtKeys.PENDANT_ITEM),
    new PersistedSlot(DogEquipmentSlot.COSMETIC, ModNbtKeys.COSMETIC_ITEM),
    new PersistedSlot(DogEquipmentSlot.COSTUME, ModNbtKeys.COSTUME_ITEM),
    new PersistedSlot(DogEquipmentSlot.SADDLEBAGS, ModNbtKeys.SADDLEBAGS_ITEM)
  };

  private final UnleashedDogEntity dog;

  DogEquipmentHolder(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  private record PersistedSlot(DogEquipmentSlot slot, String nbtKey) {}

  public static boolean isArmourSlot(final EquipmentSlot slot) {
    return slot == EquipmentSlot.BODY;
  }

  void guaranteeArmourDrop() {
    this.dog.setEquipmentDropChance(EquipmentSlot.BODY, GUARANTEED_ARMOUR_DROP_CHANCE);
  }

  public ItemStack getStack(final DogEquipmentSlot slot) {
    return switch (slot) {
      case ARMOUR -> this.dog.getEquippedStack(EquipmentSlot.BODY);
      case PENDANT -> this.dog.getDataTracker().get(UnleashedDogEntity.PENDANT_ITEM);
      case COSMETIC -> this.dog.getDataTracker().get(UnleashedDogEntity.COSMETIC_ITEM);
      case COSTUME -> this.dog.getDataTracker().get(UnleashedDogEntity.COSTUME_ITEM);
      case SADDLEBAGS -> this.dog.getDataTracker().get(UnleashedDogEntity.SADDLEBAGS_ITEM);
    };
  }

  public void setStack(final DogEquipmentSlot slot, final ItemStack stack) {
    switch (slot) {
      case ARMOUR -> this.dog.equipStack(EquipmentSlot.BODY, stack);
      case PENDANT -> this.dog.getDataTracker().set(UnleashedDogEntity.PENDANT_ITEM, stack);
      case COSMETIC -> this.dog.getDataTracker().set(UnleashedDogEntity.COSMETIC_ITEM, stack);
      case COSTUME -> this.dog.getDataTracker().set(UnleashedDogEntity.COSTUME_ITEM, stack);
      case SADDLEBAGS -> this.dog.getDataTracker().set(UnleashedDogEntity.SADDLEBAGS_ITEM, stack);
    }
  }

  ScreenHandler createMenu(final int syncId, final PlayerInventory playerInventory) {
    return new DogEquipmentScreenHandler(syncId, playerInventory, this.dog);
  }

  void dropModOwnedSlots() {
    this.saddlebags().dropContents();
    for (final PersistedSlot owned : MOD_OWNED_SLOTS) {
      final ItemStack stack = this.getStack(owned.slot());
      if (!stack.isEmpty()) {
        this.dog.dropStack(stack);
        this.setStack(owned.slot(), ItemStack.EMPTY);
      }
    }
  }

  public boolean fits(final DogEquipmentSlot slot) {
    return slot.fits(this.dog.isBaby());
  }

  public boolean releases(final DogEquipmentSlot slot, final PlayerEntity player) {
    return slot.releases(
        DogArmour.isBindingCursed(this.getStack(slot)),
        player.isCreative(),
        this.saddlebags().isEmpty());
  }

  private DogSaddlebags saddlebags() {
    return this.dog.getComponents().get(DogSaddlebags.class);
  }

  @Nullable
  ActionResult tryDirectEquip(final PlayerEntity player, final ItemStack heldStack) {
    final DogEquipmentSlot targetSlot = DogEquipmentSlot.directEquipSlotFor(heldStack);
    if (targetSlot == null || !this.fits(targetSlot) || !this.releases(targetSlot, player)) {
      return null;
    }
    return this.equipFromHand(player, targetSlot, heldStack);
  }

  private ActionResult equipFromHand(
      final PlayerEntity player, final DogEquipmentSlot slot, final ItemStack heldStack) {
    final ItemStack previous = this.getStack(slot);
    this.setStack(slot, heldStack.copyWithCount(1));
    DogMilestoneCriterion.INSTANCE.trigger(player, DogMilestone.GEAR_EQUIPPED);
    heldStack.decrementUnlessCreative(1, player);
    if (!previous.isEmpty()) {
      player.giveItemStack(previous);
    }
    if (slot != DogEquipmentSlot.ARMOUR) {
      this.dog.playSoundIfNotSilent(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value());
    }
    return ActionResult.SUCCESS;
  }

  void writeNbt(final NbtCompound nbt) {
    for (final PersistedSlot owned : MOD_OWNED_SLOTS) {
      final ItemStack stack = this.getStack(owned.slot());
      if (stack.isEmpty()) {
        continue;
      }
      ItemStack.CODEC
          .encodeStart(this.dog.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE), stack)
          .result()
          .ifPresent(tag -> nbt.put(owned.nbtKey(), tag));
    }
  }

  void readNbt(final NbtCompound nbt) {
    for (final PersistedSlot owned : MOD_OWNED_SLOTS) {
      if (!nbt.contains(owned.nbtKey())) {
        continue;
      }
      ItemStack.CODEC
          .parse(
              this.dog.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE),
              nbt.get(owned.nbtKey()))
          .result()
          .ifPresent(stack -> this.setStack(owned.slot(), stack));
    }
  }
}
