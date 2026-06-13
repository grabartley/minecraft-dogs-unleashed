package com.grahambartley;

import com.grahambartley.block.DogBedBlock;
import com.grahambartley.block.DogGraveBlock;
import com.grahambartley.block.FrisbeeBlock;
import com.grahambartley.block.StickBlock;
import com.grahambartley.block.TennisBallBlock;
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

  public static final Block DOG_GRAVE =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave"),
          new DogGraveBlock(
              AbstractBlock.Settings.create()
                  .mapColor(MapColor.STONE_GRAY)
                  .strength(2.0f)
                  .requiresTool()
                  .sounds(BlockSoundGroup.STONE)
                  .nonOpaque()));

  public static final Block TENNIS_BALL =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball"),
          new TennisBallBlock(
              AbstractBlock.Settings.create()
                  .strength(0.05f)
                  .breakInstantly()
                  .sounds(BlockSoundGroup.WOOL)
                  .nonOpaque()
                  .noCollision()));

  public static final Block STICK =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(DogsUnleashed.MOD_ID, "stick"),
          new StickBlock(
              AbstractBlock.Settings.create()
                  .strength(0.05f)
                  .breakInstantly()
                  .sounds(BlockSoundGroup.WOOD)
                  .nonOpaque()
                  .noCollision()));

  public static final Block FRISBEE =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(DogsUnleashed.MOD_ID, "frisbee"),
          new FrisbeeBlock(
              AbstractBlock.Settings.create()
                  .strength(0.05f)
                  .breakInstantly()
                  .sounds(BlockSoundGroup.WOOL)
                  .nonOpaque()
                  .noCollision()));

  public static void initialize() {}
}
