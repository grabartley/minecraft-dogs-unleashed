package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogFetchCarry;
import com.grahambartley.dogsunleashed.entity.DogMud;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.item.ItemStack;

public final class DogPortraitOutfits {

  private DogPortraitOutfits() {}

  public static DogPortraitOutfit of(final UnleashedDogEntity dog) {
    final Map<DogEquipmentSlot, ItemStack> gear = new EnumMap<>(DogEquipmentSlot.class);
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      gear.put(slot, dog.getEquipmentHolder().getStack(slot));
    }
    return new DogPortraitOutfit(
        gear,
        dog.isTamed()
            ? Optional.of(dog.getComponents().get(DogCollar.class).getColor())
            : Optional.empty(),
        carriedBy(dog.getFetchCarry()),
        dog.getComponents().get(DogMud.class).isMuddy(),
        dog.getComponents().get(DogAging.class).isSenior(),
        dog.isUndead());
  }

  public static void dress(final UnleashedDogEntity dog, final DogPortraitOutfit outfit) {
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      dog.getEquipmentHolder().setStack(slot, outfit.worn(slot));
    }
    outfit
        .collar()
        .ifPresent(
            colour -> {
              dog.setTamed(true, false);
              dog.getComponents().get(DogCollar.class).setColor(colour);
            });
    if (!outfit.carried().isEmpty()) {
      dog.getFetchCarry().setActiveType(null);
      dog.getFetchCarry().setCarriedStack(outfit.carried());
      dog.getFetchCarry().setCarrying(true);
    }
    if (outfit.muddy()) {
      dog.getComponents().get(DogMud.class).makeMuddy();
    }
    dog.getComponents().get(DogAging.class).showSenior(outfit.senior());
  }

  private static ItemStack carriedBy(final DogFetchCarry carry) {
    if (!carry.isCarrying()) {
      return ItemStack.EMPTY;
    }
    final FetchItemType type = carry.activeType();
    if (carry.isCarryingFetchedItem() || type == null || !carry.carriedStack().isEmpty()) {
      return carry.carriedStack().copy();
    }
    return new ItemStack(type.item());
  }
}
