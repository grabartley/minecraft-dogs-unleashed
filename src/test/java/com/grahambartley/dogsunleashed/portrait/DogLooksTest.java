package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogLooksTest {

  @ParameterizedTest(name = "{0}, undead {1} -> {2}")
  @CsvSource({
    "HUSKY, false, husky",
    "HUSKY, true, zombie_husky",
    "DACHSHUND, false, dachshund",
    "DACHSHUND, true, zombie_dachshund",
    "BEAGLE, false, beagle",
    "BEAGLE, true, zombie_beagle",
    "GOLDEN_RETRIEVER, false, goldenretriever",
    "GOLDEN_RETRIEVER, true, zombie_goldenretriever",
    "SHIBA_INU, false, shibainu",
    "SHIBA_INU, true, zombie_shibainu",
    "DIRE_WOLF, false, direwolf",
    "DIRE_WOLF, true, zombie_direwolf",
    "CROSS_BREED, false, crossbreed",
    "CROSS_BREED, true, zombie_crossbreed"
  })
  @DisplayName("an undead look draws the zombie dog of its breed and a living one the plain dog")
  void entityTypeFollowsBreedAndUndead(
      final UnleashedDogBreed breed, final boolean undead, final String entityId) {
    final DogLook look =
        new DogLook(DogStatueLook.unseen(breed), DogPortraitOutfit.NONE, undead, false);

    assertEquals(
        Identifier.of("dogs-unleashed", entityId),
        Registries.ENTITY_TYPE.getId(DogLooks.entityType(look)));
  }
}
