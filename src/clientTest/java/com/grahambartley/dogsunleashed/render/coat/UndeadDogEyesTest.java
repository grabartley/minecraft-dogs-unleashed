package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes.EyePixel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UndeadDogEyesTest {

  @ParameterizedTest(name = "{0} resolves to an eye table entry")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every rig source breed has somewhere for its eyes to glow")
  void everyBreedResolvesToAnEntry(final UnleashedDogBreed breed) {
    assertNotNull(UndeadDogEyes.of(breed));
  }

  @ParameterizedTest(name = "{0} has a matched pair of pupils inside its atlas")
  @EnumSource(UndeadDogEyes.class)
  @DisplayName("each rig carries exactly two pupils, both within the atlas")
  void eachRigCarriesTwoPupilsWithinTheAtlas(final UndeadDogEyes eyes) {
    assertEquals(2, eyes.pixels().size(), "a dog has two eyes");
    for (final EyePixel pixel : eyes.pixels()) {
      assertTrue(
          pixel.x() >= 0 && pixel.x() < eyes.atlasSize(),
          pixel + " x out of the " + eyes.atlasSize() + "px atlas");
      assertTrue(
          pixel.y() >= 0 && pixel.y() < eyes.atlasSize(),
          pixel + " y out of the " + eyes.atlasSize() + "px atlas");
    }
  }

  @ParameterizedTest(name = "the flat {0} sheet shares the shared-rig template")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"BEAGLE", "GOLDEN_RETRIEVER", "CROSS_BREED"})
  @DisplayName("breeds authored on the shared UV template resolve to the template entry")
  void templateBreedsShareTheTemplateEntry(final UnleashedDogBreed breed) {
    assertEquals(UndeadDogEyes.TEMPLATE, UndeadDogEyes.of(breed));
  }
}
