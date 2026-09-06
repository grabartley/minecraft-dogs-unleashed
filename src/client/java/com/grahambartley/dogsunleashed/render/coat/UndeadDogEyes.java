package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Where each rig keeps its pupils. The coordinates are fixed by the geometry's UV layout rather
 * than by any one coat, so one entry serves every coat painted on that rig.
 *
 * <p>Placeholder data for #60; the authored undead layers carry their own eye art and this table
 * goes with them.
 */
public enum UndeadDogEyes {
  BEAGLE(128, new EyePixel(7, 47), new EyePixel(10, 47)),
  GOLDEN_RETRIEVER(128, new EyePixel(7, 47), new EyePixel(10, 47)),
  HUSKY(64, new EyePixel(26, 20), new EyePixel(28, 20)),
  DACHSHUND(128, new EyePixel(98, 6), new EyePixel(100, 6)),
  SHIBA_INU(128, new EyePixel(50, 50), new EyePixel(53, 50));

  public record EyePixel(int x, int y) {}

  private final int atlasSize;
  private final List<EyePixel> pixels;
  private @Nullable Identifier glowTexture;

  UndeadDogEyes(final int atlasSize, final EyePixel... pixels) {
    this.atlasSize = atlasSize;
    this.pixels = List.of(pixels);
  }

  /** Cross-breeds never reach here directly: their rig source resolves to a founding breed. */
  public static UndeadDogEyes of(final UnleashedDogBreed rigSourceBreed) {
    return switch (rigSourceBreed) {
      case HUSKY -> HUSKY;
      case DACHSHUND -> DACHSHUND;
      case SHIBA_INU -> SHIBA_INU;
      case GOLDEN_RETRIEVER -> GOLDEN_RETRIEVER;
      case BEAGLE -> BEAGLE;
      case CROSS_BREED -> HUSKY;
    };
  }

  public int atlasSize() {
    return this.atlasSize;
  }

  public List<EyePixel> pixels() {
    return this.pixels;
  }

  /**
   * A texture that is transparent everywhere except the pupils, rendered fullbright over the model
   * so the eyes glow in the dark the way spider eyes do.
   */
  public Identifier glowTexture() {
    if (this.glowTexture != null) {
      return this.glowTexture;
    }
    final NativeImage image =
        new NativeImage(NativeImage.Format.RGBA, this.atlasSize, this.atlasSize, false);
    image.fillRect(0, 0, this.atlasSize, this.atlasSize, 0);
    for (final EyePixel pixel : this.pixels) {
      image.setColor(pixel.x(), pixel.y(), packedEyeColor());
    }
    final Identifier id =
        Identifier.of(MOD_ID, "undead_glow/" + this.name().toLowerCase(Locale.ROOT));
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    this.glowTexture = id;
    return id;
  }

  /** {@link NativeImage} colours are packed ABGR, so the RGB constant is byte-swapped here. */
  private static int packedEyeColor() {
    final int rgb = UndeadTextureTransform.EYE_RGB;
    return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
  }
}
