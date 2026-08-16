package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Item-backed predicate coverage lives in {@code DogEquipmentGameTest} instead: asserting against
 * real {@code Items} constants would require running {@code Items.<clinit>} on the unit-test
 * classpath, which fails verification under Yarn-mapped jars.
 */
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
  @DisplayName("every slot exposes distinct label, hint, and glyph keys under one prefix")
  void translationKeysAreDerivedFromId(final DogEquipmentSlot slot) {
    final String expected = "screen.dogs-unleashed.dog_equipment.slot." + slot.id();
    assertEquals(expected, slot.translationKey());
    assertEquals(expected + ".empty", slot.emptyHintTranslationKey());
    assertEquals(expected + ".glyph", slot.glyphTranslationKey());
  }
}
