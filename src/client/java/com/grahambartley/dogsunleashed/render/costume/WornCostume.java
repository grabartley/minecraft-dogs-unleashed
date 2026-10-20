package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.ModCostumeItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.component.type.DyedColorComponent;
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

  public static int tintOf(final UnleashedDogEntity dog) {
    final ItemStack costume = dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME);
    return CostumeTint.argb(
        ModCostumeItems.costumeOf(costume)
            .filter(DogCostume::isDyeable)
            .map(
                dyeable ->
                    OptionalInt.of(DyedColorComponent.getColor(costume, dyeable.undyedColor())))
            .orElse(OptionalInt.empty()));
  }
}
