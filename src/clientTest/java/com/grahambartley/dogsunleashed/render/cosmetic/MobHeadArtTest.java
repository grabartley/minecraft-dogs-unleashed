package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class MobHeadArtTest {

  @ParameterizedTest(name = "{0} on {1}")
  @CsvSource({
    "CARVED_PUMPKIN, BEAGLE, carved_pumpkin/beagle",
    "CARVED_PUMPKIN, DACHSHUND, carved_pumpkin/dachshund",
    "CARVED_PUMPKIN, GOLDEN_RETRIEVER, carved_pumpkin/goldenretriever",
    "CARVED_PUMPKIN, SHIBA_INU, carved_pumpkin/shibainu",
    "CARVED_PUMPKIN, HUSKY, carved_pumpkin/husky",
    "CREEPER, BEAGLE, creeper_head/beagle"
  })
  @DisplayName("art is named after the head item and the breed")
  void namedAfterHeadAndBreed(
      final MobHead head, final UnleashedDogBreed breed, final String path) {
    final MobHeadArt art = MobHeadArt.named(head, breed);

    assertEquals("geo/cosmetic/" + path + ".geo.json", art.model().getPath());
    assertEquals("textures/entity/cosmetic/" + path + ".png", art.texture().getPath());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"BEAGLE", "DACHSHUND", "GOLDEN_RETRIEVER", "SHIBA_INU"})
  @DisplayName("a head with a loaded model for the breed has art")
  void loadedModelHasArt(final UnleashedDogBreed breed) {
    final MobHeadArt expected = MobHeadArt.named(MobHead.CARVED_PUMPKIN, breed);

    assertEquals(
        Optional.of(expected),
        MobHeadArt.of(MobHead.CARVED_PUMPKIN, breed, expected.model()::equals));
  }

  @Test
  @DisplayName("a breed without a model for the head has no art")
  void missingModelHasNoArt() {
    final Identifier dachshundPumpkin =
        MobHeadArt.named(MobHead.CARVED_PUMPKIN, UnleashedDogBreed.DACHSHUND).model();

    assertTrue(
        MobHeadArt.of(MobHead.CARVED_PUMPKIN, UnleashedDogBreed.HUSKY, dachshundPumpkin::equals)
            .isEmpty());
    assertTrue(
        MobHeadArt.of(MobHead.CREEPER, UnleashedDogBreed.DACHSHUND, dachshundPumpkin::equals)
            .isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"BEAGLE", "DACHSHUND", "GOLDEN_RETRIEVER", "SHIBA_INU"})
  @DisplayName("a breed with drawn art ships its carved pumpkin")
  void drawnBreedShipsItsPumpkin(final UnleashedDogBreed breed) {
    final MobHeadArt art = MobHeadArt.named(MobHead.CARVED_PUMPKIN, breed);

    assertNotNull(resource(art.model()));
    assertNotNull(resource(art.texture()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("shipped art has its texture and rides bones its breed's rig has")
  void shippedArtIsComplete(final UnleashedDogBreed breed) {
    for (final MobHead head : MobHead.values()) {
      final MobHeadArt art = MobHeadArt.named(head, breed);
      if (resource(art.model()) == null) {
        continue;
      }
      assertNotNull(resource(art.texture()), art.texture() + " is missing");
      final BakedGeoModel rig = ShippedRigs.model(breed.serializedId());
      final BakedGeoModel drawn = ShippedRigs.modelAt(art.model().getPath());
      for (final GeoBone part : drawn.topLevelBones()) {
        assertTrue(
            rig.getBone(part.getName()).isPresent(),
            art.model() + " rides " + part.getName() + ", which " + breed + " has no bone for");
      }
    }
  }

  private static Object resource(final Identifier id) {
    return MobHeadArtTest.class.getResource("/assets/" + id.getNamespace() + "/" + id.getPath());
  }
}
