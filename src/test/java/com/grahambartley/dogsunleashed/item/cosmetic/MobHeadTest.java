package com.grahambartley.dogsunleashed.item.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MobHeadTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({
    "zombie_head, ZOMBIE",
    "skeleton_skull, SKELETON",
    "wither_skeleton_skull, WITHER_SKELETON",
    "creeper_head, CREEPER",
    "piglin_head, PIGLIN",
    "dragon_head, DRAGON",
    "carved_pumpkin, CARVED_PUMPKIN"
  })
  @DisplayName("each vanilla head item names its mob head")
  void vanillaItemsNameTheirHead(final String path, final MobHead head) {
    assertEquals(Optional.of(head), MobHead.byItemId(Identifier.ofVanilla(path)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"minecraft:player_head", "minecraft:pumpkin", "dogs-unleashed:party_hat"})
  @DisplayName("other items are not mob heads")
  void otherItemsAreNotHeads(final String id) {
    assertTrue(MobHead.byItemId(Identifier.of(id)).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(MobHead.class)
  @DisplayName("everything but the carved pumpkin is a skull")
  void onlyThePumpkinIsABlock(final MobHead head) {
    assertEquals(head != MobHead.CARVED_PUMPKIN, head.isSkull());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(MobHead.class)
  @DisplayName("only the dragon head opens its jaw on a bark")
  void onlyTheDragonBarks(final MobHead head) {
    assertEquals(head == MobHead.DRAGON, head.opensJawOnBark());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(MobHead.class)
  @DisplayName("every head but the long dragon head stretches back over the muzzle")
  void onlyTheDragonKeepsItsLength(final MobHead head) {
    assertEquals(head != MobHead.DRAGON, head.stretchesOverTheMuzzle());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(MobHead.class)
  @DisplayName("every head is a vanilla item")
  void everyHeadIsVanilla(final MobHead head) {
    assertEquals("minecraft", head.itemId().getNamespace());
    assertEquals(
        1, Arrays.stream(MobHead.values()).filter(h -> h.itemId().equals(head.itemId())).count());
  }
}
