package com.grahambartley.dogsunleashed.trade;

import com.grahambartley.dogsunleashed.ModItemTags;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class TraderCostumeOffer implements TradeOffers.Factory {

  public static void register() {
    TradeOfferHelper.registerWanderingTraderOffers(
        RareTraderOffer.RARE_POOL, factories -> factories.add(new TraderCostumeOffer()));
  }

  @Override
  public @Nullable TradeOffer create(final Entity entity, final Random random) {
    final List<Item> costumes = costumes();
    if (costumes.isEmpty()) {
      return null;
    }
    final Item costume = costumes.get(random.nextInt(costumes.size()));
    return RareTraderOffer.sellOnce(new ItemStack(costume), priceOf(entity.getWorld(), costume));
  }

  public static int priceOf(final World world, final Item costume) {
    return CostumeRecipeIngredients.recipesOf(world, costume).stream()
        .mapToInt(CostumeTradePrice::emeralds)
        .min()
        .orElse(CostumeTradePrice.emeralds(List.of()));
  }

  private static List<Item> costumes() {
    final List<Item> costumes = new ArrayList<>();
    for (final RegistryEntry<Item> entry :
        Registries.ITEM.iterateEntries(ModItemTags.DOG_COSTUMES)) {
      costumes.add(entry.value());
    }
    return costumes;
  }
}
