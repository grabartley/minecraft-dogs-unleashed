package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Entity-level lineage contract: {@code createChild} stamps both parents' UUIDs on the puppy, and
 * both survive the entity NBT round-trip. The pet-record side of lineage (registration capture,
 * legacy backfill, graph queries) is covered by {@code PetRegistrarGameTest}, {@code
 * PetLocationSyncListenerGameTest}, and {@code PetManagerConnectionsGameTest}.
 *
 * <p>AI is disabled throughout: these are persistence contracts, not behavior (gametest skill rule
 * 6).
 */
public final class DogLineageGameTest implements FabricGameTest {

  private static final BlockPos PARENT_POS = new BlockPos(1, 2, 1);
  private static final BlockPos OTHER_PARENT_POS = new BlockPos(3, 2, 1);

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void createChildStampsBothParentUuidsOnTheBaby(TestContext context) {
    final UUID ownerUuid = UUID.randomUUID();
    final HuskyEntity parent = spawnTamedDog(context, ownerUuid, PARENT_POS);
    final HuskyEntity otherParent = spawnTamedDog(context, ownerUuid, OTHER_PARENT_POS);

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) parent.createChild(context.getWorld(), otherParent);

    context.assertTrue(
        parent.getUuid().equals(baby.getParentDogUuid()),
        "Baby should remember the initiating parent, but was " + baby.getParentDogUuid());
    context.assertTrue(
        otherParent.getUuid().equals(baby.getSecondParentDogUuid()),
        "Baby should remember the partner parent, but was " + baby.getSecondParentDogUuid());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void bothParentUuidsSurviveEntityNbtRoundTrip(TestContext context) {
    final HuskyEntity original = spawnTamedDog(context, UUID.randomUUID(), PARENT_POS);
    final UUID parentUuid = UUID.randomUUID();
    final UUID secondParentUuid = UUID.randomUUID();
    original.setParentDogUuid(parentUuid);
    original.setSecondParentDogUuid(secondParentUuid);

    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);
    final HuskyEntity reloaded = spawnTamedDog(context, UUID.randomUUID(), OTHER_PARENT_POS);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        parentUuid.equals(reloaded.getParentDogUuid()),
        "First parent should survive the NBT round-trip, but was " + reloaded.getParentDogUuid());
    context.assertTrue(
        secondParentUuid.equals(reloaded.getSecondParentDogUuid()),
        "Second parent should survive the NBT round-trip, but was "
            + reloaded.getSecondParentDogUuid());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void legacyNbtWithoutSecondParentReadsAsUnknown(TestContext context) {
    final HuskyEntity original = spawnTamedDog(context, UUID.randomUUID(), PARENT_POS);
    final UUID parentUuid = UUID.randomUUID();
    original.setParentDogUuid(parentUuid);

    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);
    final HuskyEntity reloaded = spawnTamedDog(context, UUID.randomUUID(), OTHER_PARENT_POS);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        parentUuid.equals(reloaded.getParentDogUuid()),
        "Legacy single parent should still load, but was " + reloaded.getParentDogUuid());
    context.assertTrue(
        reloaded.getSecondParentDogUuid() == null,
        "A pre-lineage save must read back with no second parent, but was "
            + reloaded.getSecondParentDogUuid());
    context.complete();
  }

  private static HuskyEntity spawnTamedDog(
      final TestContext context, final UUID ownerUuid, final BlockPos relativePos) {
    final HuskyEntity husky = context.spawnEntity(ModEntities.HUSKY, relativePos);
    husky.setAiDisabled(true);
    husky.setOwnerUuid(ownerUuid);
    husky.setTamed(true, true);
    return husky;
  }
}
