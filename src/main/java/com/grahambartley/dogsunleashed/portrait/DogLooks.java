package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DogLooks {

  private DogLooks() {}

  public static @Nullable EntityType<UnleashedDogEntity> entityType(final DogLook look) {
    final UnleashedDogBreed breed = look.statue().breed();
    return look.undead()
        ? ModEntities.getZombieDogEntityType(breed)
        : ModEntities.getDogEntityType(breed);
  }

  public static @Nullable UnleashedDogEntity create(final World world, final DogLook look) {
    final EntityType<UnleashedDogEntity> type = entityType(look);
    final UnleashedDogEntity dog = type == null ? null : type.create(world);
    if (dog != null) {
      dress(dog, look);
    }
    return dog;
  }

  public static void dress(final UnleashedDogEntity dog, final DogLook look) {
    look.statue().applyTo(dog);
    DogPortraitOutfits.dress(dog, look.outfit());
    dog.getComponents().get(DogAging.class).showSeniorCoat(look.senior());
  }
}
