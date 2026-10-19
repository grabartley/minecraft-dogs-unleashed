package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import java.util.UUID;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class DogGraveFizzle {

  private static final int SMOKE_PARTICLE_COUNT = 24;
  private static final double SMOKE_SPREAD = 0.3;
  private static final double SMOKE_SPEED = 0.02;

  private DogGraveFizzle() {}

  public static void play(
      final ServerWorld world, final BlockPos gravePos, final DogGraveBlockEntity grave) {
    world.playSound(
        null, gravePos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 0.8f);
    world.spawnParticles(
        ParticleTypes.LARGE_SMOKE,
        gravePos.getX() + 0.5,
        gravePos.getY() + 0.8,
        gravePos.getZ() + 0.5,
        SMOKE_PARTICLE_COUNT,
        SMOKE_SPREAD,
        SMOKE_SPREAD,
        SMOKE_SPREAD,
        SMOKE_SPEED);
    tellInstaller(world, grave);
  }

  private static void tellInstaller(final ServerWorld world, final DogGraveBlockEntity grave) {
    final UUID installerId = grave.getTotemInstallerId();
    if (installerId == null) {
      return;
    }
    final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(installerId);
    if (player != null) {
      player.sendMessage(
          Text.translatable("block.dogs-unleashed.dog_grave.ritual_fizzled", grave.getDogName()),
          true);
    }
  }
}
