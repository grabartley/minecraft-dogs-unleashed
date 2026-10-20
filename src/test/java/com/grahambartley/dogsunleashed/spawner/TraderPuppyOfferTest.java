package com.grahambartley.dogsunleashed.spawner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TraderPuppyOfferTest {

  private final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;

  @AfterEach
  void restoreConfig() {
    DogsUnleashed.SERVER_CONFIG = this.original;
  }

  @ParameterizedTest
  @ValueSource(longs = {1L, 2L, 3L, 42L, 1234L})
  void sellsOnePuppyBasketForTwelveEmeralds(final long seed) {
    DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.defaults();
    final TradeOffer offer = new TraderPuppyOffer().create(null, Random.create(seed));
    assertNotNull(offer);
    assertEquals(Items.EMERALD, offer.getOriginalFirstBuyItem().getItem());
    assertEquals(12, offer.getOriginalFirstBuyItem().getCount());
    assertEquals(ModItems.PUPPY_BASKET, offer.getSellItem().getItem());
    assertEquals(1, offer.getSellItem().getCount());
    assertEquals(1, offer.getMaxUses());
  }
}
