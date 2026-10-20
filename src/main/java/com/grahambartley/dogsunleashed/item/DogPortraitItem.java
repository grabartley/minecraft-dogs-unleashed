package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.DogPortraitEntity;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DogPortraitItem extends Item {

  public DogPortraitItem(final Settings settings) {
    super(settings);
  }

  @Override
  public ActionResult useOnBlock(final ItemUsageContext context) {
    final ItemStack stack = context.getStack();
    final DogPortrait portrait = stack.get(ModComponents.DOG_PORTRAIT);
    final Direction side = context.getSide();
    final BlockPos pos = context.getBlockPos().offset(side);
    final PlayerEntity player = context.getPlayer();
    if (portrait == null
        || side.getAxis().isVertical()
        || (player != null && !player.canPlaceOn(pos, side, stack))) {
      return ActionResult.FAIL;
    }
    final World world = context.getWorld();
    final DogPortraitEntity entity = new DogPortraitEntity(world, pos, side, portrait);
    if (!entity.canStayAttached()) {
      return ActionResult.CONSUME;
    }
    if (!world.isClient) {
      entity.onPlace();
      world.emitGameEvent(player, GameEvent.ENTITY_PLACE, entity.getPos());
      world.spawnEntity(entity);
    }
    stack.decrement(1);
    return ActionResult.success(world.isClient);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    final DogPortrait portrait = stack.get(ModComponents.DOG_PORTRAIT);
    if (portrait == null) {
      return;
    }
    if (!portrait.name().isEmpty()) {
      tooltip.add(Text.literal(portrait.name()).formatted(Formatting.YELLOW));
    }
    tooltip.add(BlankCanvasItem.dimensionsOf(portrait.size()).formatted(Formatting.GRAY));
  }
}
