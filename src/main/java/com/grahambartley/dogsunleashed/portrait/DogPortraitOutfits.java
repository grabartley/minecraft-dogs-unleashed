package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogFetchCarry;
import com.grahambartley.dogsunleashed.entity.DogMud;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
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
    final DogFetchCarry carry = dog.getFetchCarry();
    return new DogPortraitOutfit(
        gear,
        dog.isTamed()
            ? Optional.of(dog.getComponents().get(DogCollar.class).getColor())
            : Optional.empty(),
        carry.isCarrying() ? carry.carriedStack() : ItemStack.EMPTY,
        carry.isCarrying()
            ? Optional.ofNullable(carry.activeType()).map(FetchItemType::id)
            : Optional.empty(),
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
    final DogFetchCarry carry = dog.getFetchCarry();
    carry.setCarrying(false);
    carry.setActiveType(null);
    if (outfit.carrying()) {
      carry.setActiveType(outfit.fetchType().map(FetchTypes::forId).orElse(null));
      carry.setCarriedStack(outfit.carried());
      carry.setCarrying(true);
    }
    final DogMud mud = dog.getComponents().get(DogMud.class);
    if (outfit.muddy()) {
      mud.makeMuddy();
    } else {
      mud.wash();
    }
    dog.getComponents().get(DogAging.class).showSeniorCoat(outfit.senior());
  }
}
