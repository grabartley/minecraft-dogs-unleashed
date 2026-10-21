package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModCostumeItems;
import com.grahambartley.dogsunleashed.ModItemTags;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import com.grahambartley.dogsunleashed.trade.CostumeTradePrice;
import com.grahambartley.dogsunleashed.trade.TraderCostumeOffer;
import com.grahambartley.dogsunleashed.trade.TraderPuppyOffer;
import java.util.Arrays;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

public final class TraderCostumeOfferGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_TRADER = new BlockPos(3, 2, 3);
  private static final int ROLLS = 50;

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void theRareTraderPoolHoldsOneCostumeOfferBesideThePuppyBasket(final TestContext context) {
    final TradeOffers.Factory[] pool =
        TradeOffers.WANDERING_TRADER_TRADES.get(TraderPuppyOffer.RARE_POOL);
    final long costumes = Arrays.stream(pool).filter(TraderCostumeOffer.class::isInstance).count();
    final long baskets = Arrays.stream(pool).filter(TraderPuppyOffer.class::isInstance).count();
    context.assertTrue(
        costumes == 1, "Expected one costume offer in the rare pool, not " + costumes);
    context.assertTrue(
        baskets == 1, "Expected one puppy basket offer in the rare pool, not " + baskets);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aTraderRollsATaggedCostumeAtItsRecipePrice(final TestContext context) {
    final WanderingTraderEntity trader =
        context.spawnEntity(EntityType.WANDERING_TRADER, REL_TRADER);
    trader.setAiDisabled(true);
    for (int roll = 0; roll < ROLLS; roll++) {
      final TradeOffer offer = new TraderCostumeOffer().create(trader, trader.getRandom());
      context.assertTrue(offer != null, "The costume offer rolled nothing");
      final Item costume = offer.getSellItem().getItem();
      context.assertTrue(
          offer.getSellItem().isIn(ModItemTags.DOG_COSTUMES), "The trader sold " + costume);
      context.assertTrue(
          offer.getOriginalFirstBuyItem().isOf(Items.EMERALD),
          "The costume is not sold for emeralds");
      context.assertEquals(
          offer.getOriginalFirstBuyItem().getCount(),
          TraderCostumeOffer.priceOf(context.getWorld(), costume),
          "Price of " + costume);
    }
    trader.discard();
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void aWoolOnlyCostumeIsTheCheapest(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final int sheep = price(world, DogCostume.SHEEP);
    context.assertEquals(sheep, CostumeTradePrice.BASE_EMERALDS, "Sheep costume price");
    for (final DogCostume costume : DogCostume.values()) {
      context.assertTrue(
          costume == DogCostume.SHEEP || price(world, costume) > sheep,
          costume + " should cost more than the sheep costume");
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void theEnderDragonAndWardenCostumesCostTheMost(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final int top =
        Math.min(price(world, DogCostume.ENDER_DRAGON), price(world, DogCostume.WARDEN));
    for (final DogCostume costume : DogCostume.values()) {
      if (costume != DogCostume.ENDER_DRAGON && costume != DogCostume.WARDEN) {
        context.assertTrue(
            price(world, costume) < top,
            costume + " costs " + price(world, costume) + ", not less than " + top);
      }
    }
    context.complete();
  }

  private static int price(final ServerWorld world, final DogCostume costume) {
    return TraderCostumeOffer.priceOf(world, ModCostumeItems.of(costume));
  }
}
