package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentScreenHandlerTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("every gear slot sits after the bags and maps back to itself")
  void gearSlotIndexRoundTrips(final DogEquipmentSlot slot) {
    final int index = DogEquipmentScreenHandler.gearSlotIndex(slot);
    assertEquals(DogSaddlebags.SIZE + slot.ordinal(), index);
    assertSame(slot, DogEquipmentScreenHandler.gearSlotAt(index));
  }

  @ParameterizedTest(name = "slot {0}")
  @ValueSource(ints = {0, 8, 14, 40, 41, 49})
  @DisplayName("bag and player slots are not gear")
  void nonGearSlotsHaveNoGearSlot(final int index) {
    assertNull(DogEquipmentScreenHandler.gearSlotAt(index));
  }

  @ParameterizedTest(name = "bag {0}")
  @ValueSource(ints = {0, 4, 8})
  @DisplayName("the bag slots start at the very first slot")
  void bagsStartAtZero(final int bag) {
    assertEquals(
        DogGearSlotRouting.Section.BAGS,
        DogEquipmentScreenHandler.ROUTING.sectionOf(
            DogEquipmentScreenHandler.BAG_SLOT_START + bag));
    assertEquals(0, DogEquipmentScreenHandler.BAG_SLOT_START);
  }
}
