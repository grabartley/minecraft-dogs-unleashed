package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogEquipmentHolder.DirectEquipOutcome;
import java.util.stream.Stream;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentHolderTest {

  static Stream<Arguments> directEquipOutcomes() {
    return Stream.of(
        Arguments.of(
            "shears on a dog wearing armour",
            true,
            true,
            null,
            DirectEquipOutcome.SHEAR_OFF_ARMOUR),
        Arguments.of("shears on a bare dog", true, false, null, DirectEquipOutcome.IGNORED),
        Arguments.of(
            "shears win over any resolved slot",
            true,
            false,
            DogEquipmentSlot.PENDANT,
            DirectEquipOutcome.IGNORED),
        Arguments.of("an item no slot accepts", false, false, null, DirectEquipOutcome.IGNORED),
        Arguments.of(
            "an item no slot accepts, armour already worn",
            false,
            true,
            null,
            DirectEquipOutcome.IGNORED),
        Arguments.of(
            "armour onto a bare dog",
            false,
            false,
            DogEquipmentSlot.ARMOUR,
            DirectEquipOutcome.EQUIP),
        Arguments.of(
            "armour swapped for worn armour",
            false,
            true,
            DogEquipmentSlot.ARMOUR,
            DirectEquipOutcome.EQUIP),
        Arguments.of(
            "a pendant onto an armoured dog",
            false,
            true,
            DogEquipmentSlot.PENDANT,
            DirectEquipOutcome.EQUIP));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("directEquipOutcomes")
  @DisplayName("a direct right-click resolves to one outcome from the held item and worn armour")
  void directEquipOutcomeResolvesFromState(
      final String label,
      final boolean holdingShears,
      final boolean armourEquipped,
      final DogEquipmentSlot targetSlot,
      final DirectEquipOutcome expected) {
    assertEquals(
        expected, DogEquipmentHolder.directEquipOutcome(holdingShears, armourEquipped, targetSlot));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEquipmentSlot.class)
  @DisplayName("any resolved slot equips, whether or not armour is already worn")
  void everyResolvedSlotEquips(final DogEquipmentSlot slot) {
    assertEquals(
        DirectEquipOutcome.EQUIP, DogEquipmentHolder.directEquipOutcome(false, false, slot));
    assertEquals(
        DirectEquipOutcome.EQUIP, DogEquipmentHolder.directEquipOutcome(false, true, slot));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(EquipmentSlot.class)
  @DisplayName("the body slot is the only vanilla slot a dog claims for its armour")
  void onlyTheBodySlotIsTheArmourSlot(final EquipmentSlot slot) {
    if (slot == EquipmentSlot.BODY) {
      assertTrue(DogEquipmentHolder.isArmourSlot(slot));
    } else {
      assertFalse(DogEquipmentHolder.isArmourSlot(slot));
    }
  }
}
