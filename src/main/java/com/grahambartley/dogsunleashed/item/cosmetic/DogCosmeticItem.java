package com.grahambartley.dogsunleashed.item.cosmetic;

import java.util.List;
import java.util.Optional;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class DogCosmeticItem extends Item {

  private final DogCosmetic cosmetic;

  public DogCosmeticItem(final DogCosmetic cosmetic, final Item.Settings settings) {
    super(settings);
    this.cosmetic = cosmetic;
  }

  public DogCosmetic cosmetic() {
    return this.cosmetic;
  }

  public static Optional<DogCosmetic> cosmeticOf(final ItemStack stack) {
    return stack.getItem() instanceof DogCosmeticItem item
        ? Optional.of(item.cosmetic)
        : Optional.empty();
  }

  public static boolean takesBannerPatterns(final ItemStack stack) {
    return cosmeticOf(stack).filter(DogCosmetic::takesBannerPatterns).isPresent();
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    if (this.cosmetic.takesBannerPatterns()) {
      BannerItem.appendBannerTooltip(stack, tooltip);
    }
  }
}
