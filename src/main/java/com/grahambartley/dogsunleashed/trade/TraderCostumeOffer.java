package com.grahambartley.dogsunleashed.trade;

import com.grahambartley.dogsunleashed.ModItemTags;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class TraderCostumeOffer implements TradeOffers.Factory {

  public static final int MAX_USES = 1;
  public static final int MERCHANT_EXPERIENCE = 1;
  public static final float PRICE_MULTIPLIER = 0.05F;

  public static void register() {
    TradeOfferHelper.registerWanderingTraderOffers(
        TraderPuppyOffer.RARE_POOL, factories -> factories.add(new TraderCostumeOffer()));
  }

  @Override
  public @Nullable TradeOffer create(final Entity entity, final Random random) {
    final List<Item> costumes = costumes();
    if (costumes.isEmpty()) {
      return null;
    }
    final Item costume = costumes.get(random.nextInt(costumes.size()));
    return offerFor(costume, priceOf(entity.getWorld(), costume));
  }

  public static TradeOffer offerFor(final Item costume, final int emeralds) {
    return new TradeOffer(
        new TradedItem(Items.EMERALD, emeralds),
        new ItemStack(costume),
        MAX_USES,
        MERCHANT_EXPERIENCE,
        PRICE_MULTIPLIER);
  }

  public static List<Item> costumes() {
    final List<Item> costumes = new ArrayList<>();
    for (final RegistryEntry<Item> entry :
        Registries.ITEM.iterateEntries(ModItemTags.DOG_COSTUMES)) {
      costumes.add(entry.value());
    }
    return costumes;
  }

  public static int priceOf(final World world, final Item costume) {
    return CostumeTradePrice.emeralds(
        recipeIngredients(world, costume).stream().map(TraderCostumeOffer::tierOf).toList());
  }

  private static Set<Item> recipeIngredients(final World world, final Item costume) {
    for (final RecipeEntry<?> entry : world.getRecipeManager().values()) {
      if (entry.value().getResult(world.getRegistryManager()).isOf(costume)) {
        final Set<Item> items = new LinkedHashSet<>();
        for (final Ingredient ingredient : entry.value().getIngredients()) {
          Arrays.stream(ingredient.getMatchingStacks())
              .findFirst()
              .map(ItemStack::getItem)
              .ifPresent(items::add);
        }
        return items;
      }
    }
    return Set.of();
  }

  static CostumeTradePrice.Tier tierOf(final Item ingredient) {
    final ItemStack stack = new ItemStack(ingredient);
    if (stack.isIn(ModItemTags.RARE_COSTUME_INGREDIENTS)) {
      return CostumeTradePrice.Tier.RARE;
    }
    return stack.isIn(ItemTags.WOOL) ? CostumeTradePrice.Tier.WOOL : CostumeTradePrice.Tier.COMMON;
  }
}
