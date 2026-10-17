package com.grahambartley.dogsunleashed.render.rig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public record CollarTextureMask(int width, int height, boolean[] paintedTexels)
    implements CollarBandLocator.CollarMask {

  public static Optional<CollarTextureMask> load(
      final ResourceManager resources, final Identifier texture) {
    return resources
        .getResource(texture)
        .flatMap(
            resource -> {
              try (InputStream stream = resource.getInputStream();
                  NativeImage image = NativeImage.read(stream)) {
                final boolean[] painted = new boolean[image.getWidth() * image.getHeight()];
                for (int y = 0; y < image.getHeight(); y++) {
                  for (int x = 0; x < image.getWidth(); x++) {
                    painted[y * image.getWidth() + x] = (image.getColor(x, y) >>> 24) != 0;
                  }
                }
                return Optional.of(
                    new CollarTextureMask(image.getWidth(), image.getHeight(), painted));
              } catch (final IOException exception) {
                return Optional.empty();
              }
            });
  }

  @Override
  public boolean painted(final int x, final int y) {
    return this.paintedTexels[y * this.width + x];
  }
}
