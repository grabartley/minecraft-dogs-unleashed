package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RopeToyModelTest {

  private final RopeToyModel model = new RopeToyModel();

  @ParameterizedTest(name = "bone {0}")
  @ValueSource(strings = {"rope_toy"})
  @DisplayName("the model points at the shipped rope toy geometry")
  void geometryIsShipped(final String bone) {
    assertTrue(
        ShippedRigs.modelAt(this.model.getModelResource(null).getPath()).getBone(bone).isPresent(),
        "missing bone " + bone);
  }

  @ParameterizedTest(name = "{0}x{0} texture")
  @ValueSource(ints = {64})
  @DisplayName("the model points at the shipped rope toy texture, sized for its UVs")
  void textureIsShipped(final int size) throws IOException {
    try (InputStream in =
        RopeToyModelTest.class.getResourceAsStream(
            "/assets/dogs-unleashed/" + this.model.getTextureResource(null).getPath())) {
      assertNotNull(in, "texture is missing");
      final BufferedImage image = ImageIO.read(in);
      assertEquals(size, image.getWidth());
      assertEquals(size, image.getHeight());
    }
  }
}
