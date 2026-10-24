package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.advancement.DogHowledCriterion;
import com.grahambartley.dogsunleashed.advancement.DogMilestone;
import com.grahambartley.dogsunleashed.advancement.DogMilestoneCriterion;
import com.grahambartley.dogsunleashed.advancement.PetCuredCriterion;
import com.grahambartley.dogsunleashed.advancement.PetResurrectedCriterion;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.goal.FetchReturnGoal;
import com.grahambartley.dogsunleashed.gametest.util.AdvancementTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class DogAdvancementGameTest implements FabricGameTest {
  private static final Map<String, String> PARENTS =
      Map.ofEntries(
          Map.entry("best_friend", "root"),
          Map.entry("cherry_companion", "best_friend"),
          Map.entry("the_whole_pack", "best_friend"),
          Map.entry("winter_is_coming", "the_whole_pack"),
          Map.entry("good_dog", "best_friend"),
          Map.entry("show_dog", "good_dog"),
          Map.entry("fetch", "best_friend"),
          Map.entry("sweet_dreams", "best_friend"),
          Map.entry("bad_hair_day", "best_friend"),
          Map.entry("mush", "best_friend"),
          Map.entry("full_team", "mush"),
          Map.entry("off_exploring", "best_friend"),
          Map.entry("rare_find", "off_exploring"),
          Map.entry("good_dog_of_the_village", "best_friend"),
          Map.entry("bark_at_the_moon", "best_friend"),
          Map.entry("forever_in_our_hearts", "best_friend"),
          Map.entry("second_leash_of_life", "forever_in_our_hearts"),
          Map.entry("best_friend_again", "second_leash_of_life"),
          Map.entry("puppy_love", "best_friend"),
          Map.entry("mixed_heritage", "puppy_love"),
          Map.entry("over_there", "best_friend"),
          Map.entry("dressed_to_impress", "best_friend"),
          Map.entry("collar_couture", "best_friend"),
          Map.entry("on_the_scent", "best_friend"));
  private static final Set<String> HIDDEN =
      Set.of("bad_hair_day", "second_leash_of_life", "best_friend_again");
  private static final Map<DogMilestone, String> MILESTONE_ADVANCEMENTS =
      Map.of(
          DogMilestone.WHISTLE_POINT, "over_there",
          DogMilestone.GEAR_EQUIPPED, "dressed_to_impress",
          DogMilestone.COLLAR_DYED, "collar_couture",
          DogMilestone.SCENT_FOUND, "on_the_scent");

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void everyModAdvancementLoadsAndNoOthers(final TestContext context) {
    final Set<String> expected = new HashSet<>(PARENTS.keySet());
    expected.add("root");
    final Set<String> loaded =
        context.getWorld().getServer().getAdvancementLoader().getAdvancements().stream()
            .map(AdvancementEntry::id)
            .filter(id -> id.getNamespace().equals(DogsUnleashed.MOD_ID))
            .filter(id -> !id.getPath().startsWith("recipes/"))
            .map(Identifier::getPath)
            .collect(Collectors.toSet());
    context.assertTrue(
        loaded.equals(expected), "Loaded advancements " + loaded + " should be " + expected);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void advancementTreeBranchesByTheme(final TestContext context) {
    context.assertTrue(
        AdvancementTestHelper.advancement(context, "root").value().parent().isEmpty(),
        "root should have no parent");
    PARENTS.forEach(
        (child, parent) ->
            context.assertTrue(
                AdvancementTestHelper.advancement(context, child)
                    .value()
                    .parent()
                    .equals(Optional.of(Identifier.of(DogsUnleashed.MOD_ID, parent))),
                child + " should sit under " + parent));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void tabUsesThePawPrintBackground(final TestContext context) {
    final Identifier expected =
        Identifier.of(DogsUnleashed.MOD_ID, "textures/gui/advancements/backgrounds/paw_prints.png");
    context.assertTrue(
        display(context, "root").getBackground().equals(Optional.of(expected)),
        "The tab background should be " + expected);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void onlyTheSurprisesAreHidden(final TestContext context) {
    PARENTS
        .keySet()
        .forEach(
            id ->
                context.assertTrue(
                    display(context, id).isHidden() == HIDDEN.contains(id),
                    id + (HIDDEN.contains(id) ? " should be hidden" : " should be visible")));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goalsAndChallengesGrantExperience(final TestContext context) {
    PARENTS
        .keySet()
        .forEach(
            id -> {
              final int expected =
                  switch (display(context, id).getFrame()) {
                    case TASK -> 0;
                    case GOAL -> 50;
                    case CHALLENGE -> 100;
                  };
              context.assertEquals(
                  AdvancementTestHelper.advancement(context, id).value().rewards().experience(),
                  expected,
                  id + " experience reward");
            });
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void eachMilestoneUnlocksOnlyItsAdvancement(final TestContext context) {
    for (final DogMilestone milestone : DogMilestone.values()) {
      final ServerPlayerEntity player = CreativeTestPlayer.join(context);
      DogMilestoneCriterion.INSTANCE.trigger(player, milestone);
      MILESTONE_ADVANCEMENTS.forEach(
          (other, id) ->
              context.assertTrue(
                  AdvancementTestHelper.isDone(context, player, id) == (other == milestone),
                  milestone.asString()
                      + " should "
                      + (other == milestone ? "" : "not ")
                      + "unlock "
                      + id));
    }
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA)
  public void dyeingTheCollarEarnsCollarCouture(final TestContext context) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, null, owner, PendantTestHelper.DOG_POS);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.RED_DYE));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "collar_couture"),
        "Dyeing the collar should earn collar_couture");
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA)
  public void equippingArmourByHandEarnsDressedToImpress(final TestContext context) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, null, owner, PendantTestHelper.DOG_POS);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOLF_ARMOR));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(!dog.getArmour().worn().isEmpty(), "The dog should be wearing the armour");
    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "dressed_to_impress"),
        "Equipping gear should earn dressed_to_impress");
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA)
  public void placingArmourInTheGearScreenEarnsDressedToImpress(final TestContext context) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, null, owner, PendantTestHelper.DOG_POS);
    final DogEquipmentScreenHandler handler =
        new DogEquipmentScreenHandler(1, owner.getInventory(), dog);
    handler.setCursorStack(new ItemStack(Items.WOLF_ARMOR));

    handler.onSlotClick(DogEquipmentSlot.ARMOUR.ordinal(), 0, SlotActionType.PICKUP, owner);

    context.assertTrue(
        !dog.getArmour().worn().isEmpty(), "The gear screen should put the armour on the dog");
    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "dressed_to_impress"),
        "Placing gear in the gear screen should earn dressed_to_impress");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void everyLivingDogTypeIsTaggedAsADog(final TestContext context) {
    final TagKey<EntityType<?>> dogs =
        TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(DogsUnleashed.MOD_ID, "dogs"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final EntityType<?> living = ModEntities.getDogEntityType(breed);
      context.assertTrue(living.isIn(dogs), breed.serializedId() + " should be in #dogs");
      context.assertFalse(
          ModEntities.getZombieDogEntityType(breed).isIn(dogs),
          "zombie " + breed.serializedId() + " should not be in #dogs");
    }
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA)
  public void breedingTwoBreedsEarnsPuppyLoveAndMixedHeritage(final TestContext context) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    breed(context, owner, DogTestData.HUSKY, DogTestData.BEAGLE);

    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "puppy_love"),
        "A puppy should earn puppy_love");
    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "mixed_heritage"),
        "A cross should earn mixed_heritage");
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA)
  public void breedingOneBreedEarnsOnlyPuppyLove(final TestContext context) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    breed(context, owner, DogTestData.HUSKY, DogTestData.HUSKY);

    context.assertTrue(
        AdvancementTestHelper.isDone(context, owner, "puppy_love"),
        "A puppy should earn puppy_love");
    context.assertFalse(
        AdvancementTestHelper.isDone(context, owner, "mixed_heritage"),
        "A pure puppy should not earn mixed_heritage");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamingUnlocksBreedAdvancements(final TestContext context) {
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 1, 0));
    final UnleashedDogEntity shiba =
        context.spawnEntity(ModEntities.SHIBA_INU, new BlockPos(2, 1, 0));

    final AdvancementEntry bestFriend = AdvancementTestHelper.advancement(context, "best_friend");
    final AdvancementEntry cherryCompanion =
        AdvancementTestHelper.advancement(context, "cherry_companion");

    Criteria.TAME_ANIMAL.trigger(player, husky);
    Criteria.TAME_ANIMAL.trigger(player, shiba);

    context.assertTrue(
        player.getAdvancementTracker().getProgress(bestFriend).isDone(),
        "Taming a new breed should unlock best_friend");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(cherryCompanion).isDone(),
        "Taming a shiba inu should unlock cherry_companion");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void wholePackRequiresEveryFoundingBreed(final TestContext context) {
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);
    final AdvancementEntry wholePack = AdvancementTestHelper.advancement(context, "the_whole_pack");

    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_husky");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_dachshund");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_beagle");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_goldenretriever");

    context.assertTrue(
        !player.getAdvancementTracker().getProgress(wholePack).isDone(),
        "the_whole_pack should stay locked until every breed is tamed");

    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_shibainu");

    context.assertTrue(
        player.getAdvancementTracker().getProgress(wholePack).isDone(),
        "the_whole_pack should unlock after every breed criterion is complete");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void customTriggersUnlockTheirAdvancements(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final AdvancementEntry barkAtTheMoon =
        AdvancementTestHelper.advancement(context, "bark_at_the_moon");
    final AdvancementEntry fetch = AdvancementTestHelper.advancement(context, "fetch");
    final AdvancementEntry sweetDreams = AdvancementTestHelper.advancement(context, "sweet_dreams");
    final AdvancementEntry foreverInOurHearts =
        AdvancementTestHelper.advancement(context, "forever_in_our_hearts");

    final BlockPos absDogPos = context.getAbsolutePos(new BlockPos(1, 1, 0));
    player.refreshPositionAndAngles(absDogPos, 0.0f, 0.0f);

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 1, 0));
    husky.setTamed(true, true);
    husky.setOwnerUuid(player.getUuid());
    husky.getFetchCarry().setCarrying(true);
    husky.getFetchCarry().setActiveType(FetchTypes.TENNIS_BALL);
    husky.getFetchCarry().setCarriedStack(new ItemStack(ModItems.TENNIS_BALL));

    DogHowledCriterion.INSTANCE.trigger(player);
    new FetchReturnGoal(husky).tick();
    husky.getSleepController().startSleepingInBed(absBedPos);

    final ItemStack graveStack = new ItemStack(ModItems.DOG_GRAVE);
    player.getInventory().setStack(0, graveStack);
    Criteria.INVENTORY_CHANGED.trigger(player, player.getInventory(), graveStack);

    context.assertTrue(
        player.getAdvancementTracker().getProgress(barkAtTheMoon).isDone(),
        "dog_howled should unlock bark_at_the_moon");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(fetch).isDone(),
        "fetch_returned should unlock fetch");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(sweetDreams).isDone(),
        "dog_slept_in_bed should unlock sweet_dreams");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(foreverInOurHearts).isDone(),
        "inventory_changed with a dog grave should unlock forever_in_our_hearts");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void resurrectionAndCureTriggersUnlockTheirAdvancements(final TestContext context) {
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);
    final AdvancementEntry secondLeashOfLife =
        AdvancementTestHelper.advancement(context, "second_leash_of_life");
    final AdvancementEntry bestFriendAgain =
        AdvancementTestHelper.advancement(context, "best_friend_again");

    PetResurrectedCriterion.INSTANCE.trigger(player);
    PetCuredCriterion.INSTANCE.trigger(player);

    context.assertTrue(
        player.getAdvancementTracker().getProgress(secondLeashOfLife).isDone(),
        "pet_resurrected should unlock second_leash_of_life");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(bestFriendAgain).isDone(),
        "pet_cured should unlock best_friend_again");
    context.complete();
  }

  private static void breed(
      final TestContext context,
      final ServerPlayerEntity owner,
      final DogTestData first,
      final DogTestData second) {
    final UnleashedDogEntity mother =
        DogTestHelper.spawnTamedDog(context, first, PendantTestHelper.DOG_POS, owner.getUuid());
    final UnleashedDogEntity father =
        DogTestHelper.spawnTamedDog(
            context, second, PendantTestHelper.DOG_POS.east(), owner.getUuid());
    mother.setAiDisabled(true);
    father.setAiDisabled(true);
    mother.lovePlayer(owner);
    father.lovePlayer(owner);
    mother.breed(context.getWorld(), father);
  }

  private static AdvancementDisplay display(final TestContext context, final String path) {
    return AdvancementTestHelper.advancement(context, path)
        .value()
        .display()
        .orElseThrow(() -> new IllegalStateException(path + " has no display"));
  }
}
