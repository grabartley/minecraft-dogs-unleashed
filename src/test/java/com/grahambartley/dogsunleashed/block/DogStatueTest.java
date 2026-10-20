package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogStatueTest {

  private static DogStatue statueOf(final UnleashedDogBreed breed, final DogStatuePose pose) {
    return new DogStatue(new DogStatueLook(breed, false, 0, -1, 0, 0, null, null), pose, "Rex");
  }

  @ParameterizedTest(name = "{0} goes {1} -> {2} -> {3} -> {4}")
  @CsvSource({"HUSKY, SIT, STAND, HOWL, SIT", "BEAGLE, SIT, STAND, SIT, STAND"})
  @DisplayName("using a statue cycles its pose, with howl only for breeds that howl")
  void nextPoseFollowsTheBreed(
      final UnleashedDogBreed breed,
      final DogStatuePose first,
      final DogStatuePose second,
      final DogStatuePose third,
      final DogStatuePose fourth) {
    final DogStatue start = statueOf(breed, first);
    assertEquals(second, start.nextPose().pose());
    assertEquals(third, start.nextPose().nextPose().pose());
    assertEquals(fourth, start.nextPose().nextPose().nextPose().pose());
  }

  @Test
  @DisplayName("a saved howl on a breed that cannot howl shows sitting")
  void shownPoseFallsBack() {
    assertEquals(
        DogStatuePose.SIT, statueOf(UnleashedDogBreed.BEAGLE, DogStatuePose.HOWL).shownPose());
    assertEquals(
        DogStatuePose.HOWL, statueOf(UnleashedDogBreed.HUSKY, DogStatuePose.HOWL).shownPose());
  }

  @ParameterizedTest(name = "{0} survives the codec")
  @EnumSource(DogStatuePose.class)
  @DisplayName("the look and pose survive the item component codec")
  void codecRoundTrips(final DogStatuePose pose) {
    final DogStatue statue = statueOf(UnleashedDogBreed.HUSKY, pose);
    assertEquals(
        statue,
        DogStatue.CODEC
            .parse(
                NbtOps.INSTANCE, DogStatue.CODEC.encodeStart(NbtOps.INSTANCE, statue).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "\"{0}\" is carved as \"{1}\"")
  @CsvSource({
    "'', ''",
    "Bentley, Bentley",
    "'Sir Barksalot the Third of the Long Name Hills, Esq', 'Sir Barksalot the Third of the Long Name Hills, Es'"
  })
  @DisplayName("a carving starts sitting and keeps the name, cut to fifty characters")
  void carvedKeepsTheName(final String name, final String engraved) {
    final DogStatue statue =
        DogStatue.carved(statueOf(UnleashedDogBreed.HUSKY, DogStatuePose.HOWL).look(), name);
    assertEquals(DogStatuePose.SIT, statue.pose());
    assertEquals(engraved, statue.name());
  }

  @Test
  @DisplayName("cutting a long name never splits an emoji in half")
  void cuttingKeepsWholeCodePoints() {
    final String name = "a".repeat(DogStatue.MAX_NAME_LENGTH - 1) + "\uD83D\uDC36" + "b";
    final String engraved =
        DogStatue.carved(statueOf(UnleashedDogBreed.HUSKY, DogStatuePose.SIT).look(), name).name();
    assertEquals("a".repeat(DogStatue.MAX_NAME_LENGTH - 1) + "\uD83D\uDC36", engraved);
  }

  @Test
  @DisplayName("cycling the pose keeps the name")
  void nextPoseKeepsTheName() {
    assertEquals("Rex", statueOf(UnleashedDogBreed.HUSKY, DogStatuePose.SIT).nextPose().name());
  }

  @Test
  @DisplayName("a component saved without a pose or name reads as an unnamed sitting dog")
  void missingPoseIsSitting() {
    final NbtCompound nbt = new NbtCompound();
    nbt.put("look", statueOf(UnleashedDogBreed.BEAGLE, DogStatuePose.SIT).look().toNbt());
    final DogStatue statue = DogStatue.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
    assertEquals(DogStatuePose.SIT, statue.pose());
    assertEquals("", statue.name());
  }
}
