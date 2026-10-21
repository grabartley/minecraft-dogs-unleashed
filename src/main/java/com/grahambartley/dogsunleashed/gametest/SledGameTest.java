package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModAttachments;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledDrive;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeKeys;

public final class SledGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 1;
  private static final int STAND_Y = 2;
  private static final float FACING_EAST = -90.0f;
  private static final Vec3d REL_SLED = new Vec3d(1.5, STAND_Y, 4.5);
  private static final Vec3d REL_PLAYER = new Vec3d(1.5, STAND_Y, 6.5);
  private static final int RACE_TICKS = 25;

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void usingTheSledHitchesLeashedHuskiesAndGrownDireWolves(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity husky = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    final UnleashedDogEntity wolf = leashedDog(context, owner, ModEntities.DIRE_WOLF, 4, 5);
    wolf.getGrowth().setStage(DireWolfGrowthStage.ADULT);

    sled.interact(owner, Hand.MAIN_HAND);

    context.assertTrue(
        sled.getTeam().size() == 2, "Two dogs should be hitched, not " + sled.getTeam().size());
    for (final UnleashedDogEntity dog : List.of(husky, wolf)) {
      context.assertFalse(dog.isLeashed(), "A hitched dog is still on the lead");
      context.assertTrue(harness(dog).isHitched(), "A dog was not hitched");
    }
    context.assertTrue(owner.getVehicle() == null, "Hitching should not also seat the player");
    context.assertTrue(
        DogTestHelper.droppedStacksOf(context, Items.LEAD).isEmpty(), "Hitching dropped a lead");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void puppiesYoungWolvesOtherBreedsAndStrangersDogsStayOnTheLead(
      final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity puppy = leashedDog(context, owner, ModEntities.HUSKY, 4, 2);
    puppy.setBaby(true);
    final UnleashedDogEntity yearling = leashedDog(context, owner, ModEntities.DIRE_WOLF, 4, 4);
    yearling.getGrowth().setStage(DireWolfGrowthStage.YEARLING);
    final UnleashedDogEntity beagle = leashedDog(context, owner, ModEntities.BEAGLE, 4, 6);
    final UnleashedDogEntity strangers = leashedDog(context, owner, ModEntities.HUSKY, 6, 4);
    strangers.attachLeash(stranger, true);

    sled.interact(owner, Hand.MAIN_HAND);
    sled.interact(stranger, Hand.MAIN_HAND);

    context.assertTrue(
        sled.getTeam().size() == 0,
        "Nothing should be hitched, but " + sled.getTeam().size() + " were");
    for (final UnleashedDogEntity dog : List.of(puppy, yearling, beagle, strangers)) {
      context.assertTrue(dog.isLeashed(), "A dog that can't pull lost its lead");
    }
    SurvivalTestPlayer.leave(context, owner);
    SurvivalTestPlayer.leave(context, stranger);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aSledTakesSixDogsAndLeavesTheSeventhOnItsLead(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final List<UnleashedDogEntity> dogs = new ArrayList<>();
    for (int index = 0; index < 7; index++) {
      dogs.add(leashedDog(context, owner, ModEntities.HUSKY, 4 + index, 2));
    }

    sled.interact(owner, Hand.MAIN_HAND);

    context.assertTrue(
        sled.getTeam().size() == 6, "A sled should take six, not " + sled.getTeam().size());
    context.assertTrue(
        dogs.stream().filter(UnleashedDogEntity::isLeashed).count() == 1,
        "Exactly one dog should stay on its lead");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void sneakingUnhitchesOntoTheLeadKeepingCommandsAndEveryLead(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity stay = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    stay.getCommandController().apply(DogCommand.STAY);
    final UnleashedDogEntity heel = leashedDog(context, owner, ModEntities.HUSKY, 4, 5);
    heel.getCommandController().apply(DogCommand.HEEL);
    sled.interact(owner, Hand.MAIN_HAND);

    owner.setSneaking(true);
    sled.interact(owner, Hand.MAIN_HAND);

    context.assertTrue(sled.getTeam().size() == 0, "Sneaking should unhitch every dog");
    context.assertTrue(
        stay.getLeashHolder() == owner && heel.getLeashHolder() == owner,
        "Unhitched dogs should be back on the owner's lead");
    context.assertTrue(
        stay.getCommand() == DogCommand.STAY, "Stay was not kept, now " + stay.getCommand());
    context.assertTrue(
        heel.getCommand() == DogCommand.HEEL, "Heel was not kept, now " + heel.getCommand());
    context.assertTrue(
        DogTestHelper.droppedStacksOf(context, Items.LEAD).isEmpty(), "Unhitching dropped a lead");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void onlyTheOwnerCanUnhitchTheirDogs(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    sled.interact(owner, Hand.MAIN_HAND);

    stranger.setSneaking(true);
    sled.interact(stranger, Hand.MAIN_HAND);

    context.assertTrue(sled.getTeam().size() == 1, "A stranger unhitched someone else's dog");
    SurvivalTestPlayer.leave(context, owner);
    SurvivalTestPlayer.leave(context, stranger);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void nobodyCanPutALeadOnAHitchedDog(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity husky = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    sled.interact(owner, Hand.MAIN_HAND);

    for (final ServerPlayerEntity player : List.of(owner, stranger)) {
      player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.LEAD));
      husky.interact(player, Hand.MAIN_HAND);
      context.assertFalse(husky.isLeashed(), "A hitched dog was put on a lead");
      context.assertTrue(player.getMainHandStack().isOf(Items.LEAD), "The lead was used up");
    }
    context.assertTrue(sled.getTeam().size() == 1, "The dog left the team");
    SurvivalTestPlayer.leave(context, owner);
    SurvivalTestPlayer.leave(context, stranger);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 40)
  public void aHitchedDogThatDiesDropsItsLeadOnce(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity doomed = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    leashedDog(context, owner, ModEntities.HUSKY, 4, 5);
    sled.interact(owner, Hand.MAIN_HAND);

    doomed.kill();

    context.runAtTick(
        30,
        () -> {
          context.assertTrue(doomed.isRemoved(), "The dead dog was never removed");
          context.assertTrue(sled.getTeam().size() == 1, "The dead dog is still on the team");
          context.assertTrue(
              DogTestHelper.droppedStacksOf(context, Items.LEAD).size() == 1,
              "The dead dog's lead was not dropped");
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void breakingTheSledDropsItAndEveryLead(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity first = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    final UnleashedDogEntity second = leashedDog(context, owner, ModEntities.HUSKY, 4, 5);
    sled.interact(owner, Hand.MAIN_HAND);

    for (int hit = 0; hit < 5 && !sled.isRemoved(); hit++) {
      sled.damage(context.getWorld().getDamageSources().playerAttack(owner), 1.0f);
    }

    context.assertTrue(sled.isRemoved(), "Five hits should break the sled");
    context.assertTrue(
        DogTestHelper.droppedStacksOf(context, ModItems.SLED).size() == 1,
        "The sled did not drop itself");
    context.assertTrue(
        DogTestHelper.droppedStacksOf(context, Items.LEAD).size() == 2,
        "Each dog should drop its lead");
    context.assertFalse(
        harness(first).isHitched() || harness(second).isHitched(), "A dog is still hitched");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void theTeamSurvivesASaveAndLoad(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    final UnleashedDogEntity first = leashedDog(context, owner, ModEntities.HUSKY, 4, 3);
    final UnleashedDogEntity second = leashedDog(context, owner, ModEntities.HUSKY, 4, 5);
    sled.interact(owner, Hand.MAIN_HAND);
    final NbtCompound saved = new NbtCompound();
    sled.writeNbt(saved);
    sled.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);

    final SledEntity loaded = sled(context, REL_SLED);
    loaded.readNbt(saved);
    context.runAtTick(
        2,
        () -> {
          context.assertTrue(
              loaded.getTeam().present().containsAll(List.of(first, second)),
              "The loaded sled lost its team");
          context.assertTrue(
              harness(first).sled() == loaded, "The dog did not rejoin the loaded sled");
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = RACE_TICKS + 20)
  public void aTeamPullsFasterOnIceThanOnGrass(final TestContext context) {
    final ServerPlayerEntity iceRider = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity grassRider = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    floor(context, 2, Blocks.ICE);
    floor(context, 6, Blocks.GRASS_BLOCK);
    final SledEntity iceSled = raceTeam(context, iceRider, 2);
    final SledEntity grassSled = raceTeam(context, grassRider, 6);
    final double iceStart = iceSled.getX();
    final double grassStart = grassSled.getX();
    context.runAtEveryTick(
        () -> {
          iceRider.forwardSpeed = 1.0f;
          grassRider.forwardSpeed = 1.0f;
          walkTeamToSlots(iceSled);
          walkTeamToSlots(grassSled);
        });
    context.runAtTick(
        RACE_TICKS,
        () -> {
          final double iceRun = iceSled.getX() - iceStart;
          final double grassRun = grassSled.getX() - grassStart;
          context.assertTrue(
              iceSled.getDrive().speed() > grassSled.getDrive().speed(),
              "The ice team should be asked to go faster: ice "
                  + iceSled.getDrive().speed()
                  + ", grass "
                  + grassSled.getDrive().speed());
          context.assertTrue(grassRun > 0.5, "The grass team barely moved: " + grassRun);
          context.assertTrue(
              iceRun > grassRun + 1.0, "Ice moved " + iceRun + " and grass " + grassRun);
          SurvivalTestPlayer.leave(context, iceRider);
          SurvivalTestPlayer.leave(context, grassRider);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 40)
  public void sprintingDrainsTheTeamsStamina(final TestContext context) {
    final ServerPlayerEntity rider = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    floor(context, 4, Blocks.SNOW_BLOCK);
    final SledEntity sled = raceTeam(context, rider, 4);
    sled.getDrive().setSprintHeld(true);
    context.runAtEveryTick(
        () -> {
          rider.forwardSpeed = 1.0f;
          walkTeamToSlots(sled);
        });
    context.runAtTick(
        25,
        () -> {
          for (final UnleashedDogEntity dog : sled.getTeam().present()) {
            context.assertTrue(
                harness(dog).stamina().stamina() < harness(dog).capacity(),
                "A sprinting dog kept full stamina");
          }
          context.assertTrue(
              sled.staminaFraction() < 1.0f, "The team bar stayed full while sprinting");
          SurvivalTestPlayer.leave(context, rider);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void hitchingSixUnlocksFullTeam(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final SledEntity sled = sled(context, REL_SLED);
    for (int index = 0; index < 6; index++) {
      leashedDog(context, owner, ModEntities.HUSKY, 4 + index, 3);
    }
    sled.interact(owner, Hand.MAIN_HAND);
    context.assertTrue(isDone(context, owner, "full_team"), "Six dogs should unlock Full Team");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = RACE_TICKS + 20)
  public void sleddingThroughSnowCountsTowardMush(final TestContext context) {
    context.setBiome(BiomeKeys.SNOWY_PLAINS);
    final ServerPlayerEntity rider = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    rider.setAttached(ModAttachments.SLED_SNOW_BLOCKS, 999.5);
    floor(context, 4, Blocks.SNOW_BLOCK);
    final SledEntity sled = raceTeam(context, rider, 4);
    context.runAtEveryTick(
        () -> {
          rider.forwardSpeed = 1.0f;
          walkTeamToSlots(sled);
        });
    context.runAtTick(
        RACE_TICKS,
        () -> {
          context.assertTrue(
              rider.getAttachedOrElse(ModAttachments.SLED_SNOW_BLOCKS, 0.0) > 1000.0,
              "Snowy distance was not counted");
          context.assertTrue(
              isDone(context, rider, "mush"), "1000 snowy blocks should unlock Mush!");
          SurvivalTestPlayer.leave(context, rider);
          context.complete();
        });
  }

  private static SledEntity raceTeam(
      final TestContext context, final ServerPlayerEntity rider, final int lane) {
    final SledEntity sled = sled(context, new Vec3d(1.5, STAND_Y, lane + 0.5));
    leashedDog(context, rider, ModEntities.HUSKY, 4, lane - 1);
    leashedDog(context, rider, ModEntities.HUSKY, 4, lane + 1);
    sled.interact(rider, Hand.MAIN_HAND);
    sled.getTeam().present().forEach(dog -> dog.setAiDisabled(true));
    rider.startRiding(sled);
    return sled;
  }

  private static void walkTeamToSlots(final SledEntity sled) {
    for (final UnleashedDogEntity dog : sled.getTeam().present()) {
      final SledDrive.Target target = sled.getDrive().targetFor(dog);
      if (target != null) {
        dog.refreshPositionAndAngles(
            target.position().x, dog.getY(), target.position().z, dog.getYaw(), 0.0f);
      }
    }
  }

  private static void floor(final TestContext context, final int lane, final Block block) {
    for (int x = 0; x < 16; x++) {
      for (int z = lane - 1; z <= lane + 1; z++) {
        context.setBlockState(new BlockPos(x, FLOOR_Y, z), block.getDefaultState());
      }
    }
  }

  private static SledEntity sled(final TestContext context, final Vec3d relative) {
    final SledEntity sled = context.spawnEntity(ModEntities.SLED, relative);
    sled.placeFacing(FACING_EAST);
    return sled;
  }

  private static UnleashedDogEntity leashedDog(
      final TestContext context,
      final ServerPlayerEntity owner,
      final EntityType<UnleashedDogEntity> type,
      final int x,
      final int z) {
    final UnleashedDogEntity dog = context.spawnEntity(type, new BlockPos(x, STAND_Y, z));
    dog.setOwner(owner);
    dog.getCommandController().apply(DogCommand.FOLLOW);
    dog.attachLeash(owner, true);
    return dog;
  }

  private static DogSledHarness harness(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogSledHarness.class);
  }

  private static boolean isDone(
      final TestContext context, final ServerPlayerEntity player, final String advancement) {
    final AdvancementEntry entry =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, advancement));
    return entry != null && player.getAdvancementTracker().getProgress(entry).isDone();
  }
}
