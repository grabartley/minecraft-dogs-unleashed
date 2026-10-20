package com.grahambartley.dogsunleashed.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableDogStatueBlock extends DogStatueBlock implements Oxidizable {

  private final Oxidizable.OxidationLevel level;

  public OxidizableDogStatueBlock(
      final Oxidizable.OxidationLevel level,
      final StatueMaterial material,
      final Settings settings) {
    super(material, settings);
    this.level = level;
  }

  @Override
  public Oxidizable.OxidationLevel getDegradationLevel() {
    return this.level;
  }

  @Override
  protected void randomTick(
      final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
    this.tickDegradation(state, world, pos, random);
  }

  @Override
  protected boolean hasRandomTicks(final BlockState state) {
    return this.level != Oxidizable.OxidationLevel.OXIDIZED;
  }
}
