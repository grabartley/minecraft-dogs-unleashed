package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DireWolfPackGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_WOLF = new BlockPos(3, FLOOR_Y, 4);
  private static final BlockPos REL_PREY = new BlockPos(9, FLOOR_Y, 4);
  private static final int WATCH_TICKS = 100;
  private static final int NATURAL_PACK_SIZE = 10;
  private static final String HUNTING_OFF_BATCH = "pack-hunting-off";

  private static DogsUnleashedConfig configBeforeHuntingOff;

  @GameTest(templateName = RANGE, tickLimit = WATCH_TICKS + 20)
  public void aWildAdultHuntsANearbySheep(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    final SheepEntity sheep = context.spawnEntity(EntityType.SHEEP, REL_PREY);
    sheep.setAiDisabled(true);
    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                wolf.getTarget() == sheep, "The wild adult has not gone for the sheep"));
  }

  @GameTest(templateName = RANGE, tickLimit = WATCH_TICKS + 20)
  public void aWildPrimeHuntsAZombie(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.PRIME).get(0);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_PREY);
    zombie.setAiDisabled(true);
    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                wolf.getTarget() == zombie, "The wild prime has not gone for the zombie"));
  }

  @GameTest(templateName = RANGE, tickLimit = WATCH_TICKS + 20)
  public void pennedNamedAndTamedAnimalsAreLeftAlone(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    for (int x = 10; x <= 14; x++) {
      for (int z = 0; z <= 4; z++) {
        if (x == 10 || x == 14 || z == 0 || z == 4) {
          context.setBlockState(new BlockPos(x, FLOOR_Y, z), Blocks.OAK_FENCE);
        }
      }
    }
    context.spawnEntity(EntityType.SHEEP, new BlockPos(12, FLOOR_Y, 2)).setAiDisabled(true);
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(6, FLOOR_Y, 8));
    cow.setCustomName(Text.literal("Daisy"));
    cow.setAiDisabled(true);
    final HorseEntity horse = context.spawnEntity(EntityType.HORSE, new BlockPos(3, FLOOR_Y, 8));
    horse.setTame(true);
    horse.setAiDisabled(true);
    neverTargets(context, List.of(wolf), "a penned sheep, a named cow or a tamed horse");
  }

  @GameTest(templateName = RANGE, tickLimit = WATCH_TICKS + 20)
  public void pupsAndYearlingsNeverStartAHunt(final TestContext context) {
    final List<UnleashedDogEntity> young =
        spawnPack(context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.YEARLING);
    context.spawnEntity(EntityType.SHEEP, REL_PREY).setAiDisabled(true);
    neverTargets(context, young, "the sheep");
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aWolfWaitsTwoMinutesAfterAKill(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    context.assertTrue(wolf.getPack().mayHunt(), "A fresh wild adult should be free to hunt");
    wolf.getPack().recordKill();
    context.assertFalse(wolf.getPack().mayHunt(), "A wolf that just killed should rest first");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void tamedDireWolvesNeverHuntOnTheirOwn(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    wolf.setOwner(context.createMockCreativeServerPlayerInWorld());
    context.assertFalse(wolf.getPack().mayHunt(), "A tamed dire wolf hunts only on command");
    context.complete();
  }

  @BeforeBatch(batchId = HUNTING_OFF_BATCH)
  public void rememberConfig(final ServerWorld world) {
    configBeforeHuntingOff = DogsUnleashed.SERVER_CONFIG;
  }

  @AfterBatch(batchId = HUNTING_OFF_BATCH)
  public void restoreConfig(final ServerWorld world) {
    ServerConfigService.update(world.getServer(), configBeforeHuntingOff);
  }

  @GameTest(templateName = RANGE, tickLimit = WATCH_TICKS + 40, batchId = HUNTING_OFF_BATCH)
  public void turningPackHuntingOffMidHuntCallsItOff(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    final SheepEntity sheep = context.spawnEntity(EntityType.SHEEP, REL_PREY);
    sheep.setAiDisabled(true);
    sheep.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(1000.0);
    sheep.setHealth(1000.0f);
    final boolean[] calledOff = {false};
    context.runAtEveryTick(
        () -> {
          if (!calledOff[0] && wolf.getTarget() == sheep) {
            calledOff[0] = true;
            final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
            ServerConfigService.update(
                context.getWorld().getServer(),
                config.withDireWolf(
                    config.direWolf().withPacks(config.direWolf().packs().withHunt(false))));
          }
        });
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(calledOff[0], "The wolf never started hunting the sheep");
          context.assertTrue(
              wolf.getTarget() == null, "Hunting was turned off, but the wolf kept hunting");
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void theWholePackTurnsOnAPlayerWhoHurtsOneOfThem(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(
            context,
            DireWolfGrowthStage.ADULT,
            DireWolfGrowthStage.PRIME,
            DireWolfGrowthStage.YEARLING,
            DireWolfGrowthStage.PUP);
    final UnleashedDogEntity stranger = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    stranger.setAiDisabled(true);
    final PlayerEntity player = survivalPlayer(context);

    pack.get(2).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);

    context.assertTrue(pack.get(0).getTarget() == player, "The adult should attack the player");
    context.assertTrue(pack.get(1).getTarget() == player, "The prime should attack the player");
    context.assertTrue(
        pack.get(0).getAngryAt() != null && pack.get(0).getAngryAt().equals(player.getUuid()),
        "The adult should stay angry at the player");
    context.assertTrue(pack.get(2).getTarget() == null, "The yearling should keep out of it");
    context.assertTrue(pack.get(3).getTarget() == null, "The pup should keep out of it");
    context.assertTrue(
        pack.get(2).getPack().isThreat(player) && pack.get(3).getPack().isThreat(player),
        "The young should know to keep away from the player");
    context.assertTrue(stranger.getTarget() == null, "A wolf from another pack should stay out");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void thePackDefendsItselfFromMobsToo(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_PREY);
    zombie.setAiDisabled(true);

    pack.get(1).damage(context.getWorld().getDamageSources().mobAttack(zombie), 1.0f);

    context.assertTrue(pack.get(0).getTarget() == zombie, "The packmate should attack the zombie");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aCreativePlayerNeverDrawsThePack(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    pack.get(1).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);

    context.assertTrue(pack.get(0).getTarget() == null, "A creative player should not be attacked");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void turningPackDefenceOffLeavesPackmatesOutOfIt(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final PlayerEntity player = survivalPlayer(context);
    withConfig(
        context,
        config ->
            config.withDireWolf(
                config.direWolf().withPacks(config.direWolf().packs().withDefend(false))),
        () -> pack.get(1).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f));
    context.assertTrue(
        pack.get(0).getTarget() == null, "Pack defence is off, but the packmate attacked");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aNaturalPackNeverHasAnAlpha(final TestContext context) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    for (int i = 0; i < NATURAL_PACK_SIZE; i++) {
      pack.add(context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF.east(i % 8)));
    }
    EntityData data = null;
    for (final UnleashedDogEntity wolf : pack) {
      wolf.setAiDisabled(true);
      data =
          wolf.initialize(
              context.getWorld(),
              context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
              SpawnReason.NATURAL,
              data);
      context.assertTrue(
          wolf.getGrowth().stage() != DireWolfGrowthStage.ALPHA,
          "A natural pack member spawned as " + wolf.getGrowth().stage());
      context.assertTrue(
          wolf.getPack().id() != null && wolf.getPack().id().equals(pack.get(0).getPack().id()),
          "Every member of a natural pack shares one pack id");
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aWildPrimeNeverBecomesAnAlpha(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.PRIME).get(0);
    wolf.setAiDisabled(true);
    final PlayerEntity player = survivalPlayer(context);
    final ItemStack apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
    player.setStackInHand(Hand.MAIN_HAND, apple);
    wolf.interactMob(player, Hand.MAIN_HAND);
    context.assertTrue(
        wolf.getGrowth().stage() == DireWolfGrowthStage.PRIME,
        "A wild prime became " + wolf.getGrowth().stage());
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void thePackIdSurvivesASaveAndLoad(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    final NbtCompound nbt = new NbtCompound();
    wolf.writeNbt(nbt);
    final UnleashedDogEntity loaded = context.spawnEntity(ModEntities.DIRE_WOLF, REL_PREY);
    loaded.setAiDisabled(true);
    loaded.readNbt(nbt);
    context.assertTrue(
        wolf.getPack().id().equals(loaded.getPack().id()), "The pack id was lost on reload");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 40)
  public void anAngryDireWolfGrowlsAndStopsWhenCalm(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_PREY);
    zombie.setAiDisabled(true);
    wolf.setTarget(zombie);
    context.runAtTick(
        3,
        () -> {
          context.assertTrue(wolf.getGrowl().isGrowling(), "An angry dire wolf should growl");
          wolf.setTarget(null);
        });
    context.runAtTick(
        6,
        () -> {
          context.assertFalse(
              wolf.getGrowl().isGrowling(), "A calm dire wolf should stop growling");
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void otherBreedsNeverGrowl(final TestContext context) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, REL_WOLF);
    husky.setAiDisabled(true);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_PREY);
    zombie.setAiDisabled(true);
    husky.setTarget(zombie);
    context.runAtTick(
        3,
        () -> {
          context.assertFalse(husky.getGrowl().isGrowling(), "A husky should not growl");
          context.complete();
        });
  }

  private static List<UnleashedDogEntity> spawnPack(
      final TestContext context, final DireWolfGrowthStage... stages) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    EntityData data = null;
    for (int i = 0; i < stages.length; i++) {
      final UnleashedDogEntity wolf =
          context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF.south(i % 4).west(i / 4));
      data =
          wolf.initialize(
              context.getWorld(),
              context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
              SpawnReason.NATURAL,
              data);
      wolf.getGrowth().setStage(stages[i]);
      pack.add(wolf);
    }
    return pack;
  }

  private static void neverTargets(
      final TestContext context, final List<UnleashedDogEntity> wolves, final String prey) {
    context.runAtEveryTick(
        () -> {
          for (final UnleashedDogEntity wolf : wolves) {
            final LivingEntity target = wolf.getTarget();
            context.assertTrue(
                target == null,
                wolf.getGrowth().stage() + " went for " + target + " instead of leaving " + prey);
          }
        });
    context.runAtTick(WATCH_TICKS, context::complete);
  }

  private static PlayerEntity survivalPlayer(final TestContext context) {
    return context.createMockPlayer(GameMode.SURVIVAL);
  }

  private static void withConfig(
      final TestContext context,
      final UnaryOperator<DogsUnleashedConfig> change,
      final Runnable action) {
    final MinecraftServer server = context.getWorld().getServer();
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    ServerConfigService.update(server, change.apply(before));
    try {
      action.run();
    } finally {
      ServerConfigService.update(server, before);
    }
  }
}
