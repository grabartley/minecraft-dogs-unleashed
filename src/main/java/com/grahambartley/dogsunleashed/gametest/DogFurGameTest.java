package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModArmorItems;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogFur;
import com.grahambartley.dogsunleashed.entity.DogFurRules;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class DogFurGameTest implements FabricGameTest {

  private static final String BATCH = "dog-fur";
  private static final int TICK_LIMIT = 20;
  private static final int TICKS_BEFORE_DUE = 3;
  private static final int TICKS_RUN = 10;
  private static final int NO_BOTCH = 0;
  private static final int CERTAIN_BOTCH = DogsUnleashedConfig.FUR_MULTIPLIER_MAX;

  private static Collection<TestFunction> perBreed(
      final String name, final BiConsumer<TestContext, DogTestData> body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                GeneratedGameTest.of(
                    BATCH,
                    "dogfurgametest." + name + "." + data.breed().serializedId(),
                    ARENA,
                    TICK_LIMIT,
                    context -> body.accept(context, data)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> anAdultGrowsOneStageWhenItsTimeIsUp() {
    return perBreed("grows", DogFurGameTest::testGrows);
  }

  private static void testGrows(final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = wildAdult(context, data);
    seed(dog, DogFurStage.NATURAL, false, ticksPerStage(dog) - TICKS_BEFORE_DUE);

    tick(dog, TICKS_RUN);

    context.assertTrue(
        dog.getFur().stage() == DogFurStage.SHAGGY,
        data.breed() + " should be shaggy once its stage time is up, was " + dog.getFur().stage());
    final boolean capped = dog.getFur().stage() == dog.getFur().profile().maxStage();
    context.assertEquals(
        dog.getFur().ticksInStage(),
        capped ? 0 : TICKS_RUN - TICKS_BEFORE_DUE,
        capped ? "The clock stops at the breed's cap" : "The clock restarts for the next stage");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> growthStopsAtTheBreedsLongestCoat() {
    return perBreed("stops", DogFurGameTest::testStopsAtMax);
  }

  private static void testStopsAtMax(final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = wildAdult(context, data);
    final DogFurStage max = dog.getFur().profile().maxStage();
    seed(dog, max, false, ticksPerStage(dog) - TICKS_BEFORE_DUE);

    tick(dog, TICKS_RUN);

    context.assertTrue(
        dog.getFur().stage() == max, data.breed() + " should never grow past " + max);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPuppyKeepsItsCoat(final TestContext context) {
    final UnleashedDogEntity puppy = wildAdult(context, DogTestData.HUSKY);
    puppy.setBaby(true);
    seed(puppy, DogFurStage.NATURAL, false, ticksPerStage(puppy) - TICKS_BEFORE_DUE);

    tick(puppy, TICKS_RUN);

    context.assertTrue(puppy.getFur().stage() == DogFurStage.NATURAL, "Puppies do not grow fur");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void disabledFurGrowthFreezesTheCoat(final TestContext context) {
    final UnleashedDogEntity dog = wildAdult(context, DogTestData.HUSKY);
    seed(dog, DogFurStage.NATURAL, false, ticksPerStage(dog) - TICKS_BEFORE_DUE);

    withConfig(config -> config.withFurGrowthEnabled(false), () -> tick(dog, TICKS_RUN));

    context.assertTrue(dog.getFur().stage() == DogFurStage.NATURAL, "Frozen coats do not grow");
    context.assertEquals(
        dog.getFur().ticksInStage(), ticksPerStage(dog) - TICKS_BEFORE_DUE, "The clock holds");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBotchedTrimGrowsOutOnTheNextStage(final TestContext context) {
    final UnleashedDogEntity dog = wildAdult(context, DogTestData.HUSKY);
    seed(dog, DogFurStage.TRIMMED, true, ticksPerStage(dog) - TICKS_BEFORE_DUE);

    tick(dog, TICKS_RUN);

    context.assertTrue(dog.getFur().stage() == DogFurStage.NATURAL, "The coat grows back");
    context.assertFalse(dog.getFur().isScuffed(), "The bad haircut grows out with it");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> shearsTrimTheCoatAndDropFur() {
    return perBreed("trims", DogFurGameTest::testTrims);
  }

  private static void testTrims(final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, data, owner);
    final DogFurStage max = dog.getFur().profile().maxStage();
    seed(dog, max, false, 0);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    withConfig(
        config -> config.withBadHairDayChanceMultiplierPercent(NO_BOTCH),
        () -> dog.interactMob(owner, Hand.MAIN_HAND));

    context.assertTrue(dog.getFur().stage() == DogFurStage.TRIMMED, "The coat is trimmed");
    context.assertFalse(dog.getFur().isScuffed(), "A clean trim leaves no bad haircut");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getDamage(), 1, "The shears wear by one");
    context.assertEquals(
        droppedFur(context),
        max.stagesAbove(DogFurStage.TRIMMED) * dog.getFur().profile().shearYieldPerStage(),
        data.breed() + " drops fur for every stage trimmed off");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBotchedTrimHalvesTheFurAndEarnsBadHairDay(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, DogTestData.HUSKY, owner);
    seed(dog, DogFurStage.OVERGROWN, false, 0);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    withConfig(
        config -> config.withBadHairDayChanceMultiplierPercent(CERTAIN_BOTCH),
        () -> dog.interactMob(owner, Hand.MAIN_HAND));

    context.assertTrue(dog.getFur().isScuffed(), "A botched trim leaves a bad haircut");
    context.assertEquals(
        droppedFur(context),
        DogFurRules.shearYield(DogFurStage.OVERGROWN, 2, true),
        "A botch halves the fur");
    final AdvancementEntry badHairDay =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, "bad_hair_day"));
    context.assertTrue(badHairDay != null, "Bad Hair Day exists");
    context.assertTrue(
        owner.getAdvancementTracker().getProgress(badHairDay).isDone(),
        "Botching a trim grants Bad Hair Day");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOvergrownCoatSlowsTheDogUntilItIsTrimmed(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, DogTestData.HUSKY, owner);
    final EntityAttributeInstance speed =
        dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    final double base = speed.getBaseValue();
    seed(dog, DogFurStage.OVERGROWN, false, 0);

    context.assertTrue(
        speed.hasModifier(DogFur.OVERGROWN_SPEED_MODIFIER_ID), "Overgrown dogs are slowed");
    context.assertTrue(
        Math.abs(speed.getValue() - base * 0.9) < 1.0e-9,
        "Ten percent slower, was " + speed.getValue());

    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));
    withConfig(
        config -> config.withBadHairDayChanceMultiplierPercent(NO_BOTCH),
        () -> dog.interactMob(owner, Hand.MAIN_HAND));

    context.assertFalse(
        speed.hasModifier(DogFur.OVERGROWN_SPEED_MODIFIER_ID), "A trim lifts the penalty");
    context.assertEquals(speed.getBaseValue(), base, "The base speed is never rewritten");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void trimmingAnAlreadyTrimmedCoatDoesNothing(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, DogTestData.HUSKY, owner);
    seed(dog, DogFurStage.TRIMMED, false, 0);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    dog.interactMob(owner, Hand.MAIN_HAND);

    assertUntouched(context, dog, owner, DogFurStage.TRIMMED);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPuppyCannotBeTrimmed(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity puppy = ownedDog(context, DogTestData.HUSKY, owner);
    puppy.setBaby(true);
    seed(puppy, DogFurStage.SHAGGY, false, 0);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    puppy.interactMob(owner, Hand.MAIN_HAND);

    assertUntouched(context, puppy, owner, DogFurStage.SHAGGY);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerCannotTrimSomeoneElsesDog(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger =
        PendantTestHelper.survivalOwnerAt(context, new BlockPos(2, 2, 3));
    final UnleashedDogEntity dog = ownedDog(context, DogTestData.HUSKY, owner);
    seed(dog, DogFurStage.SHAGGY, false, 0);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    assertUntouched(context, dog, stranger, DogFurStage.SHAGGY);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shearsTakeArmourOffBeforeTheyTouchTheCoat(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, DogTestData.HUSKY, owner);
    dog.getEquipmentHolder()
        .setStack(DogEquipmentSlot.ARMOUR, new ItemStack(ModArmorItems.of(DogArmorTier.IRON)));
    seed(dog, DogFurStage.SHAGGY, false, 0);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(dog.getArmour().worn().isEmpty(), "The first snip takes the armour off");
    context.assertTrue(dog.getFur().stage() == DogFurStage.SHAGGY, "The coat is left alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCoatSurvivesASaveAndReload(final TestContext context) {
    final UnleashedDogEntity dog = wildAdult(context, DogTestData.HUSKY);
    seed(dog, DogFurStage.OVERGROWN, false, 1234);
    final NbtCompound saved = new NbtCompound();
    dog.writeCustomDataToNbt(saved);

    final UnleashedDogEntity reloaded = wildAdult(context, DogTestData.HUSKY);
    reloaded.readCustomDataFromNbt(saved);

    context.assertTrue(reloaded.getFur().stage() == DogFurStage.OVERGROWN, "Stage reloads");
    context.assertEquals(reloaded.getFur().ticksInStage(), 1234, "Growth progress reloads");
    context.assertTrue(
        reloaded
            .getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            .hasModifier(DogFur.OVERGROWN_SPEED_MODIFIER_ID),
        "The overgrown penalty comes back with the coat");
    context.complete();
  }

  private static void assertUntouched(
      final TestContext context,
      final UnleashedDogEntity dog,
      final ServerPlayerEntity player,
      final DogFurStage stage) {
    context.assertTrue(dog.getFur().stage() == stage, "The coat should be unchanged");
    context.assertEquals(
        player.getStackInHand(Hand.MAIN_HAND).getDamage(), 0, "The shears should not wear");
    context.assertEquals(droppedFur(context), 0, "No fur should drop");
    context.complete();
  }

  private static UnleashedDogEntity wildAdult(final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data, DOG_POS);
    dog.setAiDisabled(true);
    return dog;
  }

  private static UnleashedDogEntity ownedDog(
      final TestContext context, final DogTestData data, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, data, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static void seed(
      final UnleashedDogEntity dog,
      final DogFurStage stage,
      final boolean scuffed,
      final int ticksInStage) {
    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.FUR_STAGE, stage.ordinal());
    nbt.putBoolean(ModNbtKeys.FUR_SCUFFED, scuffed);
    nbt.putInt(ModNbtKeys.FUR_TICKS_IN_STAGE, ticksInStage);
    dog.readCustomDataFromNbt(nbt);
  }

  private static int ticksPerStage(final UnleashedDogEntity dog) {
    return DogFurRules.ticksPerStage(
            dog.getFur().profile(),
            1.0,
            DogsUnleashed.SERVER_CONFIG.furGrowthRateMultiplierPercent())
        .orElseThrow();
  }

  private static void tick(final UnleashedDogEntity dog, final int ticks) {
    for (int i = 0; i < ticks; i++) {
      dog.tick();
    }
  }

  private static void withConfig(
      final UnaryOperator<DogsUnleashedConfig> change, final Runnable action) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = change.apply(original);
      action.run();
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
  }

  private static int droppedFur(final TestContext context) {
    return DogTestHelper.droppedStacksOf(context, ModItems.DOG_FUR).stream()
        .mapToInt(ItemStack::getCount)
        .sum();
  }
}
