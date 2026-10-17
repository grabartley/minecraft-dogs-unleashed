package com.grahambartley.dogsunleashed.render.overlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PixelGridTextures {

  private static final Logger LOGGER = LoggerFactory.getLogger(PixelGridTextures.class);

  private PixelGridTextures() {}

  public static Optional<PixelGrid> read(final Identifier texture) {
    final Optional<Resource> resource =
        MinecraftClient.getInstance().getResourceManager().getResource(texture);
    if (resource.isEmpty()) {
      return Optional.empty();
    }
    try (InputStream in = resource.get().getInputStream();
        NativeImage image = NativeImage.read(in)) {
      final PixelGrid pixels = PixelGrid.blank(image.getWidth(), image.getHeight());
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          pixels.set(x, y, image.getColor(x, y));
        }
      }
      return Optional.of(pixels);
    } catch (final IOException exception) {
      LOGGER.warn("Unreadable texture {}: {}", texture, exception.getMessage());
      return Optional.empty();
    }
  }

  public static Identifier register(final Identifier id, final PixelGrid pixels) {
    final NativeImage image =
        new NativeImage(NativeImage.Format.RGBA, pixels.width(), pixels.height(), false);
    for (int y = 0; y < pixels.height(); y++) {
      for (int x = 0; x < pixels.width(); x++) {
        image.setColor(x, y, pixels.colourAt(x, y));
      }
    }
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    return id;
  }
}
