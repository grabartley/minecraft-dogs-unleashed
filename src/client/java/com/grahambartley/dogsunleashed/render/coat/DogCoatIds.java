package com.grahambartley.dogsunleashed.render.coat;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves which coat art a dog paints itself from, and which coat each breed donates pigment from.
 */
public final class DogCoatIds {

  private static final Map<UnleashedDogBreed, String> DONORS =
      new EnumMap<>(UnleashedDogBreed.class);

  static {
    DONORS.put(UnleashedDogBreed.GOLDEN_RETRIEVER, "goldenretriever");
    DONORS.put(UnleashedDogBreed.BEAGLE, "beagle_tri1");
  }

  private DogCoatIds() {}

  /** The coat whose layers give this dog its markings and its place on the atlas. */
  public static String layoutOf(final UnleashedDogEntity dog) {
    final StringBuilder id = new StringBuilder(dog.getRigSourceBreed().serializedId());
    final UnleashedDogCoat coat = dog.getCoatVariant();
    if (coat != null) {
      id.append("_").append(coat.getTexturePrefix());
    }
    final HuskyEyeColor eyeColor = dog.getEyeColorVariant();
    if (eyeColor != null) {
      id.append("_").append(eyeColor.textureSuffix());
    }
    return id.toString();
  }

  /** The coat a breed contributes pigment from when it is not the one supplying the layout. */
  public static String donorFor(final UnleashedDogBreed breed) {
    return DONORS.getOrDefault(breed, breed.serializedId());
  }
}
