package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.TestRegistries;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("a look survives the network round trip, so previews show the same dog")
  void packetRoundTrips(final DogStatueLook look) {
    final RegistryByteBuf buf = TestRegistries.buf();
    DogStatueLook.PACKET_CODEC.encode(buf, look);
    assertEquals(look, DogStatueLook.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("the breed mix is read from the genome, with no genome meaning no mix")
  void compositionComesFromTheGenome(final DogStatueLook look) {
    assertEquals(
        look.genome() == null ? List.of() : look.genome().composition(), look.composition());
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("saved genes are kept as they are")
  void savedGenesAreKept(final DogStatueLook look) {
    final DogStatueLook withGenes = look.withGenes(HUSKY_GENES);
    assertEquals(HUSKY_GENES, withGenes.resolvedGenes());
    assertEquals(look, withGenes.withGenes(look.genes()));
  }

  @ParameterizedTest(name = "coat {0}, eyes {1}")
  @CsvSource({"2, 1", "0, 3", "4, 0"})
  @DisplayName("a look with no saved genes gets genes matching its coat and eyes")
  void missingGenesMatchTheLook(final int coat, final int eyes) {
    final DogStatueLook look =
        new DogStatueLook(UnleashedDogBreed.HUSKY, false, coat, eyes, 0, 0, null, null);
    final DogGenes genes = look.resolvedGenes();
    assertEquals(
        AllelePair.matching(coat),
        genes.pair(new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT)));
    assertEquals(
        AllelePair.matching(eyes),
        genes.pair(new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES)));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("looks")
  @DisplayName("changing the eye variant leaves the rest of the look alone")
  void withEyeColorVariantChangesOnlyTheEyes(final DogStatueLook look) {
    final DogStatueLook changed = look.withEyeColorVariant(look.eyeColorVariant() + 1);
    assertEquals(look.eyeColorVariant() + 1, changed.eyeColorVariant());
    assertEquals(look, changed.withEyeColorVariant(look.eyeColorVariant()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("an unseen dog is an adult of its breed with a natural coat and nothing rolled")
  void unseenIsABlankAdult(final UnleashedDogBreed breed) {
    assertEquals(
        new DogStatueLook(
            breed,
            false,
            UnleashedDogEntity.UNSET_VARIANT,
            UnleashedDogEntity.UNSET_VARIANT,
            DogFurStage.NATURAL.ordinal(),
            0,
            null,
            null),
        DogStatueLook.unseen(breed));
  }
}
