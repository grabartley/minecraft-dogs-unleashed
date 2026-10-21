package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.item.BlankCanvasItem;
import com.grahambartley.dogsunleashed.item.DogPortraitItem;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.DogPortraitEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModPortraits {

  public static final Item BLANK_CANVAS =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "blank_canvas"),
          new BlankCanvasItem(new Item.Settings()));

  public static final Item DOG_PORTRAIT =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_portrait"),
          new DogPortraitItem(new Item.Settings().maxCount(1)));

  public static final EntityType<DogPortraitEntity> DOG_PORTRAIT_ENTITY =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_portrait"),
          EntityType.Builder.<DogPortraitEntity>create(DogPortraitEntity::new, SpawnGroup.MISC)
              .dimensions(0.5f, 0.5f)
              .maxTrackingRange(10)
              .trackingTickInterval(Integer.MAX_VALUE)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "dog_portrait").toString()));

  private ModPortraits() {}

  public static ItemStack portraitStack(final DogPortrait portrait) {
    final ItemStack stack = new ItemStack(DOG_PORTRAIT);
    stack.set(ModComponents.DOG_PORTRAIT, portrait);
    return stack;
  }

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(BLANK_CANVAS));
  }
}
