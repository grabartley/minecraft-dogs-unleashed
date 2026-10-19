package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEquipmentHolderTest {

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
