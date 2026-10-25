package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogLooksTest {

  static Stream<Arguments> breedsAndLife() {
    return Arrays.stream(UnleashedDogBreed.values())
        .flatMap(breed -> Stream.of(Arguments.of(breed, false), Arguments.of(breed, true)));
  }

  @ParameterizedTest(name = "{0}, undead {1}")
  @MethodSource("breedsAndLife")
  @DisplayName("an undead look draws the zombie dog of its breed and a living one the plain dog")
  void entityTypeFollowsBreedAndUndead(final UnleashedDogBreed breed, final boolean undead) {
    final DogLook look =
        new DogLook(DogStatueLook.unseen(breed), DogPortraitOutfit.NONE, undead, false);

    assertSame(
        undead ? ModEntities.getZombieDogEntityType(breed) : ModEntities.getDogEntityType(breed),
        DogLooks.entityType(look));
  }
}
