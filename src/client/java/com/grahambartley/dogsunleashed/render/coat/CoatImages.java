package com.grahambartley.dogsunleashed.render.coat;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CoatImages {

  public record Source(NativeImage image, boolean owned) implements AutoCloseable {
    @Override
    public void close() {
      if (this.owned) {
        this.image.close();
      }
    }
  }

  private CoatImages() {}

  public static @Nullable Source read(final Identifier texture) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null) {
      return null;
    }
    final Resource resource = client.getResourceManager().getResource(texture).orElse(null);
    if (resource != null) {
      try (InputStream stream = resource.getInputStream()) {
        return new Source(NativeImage.read(stream), true);
      } catch (final IOException exception) {
        return null;
      }
    }
    if (client.getTextureManager().getTexture(texture)
            instanceof NativeImageBackedTexture dynamicTexture
        && dynamicTexture.getImage() != null) {
      return new Source(dynamicTexture.getImage(), false);
    }
    return null;
  }

  public static Identifier register(final Identifier id, final NativeImage image) {
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    return id;
  }

  public static int abgr(final int a, final int r, final int g, final int b) {
    return (a << 24) | (b << 16) | (g << 8) | r;
  }

  public static int alpha(final int abgr) {
    return (abgr >>> 24) & 0xFF;
  }

  public static int red(final int abgr) {
    return abgr & 0xFF;
  }

  public static int green(final int abgr) {
    return (abgr >>> 8) & 0xFF;
  }

  public static int blue(final int abgr) {
    return (abgr >>> 16) & 0xFF;
  }
}
