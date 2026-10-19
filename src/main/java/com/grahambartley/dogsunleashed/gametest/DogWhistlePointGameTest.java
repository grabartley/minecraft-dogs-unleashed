package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.item.WhistlePointing;
import com.grahambartley.dogsunleashed.network.payload.PointWithWhistlePayload;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class DogWhistlePointGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-whistle-point";
  private static final BlockPos PLAYER_CORNER = new BlockPos(1, 2, 1);
  private static final BlockPos DOG_START = new BlockPos(1, 2, 5);
  private static final BlockPos FAR_FLOOR = new BlockPos(5, 1, 5);
  private static final BlockPos TARGET_SPOT = new BlockPos(5, 2, 5);
  private static final BlockPos FAR_CORNER = new BlockPos(5, 2, 5);
  private static final double RECALL_DISTANCE = 4.0;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void pointingAtABlockTellsTheDogToStayThere(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);

    WhistlePointing.point(
        owner, PointWithWhistlePayload.atBlock(Hand.MAIN_HAND, context.getAbsolutePos(FAR_FLOOR)));

    context.assertTrue(
        dog.getCommand() == DogCommand.STAY,
        "Pointing at a block should put the dog on Stay, but it is on " + dog.getCommand());
    context.assertTrue(
        context.getAbsolutePos(TARGET_SPOT).equals(dog.getCommandController().getAnchorPos()),
        "The dog should stay on top of the pointed block, but its spot is "
            + dog.getCommandController().getAnchorPos());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 200)
  public void aDogSentToABlockWalksAllTheWayThere(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, false);
    final BlockPos target = context.getAbsolutePos(TARGET_SPOT);

    WhistlePointing.point(
        owner, PointWithWhistlePayload.atBlock(Hand.MAIN_HAND, context.getAbsolutePos(FAR_FLOOR)));

    context.runAtEveryTick(
        () -> {
          if (dog.getBlockPos().getSquaredDistance(target) <= 1.0) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void pointingAtAZombieOrdersAnAttack(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    final ZombieEntity zombie = spawnStill(context, EntityType.ZOMBIE, FAR_CORNER);

    WhistlePointing.point(owner, PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, zombie.getId()));

    context.assertTrue(
        orders(dog).attackTarget() == zombie,
        "Pointing at a zombie should order the dog to attack it, but the order was "
            + orders(dog).attackTarget());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aDogOrderedToAttackTargetsTheZombie(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, false);
    final ZombieEntity zombie = spawnStill(context, EntityType.ZOMBIE, FAR_CORNER);

    WhistlePointing.point(owner, PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, zombie.getId()));

    context.runAtEveryTick(
        () -> {
          if (dog.getTarget() == zombie) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void pointingAtADroppedItemOrdersAFetch(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    final ItemEntity diamond = context.spawnItem(Items.DIAMOND, FAR_CORNER);

    WhistlePointing.point(owner, PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, diamond.getId()));

    context.assertTrue(
        orders(dog).fetchItem() == diamond,
        "Pointing at a dropped item should order the dog to fetch it, but the order was "
            + orders(dog).fetchItem());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 200)
  public void aDogOrderedToFetchPicksTheItemUp(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, false);
    final ItemEntity diamond = context.spawnItem(Items.DIAMOND, FAR_CORNER);

    WhistlePointing.point(owner, PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, diamond.getId()));

    context.runAtEveryTick(
        () -> {
          if (dog.getFetchCarry().isCarryingFetchedItem()
              && dog.getFetchCarry().carriedStack().isOf(Items.DIAMOND)) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aDogCarryingAFetchedItemDropsItWhenItDies(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    dog.getFetchCarry().setActiveType(null);
    dog.getFetchCarry().setCarriedStack(new ItemStack(Items.DIAMOND));
    dog.getFetchCarry().setCarrying(true);

    dog.kill();

    context.assertTrue(
        !context
            .getWorld()
            .getEntitiesByClass(
                ItemEntity.class,
                dog.getBoundingBox().expand(2.0),
                item -> item.getStack().isOf(Items.DIAMOND))
            .isEmpty(),
        "A dog that dies carrying a fetched item should drop it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void pointingAtAVillagerDoesNothing(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    final VillagerEntity villager = spawnStill(context, EntityType.VILLAGER, FAR_CORNER);

    WhistlePointing.point(
        owner, PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, villager.getId()));

    context.assertTrue(
        orders(dog).attackTarget() == null && dog.getTarget() == null,
        "Pointing at a villager must never set the dog on it");
    context.assertTrue(
        dog.getCommand() == DogCommand.FOLLOW,
        "Pointing at a villager should leave the command alone, but it is " + dog.getCommand());
    context.assertTrue(
        !owner.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE),
        "A refused point should not blow the whistle");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aSittingDogIgnoresThePoint(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    dog.setSitting(true);
    dog.setInSittingPose(true);

    WhistlePointing.point(
        owner, PointWithWhistlePayload.atBlock(Hand.MAIN_HAND, context.getAbsolutePos(FAR_FLOOR)));

    context.assertTrue(
        dog.getCommandController().getAnchorPos() == null,
        "A sitting dog should not take a point order, but it was sent to "
            + dog.getCommandController().getAnchorPos());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void sneakingWithTheWhistleDoesNotSummon(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnBoundDog(context, owner, true);
    dog.refreshPositionAndAngles(context.getAbsolutePos(FAR_CORNER), 0.0f, 0.0f);
    owner.setSneaking(true);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.squaredDistanceTo(owner) > RECALL_DISTANCE * RECALL_DISTANCE,
        "Sneaking with the whistle is for pointing and must not summon the dog");
    context.complete();
  }

  private static DogPointedOrders orders(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPointedOrders.class);
  }

  private static <T extends MobEntity> T spawnStill(
      final TestContext context, final EntityType<T> type, final BlockPos pos) {
    final T mob = context.spawnEntity(type, pos);
    mob.setAiDisabled(true);
    return mob;
  }

  private static ServerPlayerEntity placeOwner(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absolute = context.getAbsolutePos(PLAYER_CORNER);
    owner.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    final ItemStack whistle = new ItemStack(ModItems.DOG_WHISTLE);
    owner.setStackInHand(Hand.MAIN_HAND, whistle);
    owner.getItemCooldownManager().remove(ModItems.DOG_WHISTLE);
    return owner;
  }

  private static UnleashedDogEntity spawnBoundDog(
      final TestContext context, final ServerPlayerEntity owner, final boolean aiDisabled) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setAiDisabled(aiDisabled);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setCustomName(Text.literal("Scout"));
    PetManager.get(world.getServer())
        .registerPet(
            new PetData(
                dog.getUuid(),
                owner.getUuid(),
                UnleashedDogBreed.HUSKY,
                "Scout",
                10.0f,
                10.0f,
                context.getAbsolutePos(DOG_START),
                world.getRegistryKey().getValue().toString(),
                PetLifeState.LIVING));
    DogWhistleItem.bind(owner.getMainHandStack(), dog.getUuid(), "Scout");
    return dog;
  }
}
