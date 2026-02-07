package com.grahambartley;

import com.grahambartley.block.DogBedBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

  public static final Block DOG_BED =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed"),
          new DogBedBlock(
              AbstractBlock.Settings.create()
                  .mapColor(MapColor.WHITE)
                  .strength(2.0f)
                  .sounds(BlockSoundGroup.WOOL)
                  .nonOpaque()));

  public static void initialize() {}
}
