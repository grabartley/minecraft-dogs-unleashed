package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.WildPackGrudges;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.network.payload.WildTrustView;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DireWolfGrudgeGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_PACK = new BlockPos(3, FLOOR_Y, 4);
  private static final BlockPos REL_LATECOMER = new BlockPos(14, FLOOR_Y, 1);
  private static final Vec3d REL_NEARBY = new Vec3d(10.5, FLOOR_Y, 4.5);

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void hurtingAnAdultLeavesEveryPupOfThePackShunningThePlayer(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, null, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.PUP);
    final UnleashedDogEntity stranger = spawnPack(context, null, DireWolfGrowthStage.PUP).get(0);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    stranger.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    try {
      final UnleashedDogEntity pup = pack.get(1);
      feed(pup, player);
      pack.get(0).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);

      context.assertTrue(pup.getTrust().isShunnedBy(player), "The pup should shun the player");
      context.assertTrue(
          ModNetworking.trustView(player, pup).equals(WildTrustView.SHUNNED),
          "The inspect card should say the pack will never trust the player");
      context.assertFalse(
          stranger.getTrust().isShunnedBy(player), "A pup of another pack shunned the player");
    } finally {
      SurvivalTestPlayer.leave(context, player);
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aShunningPupRefusesFoodAndKeepsIt(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, null, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.PUP);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    try {
      pack.get(0).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);
      final ItemStack beef = feed(pack.get(1), player);

      context.assertTrue(beef.getCount() == 1, "The shunning pup took the food");
      context.assertTrue(
          pack.get(1).getTrust().trustOf(player) == 0, "The shunning pup gained trust");
    } finally {
      SurvivalTestPlayer.leave(context, player);
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aPackmateThatWasNotThereStillHoldsTheGrudge(final TestContext context) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    final EntityData data = spawnInto(context, pack, REL_PACK, null, DireWolfGrowthStage.ADULT);
    pack.get(0).setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    try {
      pack.get(0).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);
      spawnInto(context, pack, REL_LATECOMER, data, DireWolfGrowthStage.PUP);
      pack.get(1).setAiDisabled(true);

      context.assertTrue(
          pack.get(1).getPack().bearsGrudge(player.getUuid()),
          "A packmate that joined later should hold the pack's grudge");
      context.assertTrue(
          pack.get(1).getPack().isThreat(player), "The young should keep away from the player");
    } finally {
      SurvivalTestPlayer.leave(context, player);
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 200)
  public void grownWolvesGoAfterAPlayerTheyHoldAGrudgeAgainst(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, null, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.PUP);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    WildPackGrudges.get(context.getWorld().getServer())
        .record(pack.get(0).getPack().id(), player.getUuid());
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              pack.get(0).getTarget() == player, "The adult should go after the player");
          SurvivalTestPlayer.leave(context, player);
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 200)
  public void aWolfChasingAGrudgeTurnsOnAMobThatHurtsItsPack(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, null, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    final UnleashedDogEntity chaser = pack.get(0);
    pack.get(1).setAiDisabled(true);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_LATECOMER);
    zombie.setAiDisabled(true);
    zombie.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(1000.0);
    zombie.setHealth(1000.0f);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    WildPackGrudges.get(context.getWorld().getServer())
        .record(chaser.getPack().id(), player.getUuid());
    final long[] hitAt = {-1};
    context.runAtEveryTick(
        () -> {
          if (hitAt[0] < 0 && chaser.getTarget() == player) {
            hitAt[0] = context.getTick();
            pack.get(1).damage(context.getWorld().getDamageSources().mobAttack(zombie), 1.0f);
          } else if (hitAt[0] >= 0) {
            context.assertTrue(
                chaser.getTarget() == zombie,
                "The wolf dropped the zombie for " + chaser.getTarget());
            if (context.getTick() >= hitAt[0] + 20) {
              SurvivalTestPlayer.leave(context, player);
              context.complete();
            }
          }
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aCreativePlayerNeverEarnsAGrudge(final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, null, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    wolf.damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);
    context.assertFalse(
        wolf.getPack().bearsGrudge(player.getUuid()), "A creative player earned a grudge");
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void turningPackDefenceOffStopsTheGrudgeFromTurningIntoAnAttack(
      final TestContext context) {
    final UnleashedDogEntity wolf = spawnPack(context, null, DireWolfGrowthStage.ADULT).get(0);
    wolf.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    try {
      WildPackGrudges.get(context.getWorld().getServer())
          .record(wolf.getPack().id(), player.getUuid());
      context.assertTrue(wolf.getPack().huntsDown(player), "The adult should go after the player");
      withConfig(
          context,
          config ->
              config.withDireWolf(
                  config.direWolf().withPacks(config.direWolf().packs().withDefend(false))),
          () ->
              context.assertFalse(
                  wolf.getPack().huntsDown(player),
                  "Pack defence is off, but the adult would still go after the player"));
    } finally {
      SurvivalTestPlayer.leave(context, player);
    }
    context.complete();
  }

  private static ItemStack feed(final UnleashedDogEntity wolf, final ServerPlayerEntity player) {
    final ItemStack stack = new ItemStack(Items.BEEF, 1);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    wolf.interactMob(player, Hand.MAIN_HAND);
    return stack;
  }

  private static List<UnleashedDogEntity> spawnPack(
      final TestContext context, final EntityData data, final DireWolfGrowthStage... stages) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    EntityData next = data;
    for (int i = 0; i < stages.length; i++) {
      next = spawnInto(context, pack, REL_PACK.south(i % 4).west(i / 4), next, stages[i]);
    }
    return pack;
  }

  private static EntityData spawnInto(
      final TestContext context,
      final List<UnleashedDogEntity> pack,
      final BlockPos relative,
      final EntityData data,
      final DireWolfGrowthStage stage) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, relative);
    final EntityData next =
        wolf.initialize(
            context.getWorld(),
            context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
            SpawnReason.NATURAL,
            data);
    wolf.getGrowth().setStage(stage);
    pack.add(wolf);
    return next;
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
