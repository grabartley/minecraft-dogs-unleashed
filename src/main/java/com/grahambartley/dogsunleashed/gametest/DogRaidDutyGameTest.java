package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModCosmeticItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogRaidDuty;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;

public final class DogRaidDutyGameTest implements FabricGameTest {

  private static final String BATCH = "dog-raid-duty";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos RAIDER_POS = new BlockPos(1, 2, 1);
  private static final int RAID_ID = 4930;
  private static final float BITE = 1.0f;
  private static final int TICK_LIMIT = 20;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bitingARaiderInAnActiveRaidCountsAsRaidDuty(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context, CreativeTestPlayer.join(context));
    final PillagerEntity pillager = this.spawnStillPillager(context);
    pillager.setRaid(new Raid(RAID_ID, context.getWorld(), context.getAbsolutePos(RAIDER_POS)));
    pillager.damage(context.getWorld().getDamageSources().mobAttack(dog), BITE);
    context.assertTrue(duty(dog).hasFoughtInARaid(), "biting a raid's pillager should count");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bitingAPillagerOutsideARaidDoesNotCount(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context, CreativeTestPlayer.join(context));
    final PillagerEntity pillager = this.spawnStillPillager(context);
    pillager.damage(context.getWorld().getDamageSources().mobAttack(dog), BITE);
    context.assertFalse(duty(dog).hasFoughtInARaid(), "a lone pillager is not a raid");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWonRaidPinsARosetteOnTheDogAndRewardsItsOwner(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = this.spawnOwnedDog(context, owner);
    duty(dog).rewardVictory();
    context.assertTrue(
        dog.getEquipmentHolder()
            .getStack(DogEquipmentSlot.COSMETIC)
            .isOf(ModCosmeticItems.of(DogCosmetic.ROSETTE)),
        "the dog should wear the rosette");
    context.assertTrue(
        DogTestHelper.droppedStacksOf(context, ModCosmeticItems.of(DogCosmetic.ROSETTE)).isEmpty(),
        "a worn rosette should not also drop");
    final AdvancementEntry advancement =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, "good_dog_of_the_village"));
    context.assertTrue(
        owner.getAdvancementTracker().getProgress(advancement).isDone(),
        "the owner should earn Good Dog of the Village");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDogAlreadyWearingACosmeticDropsExactlyOneRosette(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context, CreativeTestPlayer.join(context));
    final ItemStack bandana = new ItemStack(ModCosmeticItems.of(DogCosmetic.BANDANA));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSMETIC, bandana);
    duty(dog).rewardVictory();
    context.assertTrue(
        dog.getEquipmentHolder()
            .getStack(DogEquipmentSlot.COSMETIC)
            .isOf(ModCosmeticItems.of(DogCosmetic.BANDANA)),
        "the dog should keep its bandana");
    context.assertEquals(
        1,
        DogTestHelper.droppedStacksOf(context, ModCosmeticItems.of(DogCosmetic.ROSETTE)).size(),
        "exactly one rosette should drop");
    context.complete();
  }

  private static DogRaidDuty duty(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogRaidDuty.class);
  }

  private UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private PillagerEntity spawnStillPillager(final TestContext context) {
    final PillagerEntity pillager = context.spawnEntity(EntityType.PILLAGER, RAIDER_POS);
    pillager.setAiDisabled(true);
    return pillager;
  }
}
