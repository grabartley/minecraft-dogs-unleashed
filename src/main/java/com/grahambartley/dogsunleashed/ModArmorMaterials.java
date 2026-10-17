package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModArmorMaterials {

  private static final Map<DogArmorTier, RegistryEntry<ArmorMaterial>> MATERIALS = register();

  private ModArmorMaterials() {}

  private static Map<DogArmorTier, RegistryEntry<ArmorMaterial>> register() {
    final int armadilloDefense =
        ArmorMaterials.ARMADILLO.value().getProtection(ArmorItem.Type.BODY);
    final Map<DogArmorTier, RegistryEntry<ArmorMaterial>> materials =
        new EnumMap<>(DogArmorTier.class);
    for (final DogArmorTier tier : DogArmorTier.values()) {
      final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, tier.id() + "_dog");
      materials.put(
          tier,
          Registry.registerReference(
              Registries.ARMOR_MATERIAL,
              id,
              new ArmorMaterial(
                  Map.of(ArmorItem.Type.BODY, tier.defense(armadilloDefense)),
                  tier.enchantability(),
                  tier.equipSound(),
                  () -> Ingredient.ofItems(tier.repairItem()),
                  List.of(new ArmorMaterial.Layer(id, "", tier.dyeable())),
                  tier.toughness(),
                  tier.knockbackResistance())));
    }
    return Collections.unmodifiableMap(materials);
  }

  public static RegistryEntry<ArmorMaterial> of(final DogArmorTier tier) {
    return MATERIALS.get(tier);
  }

  public static void initialize() {}
}
