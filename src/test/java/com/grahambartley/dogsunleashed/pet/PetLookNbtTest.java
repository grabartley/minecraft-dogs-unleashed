package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetLookNbtTest {

  private static final List<BreedShare> MOSTLY_BEAGLE =
      List.of(
          new BreedShare(UnleashedDogBreed.BEAGLE, 0.7f),
          new BreedShare(UnleashedDogBreed.HUSKY, 0.3f));

  private static final DogGenes HUSKY_GENES =
      new DogGenes(
          Map.of(
              new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT),
              AllelePair.matching(2)));

  static Stream<Arguments> saved() {
    return Stream.of(
        Arguments.of(DogStatueLook.unseen(UnleashedDogBreed.BEAGLE), DogPortraitOutfit.NONE),
        Arguments.of(
            new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 0, null, HUSKY_GENES),
            DogPortraitOutfitFixture.dressed()),
        Arguments.of(
            new DogStatueLook(
                UnleashedDogBreed.CROSS_BREED,
                false,
                -1,
                -1,
                1,
                4,
                new DogGenome(
                    MOSTLY_BEAGLE,
                    20.0,
                    0.3,
                    4.0,
                    DogGenome.BASELINE_FUR_GROWTH_RATE,
                    UnleashedDogBreed.BEAGLE),
                null),
            DogPortraitOutfitFixture.collarOnly(DyeColor.BLUE)));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("saved")
  @DisplayName("a pet's look and outfit survive a save and reload")
  void lookAndOutfitRoundTrip(final DogStatueLook look, final DogPortraitOutfit outfit) {
    final NbtCompound nbt = new NbtCompound();
    PetLookNbt.write(nbt, look, outfit, REGISTRIES);

    assertEquals(look, PetLookNbt.readLook(nbt));
    assertEquals(outfit, PetLookNbt.readOutfit(nbt, REGISTRIES));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("saved")
  @DisplayName("the look is saved once, inside its own compound")
  void lookIsSavedOnce(final DogStatueLook look, final DogPortraitOutfit outfit) {
    final NbtCompound nbt = new NbtCompound();
    PetLookNbt.write(nbt, look, outfit, REGISTRIES);

    assertEquals(Set.of(ModNbtKeys.PET_LOOK, ModNbtKeys.PORTRAIT_OUTFIT), nbt.getKeys());
  }

  @ParameterizedTest(name = "saved fur ordinal {0} -> {1}")
  @CsvSource({"0, BOTCHED", "3, SHAGGY", "99, NATURAL", "-1, NATURAL"})
  @DisplayName("a legacy fur stage out of range falls back to a natural coat")
  void legacyFurIsClamped(final int saved, final DogFurStage expected) {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.HUSKY);
    nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, saved);

    assertEquals(expected.ordinal(), PetLookNbt.readLook(nbt).furStage());
  }

  private static NbtCompound legacyMix() {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.CROSS_BREED);
    nbt.putFloat(ModNbtKeys.MAX_HEALTH, 22.0f);
    nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(MOSTLY_BEAGLE));
    return nbt;
  }

  @ParameterizedTest(name = "saved speed {0}, attack {1}")
  @CsvSource({"0.31, 4.2", "0.2, 1.5", ","})
  @DisplayName(
      "a legacy breed mix with no genome keeps its mix in a genome, with the stats a resurrection always gave it")
  void legacyCompositionBecomesAGenome(final Float speed, final Float attack) {
    final NbtCompound nbt = legacyMix();
    if (speed != null) {
      nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, speed);
      nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, attack);
    }

    final DogGenome genome = PetLookNbt.readLook(nbt).genome();

    final DogGenome pure = DogGenome.pure(UnleashedDogBreed.CROSS_BREED);
    assertEquals(DogGenome.sortedComposition(MOSTLY_BEAGLE), genome.composition());
    assertEquals(pure.maxHealth(), genome.maxHealth(), 1e-6);
    assertEquals(pure.movementSpeed(), genome.movementSpeed(), 1e-6);
    assertEquals(pure.attackDamage(), genome.attackDamage(), 1e-6);
    assertEquals(pure.furGrowthRate(), genome.furGrowthRate(), 1e-6);
    assertEquals(UnleashedDogBreed.BEAGLE, genome.voiceBreed());
  }

  static Stream<Arguments> legacyLooks() {
    return Stream.of(
        Arguments.of(
            "a full legacy save",
            UnleashedDogBreed.HUSKY,
            (Consumer<NbtCompound>)
                nbt -> {
                  nbt.putBoolean(ModNbtKeys.PORTRAIT_BABY, true);
                  nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, 2);
                  nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, 1);
                  nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, 3);
                  nbt.putInt(ModNbtKeys.GROWTH_STAGE, 2);
                  nbt.put(ModNbtKeys.GENES, HUSKY_GENES.toNbt());
                },
            new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 2, null, HUSKY_GENES)),
        Arguments.of(
            "a bare legacy save",
            UnleashedDogBreed.BEAGLE,
            (Consumer<NbtCompound>) nbt -> {},
            DogStatueLook.unseen(UnleashedDogBreed.BEAGLE)),
        Arguments.of(
            "a look naming an unknown breed",
            UnleashedDogBreed.BEAGLE,
            (Consumer<NbtCompound>)
                nbt -> {
                  final NbtCompound broken = new NbtCompound();
                  broken.putString(ModNbtKeys.BREED_TYPE, "poodle");
                  nbt.put(ModNbtKeys.PET_LOOK, broken);
                },
            DogStatueLook.unseen(UnleashedDogBreed.BEAGLE)),
        Arguments.of(
            "a saved genome next to an older loose breed mix",
            UnleashedDogBreed.HUSKY,
            (Consumer<NbtCompound>)
                nbt -> {
                  nbt.put(ModNbtKeys.GENOME, DogGenome.pure(UnleashedDogBreed.HUSKY).toNbt());
                  nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(MOSTLY_BEAGLE));
                },
            new DogStatueLook(
                UnleashedDogBreed.HUSKY,
                false,
                UnleashedDogEntity.UNSET_VARIANT,
                UnleashedDogEntity.UNSET_VARIANT,
                DogFurStage.NATURAL.ordinal(),
                0,
                DogGenome.pure(UnleashedDogBreed.HUSKY),
                null)),
        Arguments.of(
            "an empty loose breed mix",
            UnleashedDogBreed.BEAGLE,
            (Consumer<NbtCompound>) nbt -> nbt.put(ModNbtKeys.COMPOSITION, new NbtCompound()),
            DogStatueLook.unseen(UnleashedDogBreed.BEAGLE)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("legacyLooks")
  @DisplayName("a pet saved before looks reads its loose look fields into one look")
  void legacyLookMovesIntoOneLook(
      final String description,
      final UnleashedDogBreed breed,
      final Consumer<NbtCompound> saved,
      final DogStatueLook expected) {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(breed);
    saved.accept(nbt);

    assertEquals(expected, PetLookNbt.readLook(nbt));
  }

  static Stream<Arguments> legacyCollars() {
    return Stream.of(
        Arguments.of(
            Optional.of(DyeColor.BLUE), DogPortraitOutfitFixture.collarOnly(DyeColor.BLUE)),
        Arguments.of(
            Optional.of(DyeColor.LIME), DogPortraitOutfitFixture.collarOnly(DyeColor.LIME)),
        Arguments.of(Optional.empty(), DogPortraitOutfit.NONE));
  }

  @ParameterizedTest(name = "collar {0}")
  @MethodSource("legacyCollars")
  @DisplayName("a pet saved before outfits keeps its collar in its outfit, and nothing else")
  void legacyCollarMovesIntoTheOutfit(
      final Optional<DyeColor> collar, final DogPortraitOutfit expected) {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.BEAGLE);
    collar.ifPresent(colour -> nbt.putInt(ModNbtKeys.PORTRAIT_COLLAR, colour.getId()));

    assertEquals(expected, PetLookNbt.readOutfit(nbt, REGISTRIES));
  }
}
