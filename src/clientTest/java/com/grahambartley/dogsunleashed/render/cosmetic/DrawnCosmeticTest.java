package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DrawnCosmeticTest {

  private static final Optional<DogCosmetic> HAT = Optional.of(DogCosmetic.STRAW_HAT);

  @Test
  @DisplayName("a worn cosmetic with a loaded model is drawn")
  void wornAndLoadedIsDrawn() {
    assertEquals(HAT, DrawnCosmetic.of(false, HAT, id -> true));
  }

  @Test
  @DisplayName("nothing is drawn on an invisible dog")
  void invisibleDrawsNothing() {
    assertTrue(DrawnCosmetic.of(true, HAT, id -> true).isEmpty());
  }

  @Test
  @DisplayName("a cosmetic whose model is missing is not drawn")
  void missingModelDrawsNothing() {
    assertTrue(DrawnCosmetic.of(false, HAT, id -> false).isEmpty());
  }

  @Test
  @DisplayName("the model checked is the worn cosmetic's own")
  void checksTheWornModel() {
    assertEquals(
        HAT,
        DrawnCosmetic.of(false, HAT, id -> id.equals(CosmeticAssets.model(DogCosmetic.STRAW_HAT))));
  }

  @Test
  @DisplayName("another cosmetic's model being loaded does not draw the worn one")
  void otherModelsDoNotCount() {
    assertTrue(
        DrawnCosmetic.of(
                false,
                Optional.of(DogCosmetic.BANDANA),
                id -> id.equals(CosmeticAssets.model(DogCosmetic.STRAW_HAT)))
            .isEmpty());
  }

  @Test
  @DisplayName("no cosmetic worn means nothing drawn")
  void nothingWorn() {
    assertTrue(DrawnCosmetic.of(false, Optional.empty(), id -> true).isEmpty());
  }

  @Test
  @DisplayName("an invisible dog draws no mob head")
  void invisibleDogDrawsNoMobHead() {
    assertTrue(DrawnCosmetic.mobHeadOf(true, Optional.of(MobHead.CREEPER)).isEmpty());
  }

  @Test
  @DisplayName("a visible dog draws the mob head it wears")
  void visibleDogDrawsItsMobHead() {
    assertEquals(
        Optional.of(MobHead.CREEPER), DrawnCosmetic.mobHeadOf(false, Optional.of(MobHead.CREEPER)));
  }
}
