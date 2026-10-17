package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class PendantLoyaltyGameTest implements FabricGameTest {

  private static final String BATCH = "pendant-loyalty";
  private static final int TICK_LIMIT = 40;
  private static final int AFTER_DAMAGE_IMMUNITY = 25;
  private static final float FATAL = 1000.0f;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loyaltySavesTheDogFromFatalDamageOnce(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, PendantType.LOYALTY, owner, DOG_POS);
    register(context, owner, dog);

    dog.damage(context.getWorld().getDamageSources().generic(), FATAL);

    context.assertTrue(dog.isAlive(), "Loyalty should keep the dog alive");
    context.assertEquals(dog.getHealth(), 1.0f, "The dog should survive at one health");
    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.PENDANT).isEmpty(),
        "The pendant should be used up");
    context.assertTrue(
        dog.hasStatusEffect(StatusEffects.REGENERATION)
            && dog.hasStatusEffect(StatusEffects.ABSORPTION),
        "The save should grant the totem's effects");
    context.assertEquals(
        PetManager.get(context.getWorld().getServer())
            .getPetByEntityId(dog.getUuid())
            .getLifeState(),
        PetLifeState.LIVING,
        "The pet record stays alive");
    context.assertFalse(hasGrave(context), "No grave should appear");

    context.runAtTick(
        AFTER_DAMAGE_IMMUNITY,
        () -> {
          dog.damage(context.getWorld().getDamageSources().generic(), FATAL);
          context.assertFalse(dog.isAlive(), "A spent pendant cannot save the dog a second time");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loyaltyDoesNotSaveFromDamageThatBypassesInvulnerability(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, PendantType.LOYALTY, owner, DOG_POS);

    dog.damage(context.getWorld().getDamageSources().outOfWorld(), FATAL);

    context.assertFalse(dog.isAlive(), "Like a totem, loyalty cannot cheat the void");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void otherPendantsDoNotSaveTheDog(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, PendantType.VITALITY, owner, DOG_POS);

    dog.damage(context.getWorld().getDamageSources().generic(), FATAL);

    context.assertFalse(dog.isAlive(), "Only loyalty saves a dog from fatal damage");
    context.complete();
  }

  private static void register(
      final TestContext context, final ServerPlayerEntity owner, final UnleashedDogEntity dog) {
    final PetData pet =
        new PetData(
            dog.getUuid(),
            owner.getUuid(),
            UnleashedDogBreed.HUSKY,
            "Loyal",
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            "minecraft:overworld",
            PetLifeState.LIVING);
    PetManager.get(context.getWorld().getServer()).registerPet(pet);
  }

  private static boolean hasGrave(final TestContext context) {
    final AtomicBoolean found = new AtomicBoolean(false);
    context.forEachRelativePos(
        pos -> {
          if (context.getBlockState(pos).isOf(ModBlocks.DOG_GRAVE)) {
            found.set(true);
          }
        });
    return found.get();
  }
}
