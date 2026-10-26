package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetNamesTest {

  @Test
  @DisplayName("a registered pet is called by the name its owner gave it")
  void registeredPetUsesItsName() {
    final PetData pet = PetDataFixture.pet(UnleashedDogBreed.HUSKY, "Balto");

    assertEquals("Balto", PetNames.nameOf(pet, UnleashedDogBreed.HUSKY));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a dog with no pet record is called by its breed name")
  void unregisteredDogUsesBreedName(final UnleashedDogBreed breed) {
    assertEquals(
        Text.translatable(breed.translationKey()).getString(), PetNames.nameOf(null, breed));
  }
}
