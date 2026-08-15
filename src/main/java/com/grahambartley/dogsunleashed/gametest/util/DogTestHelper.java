package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogTestHelper {
  private static final BlockPos DEFAULT_SPAWN_POS = new BlockPos(0, 1, 0);

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
}
