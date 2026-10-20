package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class WornCostume {

  private WornCostume() {}

  public static Optional<CostumeAssets> on(final UnleashedDogEntity dog) {
    final ItemStack costume = dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME);
    if (!CostumeVisibility.shows(
        !costume.isEmpty(), !dog.getArmour().worn().isEmpty(), dog.isInvisible())) {
      return Optional.empty();
    }
    return Optional.of(CostumeAssets.of(Registries.ITEM.getId(costume.getItem())));
  }
}
