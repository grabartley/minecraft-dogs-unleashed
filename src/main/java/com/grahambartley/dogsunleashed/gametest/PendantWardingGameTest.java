package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantFamily;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PendantWardingGameTest implements FabricGameTest {

  private static final String BATCH = "pendant-warding";
  private static final String LIVE_BATCH = "pendant-warding-live";
  private static final int LIVE_TICK_LIMIT = 260;
  private static final int LIVE_ASSERT_TICK = 240;
  private static final int TICK_LIMIT = 40;
  private static final int AFTER_TWO_SCANS = 25;
  private static final int FLEE_TICK_LIMIT = 220;
  private static final int FLEE_ASSERT_TICK = 200;
  private static final BlockPos MOB_POS = new BlockPos(1, 2, 1);
  private static final double OUT_OF_RANGE_BLOCKS = 20.0;

  @CustomTestProvider
  public Collection<TestFunction> wardClearsTargetOnOwner() {
    return wards()
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantwardinggametest.owner." + type.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testWardClearsTarget(context, type, true)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> wardClearsTargetOnDog() {
    return wards()
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantwardinggametest.dog." + type.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testWardClearsTarget(context, type, false)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> wardKeepsALiveMobOffTheDogAndOwner() {
    return wards()
        .flatMap(
            type ->
                Stream.of(true, false)
                    .map(
                        worn ->
                            GeneratedGameTest.of(
                                LIVE_BATCH,
                                "pendantwardinggametest.live."
                                    + type.id()
                                    + (worn ? ".warded" : ".unwarded"),
                                ARENA,
                                LIVE_TICK_LIMIT,
                                context -> testLiveMob(context, type, worn))))
        .toList();
  }

  private static void testLiveMob(
      final TestContext context, final PendantType ward, final boolean worn) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, worn ? ward : null, owner, DOG_POS);
    final MobEntity mob = context.spawnEntity(liveAttacker(ward), MOB_POS);
    mob.setPersistent();
    mob.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
    final AtomicBoolean targetedThem = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (mob.getTarget() == owner || mob.getTarget() == dog) {
            targetedThem.set(true);
          }
        });
    context.runAtTick(
        LIVE_ASSERT_TICK,
        () -> {
          context.assertTrue(
              targetedThem.get() != worn,
              ward.id()
                  + (worn ? " should stop a live " : " control: an unwarded live ")
                  + mob.getType().getUntranslatedName()
                  + (worn ? " ever targeting its dog or owner" : " should target the owner"));
          context.complete();
        });
  }

  private static EntityType<? extends MobEntity> liveAttacker(final PendantType type) {
    return switch (type) {
      case WARDING_UNDEAD -> EntityType.ZOMBIE;
      case WARDING_ARTHROPOD -> EntityType.CAVE_SPIDER;
      case WARDING_ILLAGER -> EntityType.VINDICATOR;
      case WARDING_CREEPER -> EntityType.CREEPER;
      default -> throw new IllegalArgumentException(type.id());
    };
  }

  private static Stream<PendantType> wards() {
    return Arrays.stream(PendantType.values())
        .filter(type -> type.family() == PendantFamily.WARDING);
  }

  private static EntityType<? extends MobEntity> familyMember(final PendantType type) {
    return switch (type) {
      case WARDING_UNDEAD -> EntityType.ZOMBIE;
      case WARDING_ARTHROPOD -> EntityType.SPIDER;
      case WARDING_ILLAGER -> EntityType.PILLAGER;
      case WARDING_CREEPER -> EntityType.CREEPER;
      default -> throw new IllegalArgumentException(type.id());
    };
  }

  private static void testWardClearsTarget(
      final TestContext context, final PendantType ward, final boolean targetOwner) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, ward, owner, DOG_POS);
    final LivingEntity victim = targetOwner ? owner : dog;
    final MobEntity mob = targetingMob(context, familyMember(ward), victim);

    context.runAtTick(
        AFTER_TWO_SCANS,
        () -> {
          context.assertTrue(
              mob.getTarget() == null, ward.id() + " should stop its family targeting " + victim);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wardDoesNotProtectAStranger(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger =
        PendantTestHelper.survivalOwnerAt(context, new BlockPos(2, 2, 3));
    PendantTestHelper.dogWearing(context, PendantType.WARDING_UNDEAD, owner, DOG_POS);
    final MobEntity zombie = targetingMob(context, EntityType.ZOMBIE, stranger);

    context.runAtTick(
        AFTER_TWO_SCANS,
        () -> {
          context.assertTrue(
              zombie.getTarget() == stranger, "A ward protects the dog and its owner only");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wardedFamilyTagsHoldTheirVanillaMobs(final TestContext context) {
    assertWards(context, PendantType.WARDING_UNDEAD, EntityType.ZOMBIE, EntityType.SKELETON);
    assertWards(context, PendantType.WARDING_ARTHROPOD, EntityType.SPIDER, EntityType.SILVERFISH);
    assertWards(
        context,
        PendantType.WARDING_ILLAGER,
        EntityType.PILLAGER,
        EntityType.VINDICATOR,
        EntityType.RAVAGER,
        EntityType.WITCH);
    assertWards(context, PendantType.WARDING_CREEPER, EntityType.CREEPER);
    context.assertFalse(
        EntityType.CREEPER.isIn(PendantType.WARDING_UNDEAD.wardedMobs()),
        "A creeper is not undead");
    context.complete();
  }

  private static void assertWards(
      final TestContext context, final PendantType ward, final EntityType<?>... members) {
    for (final EntityType<?> member : members) {
      context.assertTrue(member.isIn(ward.wardedMobs()), ward.id() + " should cover " + member);
    }
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wardLeavesOtherFamiliesAlone(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    PendantTestHelper.dogWearing(context, PendantType.WARDING_UNDEAD, owner, DOG_POS);
    final MobEntity spider = targetingMob(context, EntityType.SPIDER, owner);

    context.runAtTick(
        AFTER_TWO_SCANS,
        () -> {
          context.assertTrue(spider.getTarget() == owner, "An undead ward must not calm spiders");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void unwardedDogLeavesTargetsAlone(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    final MobEntity zombie = targetingMob(context, EntityType.ZOMBIE, owner);

    context.runAtTick(
        AFTER_TWO_SCANS,
        () -> {
          context.assertTrue(zombie.getTarget() == owner, "Without a ward the zombie keeps aim");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wardDoesNotReachBeyondItsRadius(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, PendantType.WARDING_UNDEAD, owner, DOG_POS);
    final MobEntity zombie = context.spawnEntity(EntityType.ZOMBIE, MOB_POS);
    zombie.setAiDisabled(true);
    zombie.setNoGravity(true);
    final Vec3d far = dog.getPos().add(OUT_OF_RANGE_BLOCKS, 0.0, 0.0);
    zombie.refreshPositionAndAngles(far.x, far.y, far.z, 0.0f, 0.0f);
    zombie.setTarget(owner);

    context.runAtTick(
        AFTER_TWO_SCANS,
        () -> {
          context.assertTrue(
              zombie.getTarget() == owner, "A zombie 20 blocks from the dog is out of range");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = FLEE_TICK_LIMIT)
  public void creepersFleeACreeperWard(final TestContext context) {
    assertCreeperFlees(context, PendantType.WARDING_CREEPER, true);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = FLEE_TICK_LIMIT)
  public void creepersIgnoreOtherWards(final TestContext context) {
    assertCreeperFlees(context, PendantType.WARDING_UNDEAD, false);
  }

  private static void assertCreeperFlees(
      final TestContext context, final PendantType ward, final boolean expectFlee) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    PendantTestHelper.placeAt(context, owner, OWNER_POS);
    PendantTestHelper.dogWearing(context, ward, owner, DOG_POS);
    final CreeperEntity creeper = context.spawnEntity(EntityType.CREEPER, new BlockPos(2, 2, 3));
    final AtomicBoolean fled = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (creeper.goalSelector.getGoals().stream()
              .anyMatch(goal -> goal.isRunning() && goal.getGoal() instanceof FleeEntityGoal<?>)) {
            fled.set(true);
          }
        });
    context.runAtTick(
        FLEE_ASSERT_TICK,
        () -> {
          context.assertTrue(
              fled.get() == expectFlee,
              ward.id() + (expectFlee ? " should" : " should not") + " make the creeper flee");
          context.complete();
        });
  }

  private static MobEntity targetingMob(
      final TestContext context,
      final EntityType<? extends MobEntity> type,
      final LivingEntity victim) {
    final MobEntity mob = context.spawnEntity(type, MOB_POS);
    mob.setAiDisabled(true);
    mob.setTarget(victim);
    return mob;
  }
}
