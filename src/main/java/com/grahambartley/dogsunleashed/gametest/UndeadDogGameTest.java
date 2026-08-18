package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

/**
 * What being undead costs a dog: it burns in open daylight, it dies for good, and it can be cured
 * back to its living self with Weakness and a Golden Apple.
 *
 * <p>The daylight tests pin midday, so they live in their own batch with the daylight cycle frozen
 * (gametest skill rule 3). Vanilla's daylight burn is a per-tick probability rather than a
 * certainty, so the burn test latches the first ignition it sees over a long window and retries
 * (rule 8); the tests that assert a dog is *safe* need no retry, since they must hold every tick.
 */
public final class UndeadDogGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String DAYLIGHT_BATCH = "undead-daylight";
  // Relative y1 is the template floor: entities stand at y2, and a dog placed at y1 is
  // embedded in the floor, where no sky reaches it.
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final BlockPos REL_ROOF = new BlockPos(3, 4, 3);
  private static final int MIDDAY = 6000;
  private static final int DAYLIGHT_TICK_LIMIT = 260;
  private static final int DAYLIGHT_ASSERT_TICK = 250;
  private static final int TICK_LIMIT = 100;
  private static final int WEAKNESS_DURATION_TICKS = 400;

  @BeforeBatch(batchId = DAYLIGHT_BATCH)
  public void freezeMidday(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(MIDDAY);
  }

  @AfterBatch(batchId = DAYLIGHT_BATCH)
  public void restoreDaylightCycle(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void anUndeadDogCaughtUnderTheOpenSkyBurns(final TestContext context) {
    final UnleashedDogEntity dog = spawnUndeadDog(context);

    assertCatchesFire(
        context, dog, true, "An undead dog under the open midday sky should catch fire");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void anUndeadDogUnderShelterIsSafeFromDaylight(final TestContext context) {
    final UnleashedDogEntity dog = spawnUndeadDog(context);
    // An explicit roof rather than the framework's, so this test and the burning one differ by
    // exactly one thing: whether the sky can see the dog.
    context.setBlockState(REL_ROOF, Blocks.STONE);

    assertCatchesFire(
        context, dog, false, "An undead dog with a roof over it must never catch fire");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void anUndeadDogWearingArmourIsSafeFromDaylight(final TestContext context) {
    final UnleashedDogEntity dog = spawnUndeadDog(context);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));

    assertCatchesFire(
        context,
        dog,
        false,
        "Dog armour should spare an undead dog from daylight, as a helmet does");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void aLivingDogIsUntroubledByDaylight(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, REL_DOG);
    dog.setAiDisabled(true);

    assertCatchesFire(context, dog, false, "A living dog must never burn in daylight");
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDogThatDiesIsLostForever(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedUndeadDog(context);
    final PetData pet = registerPet(context, dog, PetLifeState.UNDEAD);

    dog.kill();

    context.assertTrue(
        pet.getLifeState() == PetLifeState.LOST,
        "A pet that dies while undead must be lost for good, but it was " + pet.getLifeState());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aLivingDogThatDiesCanStillBeRaised(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, REL_DOG);
    dog.setAiDisabled(true);
    dog.setOwnerUuid(UUID.randomUUID());
    dog.setTamed(true, true);
    final PetData pet = registerPet(context, dog, PetLifeState.LIVING);

    dog.kill();

    context.assertTrue(
        pet.getLifeState() == PetLifeState.DECEASED,
        "A pet that dies alive should be resurrectable, but it was " + pet.getLifeState());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aWeakenedUndeadDogFedAGoldenAppleStartsConverting(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedUndeadDog(context);
    dog.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, WEAKNESS_DURATION_TICKS));
    final PlayerEntity player = feeder(context, dog);

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertTrue(dog.getCuring().isConverting(), "The cure should have started");
    context.assertTrue(
        player.getStackInHand(Hand.MAIN_HAND).getCount() == 1,
        "Starting the cure should consume exactly one Golden Apple");
    context.assertTrue(
        dog.hasStatusEffect(StatusEffects.STRENGTH), "A converting dog should gain Strength");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDogWithoutWeaknessRefusesTheGoldenApple(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedUndeadDog(context);
    final PlayerEntity player = feeder(context, dog);

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertTrue(
        !dog.getCuring().isConverting(), "A Golden Apple alone must not start the cure");
    context.assertTrue(
        player.getStackInHand(Hand.MAIN_HAND).getCount() == 2,
        "A refused cure must not consume the Golden Apple");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aLivingDogCannotBeCured(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, REL_DOG);
    dog.setAiDisabled(true);
    dog.setOwnerUuid(UUID.randomUUID());
    dog.setTamed(true, true);
    dog.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, WEAKNESS_DURATION_TICKS));
    final PlayerEntity player = feeder(context, dog);

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertTrue(!dog.getCuring().isConverting(), "A living dog has nothing to be cured of");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aCompletedCureBringsBackTheLivingDog(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedUndeadDog(context);
    final PetData pet = registerPet(context, dog, PetLifeState.UNDEAD);
    final UUID petId = dog.getUuid();

    context.runAtTick(5, () -> dog.getCuring().restoreFromSave(1, pet.getOwnerId()));

    context.runAtTick(
        20,
        () -> {
          final UnleashedDogEntity cured = dogByUuid(context, petId);
          context.assertTrue(cured != null, "The cured dog should still be in the world");
          context.assertTrue(!cured.isUndead(), "A completed cure should leave a living dog");
          context.assertTrue(
              pet.getLifeState() == PetLifeState.LIVING,
              "A cured pet should read LIVING, but was " + pet.getLifeState());
          context.assertTrue(
              cured.getMaxHealth() == UnleashedDogBreed.HUSKY.attributes().maxHealth(),
              "A cured dog should be back on its living max health, but had "
                  + cured.getMaxHealth());
          context.complete();
        });
  }

  /**
   * Watches a dog across the whole daylight window and reports whether it ever caught fire. Time is
   * re-pinned every tick: the world clock is shared with every other batch, and vanilla's daylight
   * burn is a per-tick roll, so a single pin in {@code @BeforeBatch} is not enough to keep midday
   * in place for the whole window.
   */
  private static void assertCatchesFire(
      final TestContext context,
      final UnleashedDogEntity dog,
      final boolean expected,
      final String message) {
    final AtomicBoolean caughtFire = new AtomicBoolean(false);
    final AtomicBoolean sawDaylight = new AtomicBoolean(false);
    context.runAtEveryTick(
        () -> {
          context.getWorld().setTimeOfDay(MIDDAY);
          if (context.getWorld().isDay()) {
            sawDaylight.set(true);
          }
          if (dog.isOnFire()) {
            caughtFire.set(true);
          }
        });
    context.runAtTick(
        DAYLIGHT_ASSERT_TICK,
        () -> {
          context.assertTrue(
              sawDaylight.get(), "The daylight window never actually reached day; check the batch");
          context.assertTrue(caughtFire.get() == expected, message);
          context.complete();
        });
  }

  private static UnleashedDogEntity spawnUndeadDog(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.ZOMBIE_HUSKY, REL_DOG);
    dog.setAiDisabled(true);
    return dog;
  }

  private static UnleashedDogEntity spawnOwnedUndeadDog(final TestContext context) {
    final UnleashedDogEntity dog = spawnUndeadDog(context);
    dog.setOwnerUuid(UUID.randomUUID());
    dog.setTamed(true, true);
    return dog;
  }

  private static PlayerEntity feeder(final TestContext context, final UnleashedDogEntity dog) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    dog.setOwnerUuid(player.getUuid());
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GOLDEN_APPLE, 2));
    return player;
  }

  private static PetData registerPet(
      final TestContext context, final UnleashedDogEntity dog, final PetLifeState lifeState) {
    final ServerWorld world = context.getWorld();
    final PetData pet =
        new PetData(
            dog.getUuid(),
            dog.getOwnerUuid(),
            dog.getBreed(),
            "Wraith",
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            world.getRegistryKey().getValue().toString(),
            lifeState);
    pet.syncAppearanceFrom(dog);
    PetManager.get(world.getServer()).registerPet(pet);
    return pet;
  }

  private static UnleashedDogEntity dogByUuid(final TestContext context, final UUID uuid) {
    return context.getWorld().getEntity(uuid) instanceof UnleashedDogEntity dog ? dog : null;
  }
}
