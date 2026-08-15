package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.DirectConnections;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Locks the contract of {@link PetManager#getDirectConnections} and {@link
 * PetManager#isConnectedToOwnedPet}, the graph queries behind the family tree. Each test builds its
 * own {@link PetManager} instance around a three-generation family so nothing leaks through the
 * world's persistent state:
 *
 * <pre>
 * grandma + grandpa -> mother;  mother + father -> puppyOne, puppyTwo
 * father + otherMate -> halfSibling;  stranger is unrelated
 * </pre>
 *
 * <p>This lives in the gametest suite for the same reason as {@code PetManagerFilterGameTest}:
 * constructing {@link PetData} needs the access-widened runtime classpath. No world ticking is
 * required, so each case runs in {@code EMPTY_STRUCTURE} and completes immediately.
 */
public final class PetManagerConnectionsGameTest implements FabricGameTest {

  private static final UUID OWNER = UUID.nameUUIDFromBytes("connections-owner".getBytes());
  private static final UUID OTHER_OWNER =
      UUID.nameUUIDFromBytes("connections-other-owner".getBytes());
  private static final UUID STRANGER_OWNER =
      UUID.nameUUIDFromBytes("connections-stranger-owner".getBytes());

  private record Family(
      PetManager manager,
      PetData grandma,
      PetData grandpa,
      PetData mother,
      PetData father,
      PetData puppyOne,
      PetData puppyTwo,
      PetData halfSibling,
      PetData stranger) {}

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void directConnectionsResolveBothParents(TestContext context) {
    final Family family = buildFamily();

    final DirectConnections connections =
        family.manager().getDirectConnections(family.puppyOne().getPetId());

    assertPetIds(
        context,
        "parents",
        connections.parents(),
        Set.of(family.mother().getPetId(), family.father().getPetId()));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void directConnectionsResolveChildren(TestContext context) {
    final Family family = buildFamily();

    final DirectConnections connections =
        family.manager().getDirectConnections(family.mother().getPetId());

    assertPetIds(
        context,
        "children",
        connections.children(),
        Set.of(family.puppyOne().getPetId(), family.puppyTwo().getPetId()));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void directConnectionsResolveMatesAsCoParents(TestContext context) {
    final Family family = buildFamily();

    final DirectConnections connections =
        family.manager().getDirectConnections(family.mother().getPetId());

    assertPetIds(context, "mates", connections.mates(), Set.of(family.father().getPetId()));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void directConnectionsResolveFullAndHalfSiblingsWithoutSelf(TestContext context) {
    final Family family = buildFamily();

    final DirectConnections connections =
        family.manager().getDirectConnections(family.puppyOne().getPetId());

    assertPetIds(
        context,
        "siblings",
        connections.siblings(),
        Set.of(family.puppyTwo().getPetId(), family.halfSibling().getPetId()));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void directConnectionsForUnknownDogAreNull(TestContext context) {
    final Family family = buildFamily();

    context.assertTrue(
        family.manager().getDirectConnections(UUID.randomUUID()) == null,
        "An unknown dog has no connections to resolve");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void connectivityHoldsForYourOwnDog(TestContext context) {
    final Family family = buildFamily();

    context.assertTrue(
        family.manager().isConnectedToOwnedPet(OWNER, family.puppyOne().getPetId()),
        "A dog you own is trivially connected to your pets");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void connectivityReachesAcrossGenerationsAndOwners(TestContext context) {
    final Family family = buildFamily();

    context.assertTrue(
        family.manager().isConnectedToOwnedPet(OWNER, family.grandma().getPetId()),
        "A grandparent owned by someone else is still connected through the lineage graph");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void connectivityDeniesUnrelatedDogs(TestContext context) {
    final Family family = buildFamily();

    context.assertTrue(
        !family.manager().isConnectedToOwnedPet(OWNER, family.stranger().getPetId()),
        "A dog with no lineage path to your pets must not be browsable");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void parentBackfillOnUpdateReindexesChildren(TestContext context) {
    final Family family = buildFamily();
    final PetData latePuppy = pet("late-puppy", OWNER);
    family.manager().registerPet(latePuppy);

    latePuppy.recordParents(family.mother().getPetId(), null);
    family.manager().updatePet(latePuppy);

    final DirectConnections connections =
        family.manager().getDirectConnections(family.mother().getPetId());
    context.assertTrue(
        connections.children().stream()
            .anyMatch(child -> child.getPetId().equals(latePuppy.getPetId())),
        "A backfilled parent link should surface the child in the parent's connections");
    context.complete();
  }

  private static void assertPetIds(
      final TestContext context,
      final String relation,
      final List<PetData> actual,
      final Set<UUID> expectedIds) {
    final Set<UUID> actualIds = actual.stream().map(PetData::getPetId).collect(Collectors.toSet());
    context.assertTrue(
        actualIds.equals(expectedIds) && actual.size() == expectedIds.size(),
        "Expected " + relation + " " + expectedIds + " but got " + actualIds);
  }

  private static Family buildFamily() {
    final PetManager manager = new PetManager();
    final PetData grandma = pet("grandma", OTHER_OWNER);
    final PetData grandpa = pet("grandpa", OTHER_OWNER);
    final PetData mother = pet("mother", OTHER_OWNER);
    final PetData father = pet("father", OTHER_OWNER);
    final PetData otherMate = pet("other-mate", OTHER_OWNER);
    final PetData puppyOne = pet("puppy-one", OWNER);
    final PetData puppyTwo = pet("puppy-two", OTHER_OWNER);
    final PetData halfSibling = pet("half-sibling", OTHER_OWNER);
    final PetData stranger = pet("stranger", STRANGER_OWNER);

    mother.recordParents(grandma.getPetId(), grandpa.getPetId());
    puppyOne.recordParents(mother.getPetId(), father.getPetId());
    puppyTwo.recordParents(mother.getPetId(), father.getPetId());
    halfSibling.recordParents(father.getPetId(), otherMate.getPetId());

    for (final PetData petData :
        List.of(
            grandma,
            grandpa,
            mother,
            father,
            otherMate,
            puppyOne,
            puppyTwo,
            halfSibling,
            stranger)) {
      manager.registerPet(petData);
    }
    return new Family(
        manager, grandma, grandpa, mother, father, puppyOne, puppyTwo, halfSibling, stranger);
  }

  private static PetData pet(final String name, final UUID ownerId) {
    return new PetData(
        UUID.nameUUIDFromBytes(("connections-pet-" + name).getBytes()),
        ownerId,
        UnleashedDogBreed.HUSKY,
        name,
        20.0f,
        20.0f,
        new BlockPos(0, 64, 0),
        "minecraft:overworld",
        true);
  }
}
