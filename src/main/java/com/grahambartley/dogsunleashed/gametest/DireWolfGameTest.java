package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModArmorItems;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.ModPendantItems;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogGrowthRules;
import com.grahambartley.dogsunleashed.entity.DogResurrection;
import com.grahambartley.dogsunleashed.entity.DogTraits;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.DireWolfCoat;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DireWolfGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_WOLF = new BlockPos(3, 2, 3);
  private static final int PACK_SIZE = 10;
  private static final int FIRST_HOTBAR_SLOT_INDEX = DogEquipmentScreenHandler.DOG_SLOT_COUNT + 27;

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aYearlingGrowsIntoAnAdultWhenItsTwentyMinutesAreUp(final TestContext context) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    wolf.getGrowth().setStage(DireWolfGrowthStage.YEARLING);
    wolf.setHealth(10.0f);
    final NbtCompound nbt = new NbtCompound();
    wolf.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, DireWolfGrowthStage.YEARLING.ticksToGrow() - 1);
    wolf.readCustomDataFromNbt(nbt);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              wolf.getGrowth().stage() == DireWolfGrowthStage.ADULT,
              "The yearling should have grown, but it is " + wolf.getGrowth().stage());
          context.assertTrue(
              wolf.getMaxHealth() == 40.0f, "An adult has 40 health, not " + wolf.getMaxHealth());
          context.assertTrue(
              wolf.getHealth() == 20.0f,
              "Growing adds the 10 health gained rather than healing, but health is "
                  + wolf.getHealth());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void eachStageHasTheHitboxAndStatsOfTheGrowthTable(final TestContext context) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    for (final DireWolfGrowthStage stage : DireWolfGrowthStage.values()) {
      wolf.getGrowth().setStage(stage);
      context.assertTrue(wolf.getGrowth().stage() == stage, "stage " + stage);
      context.assertTrue(
          Math.abs(wolf.getWidth() - stage.width()) < 0.001f
              && Math.abs(wolf.getHeight() - stage.height()) < 0.001f,
          stage
              + " hitbox should be "
              + stage.width()
              + "x"
              + stage.height()
              + " but was "
              + wolf.getWidth()
              + "x"
              + wolf.getHeight());
      context.assertTrue(
          Math.abs(wolf.getMaxHealth() - stage.maxHealth()) < 0.001,
          stage + " max health " + wolf.getMaxHealth());
      context.assertTrue(
          Math.abs(
                  wolf.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                      - stage.movementSpeed())
              < 0.0001,
          stage + " speed");
      context.assertTrue(
          Math.abs(
                  wolf.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                      - stage.attackDamage())
              < 0.001,
          stage + " attack damage");
      context.assertTrue(
          Math.abs(wolf.getArmor() - stage.armor()) < 0.001, stage + " armor " + wolf.getArmor());
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theStageAndTimerSurviveASaveAndReload(final TestContext context) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PRIME);
    final NbtCompound nbt = new NbtCompound();
    wolf.writeNbt(nbt);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, 1234);

    final UnleashedDogEntity reloaded = ModEntities.DIRE_WOLF.create(context.getWorld());
    reloaded.readNbt(nbt);

    context.assertTrue(
        reloaded.getGrowth().stage() == DireWolfGrowthStage.PRIME,
        "Reloaded stage should be PRIME but was " + reloaded.getGrowth().stage());
    context.assertTrue(
        reloaded.getGrowth().ticksInStage() == 1234,
        "Reloaded timer should be 1234 but was " + reloaded.getGrowth().ticksInStage());
    context.assertTrue(
        reloaded.getMaxHealth() == 50.0f && reloaded.getHealth() == 50.0f,
        "A reloaded prime keeps its 50 health, not "
            + reloaded.getHealth()
            + "/"
            + reloaded.getMaxHealth());
    context.assertTrue(
        Math.abs(reloaded.getHeight() - DireWolfGrowthStage.PRIME.height()) < 0.001f,
        "A reloaded prime keeps its hitbox height, not " + reloaded.getHeight());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aPupGrowsUpIntoAYearlingWithoutAFreeHeal(final TestContext context) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PUP);
    context.assertTrue(wolf.isBaby(), "A stage 1 dire wolf is a puppy");
    context.assertTrue(wolf.getMaxHealth() == 20.0f, "A pup has 20 health");
    wolf.setHealth(5.0f);

    wolf.setBaby(false);

    context.assertTrue(
        wolf.getGrowth().stage() == DireWolfGrowthStage.YEARLING,
        "A grown pup becomes a yearling, not " + wolf.getGrowth().stage());
    context.assertTrue(
        wolf.getHealth() == 15.0f, "Growing up adds 10 health, but health is " + wolf.getHealth());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWildPackHasAPupThreeAdultsAndNothingAboveAnAdult(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    EntityData packData = null;
    for (int member = 0; member < PACK_SIZE; member++) {
      final UnleashedDogEntity wolf =
          context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF.east(member % 3));
      wolf.setAiDisabled(true);
      packData =
          wolf.initialize(
              world, world.getLocalDifficulty(wolf.getBlockPos()), SpawnReason.NATURAL, packData);
      pack.add(wolf);
    }

    context.assertTrue(
        pack.stream().anyMatch(wolf -> wolf.getGrowth().stage() == DireWolfGrowthStage.PUP),
        "Every pack has a pup");
    context.assertTrue(
        pack.stream()
                .filter(
                    wolf ->
                        wolf.getGrowth().stage() == DireWolfGrowthStage.YEARLING
                            || wolf.getGrowth().stage() == DireWolfGrowthStage.ADULT)
                .count()
            >= 3,
        "Every pack has at least three stage 2 or 3 adults");
    context.assertTrue(
        pack.stream()
            .allMatch(
                wolf -> wolf.getGrowth().stage().number() <= DireWolfGrowthStage.ADULT.number()),
        "No wild pack member is above stage 3");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aPrimeNeverGrowsIntoAnAlphaWithTimeAlone(final TestContext context) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PRIME);
    final NbtCompound nbt = new NbtCompound();
    wolf.writeCustomDataToNbt(nbt);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, 10_000_000);
    wolf.readCustomDataFromNbt(nbt);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              wolf.getGrowth().stage() == DireWolfGrowthStage.PRIME,
              "A prime only becomes an alpha through its bond, but it is "
                  + wolf.getGrowth().stage());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aPrimeEvolvesAfterTwentyBattlesAndAnEnchantedGoldenApple(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.changeGameMode(GameMode.SURVIVAL);
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);
    final ItemStack apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2);

    for (int battle = 0; battle < DogGrowthRules.BATTLES_TO_EVOLVE - 1; battle++) {
      wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2)));
    }
    wolf.getGrowth().tryEvolve(owner, apple);
    context.assertTrue(
        wolf.getGrowth().stage() == DireWolfGrowthStage.PRIME && apple.getCount() == 2,
        "One battle short, the prime stays a prime and keeps the apple");

    wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.SKELETON, REL_WOLF.east(2)));
    wolf.getGrowth().tryEvolve(owner, apple);
    context.assertTrue(
        wolf.getGrowth().stage() == DireWolfGrowthStage.ALPHA,
        "After twenty battles the apple should make it an alpha, but it is "
            + wolf.getGrowth().stage());
    context.assertTrue(apple.getCount() == 1, "Evolving eats one apple");
    context.assertTrue(
        Math.abs(wolf.getHeight() - DireWolfGrowthStage.ALPHA.height()) < 0.001f,
        "An alpha stands at its full height");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theBattlesCommandChangesHowManyBattlesAnAlphaNeeds(final TestContext context) {
    final MinecraftServer server = context.getWorld().getServer();
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.changeGameMode(GameMode.SURVIVAL);
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);
    final ItemStack apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
    try {
      server
          .getCommandManager()
          .executeWithPrefix(server.getCommandSource(), "dogsunleashed config direwolf battles 3");
      context.assertTrue(
          DogsUnleashed.SERVER_CONFIG.direWolf().battlesToEvolve() == 3,
          "The command should set the battles needed to 3");
      for (int battle = 0; battle < 3; battle++) {
        wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2)));
      }
      wolf.getGrowth().tryEvolve(owner, apple);
      context.assertTrue(
          wolf.getGrowth().stage() == DireWolfGrowthStage.ALPHA,
          "Three battles should be enough after the change, but it is " + wolf.getGrowth().stage());
    } finally {
      ServerConfigService.update(server, before);
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void onlyHostileKillsNearTheOwnerCountAsBattles(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);

    wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.SHEEP, REL_WOLF.east(2)));
    context.assertTrue(wolf.getGrowth().battlesWon() == 0, "A sheep is not a battle");

    owner.refreshPositionAndAngles(wolf.getX() + 40, wolf.getY(), wolf.getZ(), 0, 0);
    wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2)));
    context.assertTrue(
        wolf.getGrowth().battlesWon() == 0, "A fight with the owner far away does not count");

    owner.refreshPositionAndAngles(wolf.getX() + 2, wolf.getY(), wolf.getZ(), 0, 0);
    wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2)));
    context.assertTrue(
        wolf.getGrowth().battlesWon() == 1, "A hostile kill beside the owner counts");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aPrimeThatKillsAZombieBesideItsOwnerWinsABattle(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2));

    zombie.damage(context.getWorld().getDamageSources().mobAttack(wolf), 1000.0f);

    context.assertTrue(zombie.isDead(), "The zombie should die");
    context.assertTrue(
        wolf.getGrowth().battlesWon() == 1,
        "Killing it should count as a battle, but battles won is " + wolf.getGrowth().battlesWon());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void battlesWonSurviveASaveAndReload(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);
    for (int battle = 0; battle < 7; battle++) {
      wolf.getGrowth().recordBattle(context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(2)));
    }
    final NbtCompound nbt = new NbtCompound();
    wolf.writeNbt(nbt);
    final UnleashedDogEntity reloaded = ModEntities.DIRE_WOLF.create(context.getWorld());
    reloaded.readNbt(nbt);

    context.assertTrue(
        reloaded.getGrowth().battlesWon() == 7,
        "Reloaded battles should be 7 but were " + reloaded.getGrowth().battlesWon());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDireWolfCannotBeGivenABedOrADogHouse(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity wolf = spawnWolf(context);
    wolf.setOwnerUuid(owner.getUuid());
    wolf.setTamed(true, true);
    final BlockPos bed = REL_WOLF.west(2);
    final BlockPos house = REL_WOLF.north(2);
    context.setBlockState(bed, ModBlocks.DOG_BED.getDefaultState());
    context.setBlockState(house, ModBlocks.DOG_HOUSE.getDefaultState());

    for (final BlockPos spot : List.of(bed, house)) {
      DogSleepSpotAssignment.setPendingAssignment(owner.getUuid(), wolf.getUuid());
      context.useBlock(spot, owner);
      context.assertTrue(
          wolf.getAssignedBedPos().isEmpty(),
          "A dire wolf must refuse " + context.getBlockState(spot).getBlock());
    }
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> aDireWolfRefusesToBreedWithEveryOtherBreed() {
    final List<TestFunction> tests = new ArrayList<>();
    for (final DogTestData other : DogTestData.getAllBreeds()) {
      if (other.breed() == UnleashedDogBreed.DIRE_WOLF) {
        continue;
      }
      tests.add(
          new TestFunction(
              "defaultBatch",
              "direwolfgametest.refusestobreedwith." + other.breed().serializedId(),
              ARENA,
              20,
              0L,
              true,
              context -> assertBreeding(context, other, false)));
    }
    tests.add(
        new TestFunction(
            "defaultBatch",
            "direwolfgametest.breedswithanotherdirewolf",
            ARENA,
            20,
            0L,
            true,
            context -> assertBreeding(context, DogTestData.DIRE_WOLF, true)));
    return tests;
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anAlphaWearsArmourAndAPendantFromTheEquipmentScreen(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.changeGameMode(GameMode.SURVIVAL);
    final UnleashedDogEntity wolf = spawnOwnedPrime(context, owner);
    wolf.getGrowth().setStage(DireWolfGrowthStage.ALPHA);
    final ItemStack armour = new ItemStack(ModArmorItems.of(DogArmorTier.IRON));
    final ItemStack pendant = new ItemStack(ModPendantItems.of(PendantType.WARDING_UNDEAD));
    owner.getInventory().setStack(0, armour.copy());
    owner.getInventory().setStack(1, pendant.copy());
    final DogEquipmentScreenHandler handler =
        new DogEquipmentScreenHandler(1, owner.getInventory(), wolf);

    handler.quickMove(owner, FIRST_HOTBAR_SLOT_INDEX);
    handler.quickMove(owner, FIRST_HOTBAR_SLOT_INDEX + 1);

    context.assertTrue(
        wolf.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isOf(armour.getItem()),
        "An alpha should wear dog armour");
    context.assertTrue(
        wolf.getEquipmentHolder().getStack(DogEquipmentSlot.PENDANT).isOf(pendant.getItem()),
        "An alpha should wear a pendant");
    context.runAtTick(
        2,
        () -> {
          wolf.damage(context.getWorld().getDamageSources().generic(), 4.0f);
          context.assertTrue(
              wolf.getHealth() == wolf.getMaxHealth(), "The armour should take the hit");
          context.assertTrue(
              wolf.getArmour().worn().getDamage() > 0, "The armour should wear from the hit");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void twoDireWolvesHaveAStageOnePup(final TestContext context) {
    final UUID owner = UUID.randomUUID();
    final UnleashedDogEntity mother = spawnTamed(context, ModEntities.DIRE_WOLF, owner, 0);
    final UnleashedDogEntity father = spawnTamed(context, ModEntities.DIRE_WOLF, owner, 2);

    final UnleashedDogEntity pup =
        (UnleashedDogEntity) mother.createChild(context.getWorld(), father);

    context.assertTrue(pup != null, "Two dire wolves should have a pup");
    context.assertTrue(
        pup.getBreed() == UnleashedDogBreed.DIRE_WOLF, "The pup is a pure dire wolf");
    context.assertTrue(
        pup.getGrowth().stage() == DireWolfGrowthStage.PUP,
        "The pup starts at stage 1, not " + pup.getGrowth().stage());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void anAlphaComesBackFromItsGraveStillAnAlpha(final TestContext context) {
    final UnleashedDogEntity wolf =
        spawnTamed(context, ModEntities.DIRE_WOLF, UUID.randomUUID(), 0);
    wolf.getGrowth().setStage(DireWolfGrowthStage.ALPHA);
    registerPet(context, wolf);
    final UUID wolfId = wolf.getUuid();

    wolf.kill();

    context.runAtTick(
        5,
        () -> {
          final List<BlockPos> graves = new ArrayList<>();
          context.forEachRelativePos(
              pos -> {
                if (context.getBlockState(pos).isOf(ModBlocks.DOG_GRAVE)) {
                  graves.add(context.getAbsolutePos(pos));
                }
              });
          context.assertTrue(graves.size() == 1, "A dead alpha dire wolf should leave a grave");
          final BlockPos gravePos = graves.get(0);
          final DogGraveBlockEntity grave =
              (DogGraveBlockEntity) context.getWorld().getBlockEntity(gravePos);
          grave.installTotem(UUID.randomUUID());

          context.assertTrue(
              DogResurrection.resurrect(context.getWorld(), gravePos, grave),
              "The alpha should rise from its grave");
          final UnleashedDogEntity risen =
              (UnleashedDogEntity) context.getWorld().getEntity(wolfId);
          context.assertTrue(
              risen.getGrowth().stage() == DireWolfGrowthStage.ALPHA,
              "It should still be an alpha, not " + risen.getGrowth().stage());
          context.assertTrue(
              Math.abs(risen.getHeight() - DireWolfGrowthStage.ALPHA.height()) < 0.001f,
              "It should keep the alpha's size, not " + risen.getHeight());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void winterIsComingNeedsADireWolfOfEveryCoat(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final AdvancementEntry winter =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, "winter_is_coming"));
    context.assertTrue(winter != null, "winter_is_coming should load");

    final DireWolfCoat[] coats = DireWolfCoat.values();
    for (int index = 0; index < coats.length; index++) {
      context.assertTrue(
          !player.getAdvancementTracker().getProgress(winter).isDone(),
          "Winter is Coming must wait for every coat, but was granted after " + index);
      final UnleashedDogEntity wolf =
          context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF.east(index % 3));
      wolf.setAiDisabled(true);
      wolf.applyTraits(new DogTraits(UnleashedDogBreed.DIRE_WOLF, coats[index].getOrdinal(), 0));
      wolf.getInteractions().tame(player);
    }

    context.assertTrue(
        player.getAdvancementTracker().getProgress(winter).isDone(),
        "Taming all six coats should grant Winter is Coming");
    context.complete();
  }

  private static void assertBreeding(
      final TestContext context, final DogTestData other, final boolean expected) {
    final UUID owner = UUID.randomUUID();
    final UnleashedDogEntity wolf = spawnTamed(context, ModEntities.DIRE_WOLF, owner, 0);
    final UnleashedDogEntity partner = spawnTamed(context, other.entityType(), owner, 2);
    wolf.setLoveTicks(600);
    partner.setLoveTicks(600);

    context.assertTrue(
        wolf.canBreedWith(partner) == expected && partner.canBreedWith(wolf) == expected,
        "A dire wolf breeding with " + other.breed() + " should be " + expected);
    context.complete();
  }

  private static UnleashedDogEntity spawnOwnedPrime(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity wolf = spawnWolf(context);
    owner.refreshPositionAndAngles(wolf.getX() + 2, wolf.getY(), wolf.getZ(), 0, 0);
    wolf.setOwnerUuid(owner.getUuid());
    wolf.setTamed(true, true);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PRIME);
    return wolf;
  }

  private static UnleashedDogEntity spawnWolf(final TestContext context) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF);
    wolf.setAiDisabled(true);
    return wolf;
  }

  private static UnleashedDogEntity spawnTamed(
      final TestContext context,
      final EntityType<UnleashedDogEntity> type,
      final UUID owner,
      final int offset) {
    final UnleashedDogEntity dog = context.spawnEntity(type, REL_WOLF.east(offset));
    dog.setAiDisabled(true);
    dog.setOwnerUuid(owner);
    dog.setTamed(true, true);
    return dog;
  }

  private static void registerPet(final TestContext context, final UnleashedDogEntity dog) {
    final ServerWorld world = context.getWorld();
    final PetData pet =
        new PetData(
            dog.getUuid(),
            dog.getOwnerUuid(),
            dog.getBreed(),
            "Frost",
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            world.getRegistryKey().getValue().toString(),
            PetLifeState.LIVING);
    pet.syncFrom(dog);
    PetManager.get(world.getServer()).registerPet(pet);
  }
}
