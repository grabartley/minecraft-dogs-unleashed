package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.BondFollowOwnerGoal;
import com.grahambartley.dogsunleashed.entity.goal.BringToyGoal;
import com.grahambartley.dogsunleashed.entity.goal.DogEscapeDangerGoal;
import com.grahambartley.dogsunleashed.entity.goal.GreedySnackGoal;
import com.grahambartley.dogsunleashed.entity.goal.LonelyWhineGoal;
import com.grahambartley.dogsunleashed.entity.goal.PuppyAwareWanderGoal;
import com.grahambartley.dogsunleashed.entity.goal.ThreatStanceGoal;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import java.util.EnumSet;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogPersonalityGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d REL_OWNER = new Vec3d(3.5, 2, 1.5);

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void braveDogNeverFleesAnAttacker(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity brave = dog(context, owner, DogCommand.FOLLOW, DogTrait.BRAVE);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);

    context.assertFalse(
        new DogEscapeDangerGoal(brave, 1.5).mayFlee(), "A brave dog must not flee a fight");
    context.assertTrue(
        new DogEscapeDangerGoal(plain, 1.5).mayFlee(), "A plain Follow dog still flees");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void timidGuardDogFleesAtHalfHealth(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity timid = dog(context, owner, DogCommand.GUARD, DogTrait.TIMID);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.GUARD);
    final DogEscapeDangerGoal timidFlee = new DogEscapeDangerGoal(timid, 1.5);
    final DogEscapeDangerGoal plainFlee = new DogEscapeDangerGoal(plain, 1.5);

    context.assertFalse(timidFlee.mayFlee(), "A healthy timid guard holds its ground");
    timid.setHealth(timid.getMaxHealth() * 0.4f);
    plain.setHealth(plain.getMaxHealth() * 0.4f);
    context.assertTrue(timidFlee.mayFlee(), "A timid guard below half health flees");
    context.assertFalse(plainFlee.mayFlee(), "A plain guard below half health holds on");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void timidDogStaysOutOfItsOwnersFights(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity timid = dog(context, owner, DogCommand.FOLLOW, DogTrait.TIMID);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);
    final ZombieEntity zombie = zombie(context, new BlockPos(1, 2, 1));

    context.assertFalse(
        timid.canAttackWithOwner(zombie, owner), "A timid dog must not join its owner's fight");
    context.assertTrue(plain.canAttackWithOwner(zombie, owner), "A plain dog joins the fight");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void braveAndTimidDogsTakeAStanceNearAThreat(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity brave = dog(context, owner, DogCommand.FOLLOW, DogTrait.BRAVE);
    final UnleashedDogEntity timid = dog(context, owner, DogCommand.FOLLOW, DogTrait.TIMID);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);
    final ZombieEntity zombie = zombie(context, new BlockPos(1, 2, 1));

    final ThreatStanceGoal braveStance = new ThreatStanceGoal(brave);
    final ThreatStanceGoal timidStance = new ThreatStanceGoal(timid);
    context.assertTrue(braveStance.canStart(), "A brave dog squares up to a nearby zombie");
    context.assertTrue(braveStance.threat() == zombie, "The brave dog faces the zombie");
    context.assertTrue(timidStance.canStart(), "A timid dog backs off from a nearby zombie");
    context.assertFalse(new ThreatStanceGoal(plain).canStart(), "A plain dog takes no stance");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void greedyDogEatsDroppedFood(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity greedy = dog(context, owner, DogCommand.FOLLOW, DogTrait.GREEDY);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);
    final ItemEntity bread = context.spawnItem(Items.BREAD, REL_DOG);
    bread.getStack().setCount(3);
    greedy.setHealth(greedy.getMaxHealth() - 6.0f);

    context.assertFalse(
        new GreedySnackGoal(plain, () -> true).canStart(), "A plain dog leaves food alone");
    final GreedySnackGoal snack = new GreedySnackGoal(greedy, () -> true);
    context.assertTrue(snack.canStart(), "A greedy dog goes for dropped bread");
    snack.start();
    snack.tick();
    context.assertTrue(
        bread.getStack().getCount() == 2, "The dog eats one bread, left " + bread.getStack());
    context.assertTrue(
        greedy.getHealth() > greedy.getMaxHealth() - 6.0f, "Eating heals the greedy dog");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void greedyDogTakesFoodFromAnOpenChest(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity greedy = dog(context, owner, DogCommand.FOLLOW, DogTrait.GREEDY);
    final BlockPos relChest = REL_DOG.east();
    context.setBlockState(relChest, Blocks.CHEST);
    final ChestBlockEntity chest = context.getBlockEntity(relChest);
    chest.setStack(0, new ItemStack(Items.STONE));
    chest.setStack(4, new ItemStack(Items.COOKED_BEEF, 2));

    final GreedySnackGoal snack = new GreedySnackGoal(greedy, () -> true);
    context.assertFalse(snack.canStart(), "A closed chest is safe from a greedy dog");
    chest.onOpen(owner);
    context.assertTrue(snack.canStart(), "A greedy dog spots food in an open chest");
    snack.start();
    snack.tick();
    context.assertTrue(
        chest.getStack(4).getCount() == 1, "The dog takes one beef, left " + chest.getStack(4));
    context.assertTrue(chest.getStack(0).isOf(Items.STONE), "Non-food stays in the chest");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void lazyDogWandersMoreSlowly(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity lazy = dog(context, owner, DogCommand.FREE_ROAM, DogTrait.LAZY);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FREE_ROAM);

    final double lazySpeed = new PuppyAwareWanderGoal(lazy, 1.0).walkSpeed();
    final double plainSpeed = new PuppyAwareWanderGoal(plain, 1.0).walkSpeed();
    context.assertTrue(
        lazySpeed < plainSpeed, "Lazy wanders at " + lazySpeed + ", plain at " + plainSpeed);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void clingyDogFollowsFromCloser(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER.add(7, 0, 1.5));
    final UnleashedDogEntity clingy = dog(context, owner, DogCommand.FOLLOW, DogTrait.CLINGY);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);

    context.assertTrue(
        follow(clingy).canStart(), "A clingy dog follows an owner seven blocks away");
    context.assertFalse(follow(plain).canStart(), "A plain dog waits until ten blocks");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void clingyDogLeftOnStayIsLonely(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER.add(20, 0, 0));
    final UnleashedDogEntity clingy = dog(context, owner, DogCommand.STAY, DogTrait.CLINGY);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.STAY);

    context.assertTrue(
        new LonelyWhineGoal(clingy).canStart(), "A clingy dog left on Stay gets lonely");
    context.assertFalse(new LonelyWhineGoal(plain).canStart(), "A plain dog waits quietly");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void playfulDogBringsALooseToy(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity playful = dog(context, owner, DogCommand.FOLLOW, DogTrait.PLAYFUL);
    final UnleashedDogEntity plain = dog(context, owner, DogCommand.FOLLOW);
    final ItemEntity ball = context.spawnItem(ModItems.TENNIS_BALL, new BlockPos(5, 2, 5));
    ball.resetPickupDelay();

    context.assertFalse(
        new BringToyGoal(plain, () -> true).canStart(), "A plain dog ignores a loose toy");
    final BringToyGoal bring = new BringToyGoal(playful, () -> true);
    context.assertTrue(bring.canStart(), "A playful dog goes for a loose toy");
    bring.start();
    context.assertTrue(
        playful.getComponents().get(DogPointedOrders.class).fetchItem() == ball,
        "The playful dog sets off to fetch the ball");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void personalitySurvivesSaveAndLoad(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog =
        dog(context, owner, DogCommand.FOLLOW, DogTrait.GREEDY, DogTrait.CLINGY);
    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    final UnleashedDogEntity reloaded = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);
    context.assertTrue(
        traits(reloaded).equals(DogTraits.of(DogTrait.GREEDY, DogTrait.CLINGY)),
        "Traits reload as saved, got " + traits(reloaded));

    nbt.remove(ModNbtKeys.PERSONALITY);
    final UnleashedDogEntity legacy = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    legacy.setAiDisabled(true);
    legacy.readCustomDataFromNbt(nbt);
    context.assertFalse(traits(legacy).isEmpty(), "A dog saved before traits rolls some");
    context.complete();
  }

  private static BondFollowOwnerGoal follow(final UnleashedDogEntity dog) {
    return new BondFollowOwnerGoal(dog, 1.0, 10.0f, 2.0f, EnumSet.of(DogCommand.FOLLOW));
  }

  private static ZombieEntity zombie(final TestContext context, final BlockPos pos) {
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, pos);
    zombie.setAiDisabled(true);
    return zombie;
  }

  private static ServerPlayerEntity ownerAt(final TestContext context, final Vec3d relative) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    owner.refreshPositionAndAngles(context.getAbsolute(relative), 0.0f, 0.0f);
    return owner;
  }

  private static UnleashedDogEntity dog(
      final TestContext context,
      final ServerPlayerEntity owner,
      final DogCommand command,
      final DogTrait... traits) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(command);
    dog.getComponents().get(DogPersonality.class).set(DogTraits.of(traits));
    return dog;
  }

  private static DogTraits traits(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPersonality.class).traits();
  }
}
