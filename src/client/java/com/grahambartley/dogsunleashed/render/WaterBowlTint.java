package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public final class WaterBowlTint {

  static final int DEFAULT_WATER = 0x3F76E4;

  private WaterBowlTint() {}

  public static void register() {
    ColorProviderRegistry.BLOCK.register(
        (state, world, pos, tintIndex) -> tint(world, pos), ModBlocks.WATER_BOWL);
  }

  static int tint(final @Nullable BlockRenderView world, final @Nullable BlockPos pos) {
    return world == null || pos == null ? DEFAULT_WATER : BiomeColors.getWaterColor(world, pos);
  }
}
