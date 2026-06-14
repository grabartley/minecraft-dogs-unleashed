package com.grahambartley;

import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.block.entity.DogGraveBlockEntity;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.block.entity.StickBlockEntity;
import com.grahambartley.block.entity.TennisBallBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

  public static final BlockEntityType<DogBedBlockEntity> DOG_BED =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed"),
          BlockEntityType.Builder.create(DogBedBlockEntity::new, ModBlocks.DOG_BED).build());

  public static final BlockEntityType<DogGraveBlockEntity> DOG_GRAVE =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave"),
          BlockEntityType.Builder.create(DogGraveBlockEntity::new, ModBlocks.DOG_GRAVE).build());

  public static final BlockEntityType<TennisBallBlockEntity> TENNIS_BALL =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball"),
          BlockEntityType.Builder.create(TennisBallBlockEntity::new, ModBlocks.TENNIS_BALL)
              .build());

  public static final BlockEntityType<StickBlockEntity> STICK =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "stick"),
          BlockEntityType.Builder.create(StickBlockEntity::new, ModBlocks.STICK).build());

  public static final BlockEntityType<FrisbeeBlockEntity> FRISBEE =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "frisbee"),
          BlockEntityType.Builder.create(FrisbeeBlockEntity::new, ModBlocks.FRISBEE).build());

  public static void initialize() {}
}
