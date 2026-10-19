package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogGrowth;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public final class UndeadDogGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String DAYLIGHT_BATCH = "undead-daylight";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final BlockPos REL_ROOF = new BlockPos(3, 4, 3);
  private static final int MIDDAY = 6000;
  private static final int SKY_LIGHT_SETTLE_DEADLINE = 40;
  private static final int DAYLIGHT_WATCH_TICKS = 200;
  private static final int DAYLIGHT_TICK_LIMIT =
      SKY_LIGHT_SETTLE_DEADLINE + DAYLIGHT_WATCH_TICKS + 20;
  private static final long DAYLIGHT_ROLL_SEED = 0L;
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
      skyAccess = true)
  public void anUndeadDogCaughtUnderTheOpenSkyBurns(final TestContext context) {
    assertCatchesFire(
        context,
        UndeadDogGameTest::spawnDaylightUndeadDog,
        true,
        true,
        "An undead dog under the open midday sky should catch fire");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void anUndeadDogUnderShelterIsSafeFromDaylight(final TestContext context) {
    context.setBlockState(REL_ROOF, Blocks.STONE);

    assertCatchesFire(
        context,
        UndeadDogGameTest::spawnDaylightUndeadDog,
        false,
        false,
        "An undead dog with a roof over it must never catch fire");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void anUndeadDogWearingArmourIsSafeFromDaylight(final TestContext context) {
    assertCatchesFire(
        context,
        ctx -> {
          final UnleashedDogEntity dog = spawnDaylightUndeadDog(ctx);
          dog.getEquipmentHolder()
              .setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
          return dog;
        },
        true,
        false,
        "Dog armour should spare an undead dog from daylight, as a helmet does");
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void aLivingDogIsUntroubledByDaylight(final TestContext context) {
    assertCatchesFire(
        context,
        ctx -> seeded(ctx.spawnEntity(ModEntities.HUSKY, REL_DOG)),
        true,
        false,
        "A living dog must never burn in daylight");
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDogThatDiesIsLostForever(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedUndeadDog(context);
    final PetData pet = registerPet(context, dog, PetLifeState.UNDEAD);

    dog.kill();

    context.assertTrue(
        pet.getLifeState() == PetLifeState.LOST,
        "A pet that dies while undead must be lost for good, but it was " + pet.getLifeState());
    final DogGraveBlockEntity grave = onlyGraveFor(context, dog.getUuid());
    context.assertTrue(grave.isLost(), "A grave left by an undead death should be lost");
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
    final DogGraveBlockEntity grave = onlyGraveFor(context, dog.getUuid());
    context.assertTrue(!grave.isLost(), "A living dog's grave should still be raisable");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDogSpeaksWithAZombiesVoice(final TestContext context) {
    final UnleashedDogEntity undeadHusky = spawnUndeadDog(context);
    final UnleashedDogEntity undeadBeagle =
        context.spawnEntity(ModEntities.ZOMBIE_BEAGLE, REL_DOG.east(2));
    undeadBeagle.setAiDisabled(true);
    final UnleashedDogEntity livingBeagle =
        context.spawnEntity(ModEntities.BEAGLE, REL_DOG.east(4));
    livingBeagle.setAiDisabled(true);

    context.assertTrue(
        undeadHusky.getVocalization().barkSound() == SoundEvents.ENTITY_ZOMBIE_AMBIENT,
        "An undead husky should groan like a zombie");
    context.assertTrue(
        undeadHusky.getVocalization().howlSound() == SoundEvents.ENTITY_ZOMBIE_AMBIENT,
        "An undead husky's moon howl should come out as a zombie groan");
    context.assertTrue(
        undeadBeagle.getVocalization().barkSound() == SoundEvents.ENTITY_ZOMBIE_AMBIENT,
        "An undead beagle should groan rather than bark");
    context.assertTrue(
        livingBeagle.getVocalization().barkSound() == ModSounds.BEAGLE_BARK,
        "A living beagle must keep its own bark");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadHuskyGainsAVoiceItNeverHadInLife(final TestContext context) {
    final UnleashedDogEntity undeadHusky = spawnUndeadDog(context);

    context.runAtTick(5, () -> undeadHusky.setHealth(1.0f));

    context.runAtTick(
        15,
        () -> {
          context.assertTrue(
              undeadHusky.getVocalization().getBarkCooldownTicks() > 0,
              "A hurt undead husky should have groaned; in life the breed has no bark at all");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void theDeadDoNotSireTheLiving(final TestContext context) {
    final UnleashedDogEntity undead = spawnOwnedUndeadDog(context);
    final UnleashedDogEntity living = context.spawnEntity(ModEntities.HUSKY, REL_DOG.east(2));
    living.setAiDisabled(true);
    living.setOwnerUuid(undead.getOwnerUuid());
    living.setTamed(true, true);
    undead.setLoveTicks(600);
    living.setLoveTicks(600);

    context.assertTrue(
        !undead.canBreedWith(living), "An undead dog must not breed with a living one");
    context.assertTrue(
        !living.canBreedWith(undead), "A living dog must not breed with an undead one");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDogRefusesBreedingFood(final TestContext context) {
    final UnleashedDogEntity undead = spawnOwnedUndeadDog(context);
    final UnleashedDogEntity living = context.spawnEntity(ModEntities.HUSKY, REL_DOG.east(2));
    living.setAiDisabled(true);
    final ItemStack breedingFood = new ItemStack(Items.COOKED_CHICKEN);

    context.assertTrue(
        living.isBreedingItem(breedingFood),
        "A living dog should accept the breeding food this test relies on");
    context.assertTrue(
        !undead.isBreedingItem(breedingFood),
        "An undead dog must refuse breeding food, so no love mode and no food healing");
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
  public void anUndeadDireWolfHasHalfItsStageStatsAtEveryStage(final TestContext context) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.ZOMBIE_DIRE_WOLF, REL_DOG);
    wolf.setAiDisabled(true);
    wolf.getUndeadState().applyAttributeScaling();
    for (final DireWolfGrowthStage stage : DireWolfGrowthStage.values()) {
      wolf.getGrowth().setStage(stage);
      assertStats(context, wolf, stage, 0.5, "An undead " + stage);
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void anUndeadDireWolfSavedWithTheFullStageBonusLoadsAtHalfStats(
      final TestContext context) {
    final DireWolfGrowthStage stage = DireWolfGrowthStage.ADULT;
    final UnleashedDogBreed.Attributes base = UnleashedDogBreed.DIRE_WOLF.attributes();
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.ZOMBIE_DIRE_WOLF, REL_DOG);
    wolf.setAiDisabled(true);
    wolf.getGrowth().setStage(stage);
    wolf.getUndeadState().applyAttributeScaling();
    overwriteBonus(
        wolf,
        EntityAttributes.GENERIC_MAX_HEALTH,
        DogGrowth.MAX_HEALTH_MODIFIER_ID,
        stage.maxHealth() - base.maxHealth());
    overwriteBonus(
        wolf,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        DogGrowth.ATTACK_DAMAGE_MODIFIER_ID,
        stage.attackDamage() - base.attackDamage());
    wolf.setHealth(wolf.getMaxHealth());
    final UnleashedDogEntity reloaded =
        context.spawnEntity(ModEntities.ZOMBIE_DIRE_WOLF, REL_DOG.east(2));
    reloaded.setAiDisabled(true);

    reloaded.readNbt(wolf.writeNbt(new NbtCompound()));

    assertStats(context, reloaded, stage, 0.5, "An undead adult loaded from an old save");
    context.assertTrue(
        reloaded.getHealth() <= reloaded.getMaxHealth(),
        "Health " + reloaded.getHealth() + " must be capped at " + reloaded.getMaxHealth());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aCuredDireWolfGetsItsFullStageStatsBack(final TestContext context) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.ZOMBIE_DIRE_WOLF, REL_DOG);
    wolf.setAiDisabled(true);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PRIME);
    wolf.getUndeadState().applyAttributeScaling();
    final UUID uuid = wolf.getUuid();

    wolf.getCuring().restoreFromSave(1, null);

    context.waitAndRun(
        5,
        () -> {
          final UnleashedDogEntity cured = dogByUuid(context, uuid);
          context.assertTrue(
              cured != null && !cured.isUndead(), "The dire wolf should have been cured");
          assertStats(context, cured, DireWolfGrowthStage.PRIME, 1.0, "A cured prime");
          context.complete();
        });
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
    dog.setCustomName(net.minecraft.text.Text.literal("Wraith"));
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
          context.assertTrue(
              cured.hasCustomName() && "Wraith".equals(cured.getCustomName().getString()),
              "The cure must carry the dog's name across the entity swap");
          context.assertTrue(
              pet.getMaxHealth() == UnleashedDogBreed.HUSKY.attributes().maxHealth(),
              "The pet record should carry the living max health again, but had "
                  + pet.getMaxHealth());
          context.complete();
        });
  }

  private static void assertCatchesFire(
      final TestContext context,
      final Function<TestContext, UnleashedDogEntity> spawn,
      final boolean skyVisible,
      final boolean expected,
      final String message) {
    final AtomicReference<UnleashedDogEntity> dog = new AtomicReference<>();
    final AtomicLong watchEndTick = new AtomicLong();
    final AtomicBoolean caughtFire = new AtomicBoolean(false);
    context.runAtEveryTick(
        () -> {
          final ServerWorld world = context.getWorld();
          world.setTimeOfDay(MIDDAY);
          if (dog.get() == null) {
            if (skyVisibleAt(context, REL_DOG) != skyVisible
                || skyVisibleAt(context, REL_DOG.up()) != skyVisible) {
              context.assertTrue(
                  context.getTick() < SKY_LIGHT_SETTLE_DEADLINE,
                  "Sky light at the dog never settled to match the scene");
              return;
            }
            final UnleashedDogEntity spawned = spawn.apply(context);
            context.assertTrue(
                world.isSkyVisible(BlockPos.ofFloored(spawned.getEyePos())) == skyVisible,
                "The sky at the dog's eyes does not match the scene");
            context.assertTrue(world.isDay(), "The daylight window never reached day");
            dog.set(spawned);
            watchEndTick.set(context.getTick() + DAYLIGHT_WATCH_TICKS);
            return;
          }
          if (dog.get().isOnFire()) {
            caughtFire.set(true);
          }
          if (context.getTick() >= watchEndTick.get()) {
            context.assertTrue(caughtFire.get() == expected, message);
            context.complete();
          }
        });
  }

  private static boolean skyVisibleAt(final TestContext context, final BlockPos relative) {
    return context.getWorld().isSkyVisible(context.getAbsolutePos(relative));
  }

  private static UnleashedDogEntity spawnDaylightUndeadDog(final TestContext context) {
    return seeded(spawnUndeadDog(context));
  }

  private static UnleashedDogEntity seeded(final UnleashedDogEntity dog) {
    dog.setAiDisabled(true);
    dog.getRandom().setSeed(DAYLIGHT_ROLL_SEED);
    return dog;
  }

  private static UnleashedDogEntity spawnUndeadDog(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.ZOMBIE_HUSKY, REL_DOG);
    dog.setAiDisabled(true);
    return dog;
  }

  private static DogGraveBlockEntity onlyGraveFor(final TestContext context, final UUID dogId) {
    final List<DogGraveBlockEntity> graves = new ArrayList<>();
    context.forEachRelativePos(
        pos -> {
          if (context.getWorld().getBlockEntity(context.getAbsolutePos(pos))
                  instanceof DogGraveBlockEntity grave
              && dogId.equals(grave.getDogUuid())) {
            graves.add(grave);
          }
        });
    context.assertTrue(
        graves.size() == 1, "The dead dog should leave one grave, not " + graves.size());
    return graves.get(0);
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
    pet.syncFrom(dog);
    PetManager.get(world.getServer()).registerPet(pet);
    return pet;
  }

  private static void assertStats(
      final TestContext context,
      final UnleashedDogEntity wolf,
      final DireWolfGrowthStage stage,
      final double scale,
      final String label) {
    final double health = wolf.getMaxHealth();
    final double damage = wolf.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    context.assertTrue(
        Math.abs(health - stage.maxHealth() * scale) < 0.001,
        label + " should have " + stage.maxHealth() * scale + " max health, not " + health);
    context.assertTrue(
        Math.abs(damage - stage.attackDamage() * scale) < 0.001,
        label + " should deal " + stage.attackDamage() * scale + " damage, not " + damage);
  }

  private static void overwriteBonus(
      final UnleashedDogEntity wolf,
      final RegistryEntry<EntityAttribute> attribute,
      final Identifier id,
      final double amount) {
    wolf.getAttributeInstance(attribute)
        .overwritePersistentModifier(
            new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE));
  }

  private static UnleashedDogEntity dogByUuid(final TestContext context, final UUID uuid) {
    return context.getWorld().getEntity(uuid) instanceof UnleashedDogEntity dog ? dog : null;
  }
}
