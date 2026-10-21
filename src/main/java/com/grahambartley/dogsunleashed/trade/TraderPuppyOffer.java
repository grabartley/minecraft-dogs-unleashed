package com.grahambartley.dogsunleashed.trade;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.Nullable;

public final class TraderPuppyOffer implements TradeOffers.Factory {

  public static final int PRICE_EMERALDS = 12;

  public static void register() {
    TradeOfferHelper.registerWanderingTraderOffers(
        RareTraderOffer.RARE_POOL, factories -> factories.add(new TraderPuppyOffer()));
  }

  @Override
  public @Nullable TradeOffer create(final Entity entity, final Random random) {
    if (DogsUnleashed.SERVER_CONFIG.spawning().traderPuppies()) {
      return RareTraderOffer.sellOnce(new ItemStack(ModItems.PUPPY_BASKET), PRICE_EMERALDS);
    }
    final List<TradeOffers.Factory> others = otherRareOffers();
    return others.isEmpty()
        ? null
        : others.get(random.nextInt(others.size())).create(entity, random);
  }

  private static List<TradeOffers.Factory> otherRareOffers() {
    final TradeOffers.Factory[] pool =
        TradeOffers.WANDERING_TRADER_TRADES.get(RareTraderOffer.RARE_POOL);
    if (pool == null) {
      return List.of();
    }
    return Arrays.stream(pool).filter(factory -> !(factory instanceof TraderPuppyOffer)).toList();
  }
}
