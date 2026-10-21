package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.BondFollowOwnerGoal;
import com.grahambartley.dogsunleashed.entity.goal.EndermanBarkGoal;
import com.grahambartley.dogsunleashed.entity.goal.HuntTargetGoal;
import com.grahambartley.dogsunleashed.entity.goal.NetherUneaseGoal;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public final class DogDimensionReactionGameTest implements FabricGameTest {

  private static final String BATCH = "dog-dimension-reaction";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d LANDING = new Vec3d(8.5, 140.0, 8.5);
  private static final Vec3d OWNER_SEVEN_BLOCKS_AWAY = LANDING.add(7.0, 0.0, 0.0);
  private static final Vec3d ENDERMAN_SPOT = LANDING.add(4.0, 0.0, 0.0);
  private static final int HUNT_ATTEMPTS = 500;
  private static final int TRACKING_TICK_LIMIT = 200;
  private static final List<RegistryKey<World>> OTHER_DIMENSIONS = List.of(World.NETHER, World.END);

  @BeforeBatch(batchId = BATCH)
  public void forceLandingChunks(final ServerWorld world) {
    this.setLandingChunksForced(world, true);
  }

  @AfterBatch(batchId = BATCH)
  public void releaseLandingChunks(final ServerWorld world) {
    this.setLandingChunksForced(world, false);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void dogInTheNetherFollowsFromCloser(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity overworldDog = dog(context, owner, DogCommand.FOLLOW);
    owner.refreshPositionAndAngles(overworldDog.getPos().add(7.0, 0.0, 0.0), 0.0f, 0.0f);
    context.assertFalse(
        follow(overworldDog).canStart(), "An Overworld dog waits until its owner is ten away");

    final ServerWorld nether = world(context, World.NETHER);
    owner.teleport(
        nether,
        OWNER_SEVEN_BLOCKS_AWAY.x,
        OWNER_SEVEN_BLOCKS_AWAY.y,
        OWNER_SEVEN_BLOCKS_AWAY.z,
        0.0f,
        0.0f);
    final UnleashedDogEntity netherDog = moveTo(context, overworldDog, nether, LANDING);
    final boolean follows = follow(netherDog).canStart();
    PendantTestHelper.wear(netherDog, PendantType.GUARDIAN_FIRE);
    final boolean calmFollows = follow(netherDog).canStart();
    netherDog.discard();

    context.assertTrue(follows, "A Nether dog follows an owner seven blocks away");
    context.assertFalse(calmFollows, "A dog wearing the Fire Guardian pendant follows as usual");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void dogInTheNetherIsUneasyUnlessItWearsTheFireGuardian(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity overworldDog = dog(context, owner, DogCommand.FOLLOW);
    context.assertFalse(
        new NetherUneaseGoal(overworldDog).canStart(), "An Overworld dog is at ease");

    final UnleashedDogEntity netherDog =
        moveTo(context, overworldDog, world(context, World.NETHER), LANDING);
    final boolean uneasy = new NetherUneaseGoal(netherDog).canStart();
    PendantTestHelper.wear(netherDog, PendantType.GUARDIAN_FIRE);
    final boolean guardedUneasy = new NetherUneaseGoal(netherDog).canStart();
    netherDog.discard();

    context.assertTrue(uneasy, "A dog in the Nether is uneasy");
    context.assertFalse(guardedUneasy, "A dog wearing the Fire Guardian pendant is calm");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TRACKING_TICK_LIMIT)
  public void huntingDogInTheEndTargetsAnEndermite(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final ServerWorld end = world(context, World.END);
    final UnleashedDogEntity dog =
        moveTo(context, dog(context, owner, DogCommand.HUNT), end, LANDING);
    final EndermiteEntity endermite =
        moveTo(context, context.spawnEntity(EntityType.ENDERMITE, REL_DOG), end, ENDERMAN_SPOT);

    whenTracked(
        context,
        List.of(dog, endermite),
        () -> {
          final HuntTargetGoal hunt = new HuntTargetGoal(dog);
          boolean started = false;
          for (int attempt = 0; attempt < HUNT_ATTEMPTS && !started; attempt++) {
            started = hunt.canStart();
          }
          if (started) {
            hunt.start();
          }
          final Entity target = dog.getTarget();
          dog.discard();
          endermite.discard();

          context.assertTrue(started, "A dog on Hunt in the End should look for a target");
          context.assertTrue(target == endermite, "A dog on Hunt in the End chases the endermite");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void huntingDogSparesEndermenOnlyInTheEnd(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity overworldDog = dog(context, owner, DogCommand.HUNT);
    final EndermanEntity enderman = context.spawnEntity(EntityType.ENDERMAN, REL_DOG.add(2, 0, 0));
    enderman.setAiDisabled(true);
    context.assertTrue(
        HuntTargetGoal.huntsTarget(overworldDog, enderman), "Overworld hunting is unchanged");

    final UnleashedDogEntity endDog =
        moveTo(context, overworldDog, world(context, World.END), LANDING);
    final boolean huntsEnderman = HuntTargetGoal.huntsTarget(endDog, enderman);
    endDog.discard();

    context.assertFalse(huntsEnderman, "A dog in the End leaves endermen alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TRACKING_TICK_LIMIT)
  public void dogInTheEndBarksAtANearbyEnderman(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity overworldDog = dog(context, owner, DogCommand.FOLLOW);
    final EndermanEntity overworldEnderman =
        context.spawnEntity(EntityType.ENDERMAN, REL_DOG.add(2, 0, 0));
    overworldEnderman.setAiDisabled(true);
    context.assertFalse(
        new EndermanBarkGoal(overworldDog).canStart(), "An Overworld dog ignores endermen");

    final ServerWorld end = world(context, World.END);
    final UnleashedDogEntity endDog = moveTo(context, overworldDog, end, LANDING);
    final EndermanEntity enderman = moveTo(context, overworldEnderman, end, ENDERMAN_SPOT);
    whenTracked(
        context,
        List.of(endDog, enderman),
        () -> {
          final EndermanBarkGoal bark = new EndermanBarkGoal(endDog);
          final boolean watches = bark.canStart();
          final EndermanEntity watched = bark.enderman();
          bark.start();
          bark.tick();
          final boolean provoked = enderman.isAngry() || enderman.getTarget() != null;
          endDog.discard();
          enderman.discard();

          context.assertTrue(watches, "A dog in the End watches a nearby enderman");
          context.assertTrue(watched == enderman, "The dog watches that enderman");
          context.assertFalse(provoked, "Barking at the enderman does not anger it");
          context.complete();
        });
  }

  private static void whenTracked(
      final TestContext context, final List<Entity> entities, final Runnable check) {
    final boolean[] checked = {false};
    context.runAtEveryTick(
        () -> {
          if (!checked[0] && entities.stream().allMatch(DogDimensionReactionGameTest::isTracked)) {
            checked[0] = true;
            check.run();
          }
        });
  }

  private static boolean isTracked(final Entity entity) {
    return !entity
        .getWorld()
        .getEntitiesByClass(Entity.class, entity.getBoundingBox(), found -> found == entity)
        .isEmpty();
  }

  private static UnleashedDogEntity dog(
      final TestContext context, final ServerPlayerEntity owner, final DogCommand command) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(command);
    dog.getComponents().get(DogPersonality.class).set(DogTraits.of());
    return dog;
  }

  private static BondFollowOwnerGoal follow(final UnleashedDogEntity dog) {
    return new BondFollowOwnerGoal(dog, 1.0, 10.0f, 2.0f, EnumSet.of(DogCommand.FOLLOW));
  }

  private static ServerWorld world(final TestContext context, final RegistryKey<World> key) {
    return context.getWorld().getServer().getWorld(key);
  }

  @SuppressWarnings("unchecked")
  private static <E extends Entity> E moveTo(
      final TestContext context, final E entity, final ServerWorld destination, final Vec3d spot) {
    final Entity moved =
        entity.teleportTo(
            new TeleportTarget(destination, spot, Vec3d.ZERO, 0.0f, 0.0f, TeleportTarget.NO_OP));
    context.assertTrue(
        moved != null && moved.getWorld() == destination,
        "The entity should have moved to " + destination.getRegistryKey().getValue());
    if (moved instanceof MobEntity mob) {
      mob.setAiDisabled(true);
    }
    if (moved instanceof UnleashedDogEntity dog) {
      dog.getComponents().get(DogPersonality.class).set(DogTraits.of());
    }
    return (E) moved;
  }

  private void setLandingChunksForced(final ServerWorld world, final boolean forced) {
    final ChunkPos chunk = new ChunkPos(BlockPos.ofFloored(LANDING));
    for (final RegistryKey<World> key : OTHER_DIMENSIONS) {
      final ServerWorld other = world.getServer().getWorld(key);
      for (int dx = 0; dx <= 1; dx++) {
        other.setChunkForced(chunk.x + dx, chunk.z, forced);
      }
    }
  }
}
