package com.grahambartley.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.MinecraftBootstrapExtension;
import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
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
}
