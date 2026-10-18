package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.BannerItem;
import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(LoomScreenHandler.class)
public abstract class LoomScreenHandlerMixin {

  @WrapOperation(method = "quickMove", constant = @Constant(classValue = BannerItem.class))
  private boolean dogsUnleashed$shiftClickPatternableCosmetics(
      final Object item, final Operation<Boolean> original) {
    return original.call(item)
        || (item instanceof DogCosmeticItem cosmetic && cosmetic.cosmetic().takesBannerPatterns());
  }
}
