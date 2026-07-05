package com.grahambartley.dogsunleashed.entity.fetch;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class FetchTypes {
  private static final Map<Item, FetchItemType> BY_ITEM = new HashMap<>();
  private static final Map<Block, FetchItemType> BY_BLOCK = new HashMap<>();
  private static final Map<EntityType<?>, FetchItemType> BY_ENTITY_TYPE = new HashMap<>();
  private static final Map<Identifier, FetchItemType> BY_ID = new HashMap<>();
  private static final List<FetchItemType> ALL = new ArrayList<>();

  public static final FetchItemType TENNIS_BALL =
      register(
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball"),
          ModItems.TENNIS_BALL,
          ModEntities.TENNIS_BALL_PROJECTILE,
          ModBlocks.TENNIS_BALL,
          ModBlockEntities.TENNIS_BALL);

  public static final FetchItemType STICK =
      register(
          Identifier.of(DogsUnleashed.MOD_ID, "stick"),
          Items.STICK,
          ModEntities.STICK_PROJECTILE,
          ModBlocks.STICK,
          ModBlockEntities.STICK);

  public static final FetchItemType FRISBEE =
      register(
          Identifier.of(DogsUnleashed.MOD_ID, "frisbee"),
          ModItems.FRISBEE,
          ModEntities.FRISBEE_PROJECTILE,
          ModBlocks.FRISBEE,
          ModBlockEntities.FRISBEE);

  private FetchTypes() {}

  private static FetchItemType register(
      Identifier id,
      Item item,
      EntityType<?> projectileType,
      Block landedBlock,
      BlockEntityType<?> landedBlockEntityType) {
    FetchItemType fetchItemType =
        new FetchItemType(id, item, projectileType, landedBlock, landedBlockEntityType);
    ALL.add(fetchItemType);
    BY_ID.put(id, fetchItemType);
    BY_ITEM.put(item, fetchItemType);
    BY_BLOCK.put(landedBlock, fetchItemType);
    BY_ENTITY_TYPE.put(projectileType, fetchItemType);
    return fetchItemType;
  }

  public static @Nullable FetchItemType forId(Identifier id) {
    return BY_ID.get(id);
  }

  public static @Nullable FetchItemType forItem(Item item) {
    return BY_ITEM.get(item);
  }

  public static @Nullable FetchItemType forBlock(Block block) {
    return BY_BLOCK.get(block);
  }

  public static @Nullable FetchItemType forEntityType(EntityType<?> entityType) {
    return BY_ENTITY_TYPE.get(entityType);
  }

  public static Collection<FetchItemType> all() {
    return List.copyOf(ALL);
  }

  public static Ingredient asIngredient() {
    return Ingredient.ofItems(ALL.stream().map(FetchItemType::item).toArray(Item[]::new));
  }
}
