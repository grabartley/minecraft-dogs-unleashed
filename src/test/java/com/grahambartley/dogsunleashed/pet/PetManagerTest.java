package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetManagerTest {

  static Stream<DogPortraitOutfit> outfits() {
    return DogPortraitOutfitFixture.samples();
  }

  @ParameterizedTest
  @MethodSource("outfits")
  @DisplayName("a saved pet keeps its outfit when the manager is saved and reloaded")
  void savedPetKeepsItsOutfit(final DogPortraitOutfit outfit) {
    final UUID ownerId = UUID.randomUUID();
    final PetData pet = PetDataFixture.wearing(PetDataFixture.pet(ownerId), outfit);
    final PetManager manager = new PetManager();
    manager.registerPet(pet);

    final PetManager reloaded =
        PetManager.fromNbt(manager.writeNbt(new NbtCompound(), REGISTRIES), REGISTRIES);

    assertEquals(outfit, reloaded.getPet(ownerId, pet.getPetId()).getOutfit());
  }
}
