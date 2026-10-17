package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.ModArmorItems;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class WolfDogArmourGameTest implements FabricGameTest {

  private static final String BATCH = "wolf-dog-armour";
  private static final int TICK_LIMIT = 20;
  private static final float HIT = 4.0f;
  private static final BlockPos STRANGER_POS = new BlockPos(2, 2, 3);

  private static Collection<TestFunction> perTier(
      final String name, final BiConsumer<TestContext, DogArmorTier> body) {
    return Arrays.stream(DogArmorTier.values())
        .map(
            tier ->
                GeneratedGameTest.of(
                    BATCH,
                    "wolfdogarmourgametest." + name + "." + tier.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> body.accept(context, tier)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> anOwnerPutsEachTierOnTheirWolf() {
    return perTier("equips", WolfDogArmourGameTest::testEquip);
  }

  private static void testEquip(final TestContext context, final DogArmorTier tier) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final WolfEntity wolf = tamedWolf(context, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModArmorItems.of(tier), 2));

    wolf.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        wolf.getBodyArmor().isOf(ModArmorItems.of(tier)), tier.id() + " should be worn");
    context.assertEquals(owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "One piece used");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> eachTierTakesTheHitInsteadOfTheWolf() {
    return perTier("absorbs", WolfDogArmourGameTest::testAbsorbs);
  }

  private static void testAbsorbs(final TestContext context, final DogArmorTier tier) {
    final WolfEntity wolf = armouredWolf(context, tier);

    wolf.damage(context.getWorld().getDamageSources().generic(), HIT);

    context.assertEquals(wolf.getHealth(), wolf.getMaxHealth(), "The wolf should take no damage");
    context.assertTrue(wolf.getBodyArmor().getDamage() > 0, tier.id() + " should wear");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> eachTierIsMendedWithItsMaterial() {
    return perTier("mends", WolfDogArmourGameTest::testMend);
  }

  private static void testMend(final TestContext context, final DogArmorTier tier) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final WolfEntity wolf = armouredWolf(context, owner, tier);
    final ItemStack worn = wolf.getBodyArmor();
    final int damaged = worn.getMaxDamage() / 2;
    worn.setDamage(damaged);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(tier.repairItem(), 2));

    wolf.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        wolf.getBodyArmor().getDamage(),
        damaged - worn.getMaxDamage() / 8,
        tier.id() + " should mend by an eighth");
    context.assertEquals(owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "One item used");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anArmadilloScuteDoesNotMendDogArmour(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final WolfEntity wolf = armouredWolf(context, owner, DogArmorTier.IRON);
    wolf.setInSittingPose(true);
    wolf.getBodyArmor().setDamage(40);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.ARMADILLO_SCUTE, 2));

    wolf.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(wolf.getBodyArmor().getDamage(), 40, "Only iron mends iron armour");
    context.assertEquals(owner.getStackInHand(Hand.MAIN_HAND).getCount(), 2, "No scute used");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerCannotArmourSomeoneElsesWolf(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = PendantTestHelper.survivalOwnerAt(context, STRANGER_POS);
    final WolfEntity wolf = tamedWolf(context, owner);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModArmorItems.of(DogArmorTier.IRON)));

    wolf.interactMob(stranger, Hand.MAIN_HAND);

    context.assertTrue(wolf.getBodyArmor().isEmpty(), "Only the owner armours a wolf");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPupCannotWearDogArmour(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final WolfEntity wolf = tamedWolf(context, owner);
    wolf.setBaby(true);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModArmorItems.of(DogArmorTier.IRON)));

    wolf.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(wolf.getBodyArmor().isEmpty(), "A pup is too small for armour");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shearsTakeDogArmourOffAWolf(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final WolfEntity wolf = armouredWolf(context, owner, DogArmorTier.DIAMOND);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    wolf.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(wolf.getBodyArmor().isEmpty(), "Shears should take the armour off");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void damageThatBypassesWolfArmourHitsTheWolf(final TestContext context) {
    final WolfEntity wolf = armouredWolf(context, DogArmorTier.DIAMOND);

    wolf.damage(context.getWorld().getDamageSources().magic(), HIT);

    context.assertTrue(wolf.getHealth() < wolf.getMaxHealth(), "Magic should hurt the wolf");
    context.assertEquals(wolf.getBodyArmor().getDamage(), 0, "Magic should skip the armour");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void betterArmourWearsMoreSlowlyOnAWolf(final TestContext context) {
    final WolfEntity leather = armouredWolf(context, DogArmorTier.LEATHER);
    final WolfEntity diamond = armouredWolf(context, DogArmorTier.DIAMOND);

    context.runAtTick(
        2,
        () -> {
          leather.damage(context.getWorld().getDamageSources().cactus(), 20.0f);
          diamond.damage(context.getWorld().getDamageSources().cactus(), 20.0f);
          context.assertTrue(
              diamond.getBodyArmor().getDamage() < leather.getBodyArmor().getDamage(),
              "Diamond should lose less durability than leather from the same hit");
          context.complete();
        });
  }

  private static WolfEntity tamedWolf(final TestContext context, final ServerPlayerEntity owner) {
    final WolfEntity wolf = context.spawnEntity(EntityType.WOLF, DOG_POS);
    wolf.setOwner(owner);
    wolf.setAiDisabled(true);
    return wolf;
  }

  private static WolfEntity armouredWolf(final TestContext context, final DogArmorTier tier) {
    return armouredWolf(context, PendantTestHelper.survivalOwnerAt(context, OWNER_POS), tier);
  }

  private static WolfEntity armouredWolf(
      final TestContext context, final ServerPlayerEntity owner, final DogArmorTier tier) {
    final WolfEntity wolf = tamedWolf(context, owner);
    wolf.equipBodyArmor(new ItemStack(ModArmorItems.of(tier)));
    return wolf;
  }
}
