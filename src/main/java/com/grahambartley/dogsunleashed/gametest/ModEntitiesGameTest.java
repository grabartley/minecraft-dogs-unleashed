package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class ModEntitiesGameTest implements FabricGameTest {

  private static final int VANILLA_WOLF_TRACKING_RANGE_CHUNKS = 10;

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void everyDogTracksAsFarAsAVanillaWolf(final TestContext context) {
    Stream.of(UnleashedDogBreed.values())
        .flatMap(
            breed ->
                Stream.of(
                    ModEntities.getDogEntityType(breed), ModEntities.getZombieDogEntityType(breed)))
        .forEach(type -> assertWolfTrackingRange(context, type));
    context.complete();
  }

  private static void assertWolfTrackingRange(final TestContext context, final EntityType<?> type) {
    context.assertTrue(
        type.getMaxTrackDistance() == VANILLA_WOLF_TRACKING_RANGE_CHUNKS,
        Registries.ENTITY_TYPE.getId(type)
            + " should track to "
            + VANILLA_WOLF_TRACKING_RANGE_CHUNKS
            + " chunks like a wolf but tracks to "
            + type.getMaxTrackDistance());
  }
}
