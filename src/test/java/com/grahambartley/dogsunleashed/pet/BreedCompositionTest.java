package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BreedCompositionTest {

  private final Map<UUID, PetData> pets = new HashMap<>();

  private UUID registerDog(final UnleashedDogBreed breed) {
    return registerDog(breed, null, null);
  }

  private UUID registerDog(
      final UnleashedDogBreed breed, final UUID parentAId, final UUID parentBId) {
    final UUID id = UUID.randomUUID();
    final PetData pet =
        new PetData(
            id,
            UUID.randomUUID(),
            breed,
            "Dog",
            10.0f,
            10.0f,
            new BlockPos(0, 64, 0),
            "minecraft:overworld",
            true);
    pet.recordParents(parentAId, parentBId);
    pets.put(id, pet);
    return id;
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a dog without recorded parents is a pure breed")
  void dogWithoutParentsIsPureBreed(final UnleashedDogBreed breed) {
    final UUID dogId = registerDog(breed);

    assertEquals(
        List.of(new BreedShare(breed, 1.0f)), BreedComposition.compute(dogId, breed, pets::get));
  }

  @Test
  @DisplayName("an unknown dog falls back to a pure composition of the given breed")
  void unknownDogFallsBackToPureBreed() {
    assertEquals(
        List.of(new BreedShare(UnleashedDogBreed.BEAGLE, 1.0f)),
        BreedComposition.compute(UUID.randomUUID(), UnleashedDogBreed.BEAGLE, pets::get));
  }

  @Test
  @DisplayName("a child of two different pure breeds is half of each, largest share first")
  void mixedParentsAverageToHalves() {
    final UUID husky = registerDog(UnleashedDogBreed.HUSKY);
    final UUID beagle = registerDog(UnleashedDogBreed.BEAGLE);
    final UUID child = registerDog(UnleashedDogBreed.HUSKY, husky, beagle);

    assertEquals(
        List.of(
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
            new BreedShare(UnleashedDogBreed.HUSKY, 0.5f)),
        BreedComposition.compute(child, UnleashedDogBreed.HUSKY, pets::get));
  }

  @Test
  @DisplayName("a second generation mix quarters the grandparent's breed")
  void secondGenerationMixQuartersGrandparentBreed() {
    final UUID husky = registerDog(UnleashedDogBreed.HUSKY);
    final UUID beagle = registerDog(UnleashedDogBreed.BEAGLE);
    final UUID mixedParent = registerDog(UnleashedDogBreed.HUSKY, husky, beagle);
    final UUID pureParent = registerDog(UnleashedDogBreed.HUSKY);
    final UUID child = registerDog(UnleashedDogBreed.HUSKY, mixedParent, pureParent);

    assertEquals(
        List.of(
            new BreedShare(UnleashedDogBreed.HUSKY, 0.75f),
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.25f)),
        BreedComposition.compute(child, UnleashedDogBreed.HUSKY, pets::get));
  }

  @Test
  @DisplayName("a missing parent record contributes the dog's own breed")
  void missingParentContributesOwnBreed() {
    final UUID beagle = registerDog(UnleashedDogBreed.BEAGLE);
    final UUID child = registerDog(UnleashedDogBreed.HUSKY, beagle, UUID.randomUUID());

    assertEquals(
        List.of(
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
            new BreedShare(UnleashedDogBreed.HUSKY, 0.5f)),
        BreedComposition.compute(child, UnleashedDogBreed.HUSKY, pets::get));
  }

  @Test
  @DisplayName("equal shares order deterministically by breed id")
  void equalSharesOrderByBreedId() {
    final UUID shiba = registerDog(UnleashedDogBreed.SHIBA_INU);
    final UUID dachshund = registerDog(UnleashedDogBreed.DACHSHUND);
    final UUID child = registerDog(UnleashedDogBreed.SHIBA_INU, shiba, dachshund);

    assertEquals(
        List.of(
            new BreedShare(UnleashedDogBreed.DACHSHUND, 0.5f),
            new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f)),
        BreedComposition.compute(child, UnleashedDogBreed.SHIBA_INU, pets::get));
  }

  @Test
  @DisplayName("a cyclic lineage still terminates at the generation cap")
  void cyclicLineageTerminates() {
    final UUID a = UUID.randomUUID();
    final UUID b = UUID.randomUUID();
    final PetData dogA =
        new PetData(
            a,
            UUID.randomUUID(),
            UnleashedDogBreed.HUSKY,
            "A",
            10.0f,
            10.0f,
            new BlockPos(0, 64, 0),
            "minecraft:overworld",
            true);
    dogA.recordParents(b, null);
    final PetData dogB =
        new PetData(
            b,
            UUID.randomUUID(),
            UnleashedDogBreed.BEAGLE,
            "B",
            10.0f,
            10.0f,
            new BlockPos(0, 64, 0),
            "minecraft:overworld",
            true);
    dogB.recordParents(a, null);
    pets.put(a, dogA);
    pets.put(b, dogB);

    final List<BreedShare> composition =
        BreedComposition.compute(a, UnleashedDogBreed.HUSKY, pets::get);

    assertEquals(1.0f, composition.stream().map(BreedShare::share).reduce(0.0f, Float::sum), 1e-4f);
  }
}
