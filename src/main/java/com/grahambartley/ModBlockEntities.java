package com.grahambartley;

import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.block.entity.DogGraveBlockEntity;
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

  public static void initialize() {}
}
