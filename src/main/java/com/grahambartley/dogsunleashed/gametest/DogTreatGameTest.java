package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogTreatBuff;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogTreatGameTest implements FabricGameTest {

  private static final String BATCH = "dog-treat";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final BlockPos STRANGER_POS = new BlockPos(2, 2, 3);
  private static final int TICK_LIMIT = 20;
  private static final int EXPIRY_TICK_LIMIT = 40;
  private static final int EXPIRY_ASSERT_TICK = 10;
  private static final int HELD_STACK_COUNT = 3;
  private static final double ATTRIBUTE_TOLERANCE = 1.0e-6;

  @CustomTestProvider
  public Collection<TestFunction> feedingAppliesBuffPerBreed() {
    return generatePerBreed(
        "feedingappliesbuff", TICK_LIMIT, DogTreatGameTest::testFeedingAppliesBuff);
  }

  @CustomTestProvider
  public Collection<TestFunction> feedingConsumesOneTreatPerBreed() {
    return generatePerBreed(
        "feedingconsumesonetreat", TICK_LIMIT, DogTreatGameTest::testFeedingConsumesOneTreat);
  }

  @CustomTestProvider
  public Collection<TestFunction> feedingRaisesAttributesPerBreed() {
    return generatePerBreed(
        "feedingraisesattributes", TICK_LIMIT, DogTreatGameTest::testFeedingRaisesAttributes);
  }

  @CustomTestProvider
  public Collection<TestFunction> untamedDogRejectsTreatPerBreed() {
    return generatePerBreed(
        "untamedrejectstreat", TICK_LIMIT, DogTreatGameTest::testUntamedDogRejectsTreat);
  }

  private static void testFeedingAppliesBuff(final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, data, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        dog.getTreatBuffState().getRemainingTicks(),
        DogTreatBuff.DURATION_TICKS,
        "Feeding a treat should start the buff at its full duration");
    context.assertTrue(
        DogTreatBuff.isApplied(dog),
        "Feeding a treat should attach the movement speed modifier to the dog");
    context.complete();
  }

  private static void testFeedingConsumesOneTreat(
      final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, data, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(),
        HELD_STACK_COUNT - 1,
        "Feeding should consume exactly one treat from the held stack");
    context.complete();
  }

  private static void testFeedingRaisesAttributes(
      final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, data, owner);
    final double speedBefore = dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    final double attackBefore = dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    assertCloseTo(
        context,
        dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        speedBefore * (1.0 + DogTreatBuff.MOVEMENT_SPEED_BONUS_FRACTION),
        "movement speed");
    assertCloseTo(
        context,
        dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        attackBefore + DogTreatBuff.ATTACK_DAMAGE_BONUS,
        "attack damage");
    context.complete();
  }

  private static void testUntamedDogRejectsTreat(
      final TestContext context, final DogTestData data) {
    final ServerPlayerEntity player = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data, DOG_POS);
    dog.setAiDisabled(true);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertFalse(
        dog.getTreatBuffState().isActive(), "An untamed dog must not gain the treat buff");
    context.assertEquals(
        player.getStackInHand(Hand.MAIN_HAND).getCount(),
        HELD_STACK_COUNT,
        "Feeding an untamed dog must not consume a treat");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void nonOwnerCannotFeedTamedDog(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = survivalOwnerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertFalse(
        dog.getTreatBuffState().isActive(),
        "A dog must not accept a treat from a player who does not own it");
    context.assertEquals(
        stranger.getStackInHand(Hand.MAIN_HAND).getCount(),
        HELD_STACK_COUNT,
        "A rejected feed must not consume a treat");
    context.complete();
  }

  /**
   * Sneak-right-clicking with any non-taming item starts the bed assignment flow, and holding a
   * pocket of treats must not take that away from the owner.
   */
  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sneakingOwnerDoesNotFeedTheTreat(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));
    owner.setSneaking(true);

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertFalse(
        dog.getTreatBuffState().isActive(),
        "Sneak-right-click is the bed assignment gesture, not a feed");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(),
        HELD_STACK_COUNT,
        "A sneak interaction must not consume a treat");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void refeedingRefreshesRatherThanStacks(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    final double speedBefore = dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);
    dog.interactMob(owner, Hand.MAIN_HAND);

    assertCloseTo(
        context,
        dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        speedBefore * (1.0 + DogTreatBuff.MOVEMENT_SPEED_BONUS_FRACTION),
        "movement speed after a second treat");
    context.complete();
  }

  /**
   * Drives the buff to its final tick through the persisted counter rather than waiting out the
   * full sixty seconds, so the expiry path is exercised inside a normal tick budget.
   */
  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = EXPIRY_TICK_LIMIT)
  public void buffExpiryRemovesAttributeModifiers(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    final double speedBefore = dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT));

    dog.interactMob(owner, Hand.MAIN_HAND);
    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.TREAT_BUFF_TICKS, 1);
    dog.readCustomDataFromNbt(nbt);

    context.runAtTick(
        EXPIRY_ASSERT_TICK,
        () -> {
          context.assertFalse(
              dog.getTreatBuffState().isActive(), "The treat buff should expire once it runs out");
          context.assertFalse(
              DogTreatBuff.isApplied(dog),
              "Expiry should detach the movement speed modifier from the dog");
          assertCloseTo(
              context,
              dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
              speedBefore,
              "movement speed after expiry");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void treatBuffSurvivesNbtRoundTrip(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity fed = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT));
    fed.interactMob(owner, Hand.MAIN_HAND);

    final NbtCompound nbt = new NbtCompound();
    fed.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertEquals(
        reloaded.getTreatBuffState().getRemainingTicks(),
        fed.getTreatBuffState().getRemainingTicks(),
        "The remaining treat buff duration should survive a save and load");
    context.assertTrue(
        DogTreatBuff.isApplied(reloaded),
        "Loading a dog mid-buff should re-attach the movement speed modifier");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wheatBoneMealAndSugarCraftFourTreats(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(
            3,
            1,
            List.of(
                new ItemStack(Items.WHEAT),
                new ItemStack(Items.BONE_MEAL),
                new ItemStack(Items.SUGAR)));

    final ItemStack result =
        world
            .getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, input, world)
            .map(entry -> entry.value().craft(input, world.getRegistryManager()))
            .orElse(ItemStack.EMPTY);

    context.assertTrue(
        result.isOf(ModItems.DOG_TREAT),
        "Wheat plus bone meal plus sugar should craft a dog treat but crafted " + result);
    context.assertEquals(result.getCount(), 4, "One craft should yield four treats");
    context.complete();
  }

  private static void assertCloseTo(
      final TestContext context, final double actual, final double expected, final String label) {
    context.assertTrue(
        Math.abs(actual - expected) < ATTRIBUTE_TOLERANCE,
        "Expected " + label + " of " + expected + " but was " + actual);
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final DogTestData data, final PlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, data, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static ServerPlayerEntity survivalOwnerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.changeGameMode(GameMode.SURVIVAL);
    final BlockPos absolutePos = context.getAbsolutePos(relativePos);
    owner.refreshPositionAndAngles(
        absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0f, 0f);
    return owner;
  }

  private Collection<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
                    "dogtreatgametest." + behavior + "." + data.breed().serializedId(),
                    ARENA,
                    tickLimit,
                    /* setupTicks */ 0L,
                    /* required */ true,
                    context -> body.run(context, data)))
        .toList();
  }

  @FunctionalInterface
  private interface PerBreedBody {
    void run(TestContext context, DogTestData data);
  }
}
