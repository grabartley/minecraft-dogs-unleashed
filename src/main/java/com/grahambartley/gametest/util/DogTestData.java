package com.grahambartley.gametest.util;

import com.grahambartley.ModEntities;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.List;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public record DogTestData<T extends UnleashedDogEntity>(
    EntityType<T> entityType,
    Function<World, T> factory,
    UnleashedDogBreed breed,
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
          UnleashedDogBreed.HUSKY,
          UnleashedDogBreed.HUSKY.dimensions().width(),
          UnleashedDogBreed.HUSKY.dimensions().height(),
          UnleashedDogBreed.HUSKY.attributes().maxHealth(),
          UnleashedDogBreed.HUSKY.attributes().movementSpeed(),
          UnleashedDogBreed.HUSKY.attributes().attackDamage(),
          ModSounds.HUSKY_BARK);

  public static final DogTestData<DachshundEntity> DACHSHUND =
      new DogTestData<>(
          ModEntities.DACHSHUND,
          world -> new DachshundEntity(ModEntities.DACHSHUND, world),
          UnleashedDogBreed.DACHSHUND,
          UnleashedDogBreed.DACHSHUND.dimensions().width(),
          UnleashedDogBreed.DACHSHUND.dimensions().height(),
          UnleashedDogBreed.DACHSHUND.attributes().maxHealth(),
          UnleashedDogBreed.DACHSHUND.attributes().movementSpeed(),
          UnleashedDogBreed.DACHSHUND.attributes().attackDamage(),
          ModSounds.DACHSHUND_BARK);

  public static final DogTestData<BeagleEntity> BEAGLE =
      new DogTestData<>(
          ModEntities.BEAGLE,
          world -> new BeagleEntity(ModEntities.BEAGLE, world),
          UnleashedDogBreed.BEAGLE,
          UnleashedDogBreed.BEAGLE.dimensions().width(),
          UnleashedDogBreed.BEAGLE.dimensions().height(),
          UnleashedDogBreed.BEAGLE.attributes().maxHealth(),
          UnleashedDogBreed.BEAGLE.attributes().movementSpeed(),
          UnleashedDogBreed.BEAGLE.attributes().attackDamage(),
          ModSounds.BEAGLE_BARK);

  public static final DogTestData<GoldenRetrieverEntity> GOLDEN_RETRIEVER =
      new DogTestData<>(
          ModEntities.GOLDEN_RETRIEVER,
          world -> new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world),
          UnleashedDogBreed.GOLDEN_RETRIEVER,
          UnleashedDogBreed.GOLDEN_RETRIEVER.dimensions().width(),
          UnleashedDogBreed.GOLDEN_RETRIEVER.dimensions().height(),
          UnleashedDogBreed.GOLDEN_RETRIEVER.attributes().maxHealth(),
          UnleashedDogBreed.GOLDEN_RETRIEVER.attributes().movementSpeed(),
          UnleashedDogBreed.GOLDEN_RETRIEVER.attributes().attackDamage(),
          ModSounds.GOLDEN_RETRIEVER_BARK);

  public static final DogTestData<ShibaInuEntity> SHIBA_INU =
      new DogTestData<>(
          ModEntities.SHIBA_INU,
          world -> new ShibaInuEntity(ModEntities.SHIBA_INU, world),
          UnleashedDogBreed.SHIBA_INU,
          UnleashedDogBreed.SHIBA_INU.dimensions().width(),
          UnleashedDogBreed.SHIBA_INU.dimensions().height(),
          UnleashedDogBreed.SHIBA_INU.attributes().maxHealth(),
          UnleashedDogBreed.SHIBA_INU.attributes().movementSpeed(),
          UnleashedDogBreed.SHIBA_INU.attributes().attackDamage(),
          ModSounds.SHIBA_INU_BARK);

  public static List<DogTestData<? extends UnleashedDogEntity>> getAllBreeds() {
    return List.of(HUSKY, DACHSHUND, BEAGLE, GOLDEN_RETRIEVER, SHIBA_INU);
  }
}
