package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.DogTreatBuff;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DogTreatItem extends Item {

  public DogTreatItem(Settings settings) {
    super(settings);
  }

  @Override
  public void appendTooltip(
      ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    super.appendTooltip(stack, context, tooltip, type);
    tooltip.add(
        Text.translatable("item.dogs-unleashed.dog_treat.tooltip").formatted(Formatting.GRAY));
    tooltip.add(
        Text.translatable(
                "item.dogs-unleashed.dog_treat.effect",
                DogTreatBuff.DURATION_SECONDS,
                (int) Math.round(DogTreatBuff.MOVEMENT_SPEED_BONUS_FRACTION * 100),
                (int) DogTreatBuff.ATTACK_DAMAGE_BONUS)
            .formatted(Formatting.BLUE));
  }
}
