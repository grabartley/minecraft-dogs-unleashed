package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.spawner.StraySpawner;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestTypes;

public final class VillageStrayGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_SPAWN = new BlockPos(3, FLOOR_Y, 5);
  private static final BlockPos REL_STRAY = new BlockPos(3, FLOOR_Y, 3);
  private static final Vec3d REL_PLAYER = new Vec3d(5.5, FLOOR_Y, 3.5);
  private static final int VILLAGE_HOMES = 5;
  private static final int CLAIM_TICK = 2;
  private static final int SPAWN_TICK = 4;
  private static final int FEED_GAP = 41;

  @GameTest(templateName = ARENA, batchId = "stray-village", tickLimit = 20)
  public void aVillageWithFiveOccupiedHomesGetsAtMostTwoStrays(final TestContext context) {
    buildHomes(context, VILLAGE_HOMES, true);
    context.runAtTick(
        SPAWN_TICK,
        () -> {
          final int first = StraySpawner.trySpawnAt(context.getWorld(), spawnPos(context));
          final int second = StraySpawner.trySpawnAt(context.getWorld(), spawnPos(context));
          final int third = StraySpawner.trySpawnAt(context.getWorld(), spawnPos(context));
          final List<UnleashedDogEntity> strays = straysIn(context);
          context.assertTrue(first == 1 && second == 1, "Two strays should spawn in the village");
          context.assertTrue(third == 0, "A third stray spawned in a full village");
          context.assertTrue(strays.size() == 2, "Expected 2 strays but found " + strays.size());
          context.assertTrue(
              strays.stream().noneMatch(UnleashedDogEntity::isTamed), "A stray arrived tamed");
          cleanUp(context, strays);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = "stray-small-hamlet", tickLimit = 20)
  public void fourOccupiedHomesAreNotAVillage(final TestContext context) {
    buildHomes(context, VILLAGE_HOMES - 1, true);
    assertNoStrayAtSpawnTick(context, "Four homes drew a stray");
  }

  @GameTest(templateName = ARENA, batchId = "stray-empty-homes", tickLimit = 20)
  public void emptyHomesAreNotAVillage(final TestContext context) {
    buildHomes(context, VILLAGE_HOMES, false);
    assertNoStrayAtSpawnTick(context, "Five unclaimed beds drew a stray");
  }

  @GameTest(templateName = ARENA, batchId = "stray-switched-off", tickLimit = 20)
  public void noStrayArrivesWhenVillageStraysIsOff(final TestContext context) {
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = before.withSpawning(before.spawning().withVillageStrays(false));
    buildHomes(context, VILLAGE_HOMES, true);
    context.runAtTick(
        SPAWN_TICK,
        () -> {
          try {
            context.assertTrue(
                StraySpawner.trySpawnAt(context.getWorld(), spawnPos(context)) == 0,
                "A stray spawned with Village Strays off");
          } finally {
            DogsUnleashed.SERVER_CONFIG = before;
          }
          cleanUp(context, straysIn(context));
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = FEED_GAP * 2 + 20)
  public void aStrayTakesThreeMealsToWinOverAndNeverGetsSpooked(final TestContext context) {
    final UnleashedDogEntity stray = spawnStray(context);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    context.runAtTick(
        1,
        () -> {
          feedBeef(stray, player);
          stray.getTrust().spook(player);
          context.assertTrue(
              stray.getTrust().trustOf(player) == 40,
              "One meal should give a stray 40 trust and a spook take none, but it has "
                  + stray.getTrust().trustOf(player));
        });
    context.runAtTick(1 + FEED_GAP, () -> feedBeef(stray, player));
    context.runAtTick(
        1 + FEED_GAP * 2,
        () -> {
          feedBeef(stray, player);
          context.assertTrue(
              stray.isTamed() && player.getUuid().equals(stray.getOwnerUuid()),
              "The third meal should win the stray over");
          SurvivalTestPlayer.leave(context, player);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aStrayStaysAStrayAcrossAReload(final TestContext context) {
    final UnleashedDogEntity stray = spawnStray(context);
    final NbtCompound saved = new NbtCompound();
    stray.writeNbt(saved);
    final UnleashedDogEntity loaded = context.spawnEntity(ModEntities.HUSKY, REL_STRAY);
    loaded.setAiDisabled(true);
    loaded.readNbt(saved);
    context.assertTrue(loaded.getTrust().isStray(), "The stray flag was lost on reload");
    context.assertTrue(loaded.getTrust().applies(), "A reloaded stray stopped taming by trust");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWildAdultThatIsNotAStrayStillTamesTheOldWay(final TestContext context) {
    final UnleashedDogEntity wild = context.spawnEntity(ModEntities.HUSKY, REL_STRAY);
    wild.setAiDisabled(true);
    wild.setBreedingAge(0);
    context.assertFalse(wild.getTrust().isStray(), "A wild husky came out as a stray");
    context.assertFalse(wild.getTrust().applies(), "A wild husky tames through trust");
    context.complete();
  }

  private static void assertNoStrayAtSpawnTick(final TestContext context, final String message) {
    context.runAtTick(
        SPAWN_TICK,
        () -> {
          context.assertTrue(
              StraySpawner.trySpawnAt(context.getWorld(), spawnPos(context)) == 0, message);
          cleanUp(context, straysIn(context));
          context.complete();
        });
  }

  private static void buildHomes(
      final TestContext context, final int homes, final boolean occupied) {
    context.setBlockState(REL_SPAWN.down(), Blocks.GRASS_BLOCK);
    for (int x = 0; x < homes; x++) {
      final BlockPos foot = new BlockPos(x, FLOOR_Y, 0);
      context.setBlockState(
          foot.south(),
          Blocks.RED_BED
              .getDefaultState()
              .with(BedBlock.FACING, Direction.SOUTH)
              .with(BedBlock.PART, BedPart.HEAD));
      context.setBlockState(
          foot,
          Blocks.RED_BED
              .getDefaultState()
              .with(BedBlock.FACING, Direction.SOUTH)
              .with(BedBlock.PART, BedPart.FOOT));
    }
    if (occupied) {
      context.runAtTick(
          CLAIM_TICK,
          () -> {
            for (int x = 0; x < homes; x++) {
              final BlockPos head = context.getAbsolutePos(new BlockPos(x, FLOOR_Y, 1));
              context.assertTrue(
                  context
                      .getWorld()
                      .getPointOfInterestStorage()
                      .getPosition(
                          entry -> entry.matchesKey(PointOfInterestTypes.HOME),
                          (entry, pos) -> pos.equals(head),
                          head,
                          1)
                      .isPresent(),
                  "Could not claim the bed at " + head);
            }
          });
    }
  }

  private static void cleanUp(final TestContext context, final List<UnleashedDogEntity> strays) {
    strays.forEach(UnleashedDogEntity::discard);
    for (int x = 0; x < VILLAGE_HOMES; x++) {
      context.setBlockState(new BlockPos(x, FLOOR_Y, 0), Blocks.AIR);
      context.setBlockState(new BlockPos(x, FLOOR_Y, 1), Blocks.AIR);
    }
  }

  private static BlockPos spawnPos(final TestContext context) {
    return context.getAbsolutePos(REL_SPAWN);
  }

  private static List<UnleashedDogEntity> straysIn(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            context.getTestBox().expand(2.0),
            dog -> dog.getTrust().isStray());
  }

  private static UnleashedDogEntity spawnStray(final TestContext context) {
    final UnleashedDogEntity stray = context.spawnEntity(ModEntities.HUSKY, REL_STRAY);
    stray.setAiDisabled(true);
    stray.setBreedingAge(0);
    stray.getTrust().markStray();
    return stray;
  }

  private static void feedBeef(final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BEEF));
    dog.interactMob(player, Hand.MAIN_HAND);
  }
}
