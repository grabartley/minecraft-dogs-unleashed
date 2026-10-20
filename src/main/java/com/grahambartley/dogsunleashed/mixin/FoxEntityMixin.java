package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.entity.burial.FoxRaidGoals;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin {

  @Inject(method = "initGoals", at = @At("TAIL"))
  private void dogsUnleashed$addBoneRaidGoals(final CallbackInfo ci) {
    FoxRaidGoals.addTo((FoxEntity) (Object) this);
  }
}
