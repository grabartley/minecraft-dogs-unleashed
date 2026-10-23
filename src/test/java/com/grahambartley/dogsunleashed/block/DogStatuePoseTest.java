package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogStatuePoseTest {

  @ParameterizedTest(name = "{0} then {2} when the breed howls is {1}")
  @CsvSource({
    "SIT, true, STAND",
    "STAND, true, HOWL",
    "HOWL, true, SIT",
    "SIT, false, STAND",
    "STAND, false, SIT",
    "HOWL, false, SIT"
  })
  @DisplayName("the pose cycles sit, stand, howl, and skips howl for breeds that never howl")
  void cyclesThroughThePoses(
      final DogStatuePose pose, final boolean howls, final DogStatuePose next) {
    assertEquals(next, pose.next(howls));
  }

  @ParameterizedTest(name = "{0} shows as {2} when howls is {1}")
  @CsvSource({"SIT, false, SIT", "STAND, false, STAND", "HOWL, false, SIT", "HOWL, true, HOWL"})
  @DisplayName("a howl pose on a breed that cannot howl shows as sitting")
  void howlFallsBackToSitting(
      final DogStatuePose pose, final boolean howls, final DogStatuePose shown) {
    assertEquals(shown, pose.shownFor(howls));
  }

  @ParameterizedTest(name = "{0} sits {1}, howls {2}")
  @CsvSource({"SIT, true, false", "STAND, false, false", "HOWL, true, true"})
  @DisplayName("sit and howl are sitting poses, and only howl raises the head")
  void sittingAndHowling(final DogStatuePose pose, final boolean sits, final boolean howling) {
    assertEquals(sits, pose.sits());
    assertEquals(howling, pose.howling());
  }

  @ParameterizedTest(name = "{0} survives its saved name")
  @EnumSource(DogStatuePose.class)
  @DisplayName("every pose reads back from the name it is saved under")
  void serializedNameRoundTrips(final DogStatuePose pose) {
    assertEquals(pose, DogStatuePose.fromSerializedName(pose.serializedName()));
  }

  @ParameterizedTest(name = "\"{0}\" reads as sitting")
  @CsvSource({"''", "lying", "SIT"})
  @DisplayName("an unknown saved pose reads as sitting rather than failing the load")
  void unknownNameIsSitting(final String name) {
    assertEquals(DogStatuePose.SIT, DogStatuePose.fromSerializedName(name));
  }

  @ParameterizedTest(name = "{0} survives the codec")
  @EnumSource(DogStatuePose.class)
  @DisplayName("every pose survives the codec statues and portraits save it with")
  void codecRoundTrips(final DogStatuePose pose) {
    final NbtElement saved = DogStatuePose.CODEC.encodeStart(NbtOps.INSTANCE, pose).getOrThrow();
    assertEquals(NbtString.of(pose.serializedName()), saved);
    assertEquals(pose, DogStatuePose.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow());
  }

  @ParameterizedTest(name = "a {0} at {1} is next {2} and shows as {3}")
  @CsvSource({
    "HUSKY, STAND, HOWL, STAND",
    "HUSKY, HOWL, SIT, HOWL",
    "BEAGLE, STAND, SIT, STAND",
    "BEAGLE, HOWL, SIT, SIT"
  })
  @DisplayName("a dog's look decides whether its breed reaches and shows the howl pose")
  void lookDecidesTheHowl(
      final UnleashedDogBreed breed,
      final DogStatuePose pose,
      final DogStatuePose next,
      final DogStatuePose shown) {
    final DogStatueLook look = new DogStatueLook(breed, false, 1, -1, 0, 0, null, null);
    assertEquals(next, pose.next(look));
    assertEquals(shown, pose.shownFor(look));
  }
}
