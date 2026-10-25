package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentInventoryTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("each gear inventory holds exactly one slot, so sorting it cannot move gear")
  void holdsOneSlot(final DogEquipmentSlot slot) {
    final DogEquipmentInventory inventory = new DogEquipmentInventory(null, slot);
    assertEquals(1, inventory.size());
    assertEquals(1, inventory.getMaxCountPerStack());
    assertSame(slot, inventory.slot());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("saddlebags are valid only in the saddlebags slot")
  void saddlebagsAreValidOnlyInTheirSlot(final DogEquipmentSlot slot) {
    final DogEquipmentInventory inventory = new DogEquipmentInventory(null, slot);
    assertEquals(
        slot == DogEquipmentSlot.SADDLEBAGS,
        inventory.isValid(0, new ItemStack(ModItems.SADDLEBAGS)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("wolf armour is valid only in the armour slot")
  void wolfArmourIsValidOnlyInTheArmourSlot(final DogEquipmentSlot slot) {
    final DogEquipmentInventory inventory = new DogEquipmentInventory(null, slot);
    assertEquals(
        slot == DogEquipmentSlot.ARMOUR, inventory.isValid(0, new ItemStack(Items.WOLF_ARMOR)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("no gear slot takes a plain item or an empty stack")
  void plainItemsAndEmptyStacksAreNeverValid(final DogEquipmentSlot slot) {
    final DogEquipmentInventory inventory = new DogEquipmentInventory(null, slot);
    assertFalse(inventory.isValid(0, new ItemStack(Items.BREAD)));
    assertFalse(inventory.isValid(0, ItemStack.EMPTY));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("without a dog the inventory stays empty and ignores writes")
  void withoutADogNothingIsStored(final DogEquipmentSlot slot) {
    final DogEquipmentInventory inventory = new DogEquipmentInventory(null, slot);
    inventory.setStack(0, new ItemStack(ModItems.SADDLEBAGS));
    assertTrue(inventory.isEmpty());
    assertTrue(inventory.getStack(0).isEmpty());
    assertTrue(inventory.removeStack(0).isEmpty());
  }
}
