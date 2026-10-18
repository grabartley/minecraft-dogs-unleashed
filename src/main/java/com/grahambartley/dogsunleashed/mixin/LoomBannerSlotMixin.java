package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.screen.LoomScreenHandler$3")
public abstract class LoomBannerSlotMixin {

  @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
  private boolean dogsUnleashed$acceptPatternableCosmetics(
      final boolean original, final ItemStack stack) {
    return original || DogCosmeticItem.takesBannerPatterns(stack);
  }
}
