package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItemTags;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.function.Predicate;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum DogEquipmentSlot {
  ARMOUR(
      "armour",
      null,
      stack ->
          stack.getItem() instanceof AnimalArmorItem armour
              && armour.getType() == AnimalArmorItem.Type.CANINE),
  PENDANT("pendant", "item/empty_slot_pendant", stack -> stack.isIn(ModItemTags.PENDANTS)),
  COSMETIC("cosmetic", "item/empty_slot_cosmetic", stack -> stack.isIn(ModItemTags.DOG_COSMETICS)),
  COSTUME("costume", "item/empty_slot_costume", stack -> stack.isIn(ModItemTags.DOG_COSTUMES)),
  SADDLEBAGS("saddlebags", "item/empty_slot_saddlebags", stack -> stack.isOf(ModItems.SADDLEBAGS));

  private static final String TRANSLATION_KEY_PREFIX = "screen.dogs-unleashed.dog_equipment.slot.";

  private final String id;
  private final @Nullable Identifier emptyIcon;
  private final Predicate<ItemStack> acceptedItems;

  DogEquipmentSlot(
      final String id,
      final @Nullable String emptyIconPath,
      final Predicate<ItemStack> acceptedItems) {
    this.id = id;
    this.emptyIcon =
        emptyIconPath == null ? null : Identifier.of(DogsUnleashed.MOD_ID, emptyIconPath);
    this.acceptedItems = acceptedItems;
  }

  public String id() {
    return this.id;
  }

  public String translationKey() {
    return TRANSLATION_KEY_PREFIX + this.id;
  }

  public String emptyHintTranslationKey() {
    return TRANSLATION_KEY_PREFIX + this.id + ".empty";
  }

  public @Nullable Identifier emptyIcon() {
    return this.emptyIcon;
  }

  public boolean canHold(final ItemStack stack) {
    return !stack.isEmpty() && this.acceptedItems.test(stack);
  }

  public boolean fits(final boolean wearerIsBaby) {
    return this != ARMOUR || !wearerIsBaby;
  }

  public boolean releases(
      final boolean bindingCursed, final boolean creative, final boolean bagsEmpty) {
    return switch (this) {
      case ARMOUR -> !bindingCursed || creative;
      case SADDLEBAGS -> bagsEmpty;
      default -> true;
    };
  }

  public static @Nullable DogEquipmentSlot fromId(final String id) {
    for (final DogEquipmentSlot slot : values()) {
      if (slot.id.equals(id)) {
        return slot;
      }
    }
    return null;
  }

  public static @Nullable DogEquipmentSlot directEquipSlotFor(final ItemStack stack) {
    if (ARMOUR.canHold(stack)) {
      return ARMOUR;
    }
    if (PENDANT.canHold(stack)) {
      return PENDANT;
    }
    if (COSTUME.canHold(stack)) {
      return COSTUME;
    }
    if (SADDLEBAGS.canHold(stack)) {
      return SADDLEBAGS;
    }
    return null;
  }
}
