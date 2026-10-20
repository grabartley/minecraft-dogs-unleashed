package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogStatueLookTest {

  private static final DogGenome MOSTLY_HUSKY =
      new DogGenome(
          List.of(
              new BreedShare(UnleashedDogBreed.HUSKY, 0.75f),
              new BreedShare(UnleashedDogBreed.BEAGLE, 0.25f)),
          20.0,
          0.3,
          4.0,
          DogGenome.BASELINE_FUR_GROWTH_RATE,
          UnleashedDogBreed.HUSKY);

  private static final DogGenes HUSKY_GENES =
      new DogGenes(
          Map.of(
              new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT),
              AllelePair.matching(2)));

  static Stream<Arguments> looks() {
    return Stream.of(
        Arguments.of(new DogStatueLook(UnleashedDogBreed.BEAGLE, false, 1, -1, 0, 0, null, null)),
        Arguments.of(
            new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 0, null, HUSKY_GENES)),
        Arguments.of(
            new DogStatueLook(
                UnleashedDogBreed.CROSS_BREED, false, -1, -1, 1, 0, MOSTLY_HUSKY, HUSKY_GENES)),
        Arguments.of(
            new DogStatueLook(UnleashedDogBreed.DIRE_WOLF, false, 0, -1, 2, 3, null, null)));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("a look survives its NBT round trip, so a statue keeps its dog across a reload")
  void nbtRoundTrips(final DogStatueLook look) {
    assertEquals(look, DogStatueLook.fromNbt(look.toNbt()));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("a look survives its codec round trip, so a mined statue keeps its dog")
  void codecRoundTrips(final DogStatueLook look) {
    assertEquals(
        look,
        DogStatueLook.CODEC
            .parse(
                NbtOps.INSTANCE,
                DogStatueLook.CODEC.encodeStart(NbtOps.INSTANCE, look).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "breed \"{0}\" is not a statue")
  @CsvSource({"''", "poodle"})
  @DisplayName("NBT with an unknown breed reads as no statue rather than a wrong dog")
  void unknownBreedIsNoLook(final String breed) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.BREED_TYPE, breed);
    assertNull(DogStatueLook.fromNbt(nbt));
  }

  @Test
  @DisplayName("missing variants read as unset, not as the first variant")
  void missingVariantsAreUnset() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.BREED_TYPE, "beagle");
    final DogStatueLook look = DogStatueLook.fromNbt(nbt);
    assertEquals(UnleashedDogEntity.UNSET_VARIANT, look.coatVariant());
    assertEquals(UnleashedDogEntity.UNSET_VARIANT, look.eyeColorVariant());
  }

  @ParameterizedTest(name = "{0} rigs as {1}")
  @CsvSource({"HUSKY, HUSKY, true", "BEAGLE, BEAGLE, false", "DIRE_WOLF, DIRE_WOLF, true"})
  @DisplayName("a pure breed stands on its own rig and howls only if the breed does")
  void pureBreedRigsAsItself(
      final UnleashedDogBreed breed, final UnleashedDogBreed rig, final boolean howls) {
    final DogStatueLook look = new DogStatueLook(breed, false, 0, -1, 0, 0, null, null);
    assertEquals(rig, look.rigBreed());
    assertEquals(howls, look.howls());
  }

  @Test
  @DisplayName("a cross breed stands on its largest share's rig")
  void crossBreedRigsAsItsLargestShare() {
    final DogStatueLook look =
        new DogStatueLook(UnleashedDogBreed.CROSS_BREED, false, -1, -1, 0, 0, MOSTLY_HUSKY, null);
    assertEquals(UnleashedDogBreed.HUSKY, look.rigBreed());
    assertTrue(look.howls());
  }

  @Test
  @DisplayName("a look taken from a pet record keeps that pet's breed, size, coat and genes")
  void ofPetDataCopiesTheRecord() {
    final NbtCompound record = new NbtCompound();
    record.putUuid(ModNbtKeys.PET_ID, UUID.randomUUID());
    record.putUuid(ModNbtKeys.OWNER_ID, UUID.randomUUID());
    record.putString(ModNbtKeys.BREED_TYPE, "husky");
    record.putString(ModNbtKeys.NAME, "Bentley");
    record.putString(ModNbtKeys.DIMENSION, "minecraft:overworld");
    record.putString(ModNbtKeys.LIFE_STATE, "DECEASED");
    record.putBoolean(ModNbtKeys.PORTRAIT_BABY, true);
    record.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, 2);
    record.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, 1);
    record.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, 3);
    record.putInt(ModNbtKeys.GROWTH_STAGE, 0);
    record.put(ModNbtKeys.GENES, HUSKY_GENES.toNbt());

    final DogStatueLook look = DogStatueLook.of(PetData.fromNbt(record));

    assertEquals(
        new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 0, null, HUSKY_GENES), look);
    assertFalse(look.toNbt().contains(ModNbtKeys.GENOME));
  }
}
