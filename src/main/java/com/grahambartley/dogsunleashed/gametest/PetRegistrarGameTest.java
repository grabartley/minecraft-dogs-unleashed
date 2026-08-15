package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Contract coverage for {@link PetRegistrar}, the single path that creates the {@link PetData}
 * behind a tamed dog, plus the inherited-owner breeding branch that goes through it. A puppy bred
 * while the feeding player is unresolvable used to be tamed with no record at all, which
 * permanently hid it from the Pet Manager, summons, and cross-dimension follows.
 *
 * <p>AI is disabled throughout: these are record-creation contracts, not behavior, so the goal
 * selector must not get a chance to move dogs mid-assertion (gametest skill rule 6).
 */
public final class PetRegistrarGameTest implements FabricGameTest {

  private static final BlockPos PARENT_POS = new BlockPos(1, 2, 1);
  private static final BlockPos OTHER_PARENT_POS = new BlockPos(3, 2, 1);

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void inheritedOwnerBabyIsRegisteredAsAPet(TestContext context) {
    final UUID ownerUuid = UUID.randomUUID();
    final UnleashedDogEntity baby = breedWithoutLovingPlayer(context, ownerUuid);

    final PetData petData = petManager(context).getPetByEntityId(baby.getUuid());
    context.assertTrue(
        petData != null, "Baby bred via the inherited-owner path should have a pet record");
    context.assertTrue(
        ownerUuid.equals(petData.getOwnerId()),
        "Pet record should belong to the inherited owner, but was " + petData.getOwnerId());
    context.assertTrue(petData.isAlive(), "A newly bred puppy's record should be alive");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void inheritedOwnerBabyGetsAGeneratedName(TestContext context) {
    final UnleashedDogEntity baby = breedWithoutLovingPlayer(context, UUID.randomUUID());

    final PetData petData = petManager(context).getPetByEntityId(baby.getUuid());
    context.assertTrue(petData != null, "Baby should have a pet record to carry a name");
    context.assertTrue(
        !petData.getName().isBlank(),
        "Puppy should get a generated name so the actionbar stops falling back to the breed");
    context.assertTrue(
        petData.getName().equals(baby.getTamedName()),
        "getTamedName should resolve to the record's name, but was " + baby.getTamedName());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void inheritedOwnerBabyRecordMatchesItsAppearance(TestContext context) {
    final UnleashedDogEntity baby = breedWithoutLovingPlayer(context, UUID.randomUUID());

    final PetData petData = petManager(context).getPetByEntityId(baby.getUuid());
    context.assertTrue(petData != null, "Baby should have a pet record to carry appearance");
    context.assertTrue(
        petData.isBaby(), "Record for a freshly bred puppy should be flagged as a baby");
    context.assertTrue(
        petData.getCoatVariant() == PetData.coatVariantOf(baby),
        "Record coat variant should match the live puppy, but was " + petData.getCoatVariant());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void inheritedOwnerBabyRecordCapturesBothParents(TestContext context) {
    final UUID ownerUuid = UUID.randomUUID();
    final UnleashedDogEntity parent = spawnTamedDog(context, ownerUuid, PARENT_POS);
    final UnleashedDogEntity otherParent = spawnTamedDog(context, ownerUuid, OTHER_PARENT_POS);

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) parent.createChild(context.getWorld(), otherParent);

    final PetData petData = petManager(context).getPetByEntityId(baby.getUuid());
    context.assertTrue(petData != null, "Bred baby should have a pet record to carry lineage");
    context.assertTrue(
        parent.getUuid().equals(petData.getParentAId()),
        "Record should capture the initiating parent, but was " + petData.getParentAId());
    context.assertTrue(
        otherParent.getUuid().equals(petData.getParentBId()),
        "Record should capture the partner parent, but was " + petData.getParentBId());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void registerPetForKeepsTheExistingRecord(TestContext context) {
    final UUID ownerUuid = UUID.randomUUID();
    final UnleashedDogEntity husky = spawnTamedDog(context, ownerUuid, PARENT_POS);
    final PetData original = PetRegistrar.registerPetFor(husky, ownerUuid);

    final PetData second = PetRegistrar.registerPetFor(husky, ownerUuid);

    context.assertTrue(second == original, "Re-registering should return the existing record");
    context.assertTrue(
        petManager(context).getPetsByOwner(ownerUuid).size() == 1,
        "Re-registering must not duplicate the pet record, owner had "
            + petManager(context).getPetsByOwner(ownerUuid).size());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void registerPetForSkipsOwnerlessDogs(TestContext context) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, PARENT_POS);
    husky.setAiDisabled(true);
    husky.setTamed(true, true);

    context.assertTrue(
        PetRegistrar.registerPetFor(husky, husky.getOwnerUuid()) == null,
        "A dog with no owner should not get a pet record");
    context.assertTrue(
        petManager(context).getPetByEntityId(husky.getUuid()) == null,
        "No record should be stored for an ownerless dog");
    context.complete();
  }

  /**
   * Mirrors the production path where {@code getLovingPlayer()} returns null: both parents are
   * tamed and owned, but nobody is in breeding range, so {@code createChild} falls through to the
   * inherited-owner branch.
   */
  private static UnleashedDogEntity breedWithoutLovingPlayer(
      final TestContext context, final UUID ownerUuid) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity parent = spawnTamedDog(context, ownerUuid, PARENT_POS);
    parent.setCollarColor(DyeColor.LIME);
    final UnleashedDogEntity otherParent = spawnTamedDog(context, ownerUuid, OTHER_PARENT_POS);

    return (UnleashedDogEntity) parent.createChild(world, otherParent);
  }

  private static UnleashedDogEntity spawnTamedDog(
      final TestContext context, final UUID ownerUuid, final BlockPos relativePos) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relativePos);
    husky.setAiDisabled(true);
    husky.setOwnerUuid(ownerUuid);
    husky.setTamed(true, true);
    return husky;
  }

  private static PetManager petManager(final TestContext context) {
    return PetManager.get(context.getWorld().getServer());
  }
}
