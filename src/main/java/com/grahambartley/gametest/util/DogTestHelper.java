package com.grahambartley.gametest.util;

import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogTestHelper {
  private static final BlockPos DEFAULT_SPAWN_POS = new BlockPos(0, 1, 0);

  private DogTestHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static <T extends UnleashedDogEntity> T spawnDog(
      TestContext context, DogTestData<T> data) {
    return spawnDog(context, data, DEFAULT_SPAWN_POS);
  }

  public static <T extends UnleashedDogEntity> T spawnDog(
      TestContext context, DogTestData<T> data, BlockPos pos) {
    ServerWorld world = context.getWorld();
    T dog = data.factory().apply(world);
    dog.refreshPositionAndAngles(pos, 0.0f, 0.0f);
    world.spawnEntity(dog);
    return dog;
  }

  public static <T extends UnleashedDogEntity> T spawnTamedDog(
      TestContext context, DogTestData<T> data) {
    return spawnTamedDog(context, data, DEFAULT_SPAWN_POS);
  }

  public static <T extends UnleashedDogEntity> T spawnTamedDog(
      TestContext context, DogTestData<T> data, BlockPos pos) {
    T dog = spawnDog(context, data, pos);
    dog.setTamed(true, true);
    return dog;
  }

  public static void damageEntity(UnleashedDogEntity entity, float amount) {
    entity.damage(entity.getWorld().getDamageSources().generic(), amount);
  }
}
