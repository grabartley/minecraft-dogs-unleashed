package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.ModPendantItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public final class PendantTestHelper {

  public static final String ARENA = "dogs-unleashed:dog_arena";
  public static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  public static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);

  private PendantTestHelper() {}

  public static ServerPlayerEntity survivalOwnerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.changeGameMode(GameMode.SURVIVAL);
    placeAt(context, owner, relativePos);
    return owner;
  }

  public static void placeAt(
      final TestContext context, final PlayerEntity player, final BlockPos relativePos) {
    final BlockPos absolutePos = context.getAbsolutePos(relativePos);
    player.refreshPositionAndAngles(
        absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0f, 0f);
  }

  public static UnleashedDogEntity dogWearing(
      final TestContext context,
      final @Nullable PendantType pendant,
      final PlayerEntity owner,
      final BlockPos relativePos) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, relativePos, owner.getUuid());
    dog.setAiDisabled(true);
    if (pendant != null) {
      wear(dog, pendant);
    }
    return dog;
  }

  public static void wear(final UnleashedDogEntity dog, final PendantType pendant) {
    dog.getEquipmentHolder()
        .setStack(DogEquipmentSlot.PENDANT, new ItemStack(ModPendantItems.of(pendant)));
  }
}
