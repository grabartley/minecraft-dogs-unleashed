package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetDataTest {

  private static final float BASE_HEALTH = 18.0f;
  private static final BlockPos BASE_POS = new BlockPos(10, 64, -20);
  private static final String BASE_DIM = "minecraft:overworld";
  private static final boolean BASE_BABY = false;
  // UNSET_VARIANT is an inlined compile-time constant, so these are safe at class-load time.
  private static final int BASE_COAT = UnleashedDogEntity.UNSET_VARIANT;
  private static final int BASE_EYE = UnleashedDogEntity.UNSET_VARIANT;

  // The constructor seeds collar to DyeColor.RED (matching PetData's DEFAULT_COLLAR_COLOR_ID).
  // Source it from DyeColor directly so the test never class-loads UnleashedDogEntity, which fails
  // bytecode verification on the unit-test classpath.
  private static int baseCollar() {
    return DyeColor.RED.getId();
  }

  // The constructor seeds the appearance fields to baby=false, collar=DEFAULT, coat=UNSET,
  // eye=UNSET, which is exactly the BASE_* baseline the differsFrom arguments compare against.
  private static PetData baselinePet() {
    return new PetData(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UnleashedDogBreed.HUSKY,
        "Balto",
        BASE_HEALTH,
        20.0f,
        BASE_POS,
        BASE_DIM,
        true);
  }

  @ParameterizedTest(name = "differsFrom is true when {0} changes")
  @MethodSource("changedFieldCases")
  @DisplayName("differsFrom returns true when any tracked field changes")
  void differsFromDetectsEachChangedField(
      final String changedField,
      final float health,
      final BlockPos pos,
      final String dimension,
      final boolean baby,
      final int collar,
      final int coat,
      final int eye) {
    assertTrue(
        baselinePet().differsFrom(health, pos, dimension, baby, collar, coat, eye),
        "expected differsFrom to flag a change in " + changedField);
  }

  static Stream<Arguments> changedFieldCases() {
    return Stream.of(
        Arguments.of(
            "health",
            BASE_HEALTH - 5.0f,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE),
        Arguments.of(
            "position",
            BASE_HEALTH,
            BASE_POS.add(1, 0, 0),
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE),
        Arguments.of(
            "dimension",
            BASE_HEALTH,
            BASE_POS,
            "minecraft:the_nether",
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE),
        Arguments.of(
            "dimension going null",
            BASE_HEALTH,
            BASE_POS,
            null,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE),
        Arguments.of(
            "baby", BASE_HEALTH, BASE_POS, BASE_DIM, !BASE_BABY, baseCollar(), BASE_COAT, BASE_EYE),
        Arguments.of(
            "collarColor",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar() + 1,
            BASE_COAT,
            BASE_EYE),
        Arguments.of(
            "coatVariant",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT + 1,
            BASE_EYE),
        Arguments.of(
            "huskyEyeVariant",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE + 1));
  }

  @ParameterizedTest(name = "differsFrom is false when the position is an equal {0}")
  @MethodSource("equalPositionCases")
  @DisplayName("differsFrom returns false when all tracked fields match")
  void differsFromIgnoresUnchangedState(final String label, final BlockPos equalPos) {
    assertFalse(
        baselinePet()
            .differsFrom(
                BASE_HEALTH, equalPos, BASE_DIM, BASE_BABY, baseCollar(), BASE_COAT, BASE_EYE),
        "expected differsFrom to report no change for an unchanged pet (" + label + ")");
  }

  static Stream<Arguments> equalPositionCases() {
    return Stream.of(
        Arguments.of("same instance", BASE_POS),
        Arguments.of("equal-by-value instance", new BlockPos(10, 64, -20)));
  }

  private static final UUID PARENT_A = UUID.nameUUIDFromBytes("parent-a".getBytes());
  private static final UUID PARENT_B = UUID.nameUUIDFromBytes("parent-b".getBytes());

  @ParameterizedTest(name = "{0}")
  @MethodSource("recordParentsCases")
  @DisplayName("recordParents fills only empty slots and reports whether anything changed")
  void recordParentsIsSetOnce(
      final String label,
      final UUID firstCallA,
      final UUID firstCallB,
      final UUID secondCallA,
      final UUID secondCallB,
      final boolean expectSecondCallChanged,
      final UUID expectedParentA,
      final UUID expectedParentB) {
    final PetData pet = baselinePet();
    pet.recordParents(firstCallA, firstCallB);

    assertEquals(
        expectSecondCallChanged,
        pet.recordParents(secondCallA, secondCallB),
        "changed flag for second recordParents call (" + label + ")");
    assertEquals(expectedParentA, pet.getParentAId(), "parentA (" + label + ")");
    assertEquals(expectedParentB, pet.getParentBId(), "parentB (" + label + ")");
  }

  static Stream<Arguments> recordParentsCases() {
    final UUID other = UUID.nameUUIDFromBytes("parent-other".getBytes());
    return Stream.of(
        Arguments.of(
            "both parents recorded at once",
            null,
            null,
            PARENT_A,
            PARENT_B,
            true,
            PARENT_A,
            PARENT_B),
        Arguments.of(
            "single known parent recorded alone", null, null, PARENT_A, null, true, PARENT_A, null),
        Arguments.of(
            "second-slot-only parent still recorded",
            null,
            null,
            null,
            PARENT_B,
            true,
            null,
            PARENT_B),
        Arguments.of(
            "duplicate parent collapses to one slot",
            null,
            null,
            PARENT_A,
            PARENT_A,
            true,
            PARENT_A,
            null),
        Arguments.of(
            "existing parents never overwritten",
            PARENT_A,
            PARENT_B,
            other,
            other,
            false,
            PARENT_A,
            PARENT_B),
        Arguments.of(
            "legacy one-parent record backfills the missing slot",
            PARENT_A,
            null,
            PARENT_A,
            PARENT_B,
            true,
            PARENT_A,
            PARENT_B),
        Arguments.of("null parents are a no-op", null, null, null, null, false, null, null));
  }

  @ParameterizedTest(name = "parentA={0}, parentB={1}")
  @MethodSource("parentNbtRoundTripCases")
  @DisplayName("parent ids survive the NBT round-trip, including records with unknown parents")
  void parentsSurviveNbtRoundTrip(final UUID parentAId, final UUID parentBId) {
    final PetData pet = baselinePet();
    pet.recordParents(parentAId, parentBId);

    final PetData reloaded = PetData.fromNbt(pet.toNbt());

    assertEquals(parentAId, reloaded.getParentAId(), "parentA after round-trip");
    assertEquals(parentBId, reloaded.getParentBId(), "parentB after round-trip");
  }

  static Stream<Arguments> parentNbtRoundTripCases() {
    return Stream.of(
        Arguments.of(PARENT_A, PARENT_B), Arguments.of(PARENT_A, null), Arguments.of(null, null));
  }

  @Test
  @DisplayName("records written before parentage existed read back with no parents")
  void legacyNbtWithoutParentsReadsAsUnknown() {
    final PetData reloaded = PetData.fromNbt(baselinePet().toNbt());

    assertNull(reloaded.getParentAId(), "legacy parentA");
    assertNull(reloaded.getParentBId(), "legacy parentB");
  }
}
