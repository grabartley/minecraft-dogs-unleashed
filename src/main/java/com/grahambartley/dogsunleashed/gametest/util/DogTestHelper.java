package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogTestHelper {
  private static final BlockPos DEFAULT_SPAWN_POS = new BlockPos(0, 1, 0);
  private static final BlockPos ARENA_CLEAR_STANDING_POS = new BlockPos(5, 2, 5);

  private DogTestHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static UnleashedDogEntity spawnDog(TestContext context, DogTestData data) {
    return spawnDog(context, data, DEFAULT_SPAWN_POS);
  }

  public static UnleashedDogEntity spawnDog(
      TestContext context, DogTestData data, BlockPos relativePos) {
    return context.spawnEntity(data.entityType(), relativePos);
  }

  public static UnleashedDogEntity spawnTamedDog(TestContext context, DogTestData data) {
    return spawnTamedDog(context, data, DEFAULT_SPAWN_POS);
  }

  public static UnleashedDogEntity spawnTamedDog(
      TestContext context, DogTestData data, BlockPos relativePos) {
    UnleashedDogEntity dog = spawnDog(context, data, relativePos);
    dog.setTamed(true, true);
    return dog;
  }

  public static UnleashedDogEntity spawnTamedDog(
      TestContext context, DogTestData data, BlockPos pos, UUID ownerUuid) {
    UnleashedDogEntity dog = spawnDog(context, data, pos);
    dog.setOwnerUuid(ownerUuid);
    dog.setTamed(true, true);
    return dog;
  }

  public static void damageEntity(UnleashedDogEntity entity, float amount) {
    entity.damage(entity.getWorld().getDamageSources().generic(), amount);
  }

  public static List<ItemStack> droppedStacksOf(TestContext context, Item item) {
    final Box searchBox = context.getTestBox().expand(2.0);
    return context
        .getWorld()
        .getEntitiesByClass(ItemEntity.class, searchBox, entity -> true)
        .stream()
        .map(ItemEntity::getStack)
        .filter(stack -> stack.isOf(item))
        .toList();
  }

  /** Stands the player where nothing the arena tests place or break can collide with them. */
  public static ServerPlayerEntity mockPlayerStandingClearInArena(TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d clear = Vec3d.ofBottomCenter(context.getAbsolutePos(ARENA_CLEAR_STANDING_POS));
    player.refreshPositionAndAngles(clear.x, clear.y, clear.z, 0.0f, 0.0f);
    return player;
  }

  /** Drives the real placement path so component-carried state travels the way it does in play. */
  public static void placeStackOnTopOf(
      TestContext context, ServerPlayerEntity player, ItemStack stack, BlockPos relFloor) {
    player.setStackInHand(Hand.MAIN_HAND, stack);
    final BlockPos absFloor = context.getAbsolutePos(relFloor);
    final BlockHitResult hit =
        new BlockHitResult(Vec3d.ofCenter(absFloor), Direction.UP, absFloor, false);
    stack.useOnBlock(new ItemUsageContext(context.getWorld(), player, Hand.MAIN_HAND, stack, hit));
  }
}
