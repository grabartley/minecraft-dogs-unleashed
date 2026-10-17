package com.grahambartley.dogsunleashed.item.pendant;

import com.grahambartley.dogsunleashed.ModEntityTypeTags;
import java.util.function.Supplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

public enum PendantType {
  WARDING_UNDEAD(
      "warding_undead",
      PendantFamily.WARDING,
      () -> Items.BONE_BLOCK,
      0xE8DFC6,
      ModEntityTypeTags.WARDED_UNDEAD,
      null),
  WARDING_ARTHROPOD(
      "warding_arthropod",
      PendantFamily.WARDING,
      () -> Items.COBWEB,
      0x9C2E2E,
      ModEntityTypeTags.WARDED_ARTHROPOD,
      null),
  WARDING_ILLAGER(
      "warding_illager",
      PendantFamily.WARDING,
      () -> Items.WHITE_BANNER,
      0x3A7C77,
      ModEntityTypeTags.WARDED_ILLAGER,
      null),
  WARDING_CREEPER(
      "warding_creeper",
      PendantFamily.WARDING,
      () -> Items.TNT,
      0x5DB846,
      ModEntityTypeTags.WARDED_CREEPER,
      null),
  GUARDIAN_FIRE(
      "guardian_fire",
      PendantFamily.GUARDIAN,
      () -> Items.MAGMA_CREAM,
      0xE2671C,
      null,
      DamageTypeTags.IS_FIRE),
  GUARDIAN_FALL(
      "guardian_fall",
      PendantFamily.GUARDIAN,
      () -> Items.PHANTOM_MEMBRANE,
      0xA3B0E0,
      null,
      DamageTypeTags.IS_FALL),
  GUARDIAN_DROWNING(
      "guardian_drowning",
      PendantFamily.GUARDIAN,
      () -> Items.PUFFERFISH,
      0x2F80D3,
      null,
      DamageTypeTags.IS_DROWNING),
  VITALITY(
      "vitality", PendantFamily.VITALITY, () -> Items.GLISTERING_MELON_SLICE, 0xE0475A, null, null),
  LOYALTY("loyalty", PendantFamily.LOYALTY, () -> Items.TOTEM_OF_UNDYING, 0xE8C033, null, null);

  private final String id;
  private final PendantFamily family;
  private final Supplier<Item> reagent;
  private final int tint;
  private final @Nullable TagKey<EntityType<?>> wardedMobs;
  private final @Nullable TagKey<DamageType> guardedDamage;

  PendantType(
      final String id,
      final PendantFamily family,
      final Supplier<Item> reagent,
      final int tint,
      final @Nullable TagKey<EntityType<?>> wardedMobs,
      final @Nullable TagKey<DamageType> guardedDamage) {
    this.id = id;
    this.family = family;
    this.reagent = reagent;
    this.tint = tint;
    this.wardedMobs = wardedMobs;
    this.guardedDamage = guardedDamage;
  }

  public String id() {
    return this.id;
  }

  public String itemId() {
    return this.id + "_pendant";
  }

  public PendantFamily family() {
    return this.family;
  }

  public Item reagent() {
    return this.reagent.get();
  }

  public int tint() {
    return this.tint;
  }

  public @Nullable TagKey<EntityType<?>> wardedMobs() {
    return this.wardedMobs;
  }

  public @Nullable TagKey<DamageType> guardedDamage() {
    return this.guardedDamage;
  }

  public String effectTranslationKey() {
    return "item.dogs-unleashed." + this.itemId() + ".effect";
  }
}
