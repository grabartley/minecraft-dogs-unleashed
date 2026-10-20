package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModPortraits;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetNames;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BlankCanvasItem extends Item {

  public BlankCanvasItem(final Settings settings) {
    super(settings);
  }

  public static PortraitSize sizeOf(final ItemStack canvas) {
    return canvas.getOrDefault(ModComponents.PORTRAIT_SIZE, PortraitSize.SMALL);
  }

  public static MutableText dimensionsOf(final PortraitSize size) {
    return Text.translatable("painting.dimensions", size.width(), size.height());
  }

  public static DogPortrait paint(
      final ItemStack canvas, final PlayerEntity painter, final UnleashedDogEntity dog) {
    final DogPortrait portrait =
        DogPortrait.painted(DogStatueLook.of(dog), titleOf(dog), sizeOf(canvas));
    canvas.decrementUnlessCreative(1, painter);
    final ItemStack painting = ModPortraits.portraitStack(portrait);
    if (!painter.getInventory().insertStack(painting)) {
      painter.dropItem(painting, false);
    }
    dog.getWorld()
        .playSound(
            null,
            dog.getBlockPos(),
            SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f);
    return portrait;
  }

  private static String titleOf(final UnleashedDogEntity dog) {
    return dog.hasCustomName() ? dog.getCustomName().getString() : PetNames.tamedNameOf(dog);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack canvas = user.getStackInHand(hand);
    if (!user.isSneaking()) {
      return TypedActionResult.pass(canvas);
    }
    if (!world.isClient) {
      final PortraitSize size = sizeOf(canvas).next();
      canvas.set(ModComponents.PORTRAIT_SIZE, size);
      user.sendMessage(
          Text.translatable("item.dogs-unleashed.blank_canvas.size", dimensionsOf(size)), true);
    }
    return TypedActionResult.success(canvas, world.isClient);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    tooltip.add(dimensionsOf(sizeOf(stack)).formatted(Formatting.GRAY));
    tooltip.add(
        Text.translatable("item.dogs-unleashed.blank_canvas.hint").formatted(Formatting.GRAY));
  }
}
