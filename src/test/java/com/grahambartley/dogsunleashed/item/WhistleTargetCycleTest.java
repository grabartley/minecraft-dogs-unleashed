package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WhistleTargetCycleTest {

  private static final String OVERWORLD = "minecraft:overworld";
  private static final String NETHER = "minecraft:the_nether";
  private static final BlockPos PLAYER = new BlockPos(0, 64, 0);

  private static final UUID ALPHA = UUID.fromString("11111111-0000-0000-0000-000000000000");
  private static final UUID BRAVO = UUID.fromString("22222222-0000-0000-0000-000000000000");
  private static final UUID CHARLIE = UUID.fromString("33333333-0000-0000-0000-000000000000");

  private static PetData pet(
      final UUID id, final boolean alive, final String dimension, final BlockPos position) {
    return new PetData(
        id,
        UUID.randomUUID(),
        UnleashedDogBreed.HUSKY,
        "Dog " + id,
        10.0f,
        10.0f,
        position,
        dimension,
        alive);
  }

  private static PetData alive(final UUID id) {
    return pet(id, true, OVERWORLD, PLAYER);
  }

  private static PetData aliveAt(final UUID id, final int distance) {
    return pet(id, true, OVERWORLD, new BlockPos(distance, 64, 0));
  }

  @Test
  @DisplayName("dead pets are excluded from the pack order")
  void deadPetsAreExcluded() {
    final List<PetData> pets =
        List.of(alive(ALPHA), pet(BRAVO, false, OVERWORLD, PLAYER), alive(CHARLIE));

    assertEquals(
        List.of(ALPHA, CHARLIE),
        WhistleTargetCycle.orderedAlivePets(pets).stream().map(PetData::getPetId).toList());
  }

  @Test
  @DisplayName("pack order does not depend on the order the pets arrive in")
  void packOrderIsIndependentOfInputOrder() {
    final List<UUID> forwards =
        WhistleTargetCycle.orderedAlivePets(List.of(alive(ALPHA), alive(BRAVO), alive(CHARLIE)))
            .stream()
            .map(PetData::getPetId)
            .toList();
    final List<UUID> backwards =
        WhistleTargetCycle.orderedAlivePets(List.of(alive(CHARLIE), alive(BRAVO), alive(ALPHA)))
            .stream()
            .map(PetData::getPetId)
            .toList();

    assertEquals(forwards, backwards);
    assertEquals(List.of(ALPHA, BRAVO, CHARLIE), forwards);
  }

  @ParameterizedTest(name = "from {0} the whistle moves to {1}")
  @CsvSource({
    "11111111-0000-0000-0000-000000000000, 22222222-0000-0000-0000-000000000000",
    "22222222-0000-0000-0000-000000000000, 33333333-0000-0000-0000-000000000000",
    "33333333-0000-0000-0000-000000000000, 11111111-0000-0000-0000-000000000000",
  })
  @DisplayName("cycling advances through the pack and wraps at the end")
  void cyclingAdvancesAndWraps(final String from, final String expected) {
    final List<PetData> pets = List.of(alive(ALPHA), alive(BRAVO), alive(CHARLIE));

    assertEquals(
        UUID.fromString(expected), WhistleTargetCycle.nextTarget(pets, UUID.fromString(from)));
  }

  @Test
  @DisplayName("cycling from no target lands on the first pet in the pack")
  void cyclingFromNoTargetStartsAtTheFirstPet() {
    final List<PetData> pets = List.of(alive(BRAVO), alive(CHARLIE), alive(ALPHA));

    assertEquals(ALPHA, WhistleTargetCycle.nextTarget(pets, null));
  }

  @Test
  @DisplayName("cycling from a pet that has since died restarts at the first pet")
  void cyclingFromADeadTargetRestarts() {
    final List<PetData> pets = List.of(alive(BRAVO), pet(ALPHA, false, OVERWORLD, PLAYER));

    assertEquals(BRAVO, WhistleTargetCycle.nextTarget(pets, ALPHA));
  }

  @Test
  @DisplayName("an owner with no alive pets has nothing to cycle to")
  void cyclingWithNoAlivePetsYieldsNothing() {
    assertNull(WhistleTargetCycle.nextTarget(List.of(pet(ALPHA, false, OVERWORLD, PLAYER)), null));
    assertNull(WhistleTargetCycle.nextTarget(List.of(), ALPHA));
  }

  @Test
  @DisplayName("a live stored target is kept rather than re-resolved")
  void aliveStoredTargetIsKept() {
    final List<PetData> pets = List.of(aliveAt(ALPHA, 500), aliveAt(BRAVO, 1));

    assertEquals(ALPHA, WhistleTargetCycle.resolveTarget(pets, ALPHA, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("an unset target resolves to the nearest pet in this dimension")
  void unsetTargetResolvesToTheNearestPet() {
    final List<PetData> pets =
        List.of(aliveAt(ALPHA, 500), aliveAt(BRAVO, 12), aliveAt(CHARLIE, 90));

    assertEquals(BRAVO, WhistleTargetCycle.resolveTarget(pets, null, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("a stored target that has died resolves to the nearest pet instead")
  void deadStoredTargetFallsBackToTheNearestPet() {
    final List<PetData> pets =
        List.of(pet(ALPHA, false, OVERWORLD, PLAYER), aliveAt(BRAVO, 40), aliveAt(CHARLIE, 8));

    assertEquals(CHARLIE, WhistleTargetCycle.resolveTarget(pets, ALPHA, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("a pet in another dimension loses to any pet in this one, however far away")
  void petsInThisDimensionWinOverOtherDimensions() {
    final List<PetData> pets = List.of(pet(ALPHA, true, NETHER, PLAYER), aliveAt(BRAVO, 4000));

    assertEquals(BRAVO, WhistleTargetCycle.resolveTarget(pets, null, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("a pet in another dimension is still callable when it is the only one")
  void aPetInAnotherDimensionIsStillCallable() {
    final List<PetData> pets = List.of(pet(ALPHA, true, NETHER, new BlockPos(20, 70, 20)));

    assertEquals(ALPHA, WhistleTargetCycle.resolveTarget(pets, null, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("an owner with no alive pets has nothing to call")
  void resolvingWithNoAlivePetsYieldsNothing() {
    assertNull(WhistleTargetCycle.resolveTarget(List.of(), null, OVERWORLD, PLAYER));
    assertNull(
        WhistleTargetCycle.resolveTarget(
            List.of(pet(ALPHA, false, OVERWORLD, PLAYER)), ALPHA, OVERWORLD, PLAYER));
  }

  @Test
  @DisplayName("cycling every pet in the pack returns to where it started")
  void cyclingTheWholePackReturnsToTheStart() {
    final List<PetData> pets = List.of(alive(ALPHA), alive(BRAVO), alive(CHARLIE));

    UUID target = ALPHA;
    for (int step = 0; step < pets.size(); step++) {
      target = WhistleTargetCycle.nextTarget(pets, target);
      assertTrue(target != null);
    }

    assertEquals(ALPHA, target);
  }
}
