package com.grahambartley.gametest.util;

import com.grahambartley.ModEntities;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.List;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public record DogTestData<T extends UnleashedDogEntity>(
    EntityType<T> entityType,
    Function<World, T> factory,
    String breedId,
    float expectedWidth,
    float expectedHeight,
    double expectedMaxHealth,
    double expectedMovementSpeed,
    double expectedAttackDamage,
    SoundEvent expectedBarkSound) {

  public static final DogTestData<HuskyEntity> HUSKY =
      new DogTestData<>(
          ModEntities.HUSKY,
          world -> new HuskyEntity(ModEntities.HUSKY, world),
          "husky",
          0.8f,
          1.1f,
          25.0,
          0.30,
          5.0,
          ModSounds.HUSKY_BARK);

  public static final DogTestData<DachshundEntity> DACHSHUND =
      new DogTestData<>(
          ModEntities.DACHSHUND,
          world -> new DachshundEntity(ModEntities.DACHSHUND, world),
          "dachshund",
          0.8f,
          1.1f,
          10.0,
          0.25,
          2.0,
          ModSounds.DACHSHUND_BARK);

  public static final DogTestData<BeagleEntity> BEAGLE =
      new DogTestData<>(
          ModEntities.BEAGLE,
          world -> new BeagleEntity(ModEntities.BEAGLE, world),
          "beagle",
          0.8f,
          1.1f,
          17.0,
          0.29,
          3.0,
          ModSounds.BEAGLE_BARK);

  public static final DogTestData<GoldenRetrieverEntity> GOLDEN_RETRIEVER =
      new DogTestData<>(
          ModEntities.GOLDEN_RETRIEVER,
          world -> new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world),
          "golden_retriever",
          0.8f,
          1.1f,
          24.0,
          0.30,
          4.0,
          ModSounds.GOLDEN_RETRIEVER_BARK);

  public static final DogTestData<ShibaInuEntity> SHIBA_INU =
      new DogTestData<>(
          ModEntities.SHIBA_INU,
          world -> new ShibaInuEntity(ModEntities.SHIBA_INU, world),
          "shiba_inu",
          0.8f,
          1.1f,
          18.0,
          0.32,
          3.5,
          ModSounds.SHIBA_INU_BARK);

  public static List<DogTestData<? extends UnleashedDogEntity>> getAllBreeds() {
    return List.of(HUSKY, DACHSHUND, BEAGLE, GOLDEN_RETRIEVER, SHIBA_INU);
  }
}
