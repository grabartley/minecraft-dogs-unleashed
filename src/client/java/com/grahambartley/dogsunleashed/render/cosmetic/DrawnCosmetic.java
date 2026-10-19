package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;

public final class DrawnCosmetic {

  private DrawnCosmetic() {}

  public static Optional<DogCosmetic> on(final UnleashedDogEntity dog) {
    return of(
        dog.isInvisible(),
        DogCosmeticItem.cosmeticOf(dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC)),
        GeckoLibCache.getBakedModels()::containsKey);
  }

  public static Optional<MobHead> mobHeadOn(final UnleashedDogEntity dog) {
    return mobHeadOf(
        dog.isInvisible(),
        MobHead.of(dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC)));
  }

  static Optional<MobHead> mobHeadOf(final boolean invisible, final Optional<MobHead> worn) {
    return invisible ? Optional.empty() : worn;
  }

  static Optional<DogCosmetic> of(
      final boolean invisible,
      final Optional<DogCosmetic> worn,
      final Predicate<Identifier> modelLoaded) {
    if (invisible) {
      return Optional.empty();
    }
    return worn.filter(cosmetic -> modelLoaded.test(CosmeticAssets.model(cosmetic)));
  }
}
