package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.entity.herding.SheepHerding;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin {

  @Inject(method = "initGoals", at = @At("TAIL"))
  private void dogsUnleashed$addHerdingGoal(final CallbackInfo ci) {
    SheepHerding.addTo((SheepEntity) (Object) this);
  }
}
