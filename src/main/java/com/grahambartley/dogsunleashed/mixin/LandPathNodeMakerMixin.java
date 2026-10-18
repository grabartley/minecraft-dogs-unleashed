package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin extends PathNodeMaker {

  @ModifyExpressionValue(
      method = "getSuccessors",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getStepHeight()F"))
  private float dogsUnleashed$climbLevels(final float stepHeight) {
    return (float) Math.max(stepHeight, Math.ceil(climbHeight(this.entity)));
  }

  @ModifyExpressionValue(
      method = "getStepHeight",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getStepHeight()F"))
  private float dogsUnleashed$climbHeight(final float stepHeight) {
    return (float) Math.max(stepHeight, climbHeight(this.entity));
  }

  private static double climbHeight(final MobEntity entity) {
    return entity instanceof UnleashedDogEntity dog ? dog.getGrowth().pathClimbHeight() : 0.0;
  }
}
