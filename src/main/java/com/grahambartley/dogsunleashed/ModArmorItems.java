package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModArmorItems {

  private static final Map<DogArmorTier, AnimalArmorItem> ARMOR = register();

  private ModArmorItems() {}

  private static Map<DogArmorTier, AnimalArmorItem> register() {
    final int wolfArmorMaxDamage =
        ArmorItem.Type.BODY.getMaxDamage(DogArmorTier.WOLF_ARMOR_DURABILITY_MULTIPLIER);
    final Map<DogArmorTier, AnimalArmorItem> items = new EnumMap<>(DogArmorTier.class);
    for (final DogArmorTier tier : DogArmorTier.values()) {
      items.put(
          tier,
          Registry.register(
              Registries.ITEM,
              Identifier.of(DogsUnleashed.MOD_ID, tier.itemId()),
              new AnimalArmorItem(
                  ModArmorMaterials.of(tier),
                  AnimalArmorItem.Type.CANINE,
                  tier.dyeable(),
                  new Item.Settings().maxDamage(tier.maxDamage(wolfArmorMaxDamage)))));
    }
    return Collections.unmodifiableMap(items);
  }

  public static AnimalArmorItem of(final DogArmorTier tier) {
    return ARMOR.get(tier);
  }

  public static void initialize() {
    CauldronBehavior.WATER_CAULDRON_BEHAVIOR
        .map()
        .put(ARMOR.get(DogArmorTier.LEATHER), CauldronBehavior.CLEAN_DYEABLE_ITEM);
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(entries -> ARMOR.values().forEach(entries::add));
  }
}
