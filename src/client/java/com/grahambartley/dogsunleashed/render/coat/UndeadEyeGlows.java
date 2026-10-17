package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class UndeadEyeGlows {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "undead_eye_glows");

  @FunctionalInterface
  public interface Registrar {
    Identifier register(UnleashedDogBreed rig, UndeadDogEyes eyes);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(UndeadEyeGlows.class);

  private static final UndeadEyeGlows SHARED =
      new UndeadEyeGlows(
          RigModels::loaded,
          rig -> RigProfiles.of(rig).pupils(),
          SlotRegions.shared(),
          UndeadEyeGlows::registerGlow);

  private final Function<UnleashedDogBreed, Optional<BakedGeoModel>> models;
  private final Function<UnleashedDogBreed, List<Texel>> pupils;
  private final SlotRegions regions;
  private final Registrar registrar;
  private final Map<UnleashedDogBreed, UndeadDogEyes> eyesByRig =
      new EnumMap<>(UnleashedDogBreed.class);
  private final Map<UnleashedDogBreed, Identifier> glowByRig =
      new EnumMap<>(UnleashedDogBreed.class);

  public UndeadEyeGlows(
      final Function<UnleashedDogBreed, Optional<BakedGeoModel>> models,
      final Function<UnleashedDogBreed, List<Texel>> pupils,
      final SlotRegions regions,
      final Registrar registrar) {
    this.models = models;
    this.pupils = pupils;
    this.regions = regions;
    this.registrar = registrar;
  }

  public static UndeadEyeGlows shared() {
    return SHARED;
  }

  public synchronized UndeadDogEyes eyesOf(final UnleashedDogBreed rigSourceBreed) {
    final UnleashedDogBreed rig = rigOf(rigSourceBreed);
    final UndeadDogEyes cached = this.eyesByRig.get(rig);
    if (cached != null) {
      return cached;
    }
    final Optional<BakedGeoModel> model = this.models.apply(rig);
    if (model.isEmpty()) {
      return UndeadDogEyes.NONE;
    }
    final List<Texel> declared = this.pupils.apply(rig);
    final UndeadDogEyes eyes =
        UndeadDogEyes.of(model.get(), declared, this.regions.regionsOf(rig, Slot.HEAD));
    if (eyes.pupils().size() != declared.size()) {
      LOGGER.warn(
          "Rig {} declares pupils {} but only {} sit on its head",
          rig.serializedId(),
          declared,
          eyes.pupils());
    }
    this.eyesByRig.put(rig, eyes);
    return eyes;
  }

  public synchronized Identifier glowTextureOf(final UnleashedDogBreed rigSourceBreed) {
    final UnleashedDogBreed rig = rigOf(rigSourceBreed);
    final Identifier cached = this.glowByRig.get(rig);
    if (cached != null) {
      return cached;
    }
    final Identifier glow = this.registrar.register(rig, this.eyesOf(rig));
    this.glowByRig.put(rig, glow);
    return glow;
  }

  public synchronized void clear(final TextureManager textureManager) {
    this.glowByRig.values().forEach(textureManager::destroyTexture);
    this.glowByRig.clear();
    this.eyesByRig.clear();
  }

  static UnleashedDogBreed rigOf(final UnleashedDogBreed rigSourceBreed) {
    return rigSourceBreed == UnleashedDogBreed.CROSS_BREED
        ? UnleashedDogBreed.FALLBACK_RIG
        : rigSourceBreed;
  }

  private static Identifier registerGlow(final UnleashedDogBreed rig, final UndeadDogEyes eyes) {
    final int width = Math.max(1, eyes.atlasWidth());
    final int height = Math.max(1, eyes.atlasHeight());
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
    image.fillRect(0, 0, width, height, 0);
    for (final Texel pupil : eyes.pupils()) {
      image.setColor(pupil.x(), pupil.y(), packedEyeColor());
    }
    final Identifier id = Identifier.of(MOD_ID, "undead_glow/" + rig.serializedId());
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    return id;
  }

  private static int packedEyeColor() {
    final int rgb = UndeadTextureTransform.EYE_RGB;
    return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
  }
}
