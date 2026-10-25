package com.grahambartley.dogsunleashed.gametest.util;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;

public final class SaddlebagsTestHelper {

  private SaddlebagsTestHelper() {}

  public static UnleashedDogEntity ownedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  public static UnleashedDogEntity wearingBags(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = ownedDog(context, owner);
    dog.getEquipmentHolder()
        .setStack(DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS));
    return dog;
  }

  public static SimpleInventory bags(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogSaddlebags.class).contents();
  }

  public static DogEquipmentScreenHandler open(
      final ServerPlayerEntity player, final UnleashedDogEntity dog) {
    return new DogEquipmentScreenHandler(1, player.getInventory(), dog);
  }
}
