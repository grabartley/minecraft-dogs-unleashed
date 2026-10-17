package com.grahambartley.dogsunleashed.item.armor;

import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum DogArmorTier {
  LEATHER(
      "leather",
      0,
      0.0f,
      0.0f,
      1.0f,
      () -> Items.LEATHER,
      true,
      15,
      SoundEvents.ITEM_ARMOR_EQUIP_LEATHER),
  IRON(
      "iron",
      4,
      0.0f,
      0.0f,
      2.0f,
      () -> Items.IRON_INGOT,
      false,
      9,
      SoundEvents.ITEM_ARMOR_EQUIP_IRON),
  GOLD(
      "gold",
      4,
      0.0f,
      0.0f,
      1.5f,
      () -> Items.GOLD_INGOT,
      false,
      25,
      SoundEvents.ITEM_ARMOR_EQUIP_GOLD),
  DIAMOND(
      "diamond",
      8,
      2.0f,
      0.0f,
      4.0f,
      () -> Items.DIAMOND,
      false,
      10,
      SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND),
  NETHERITE(
      "netherite",
      9,
      3.0f,
      0.1f,
      5.0f,
      () -> Items.NETHERITE_INGOT,
      false,
      15,
      SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE);

  public static final int WOLF_ARMOR_DURABILITY_MULTIPLIER = 4;

  private final String id;
  private final int defenseOverArmadillo;
  private final float toughness;
  private final float knockbackResistance;
  private final float durabilityOverWolfArmor;
  private final Supplier<Item> repairItem;
  private final boolean dyeable;
  private final int enchantability;
  private final RegistryEntry<SoundEvent> equipSound;

  DogArmorTier(
      final String id,
      final int defenseOverArmadillo,
      final float toughness,
      final float knockbackResistance,
      final float durabilityOverWolfArmor,
      final Supplier<Item> repairItem,
      final boolean dyeable,
      final int enchantability,
      final RegistryEntry<SoundEvent> equipSound) {
    this.id = id;
    this.defenseOverArmadillo = defenseOverArmadillo;
    this.toughness = toughness;
    this.knockbackResistance = knockbackResistance;
    this.durabilityOverWolfArmor = durabilityOverWolfArmor;
    this.repairItem = repairItem;
    this.dyeable = dyeable;
    this.enchantability = enchantability;
    this.equipSound = equipSound;
  }

  public RegistryEntry<SoundEvent> equipSound() {
    return this.equipSound;
  }

  public String id() {
    return this.id;
  }

  public String itemId() {
    return this.id + "_dog_armor";
  }

  public int defense(final int armadilloDefense) {
    return armadilloDefense + this.defenseOverArmadillo;
  }

  public int maxDamage(final int wolfArmorMaxDamage) {
    return Math.round(wolfArmorMaxDamage * this.durabilityOverWolfArmor);
  }

  public float toughness() {
    return this.toughness;
  }

  public float knockbackResistance() {
    return this.knockbackResistance;
  }

  public Item repairItem() {
    return this.repairItem.get();
  }

  public boolean dyeable() {
    return this.dyeable;
  }

  public int enchantability() {
    return this.enchantability;
  }
}
