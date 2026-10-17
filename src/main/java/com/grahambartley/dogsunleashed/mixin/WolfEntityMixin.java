package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.entity.WolfDogArmour;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin {

  @ModifyReturnValue(method = "shouldArmorAbsorbDamage", at = @At("RETURN"))
  private boolean dogsUnleashed$dogArmourAbsorbs(
      final boolean original, final DamageSource source) {
    return original || WolfDogArmour.absorbs((WolfEntity) (Object) this, source);
  }

  @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$wearDogArmour(
      final DamageSource source, final float amount, final CallbackInfo ci) {
    if (WolfDogArmour.tryAbsorb((WolfEntity) (Object) this, source, amount)) {
      ci.cancel();
    }
  }

  @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$equipMendOrShearDogArmour(
      final PlayerEntity player, final Hand hand, final CallbackInfoReturnable<ActionResult> cir) {
    final ActionResult result = WolfDogArmour.interact((WolfEntity) (Object) this, player, hand);
    if (result != null) {
      cir.setReturnValue(result);
    }
  }
}
