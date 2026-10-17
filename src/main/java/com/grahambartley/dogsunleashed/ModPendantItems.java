package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.pendant.PendantItem;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModPendantItems {

  public static final Item BLANK_PENDANT =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "blank_pendant"),
          new Item(new Item.Settings().maxCount(16)));

  private static final Map<PendantType, PendantItem> PENDANTS = registerPendants();

  private ModPendantItems() {}

  private static Map<PendantType, PendantItem> registerPendants() {
    final Map<PendantType, PendantItem> pendants = new EnumMap<>(PendantType.class);
    for (final PendantType type : PendantType.values()) {
      pendants.put(
          type,
          Registry.register(
              Registries.ITEM,
              Identifier.of(DogsUnleashed.MOD_ID, type.itemId()),
              new PendantItem(type, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON))));
    }
    return Collections.unmodifiableMap(pendants);
  }

  public static PendantItem of(final PendantType type) {
    return PENDANTS.get(type);
  }

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(
            entries -> {
              entries.add(BLANK_PENDANT);
              PENDANTS.values().forEach(entries::add);
            });
  }
}
