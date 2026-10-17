package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class PendantVitalityGameTest implements FabricGameTest {

  private static final String BATCH = "pendant-vitality";
  private static final int TICK_LIMIT = 80;
  private static final int SUPPRESSION_TICK_LIMIT = 160;
  private static final int AFTER_SPAWN_PROTECTION = 70;
  private static final int AFTER_TWO_CHECKS = 45;
  private static final double OUT_OF_RANGE_BLOCKS = 12.0;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void ownerNearAVitalityDogGetsRegenerationOne(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, DOG_POS);
    PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, new BlockPos(2, 2, 3));

    context.runAtTick(
        AFTER_TWO_CHECKS,
        () -> {
          final StatusEffectInstance regeneration =
              owner.getStatusEffect(StatusEffects.REGENERATION);
          context.assertTrue(regeneration != null, "The owner should be regenerating");
          context.assertEquals(
              regeneration.getAmplifier(), 0, "Two vitality dogs still give Regeneration I");
          context.assertFalse(
              regeneration.shouldShowParticles(), "Vitality regeneration shows no particles");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = SUPPRESSION_TICK_LIMIT)
  public void recentDamageSuppressesVitality(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);

    context.runAtTick(
        AFTER_SPAWN_PROTECTION,
        () -> {
          final float before = owner.getHealth();
          owner.damage(context.getWorld().getDamageSources().generic(), 1.0f);
          context.assertTrue(owner.getHealth() < before, "The hit should land on the owner");
          PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, DOG_POS);
        });
    context.runAtTick(
        AFTER_SPAWN_PROTECTION + AFTER_TWO_CHECKS,
        () -> {
          context.assertFalse(
              owner.hasStatusEffect(StatusEffects.REGENERATION),
              "Vitality should wait 10 seconds after the owner is hurt");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void vitalityDoesNotReachAnOwnerOutOfRange(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, DOG_POS);
    owner.setNoGravity(true);
    owner.refreshPositionAndAngles(
        owner.getX() + OUT_OF_RANGE_BLOCKS, owner.getY(), owner.getZ(), 0.0f, 0.0f);

    context.runAtTick(
        AFTER_TWO_CHECKS,
        () -> {
          context.assertFalse(
              owner.hasStatusEffect(StatusEffects.REGENERATION),
              "An owner 12 blocks away is out of vitality range");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerBesideSomeoneElsesDogGetsNothing(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger =
        PendantTestHelper.survivalOwnerAt(context, new BlockPos(2, 2, 3));
    PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, DOG_POS);

    context.runAtTick(
        AFTER_TWO_CHECKS,
        () -> {
          context.assertTrue(
              owner.hasStatusEffect(StatusEffects.REGENERATION), "The owner should regenerate");
          context.assertFalse(
              stranger.hasStatusEffect(StatusEffects.REGENERATION),
              "Vitality only helps the dog's own owner");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void otherPendantsGiveNoRegeneration(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    PendantTestHelper.dogWearing(context, PendantType.LOYALTY, owner, DOG_POS);

    context.runAtTick(
        AFTER_TWO_CHECKS,
        () -> {
          context.assertFalse(
              owner.hasStatusEffect(StatusEffects.REGENERATION),
              "Only vitality regenerates the owner");
          context.complete();
        });
  }
}
