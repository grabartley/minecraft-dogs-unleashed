package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModItemTags;
import java.util.function.Predicate;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum DogEquipmentSlot {
  ARMOUR(
      "armour",
      stack ->
          stack.getItem() instanceof AnimalArmorItem armour
              && armour.getType() == AnimalArmorItem.Type.CANINE),
  PENDANT("pendant", stack -> stack.isIn(ModItemTags.PENDANTS)),
  COSMETIC("cosmetic", stack -> stack.isIn(ModItemTags.DOG_COSMETICS));

  private static final String TRANSLATION_KEY_PREFIX = "screen.dogs-unleashed.dog_equipment.slot.";

  private final String id;
  private final Predicate<ItemStack> acceptedItems;

  DogEquipmentSlot(final String id, final Predicate<ItemStack> acceptedItems) {
    this.id = id;
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

  public String glyphTranslationKey() {
    return TRANSLATION_KEY_PREFIX + this.id + ".glyph";
  }

  public boolean canHold(final ItemStack stack) {
    return !stack.isEmpty() && this.acceptedItems.test(stack);
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
    return null;
  }
}
