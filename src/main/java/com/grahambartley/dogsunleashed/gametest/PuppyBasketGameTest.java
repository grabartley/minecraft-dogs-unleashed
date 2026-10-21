package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.trade.RareTraderOffer;
import com.grahambartley.dogsunleashed.trade.TraderPuppyOffer;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

public final class PuppyBasketGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_GROUND = new BlockPos(3, FLOOR_Y - 1, 3);
  private static final Vec3d REL_PLAYER = new Vec3d(5.5, FLOOR_Y, 3.5);
  private static final int OFF_ROLLS = 50;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBasketReleasesAPuppyTamedToWhoeverOpensIt(final TestContext context) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ItemStack basket = new ItemStack(ModItems.PUPPY_BASKET);
    player.setStackInHand(Hand.MAIN_HAND, basket);
    context.useStackOnBlock(player, basket, REL_GROUND, Direction.UP);
    final List<UnleashedDogEntity> dogs =
        context
            .getWorld()
            .getEntitiesByClass(UnleashedDogEntity.class, context.getTestBox(), dog -> true);
    context.assertTrue(dogs.size() == 1, "Expected one puppy but found " + dogs.size());
    final UnleashedDogEntity puppy = dogs.get(0);
    context.assertTrue(puppy.isBaby(), "The basket released an adult");
    context.assertTrue(
        puppy.isTamed() && player.getUuid().equals(puppy.getOwnerUuid()),
        "The puppy is not tamed to the player who opened the basket");
    context.assertTrue(basket.isEmpty(), "The basket was not used up");
    SurvivalTestPlayer.leave(context, player);
    puppy.discard();
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void theWanderingTraderCanRollAPuppyBasket(final TestContext context) {
    final long registered =
        Arrays.stream(TradeOffers.WANDERING_TRADER_TRADES.get(RareTraderOffer.RARE_POOL))
            .filter(TraderPuppyOffer.class::isInstance)
            .count();
    context.assertTrue(
        registered == 1,
        "The rare wandering trader pool should hold one puppy basket offer, not " + registered);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void withTraderPuppiesOffTheTraderSellsSomethingElse(final TestContext context) {
    final WanderingTraderEntity trader =
        context.spawnEntity(EntityType.WANDERING_TRADER, new BlockPos(3, FLOOR_Y, 3));
    trader.setAiDisabled(true);
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = before.withSpawning(before.spawning().withTraderPuppies(false));
      for (int roll = 0; roll < OFF_ROLLS; roll++) {
        final TradeOffer offer = new TraderPuppyOffer().create(trader, trader.getRandom());
        context.assertTrue(offer != null, "The rare slot was left empty");
        context.assertFalse(
            offer.getSellItem().isOf(ModItems.PUPPY_BASKET),
            "The trader sold a puppy basket with Trader Puppies off");
      }
    } finally {
      DogsUnleashed.SERVER_CONFIG = before;
    }
    trader.discard();
    context.complete();
  }
}
