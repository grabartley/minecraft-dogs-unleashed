package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.wood.WoodParticles;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockDustParticle.class)
public abstract class BlockDustParticleMixin {

  @ModifyArg(
      method =
          "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/block/BlockModels;getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
  private BlockState dogsUnleashed$showWoodenBlocksAsTheirPlanks(
      final BlockState state,
      @Local(argsOnly = true) final ClientWorld world,
      @Local(argsOnly = true) final BlockPos pos) {
    return WoodParticles.appearanceOf(state, world, pos);
  }
}
