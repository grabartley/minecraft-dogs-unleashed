package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItemTags;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogScent;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogScentGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-scent";
  private static final BlockPos OWNER_SPOT = new BlockPos(1, 2, 3);
  private static final BlockPos DOG_START = new BlockPos(1, 2, 1);
  private static final BlockPos TARGET_FLOOR = new BlockPos(5, 1, 5);
  private static final BlockPos TARGET_SPOT = new BlockPos(5, 2, 5);
  private static final double MOB_REACH = 4.0;
  private static final double BLOCK_REACH = 1.5;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void leatherLeadsTheDogToACow(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final MobEntity cow = spawnStill(context, EntityType.COW, TARGET_SPOT);

    give(dog, owner, Items.LEATHER);

    context.runAtEveryTick(
        () -> {
          if (scent(dog).phase() == ScentPhase.POINTING && dog.distanceTo(cow) <= MOB_REACH) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void rawIronLeadsTheDogToIronOre(TestContext context) {
    context.setBlockState(TARGET_FLOOR, Blocks.IRON_ORE);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);

    give(dog, owner, Items.RAW_IRON);

    context.runAtEveryTick(
        () -> {
          if (scent(dog).phase() == ScentPhase.POINTING
              && horizontalDistance(dog, context.getAbsolutePos(TARGET_FLOOR)) <= BLOCK_REACH) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void aBrushLeadsTheDogToDigAtSuspiciousSand(TestContext context) {
    context.setBlockState(TARGET_FLOOR.down(), Blocks.STONE);
    context.setBlockState(TARGET_FLOOR, Blocks.SUSPICIOUS_SAND);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);

    give(dog, owner, Items.BRUSH);

    context.runAtEveryTick(
        () -> {
          if (scent(dog).phase() == ScentPhase.DIGGING
              && horizontalDistance(dog, context.getAbsolutePos(TARGET_FLOOR)) <= BLOCK_REACH) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 80)
  public void aDogWithNothingToFindGivesUp(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);

    give(dog, owner, Items.FEATHER);
    context.assertTrue(scent(dog).isTracking(), "A feather should start a scent");

    context.runAtEveryTick(
        () -> {
          if (!scent(dog).isTracking()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void dogFoodStillFeedsInsteadOfTracking(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, owner, Items.ROTTEN_FLESH);

    context.assertFalse(scent(dog).isTracking(), "Dog food must never start a scent");
    context.assertTrue(dog.isInLove(), "Rotten flesh should still put a healthy dog in love");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 100)
  public void aDogInAFightDoesNotLead(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final MobEntity cow = spawnStill(context, EntityType.COW, TARGET_SPOT);
    final MobEntity zombie = spawnStill(context, EntityType.ZOMBIE, new BlockPos(0, 2, 5));
    zombie.setInvulnerable(true);

    give(dog, owner, Items.LEATHER);
    dog.setTarget(zombie);

    context.runAtTick(
        60,
        () -> {
          context.assertTrue(
              scent(dog).phase() != ScentPhase.POINTING,
              "A dog with a target should fight, not lead to the cow");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void givingTheSameItemAgainStopsTracking(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, owner, Items.LEATHER);
    context.assertTrue(scent(dog).isTracking(), "Leather should start a scent");
    give(dog, owner, Items.LEATHER);

    context.assertFalse(scent(dog).isTracking(), "Giving the leather again should stop it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aCommandStopsTracking(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, owner, Items.LEATHER);
    dog.getCommandController().apply(DogCommand.HEEL);

    context.assertFalse(scent(dog).isTracking(), "A command should stop the scent");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aSittingDogStandsUpToTrack(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);
    dog.getCommandController().apply(DogCommand.SIT);

    give(dog, owner, Items.LEATHER);

    context.assertTrue(scent(dog).isTracking(), "A sitting dog should still take the scent");
    context.assertTrue(
        dog.getCommand() == DogCommand.FOLLOW,
        "A sitting dog should get up to track, but its command is " + dog.getCommand());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aWildDogIgnoresScentItems(TestContext context) {
    final ServerPlayerEntity player = placeOwner(context);
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setAiDisabled(true);

    give(dog, player, Items.LEATHER);

    context.assertFalse(scent(dog).isTracking(), "A wild dog must ignore a scent item");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void anotherPlayersDogIgnoresScentItems(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final ServerPlayerEntity stranger = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, stranger, Items.LEATHER);

    context.assertFalse(
        scent(dog).isTracking(), "Another player's dog must ignore a stranger's scent item");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void anActiveScentSurvivesASaveAndLoad(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);
    give(dog, owner, Items.RAW_IRON);

    final NbtCompound saved = new NbtCompound();
    dog.writeCustomDataToNbt(saved);
    final UnleashedDogEntity loaded = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    loaded.setAiDisabled(true);
    loaded.readCustomDataFromNbt(saved);

    context.assertTrue(scent(loaded).isTracking(), "A saved scent should load back");
    context.assertTrue(
        scent(loaded).rule() != null
            && scent(loaded).rule().matchesItem(Items.RAW_IRON.getDefaultStack()),
        "The loaded scent should still be the raw iron rule");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void everyScentItemHasARule(TestContext context) {
    for (final RegistryEntry<Item> item : Registries.ITEM.iterateEntries(ModItemTags.SCENT_ITEMS)) {
      context.assertTrue(
          ScentRules.forItem(context.getWorld(), new ItemStack(item)).isPresent(),
          item.getIdAsString() + " is a scent item with no scent rule");
    }
    context.complete();
  }

  private static void give(
      final UnleashedDogEntity dog, final ServerPlayerEntity player, final Item item) {
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(item));
    dog.interactMob(player, Hand.MAIN_HAND);
  }

  private static double horizontalDistance(final UnleashedDogEntity dog, final BlockPos block) {
    final Vec3d center = Vec3d.ofCenter(block);
    return Math.hypot(center.x - dog.getX(), center.z - dog.getZ());
  }

  private static <T extends MobEntity> T spawnStill(
      final TestContext context, final EntityType<T> type, final BlockPos pos) {
    final T mob = context.spawnEntity(type, pos);
    mob.setAiDisabled(true);
    return mob;
  }

  private static ServerPlayerEntity placeOwner(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absolute = context.getAbsolutePos(OWNER_SPOT);
    player.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    return player;
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    return dog;
  }

  private static DogScent scent(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogScent.class);
  }
}
