package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.pendant.PendantWarding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

  @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$refuseWardedTarget(final LivingEntity target, final CallbackInfo ci) {
    final MobEntity mob = (MobEntity) (Object) this;
    if (target != null && !mob.getWorld().isClient() && PendantWarding.isWarded(mob, target)) {
      ci.cancel();
    }
  }
}
