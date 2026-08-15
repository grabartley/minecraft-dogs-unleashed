package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record DogTestData(
    EntityType<UnleashedDogEntity> entityType,
    Function<World, UnleashedDogEntity> factory,
    UnleashedDogBreed breed,
    float expectedWidth,
    float expectedHeight,
    double expectedMaxHealth,
    double expectedMovementSpeed,
    double expectedAttackDamage,
    @Nullable SoundEvent expectedBarkSound) {

  public static final DogTestData HUSKY = fromPreset(UnleashedDogBreed.HUSKY, null);

  public static final DogTestData DACHSHUND =
      fromPreset(UnleashedDogBreed.DACHSHUND, ModSounds.DACHSHUND_BARK);

  public static final DogTestData BEAGLE =
      fromPreset(UnleashedDogBreed.BEAGLE, ModSounds.BEAGLE_BARK);

  public static final DogTestData GOLDEN_RETRIEVER =
      fromPreset(UnleashedDogBreed.GOLDEN_RETRIEVER, ModSounds.GOLDEN_RETRIEVER_BARK);

  public static final DogTestData SHIBA_INU =
      fromPreset(UnleashedDogBreed.SHIBA_INU, ModSounds.SHIBA_INU_BARK);

  private static DogTestData fromPreset(
      final UnleashedDogBreed breed, final @Nullable SoundEvent expectedBarkSound) {
    final EntityType<UnleashedDogEntity> entityType = ModEntities.getDogEntityType(breed);
    return new DogTestData(
        entityType,
        entityType::create,
        breed,
        breed.dimensions().width(),
        breed.dimensions().height(),
        breed.attributes().maxHealth(),
        breed.attributes().movementSpeed(),
        breed.attributes().attackDamage(),
        expectedBarkSound);
  }

  public static List<DogTestData> getAllBreeds() {
    return List.of(HUSKY, DACHSHUND, BEAGLE, GOLDEN_RETRIEVER, SHIBA_INU);
  }
}
