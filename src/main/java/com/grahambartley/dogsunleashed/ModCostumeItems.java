package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModCostumeItems {

  private static final Map<DogCostume, Item> COSTUMES = registerCostumes();

  private ModCostumeItems() {}

  private static Map<DogCostume, Item> registerCostumes() {
    final Map<DogCostume, Item> costumes = new EnumMap<>(DogCostume.class);
    for (final DogCostume costume : DogCostume.values()) {
      costumes.put(
          costume,
          Registry.register(
              Registries.ITEM,
              Identifier.of(DogsUnleashed.MOD_ID, costume.itemId()),
              new Item(new Item.Settings().maxCount(1))));
    }
    return Collections.unmodifiableMap(costumes);
  }

  public static Item of(final DogCostume costume) {
    return COSTUMES.get(costume);
  }

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> COSTUMES.values().forEach(entries::add));
  }
}
