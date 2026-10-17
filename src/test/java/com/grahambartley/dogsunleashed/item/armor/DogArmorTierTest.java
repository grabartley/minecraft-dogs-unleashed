package com.grahambartley.dogsunleashed.item.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogArmorTierTest {

  @ParameterizedTest(name = "anchor {0}")
  @ValueSource(ints = {3, 11, 20})
  @DisplayName("leather matches the anchor and each tier protects at least as much as the last")
  void ladderClimbsFromTheAnchor(final int armadillo) {
    assertEquals(armadillo, DogArmorTier.LEATHER.defense(armadillo));
    int previous = 0;
    for (final DogArmorTier tier : DogArmorTier.values()) {
      assertTrue(tier.defense(armadillo) >= previous, tier.id());
      previous = tier.defense(armadillo);
    }
  }

  @Test
  @DisplayName("gold protects like iron but wears out faster")
  void goldTradesDurabilityForShine() {
    assertEquals(DogArmorTier.IRON.defense(11), DogArmorTier.GOLD.defense(11));
    assertTrue(DogArmorTier.GOLD.maxDamage(64) < DogArmorTier.IRON.maxDamage(64));
  }

  @Test
  @DisplayName("only leather takes dye")
  void onlyLeatherIsDyeable() {
    assertEquals(1, Arrays.stream(DogArmorTier.values()).filter(DogArmorTier::dyeable).count());
    assertTrue(DogArmorTier.LEATHER.dyeable());
  }

  @Test
  @DisplayName("item ids follow the dog_armor naming")
  void itemIds() {
    assertEquals("netherite_dog_armor", DogArmorTier.NETHERITE.itemId());
  }
}
