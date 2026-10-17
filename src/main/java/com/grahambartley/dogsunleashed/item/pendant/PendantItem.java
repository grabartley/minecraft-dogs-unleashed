package com.grahambartley.dogsunleashed.item.pendant;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PendantItem extends Item {

  static final String NO_STACKING_KEY = "item.dogs-unleashed.pendant.no_stacking";

  private final PendantType type;

  public PendantItem(final PendantType type, final Item.Settings settings) {
    super(settings);
    this.type = type;
  }

  public PendantType type() {
    return this.type;
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    tooltip.add(Text.translatable(this.type.effectTranslationKey()).formatted(Formatting.GRAY));
    if (this.type.family().ownerFacing()) {
      tooltip.add(Text.translatable(NO_STACKING_KEY).formatted(Formatting.DARK_GRAY));
    }
  }
}
