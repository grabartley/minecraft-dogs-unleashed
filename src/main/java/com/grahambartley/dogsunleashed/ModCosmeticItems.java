package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModCosmeticItems {

  private static final Map<DogCosmetic, DogCosmeticItem> COSMETICS = registerCosmetics();

  private ModCosmeticItems() {}

  private static Map<DogCosmetic, DogCosmeticItem> registerCosmetics() {
    final Map<DogCosmetic, DogCosmeticItem> cosmetics = new EnumMap<>(DogCosmetic.class);
    for (final DogCosmetic cosmetic : DogCosmetic.values()) {
      cosmetics.put(
          cosmetic,
          Registry.register(
              Registries.ITEM,
              Identifier.of(DogsUnleashed.MOD_ID, cosmetic.itemId()),
              new DogCosmeticItem(cosmetic, new Item.Settings().maxCount(1))));
    }
    return Collections.unmodifiableMap(cosmetics);
  }

  public static DogCosmeticItem of(final DogCosmetic cosmetic) {
    return COSMETICS.get(cosmetic);
  }

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> COSMETICS.values().forEach(entries::add));
  }
}
