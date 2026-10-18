package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {

  @ModifyExpressionValue(
      method = "drawBackground",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
  private Item dogsUnleashed$previewCosmeticOnPlainBanner(final Item item) {
    return item instanceof DogCosmeticItem ? Items.WHITE_BANNER : item;
  }
}
