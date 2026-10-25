package com.grahambartley.dogsunleashed.render.portrait;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import com.grahambartley.dogsunleashed.render.coat.CoatImages;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PortraitTextures {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "portrait_textures");

  private static final int PIXELS_PER_BLOCK = 64;
  private static final int FRAME_ART_PIXELS_PER_BLOCK = 16;
  private static final int FRAME_BORDER = PIXELS_PER_BLOCK / FRAME_ART_PIXELS_PER_BLOCK;

  private static final PortraitBaker BAKER = new PortraitBaker();

  private static final PortraitTextureCache CACHE =
      new PortraitTextureCache(PortraitTextures::bake);

  private static int baked;

  private PortraitTextures() {}

  public static PortraitTextureCache cache() {
    return CACHE;
  }

  public static void reset(final TextureManager textureManager) {
    CACHE.clear(textureManager);
    BAKER.forgetRenderer();
  }

  private static @Nullable Identifier bake(final DogPortrait portrait) {
    final PortraitSize size = portrait.size();
    final int width = size.width() * PIXELS_PER_BLOCK;
    final int height = size.height() * PIXELS_PER_BLOCK;
    final List<PortraitComposer.Layer> layers = new ArrayList<>();
    if (!addLayer(layers, Identifier.of(MOD_ID, portrait.background().texturePath(size)))) {
      return null;
    }
    try (NativeImage dog = BAKER.bake(portrait.look(), portrait.shownPose())) {
      if (dog == null) {
        return null;
      }
      layers.add(
          new PortraitComposer.Layer(
              PortraitFraming.fit(layerOf(dog), width, height, FRAME_BORDER), width, height));
    }
    if (!addLayer(
        layers,
        Identifier.of(MOD_ID, "textures/portrait/frame_" + size.serializedName() + ".png"))) {
      return null;
    }
    return CoatImages.register(
        Identifier.of(MOD_ID, "portrait/baked_" + baked++),
        CoatImages.imageOf(PortraitComposer.compose(width, height, layers), width, height));
  }

  private static boolean addLayer(
      final List<PortraitComposer.Layer> layers, final Identifier texture) {
    try (CoatImages.Source source = CoatImages.read(texture)) {
      if (source == null) {
        return false;
      }
      layers.add(layerOf(source.image()));
      return true;
    }
  }

  private static PortraitComposer.Layer layerOf(final NativeImage image) {
    return new PortraitComposer.Layer(
        CoatImages.pixelsOf(image), image.getWidth(), image.getHeight());
  }
}
