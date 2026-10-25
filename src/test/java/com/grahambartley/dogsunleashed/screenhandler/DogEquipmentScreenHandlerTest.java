package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.screenhandler.DogGearSlotRouting.Section;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentScreenHandlerTest {

  private static final DogGearSlotRouting ROUTING = DogEquipmentScreenHandler.ROUTING;

  static IntStream nonGearSlots() {
    return IntStream.concat(
        IntStream.range(0, ROUTING.gearStart()),
        IntStream.of(ROUTING.mainStart(), ROUTING.hotbarStart(), ROUTING.total() - 1));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("every gear slot sits in the gear section and maps back to itself")
  void gearSlotIndexRoundTrips(final DogEquipmentSlot slot) {
    final int index = DogEquipmentScreenHandler.gearSlotIndex(slot);
    assertEquals(Section.GEAR, ROUTING.sectionOf(index));
    assertSame(slot, DogEquipmentScreenHandler.gearSlotAt(index));
  }

  @ParameterizedTest(name = "slot {0}")
  @MethodSource("nonGearSlots")
  @DisplayName("bag and player slots are not gear")
  void nonGearSlotsHaveNoGearSlot(final int index) {
    assertNull(DogEquipmentScreenHandler.gearSlotAt(index));
  }

  @Test
  @DisplayName("the bags start at the very first slot, so sort mods find them")
  void bagsStartAtTheFirstSlot() {
    assertEquals(0, DogEquipmentScreenHandler.BAG_SLOT_START);
    assertEquals(Section.BAGS, ROUTING.sectionOf(DogEquipmentScreenHandler.BAG_SLOT_START));
  }
}
