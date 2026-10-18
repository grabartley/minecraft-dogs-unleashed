package com.grahambartley.dogsunleashed.render.cosmetic;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class BandanaPrints {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "bandana_prints");

  private static final Logger LOGGER = LoggerFactory.getLogger(BandanaPrints.class);
  private static final BandanaPrints SHARED = new BandanaPrints();

  private final Map<Identifier, Optional<Identifier>> byPattern = new HashMap<>();

  private BandanaPrints() {}

  public static BandanaPrints shared() {
    return SHARED;
  }

  public synchronized Optional<Identifier> textureFor(final RegistryEntry<BannerPattern> pattern) {
    return this.byPattern.computeIfAbsent(pattern.value().assetId(), this::register);
  }

  public synchronized void clear(final TextureManager textureManager) {
    this.byPattern.values().forEach(texture -> texture.ifPresent(textureManager::destroyTexture));
    this.byPattern.clear();
  }

  private Optional<Identifier> register(final Identifier assetId) {
    final BakedGeoModel model =
        GeckoLibCache.getBakedModels().get(CosmeticAssets.model(DogCosmetic.BANDANA));
    if (model == null) {
      return Optional.empty();
    }
    final Identifier patternTexture =
        assetId.withPath(path -> "textures/entity/banner/" + path + ".png");
    try (NativeImage cloth = read(CosmeticAssets.texture(DogCosmetic.BANDANA));
        NativeImage pattern = read(patternTexture)) {
      final List<BannerPrint.Face> faces =
          BannerPrint.facesOf(model, cloth.getWidth(), cloth.getHeight());
      final int[] printed = BannerPrint.print(imageOf(cloth), imageOf(pattern), faces);
      final NativeImage image =
          new NativeImage(NativeImage.Format.RGBA, cloth.getWidth(), cloth.getHeight(), false);
      for (int y = 0; y < cloth.getHeight(); y++) {
        for (int x = 0; x < cloth.getWidth(); x++) {
          image.setColor(x, y, printed[y * cloth.getWidth() + x]);
        }
      }
      final Identifier id =
          Identifier.of(
              MOD_ID, "bandana_print/" + assetId.getNamespace() + "/" + assetId.getPath());
      MinecraftClient.getInstance()
          .getTextureManager()
          .registerTexture(id, new NativeImageBackedTexture(image));
      return Optional.of(id);
    } catch (final IOException exception) {
      LOGGER.warn("Unreadable banner pattern {}: {}", patternTexture, exception.getMessage());
      return Optional.empty();
    }
  }

  private static NativeImage read(final Identifier texture) throws IOException {
    final Resource resource =
        MinecraftClient.getInstance()
            .getResourceManager()
            .getResource(texture)
            .orElseThrow(() -> new IOException("missing " + texture));
    try (InputStream stream = resource.getInputStream()) {
      return NativeImage.read(stream);
    }
  }

  private static BannerPrint.Image imageOf(final NativeImage image) {
    return new BannerPrint.Image() {
      @Override
      public int width() {
        return image.getWidth();
      }

      @Override
      public int height() {
        return image.getHeight();
      }

      @Override
      public int get(final int x, final int y) {
        return image.getColor(x, y);
      }
    };
  }
}
