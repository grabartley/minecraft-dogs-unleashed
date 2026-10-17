package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantFamily;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;

public final class PendantGuardianGameTest implements FabricGameTest {

  private static final String BATCH = "pendant-guardian";
  private static final String DAYLIGHT_BATCH = "undead-daylight";
  private static final int TICK_LIMIT = 20;
  private static final int DAYLIGHT_TICK_LIMIT = 260;
  private static final int DAYLIGHT_ASSERT_TICK = 250;
  private static final float HIT = 4.0f;
  private static final int LOW_AIR = 10;

  @CustomTestProvider
  public Collection<TestFunction> guardianBlocksItsOwnDamage() {
    return guardians()
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantguardiangametest.blocksown." + type.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testBlocksOwnDamage(context, type)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> guardianStillTakesOtherDamage() {
    return guardians()
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantguardiangametest.takesother." + type.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testTakesOtherDamage(context, type)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> unguardedDogTakesEachGuardedDamage() {
    return guardians()
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantguardiangametest.unguarded." + type.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testUnguardedDogIsHurt(context, type)))
        .toList();
  }

  private static java.util.stream.Stream<PendantType> guardians() {
    return Arrays.stream(PendantType.values())
        .filter(type -> type.family() == PendantFamily.GUARDIAN);
  }

  private static Function<DamageSources, DamageSource> guardedSource(final PendantType type) {
    return switch (type) {
      case GUARDIAN_FIRE -> DamageSources::lava;
      case GUARDIAN_FALL -> DamageSources::fall;
      case GUARDIAN_DROWNING -> DamageSources::drown;
      default -> throw new IllegalArgumentException(type.id());
    };
  }

  private static void testBlocksOwnDamage(final TestContext context, final PendantType type) {
    final UnleashedDogEntity dog = dogWearing(context, type);
    final float before = dog.getHealth();

    dog.damage(guardedSource(type).apply(context.getWorld().getDamageSources()), HIT);

    context.assertEquals(dog.getHealth(), before, type.id() + " should block its own damage");
    context.complete();
  }

  private static void testTakesOtherDamage(final TestContext context, final PendantType type) {
    final UnleashedDogEntity dog = dogWearing(context, type);
    final float before = dog.getHealth();

    dog.damage(context.getWorld().getDamageSources().generic(), HIT);

    context.assertTrue(dog.getHealth() < before, type.id() + " must not block unrelated damage");
    context.complete();
  }

  private static void testUnguardedDogIsHurt(final TestContext context, final PendantType type) {
    final UnleashedDogEntity dog = dogWearing(context, null);
    final float before = dog.getHealth();

    dog.damage(guardedSource(type).apply(context.getWorld().getDamageSources()), HIT);

    context.assertTrue(
        dog.getHealth() < before, "Without a pendant the dog should take " + type.id() + " damage");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void fireGuardianStopsTheDogCatchingFire(final TestContext context) {
    final UnleashedDogEntity dog = dogWearing(context, PendantType.GUARDIAN_FIRE);

    dog.setOnFireFor(5.0f);

    context.assertFalse(dog.isOnFire(), "A fire-guarded dog should not burn");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void drowningGuardianKeepsAirFull(final TestContext context) {
    final UnleashedDogEntity dog = dogWearing(context, PendantType.GUARDIAN_DROWNING);
    dog.setAir(LOW_AIR);

    context.runAtTick(
        5,
        () -> {
          context.assertEquals(
              dog.getAir(), dog.getMaxAir(), "A drowning-guarded dog should never run out of air");
          context.complete();
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = DAYLIGHT_BATCH,
      tickLimit = DAYLIGHT_TICK_LIMIT,
      skyAccess = true)
  public void fireGuardianStopsUndeadDogBurningInDaylight(final TestContext context) {
    final UnleashedDogEntity guarded = undeadDog(context, PendantType.GUARDIAN_FIRE, 3);
    final AtomicBoolean burned = new AtomicBoolean(false);

    context.runAtEveryTick(() -> burned.compareAndSet(false, guarded.isOnFire()));
    context.runAtTick(
        DAYLIGHT_ASSERT_TICK,
        () -> {
          context.assertFalse(burned.get(), "A fire-guarded undead dog should never burn");
          context.assertEquals(
              guarded.getHealth(), guarded.getMaxHealth(), "It should take no burn damage");
          context.complete();
        });
  }

  private static UnleashedDogEntity undeadDog(
      final TestContext context, final PendantType pendant, final int x) {
    final UnleashedDogEntity dog =
        context.spawnEntity(
            ModEntities.ZOMBIE_HUSKY, new net.minecraft.util.math.BlockPos(x, 2, 3));
    dog.setAiDisabled(true);
    if (pendant != null) {
      PendantTestHelper.wear(dog, pendant);
    }
    return dog;
  }

  private static UnleashedDogEntity dogWearing(final TestContext context, final PendantType type) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    return PendantTestHelper.dogWearing(context, type, owner, DOG_POS);
  }
}
