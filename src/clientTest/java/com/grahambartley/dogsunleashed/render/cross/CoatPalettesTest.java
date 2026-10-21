package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.BeagleCoat;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoatPalettesTest {

  private static final List<UnleashedDogBreed> CROSSING_BREEDS =
      Arrays.stream(UnleashedDogBreed.values())
          .filter(breed -> breed != UnleashedDogBreed.CROSS_BREED)
          .filter(breed -> !breed.nature().breedsOnlyWithOwnBreed())
          .toList();

  static Stream<Arguments> shippedCoats() {
    final List<Arguments> coats = new ArrayList<>();
    for (final UnleashedDogBreed breed : CROSSING_BREEDS) {
      final List<UnleashedDogCoat> breedCoats = DogCoats.coatsOf(breed);
      if (breedCoats.isEmpty()) {
        coats.add(Arguments.of(breed, null));
      }
      breedCoats.forEach(coat -> coats.add(Arguments.of(breed, coat)));
    }
    return coats.stream();
  }

  static Stream<Arguments> names() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.BEAGLE, BeagleCoat.TRI_1, "beagle_tri1"),
        Arguments.of(
            UnleashedDogBreed.HUSKY,
            HuskyCoat.values()[0],
            "husky_" + HuskyCoat.values()[0].getTexturePrefix()),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, null, "goldenretriever"));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("names")
  @DisplayName("a palette is named after its coat, without any eye colour")
  void paletteIsNamedAfterItsCoat(
      final UnleashedDogBreed breed, final UnleashedDogCoat coat, final String name) {
    assertEquals(
        Identifier.of("dogs-unleashed", "textures/entity/palette/" + name + ".png"),
        CoatPalettes.idOf(breed, coat));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("shippedCoats")
  @DisplayName("every coat a cross can inherit from ships a four by one palette")
  void everyCoatShipsAPalette(final UnleashedDogBreed breed, final UnleashedDogCoat coat)
      throws IOException {
    final Identifier palette = CoatPalettes.idOf(breed, coat);
    try (InputStream stream =
        CoatPalettesTest.class.getResourceAsStream(
            "/assets/" + palette.getNamespace() + "/" + palette.getPath())) {
      assertNotNull(stream, palette + " is missing");
      final BufferedImage image = ImageIO.read(stream);
      assertEquals(CoatPalette.SIZE, image.getWidth(), palette.toString());
      assertEquals(1, image.getHeight(), palette.toString());
    }
  }

  @Test
  @DisplayName("each other breed pulls with its share as a percent, and a pure dog pulls nothing")
  void pullsCarryTheShares() {
    final CrossBreedLook cross =
        CrossBreedLook.of(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.625f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.375f)),
            0);
    final CrossBreedLook pure =
        CrossBreedLook.of(List.of(new BreedShare(UnleashedDogBreed.BEAGLE, 1.0f)), 0);

    assertEquals(
        List.of(
            new PaletteWeight(
                Identifier.of("dogs-unleashed", "textures/entity/palette/goldenretriever.png"),
                38)),
        CoatPalettes.pullsOf(cross));
    assertEquals(List.of(), CoatPalettes.pullsOf(pure));
  }
}
