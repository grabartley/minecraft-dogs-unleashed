package com.grahambartley.dogsunleashed.block.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModStatueBlocks;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogStatueBlockEntityTest {

  private static final RegistryWrapper.WrapperLookup REGISTRIES =
      BuiltinRegistries.createWrapperLookup();

  private static DogStatueBlockEntity base() {
    return new DogStatueBlockEntity(BlockPos.ORIGIN, ModStatueBlocks.STONE.getDefaultState());
  }

  private static DogStatue carvingOf(final UnleashedDogBreed breed) {
    return DogStatue.carved(lookOf(breed), "Rex");
  }

  private static DogStatueLook lookOf(final UnleashedDogBreed breed) {
    return new DogStatueLook(breed, false, 1, -1, 0, 0, null, null);
  }

  @Test
  @DisplayName("a fresh base has no statue and cannot change pose")
  void freshBaseIsEmpty() {
    final DogStatueBlockEntity base = base();
    assertNull(base.statue());
    assertFalse(base.cyclePose());
  }

  @Test
  @DisplayName("carving sets the dog and its name and starts it sitting")
  void carvingStartsSitting() {
    final DogStatueBlockEntity base = base();
    base.carve(carvingOf(UnleashedDogBreed.BEAGLE));
    assertEquals(
        new DogStatue(lookOf(UnleashedDogBreed.BEAGLE), DogStatuePose.SIT, "Rex"), base.statue());
  }

  @Test
  @DisplayName("carving again replaces the dog and resets the pose")
  void recarvingReplacesTheDog() {
    final DogStatueBlockEntity base = base();
    base.carve(carvingOf(UnleashedDogBreed.HUSKY));
    base.cyclePose();
    base.carve(carvingOf(UnleashedDogBreed.BEAGLE));
    assertEquals(
        new DogStatue(lookOf(UnleashedDogBreed.BEAGLE), DogStatuePose.SIT, "Rex"), base.statue());
  }

  @ParameterizedTest(name = "{0} after {1} uses is {2}")
  @CsvSource({"HUSKY, 1, STAND", "HUSKY, 2, HOWL", "BEAGLE, 2, SIT"})
  @DisplayName("each use cycles the carved pose")
  void cyclePoseAdvances(
      final UnleashedDogBreed breed, final int uses, final DogStatuePose expected) {
    final DogStatueBlockEntity base = base();
    base.carve(carvingOf(breed));
    for (int use = 0; use < uses; use++) {
      assertTrue(base.cyclePose());
    }
    assertEquals(expected, base.statue().pose());
  }

  @Test
  @DisplayName("the statue survives a save and load")
  void statueSurvivesNbt() {
    final DogStatueBlockEntity base = base();
    base.carve(carvingOf(UnleashedDogBreed.HUSKY));
    base.cyclePose();

    final BlockEntity loaded =
        BlockEntity.createFromNbt(
            BlockPos.ORIGIN,
            ModStatueBlocks.STONE.getDefaultState(),
            base.createNbtWithIdentifyingData(REGISTRIES),
            REGISTRIES);

    assertEquals(base.statue(), ((DogStatueBlockEntity) loaded).statue());
  }

  @Test
  @DisplayName("an empty base saves and loads as empty")
  void emptyBaseSurvivesNbt() {
    final BlockEntity loaded =
        BlockEntity.createFromNbt(
            BlockPos.ORIGIN,
            ModStatueBlocks.STONE.getDefaultState(),
            base().createNbtWithIdentifyingData(REGISTRIES),
            REGISTRIES);
    assertNull(((DogStatueBlockEntity) loaded).statue());
  }

  @Test
  @DisplayName("a carved base puts its statue on the item it drops")
  void carvedBaseAddsItsComponent() {
    final DogStatueBlockEntity base = base();
    base.carve(carvingOf(UnleashedDogBreed.HUSKY));
    assertEquals(base.statue(), base.createComponentMap().get(ModComponents.DOG_STATUE));
    assertNull(base().createComponentMap().get(ModComponents.DOG_STATUE));
  }
}
