package com.grahambartley.dogsunleashed.render.rig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollarTextureMask implements CollarBandLocator.CollarMask {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollarTextureMask.class);

  private final int width;
  private final int height;
  private final boolean[] paintedTexels;

  private CollarTextureMask(final int width, final int height, final boolean[] paintedTexels) {
    this.width = width;
    this.height = height;
    this.paintedTexels = paintedTexels;
  }

  public static Optional<CollarTextureMask> load(
      final ResourceManager resources, final Identifier texture) {
    final Optional<Resource> resource = resources.getResource(texture);
    if (resource.isEmpty()) {
      LOGGER.warn("Collar texture {} is missing, pendants will not show", texture);
      return Optional.empty();
    }
    try (InputStream stream = resource.get().getInputStream();
        NativeImage image = NativeImage.read(stream)) {
      final boolean[] painted = new boolean[image.getWidth() * image.getHeight()];
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          painted[y * image.getWidth() + x] = (image.getColor(x, y) >>> 24) != 0;
        }
      }
      return Optional.of(new CollarTextureMask(image.getWidth(), image.getHeight(), painted));
    } catch (final IOException exception) {
      LOGGER.warn("Could not read collar texture {}, pendants will not show", texture, exception);
      return Optional.empty();
    }
  }

  @Override
  public int width() {
    return this.width;
  }

  @Override
  public int height() {
    return this.height;
  }

  @Override
  public boolean painted(final int x, final int y) {
    return this.paintedTexels[y * this.width + x];
  }
}
