package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.riding.RiddenDog;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

  @Shadow @Final private MinecraftClient client;

  @ModifyReturnValue(method = "shouldRenderExperience", at = @At("RETURN"))
  private boolean dogsUnleashed$giveTheBarToStamina(final boolean original) {
    return original && RiddenDog.of(this.client.player) == null;
  }
}
