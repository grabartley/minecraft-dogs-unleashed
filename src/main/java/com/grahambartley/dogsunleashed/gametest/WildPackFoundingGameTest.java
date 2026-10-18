package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogSpawnerOrigin;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class WildPackFoundingGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int TICK_LIMIT = 20;
  private static final int ARENA_WIDTH = 7;
  private static final int FLOOR_Y = 1;
  private static final BlockPos REL_LEADER = new BlockPos(3, 2, 3);

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void aNaturalLeaderBringsItsWholePack(final TestContext context) {
    assertLeaderBringsWholePack(context, SpawnReason.NATURAL);
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void aChunkGenerationLeaderBringsItsWholePack(final TestContext context) {
    assertLeaderBringsWholePack(context, SpawnReason.CHUNK_GENERATION);
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void packMembersInheritWhetherTheLeaderCanDespawn(final TestContext context) {
    coverFloor(context, Blocks.GRASS_BLOCK);
    final UnleashedDogEntity leader = spawnLeader(context);
    leader.getComponents().get(DogSpawnerOrigin.class).setFromSpawner(true);
    initialize(context, leader, SpawnReason.NATURAL, null);

    final List<UnleashedDogEntity> pack = direWolves(context);
    context.assertTrue(pack.size() > 1, "Precondition: the leader brought a pack");
    context.assertTrue(
        pack.stream()
            .allMatch(wolf -> wolf.getComponents().get(DogSpawnerOrigin.class).isFromSpawner()),
        "Every member of a top-up pack should despawn like its leader");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void aSpawnEggDireWolfComesAlone(final TestContext context) {
    coverFloor(context, Blocks.GRASS_BLOCK);
    final UnleashedDogEntity wolf = spawnLeader(context);
    initialize(context, wolf, SpawnReason.SPAWN_EGG, null);

    context.assertTrue(
        direWolves(context).size() == 1,
        "A spawn egg dire wolf should come alone, but found " + direWolves(context).size());
    context.assertTrue(wolf.getPack().id() == null, "A spawn egg dire wolf has no wild pack");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void theSpawnRuleRejectsADireWolfWithNoRoomForItsPack(final TestContext context) {
    context.setBlockState(REL_LEADER.down(), Blocks.GRASS_BLOCK);
    final BlockPos absLeader = context.getAbsolutePos(REL_LEADER);
    final ServerWorld world = context.getWorld();

    context.assertTrue(
        UnleashedDogEntity.canSpawn(
            ModEntities.DIRE_WOLF, world, SpawnReason.NATURAL, absLeader, world.getRandom()),
        "Precondition: the leader's own block passes the ground and light rules");
    context.assertFalse(
        SpawnRestriction.canSpawn(
            ModEntities.DIRE_WOLF, world, SpawnReason.NATURAL, absLeader, world.getRandom()),
        "A dire wolf should not spawn where its smallest pack can't fit");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void theSpawnRuleAcceptsADireWolfWithRoomForItsPack(final TestContext context) {
    coverFloor(context, Blocks.GRASS_BLOCK);
    final ServerWorld world = context.getWorld();
    context.assertTrue(
        SpawnRestriction.canSpawn(
            ModEntities.DIRE_WOLF,
            world,
            SpawnReason.NATURAL,
            context.getAbsolutePos(REL_LEADER),
            world.getRandom()),
        "A dire wolf should spawn where its whole pack fits");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT, skyAccess = true)
  public void theSpawnRuleNeedsNoPackRoomForABreedThatDoesNotLiveInPacks(
      final TestContext context) {
    context.setBlockState(REL_LEADER.down(), Blocks.GRASS_BLOCK);
    final ServerWorld world = context.getWorld();
    context.assertTrue(
        SpawnRestriction.canSpawn(
            DogTestData.HUSKY.entityType(),
            world,
            SpawnReason.NATURAL,
            context.getAbsolutePos(REL_LEADER),
            world.getRandom()),
        "A husky needs only its own block");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aNaturalHuskyGroupSharesAGroupButIsNotAWildPack(final TestContext context) {
    EntityData groupData = null;
    UUID firstGroup = null;
    for (int i = 0; i < 2; i++) {
      final UnleashedDogEntity husky =
          DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(2 + i, 2, 3));
      husky.setAiDisabled(true);
      groupData = initialize(context, husky, SpawnReason.NATURAL, groupData);
      final UUID group = husky.getPack().id();
      context.assertTrue(group != null, "A naturally spawned husky belongs to a group");
      if (firstGroup == null) {
        firstGroup = group;
      }
      context.assertTrue(group.equals(firstGroup), "Huskies spawned together share one group");
      context.assertFalse(
          husky.getPack().isWildMember(), "A husky group never acts as a wild pack");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void vanillaPlacesOnePackLeaderPerChunkButNormalGroupsForOtherBreeds(
      final TestContext context) {
    final UnleashedDogEntity wolf = spawnLeader(context);
    final UnleashedDogEntity husky =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    context.assertTrue(
        wolf.getLimitPerChunk() == 1,
        "Vanilla spawning should place one pack leader per chunk, not " + wolf.getLimitPerChunk());
    context.assertTrue(
        husky.getLimitPerChunk() == 4,
        "Other breeds keep vanilla's limit of 4 per chunk, not " + husky.getLimitPerChunk());
    context.complete();
  }

  private static void assertLeaderBringsWholePack(
      final TestContext context, final SpawnReason reason) {
    coverFloor(context, Blocks.GRASS_BLOCK);
    final UnleashedDogEntity leader = spawnLeader(context);
    initialize(context, leader, reason, null);

    final DireWolfSettings settings = DogsUnleashed.SERVER_CONFIG.direWolf();
    final List<UnleashedDogEntity> pack = direWolves(context);
    context.assertTrue(
        pack.size() >= settings.smallestPack() && pack.size() <= settings.largestPack(),
        "A "
            + reason
            + " pack should have "
            + settings.smallestPack()
            + " to "
            + settings.largestPack()
            + " wolves but had "
            + pack.size());
    context.assertTrue(
        pack.stream().allMatch(wolf -> leader.getPack().id().equals(wolf.getPack().id())),
        "Every wolf the leader brought shares its pack id");
    context.assertTrue(
        pack.stream().anyMatch(wolf -> wolf.getGrowth().stage() == DireWolfGrowthStage.PUP),
        "The pack has a pup");
    context.assertTrue(
        pack.stream().filter(wolf -> wolf.getGrowth().stage() != DireWolfGrowthStage.PUP).count()
            >= 3,
        "The pack has at least three wolves that are not pups");
    context.complete();
  }

  private static UnleashedDogEntity spawnLeader(final TestContext context) {
    final UnleashedDogEntity leader = context.spawnEntity(ModEntities.DIRE_WOLF, REL_LEADER);
    leader.setAiDisabled(true);
    return leader;
  }

  private static EntityData initialize(
      final TestContext context,
      final UnleashedDogEntity dog,
      final SpawnReason reason,
      final EntityData data) {
    final ServerWorld world = context.getWorld();
    return dog.initialize(world, world.getLocalDifficulty(dog.getBlockPos()), reason, data);
  }

  private static List<UnleashedDogEntity> direWolves(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            context.getTestBox(),
            dog -> dog.isAlive() && dog.getType() == ModEntities.DIRE_WOLF);
  }

  private static void coverFloor(final TestContext context, final Block block) {
    for (int x = 0; x < ARENA_WIDTH; x++) {
      for (int z = 0; z < ARENA_WIDTH; z++) {
        context.setBlockState(new BlockPos(x, FLOOR_Y, z), block);
      }
    }
  }
}
