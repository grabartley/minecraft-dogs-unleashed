package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogLookTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a plain look is the body alone: living, not senior and wearing nothing")
  void plainLookIsTheBodyAlone(final UnleashedDogBreed breed) {
    final DogStatueLook statue = DogStatueLook.unseen(breed);

    final DogLook look = DogLook.plain(statue);

    assertEquals(statue, look.statue());
    assertEquals(DogPortraitOutfit.NONE, look.outfit());
    assertFalse(look.undead());
    assertFalse(look.senior());
  }
}
