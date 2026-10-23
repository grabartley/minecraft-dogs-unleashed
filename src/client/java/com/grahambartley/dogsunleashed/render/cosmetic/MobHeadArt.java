package com.grahambartley.dogsunleashed.render.cosmetic;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;

public record MobHeadArt(Identifier model, Identifier texture) {

  public static Optional<MobHeadArt> on(final UnleashedDogEntity dog) {
    return DrawnCosmetic.mobHeadOn(dog)
        .flatMap(
            head -> of(head, dog.getRigSourceBreed(), GeckoLibCache.getBakedModels()::containsKey));
  }

  static Optional<MobHeadArt> of(
      final MobHead head, final UnleashedDogBreed breed, final Predicate<Identifier> modelLoaded) {
    final MobHeadArt art = named(head, breed);
    return modelLoaded.test(art.model) ? Optional.of(art) : Optional.empty();
  }

  static MobHeadArt named(final MobHead head, final UnleashedDogBreed breed) {
    final String path = head.itemId().getPath() + "/" + breed.serializedId();
    return new MobHeadArt(
        Identifier.of(MOD_ID, "geo/cosmetic/" + path + ".geo.json"),
        Identifier.of(MOD_ID, "textures/entity/cosmetic/" + path + ".png"));
  }
}
