package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.DogTreatBuff;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A treat fed to a tamed dog for a short buff.
 *
 * <p>The feed interaction itself lives in {@code UnleashedDogEntity.interactMob} rather than {@code
 * useOnEntity}: vanilla dispatches {@code interactMob} first, and the dog's owner branch opens the
 * command wheel, so an item-side hook would never be reached for the one player allowed to feed.
 */
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
