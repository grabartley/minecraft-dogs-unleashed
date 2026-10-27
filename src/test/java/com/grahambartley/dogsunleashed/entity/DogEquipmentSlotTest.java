package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentSlotTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("no slot accepts an empty stack")
  void noSlotAcceptsEmptyStack(final DogEquipmentSlot slot) {
    assertFalse(slot.canHold(ItemStack.EMPTY));
  }

  @Test
  @DisplayName("an empty hand has no direct-equip target")
  void directEquipRejectsEmptyStack() {
    assertNull(DogEquipmentSlot.directEquipSlotFor(ItemStack.EMPTY));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"minecraft:wolf_armor, ARMOUR", "dogs-unleashed:saddlebags, SADDLEBAGS"})
  @DisplayName("an item that fits a slot equips straight from the hand into that slot")
  void anyFittingItemEquipsFromTheHand(final String itemId, final DogEquipmentSlot slot) {
    final ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemId)));
    assertSame(slot, DogEquipmentSlot.directEquipSlotFor(stack));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"minecraft:stick", "minecraft:bone", "minecraft:iron_horse_armor"})
  @DisplayName("an item no slot holds has no direct-equip target")
  void unfittingItemsHaveNoDirectEquipTarget(final String itemId) {
    final ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemId)));
    assertNull(DogEquipmentSlot.directEquipSlotFor(stack));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("every slot round-trips through its serialized id")
  void idRoundTrip(final DogEquipmentSlot slot) {
    assertSame(slot, DogEquipmentSlot.fromId(slot.id()));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(strings = {"saddle", "ARMOUR", "", "armour "})
  @DisplayName("an unknown serialized id resolves to null")
  void unknownIdResolvesToNull(final String id) {
    assertNull(DogEquipmentSlot.fromId(id));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("every slot exposes distinct label and hint keys under one prefix")
  void translationKeysAreDerivedFromId(final DogEquipmentSlot slot) {
    final String expected = "screen.dogs-unleashed.dog_equipment.slot." + slot.id();
    assertEquals(expected, slot.translationKey());
    assertEquals(expected + ".empty", slot.emptyHintTranslationKey());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "ARMOUR, dogs-unleashed:item/empty_slot_armour",
    "PENDANT, dogs-unleashed:item/empty_slot_pendant",
    "COSMETIC, dogs-unleashed:item/empty_slot_cosmetic",
    "COSTUME, dogs-unleashed:item/empty_slot_costume",
    "SADDLEBAGS, dogs-unleashed:item/empty_slot_saddlebags"
  })
  @DisplayName("every slot shows its own empty icon")
  void emptyIconMatchesTheSlot(final DogEquipmentSlot slot, final String expected) {
    assertEquals(Identifier.of(expected), slot.emptyIcon());
  }

  @ParameterizedTest(name = "{0}, baby {1} -> fits {2}")
  @CsvSource({
    "ARMOUR, false, true",
    "ARMOUR, true, false",
    "PENDANT, false, true",
    "PENDANT, true, true",
    "COSMETIC, false, true",
    "COSMETIC, true, true",
    "COSTUME, false, true",
    "COSTUME, true, true",
    "SADDLEBAGS, false, true",
    "SADDLEBAGS, true, true"
  })
  @DisplayName("only grown dogs can wear armour, while every other slot fits a puppy")
  void onlyArmourNeedsAGrownDog(
      final DogEquipmentSlot slot, final boolean wearerIsBaby, final boolean expected) {
    assertEquals(expected, slot.fits(wearerIsBaby));
  }

  @ParameterizedTest(name = "{0}, cursed {1}, creative {2}, bags empty {3} -> releases {4}")
  @CsvSource({
    "ARMOUR, false, false, true, true",
    "ARMOUR, false, true, true, true",
    "ARMOUR, true, false, true, false",
    "ARMOUR, true, true, true, true",
    "ARMOUR, false, false, false, true",
    "PENDANT, true, false, false, true",
    "COSMETIC, true, false, false, true",
    "COSTUME, true, false, false, true",
    "SADDLEBAGS, false, false, true, true",
    "SADDLEBAGS, false, false, false, false",
    "SADDLEBAGS, false, true, false, false",
    "SADDLEBAGS, true, true, true, true"
  })
  @DisplayName("binding keeps armour on outside creative, and full bags keep the saddlebags on")
  void slotsReleaseOnlyWhenNothingHoldsThem(
      final DogEquipmentSlot slot,
      final boolean bindingCursed,
      final boolean creative,
      final boolean bagsEmpty,
      final boolean expected) {
    assertEquals(expected, slot.releases(bindingCursed, creative, bagsEmpty));
  }
}
