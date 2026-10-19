package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetManagerSummon;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PetManagerSummonGameTest implements FabricGameTest {

  private static final String TELEPORT_ARENA = "dogs-unleashed:teleport_arena";
  private static final Vec3d OWNER_POS = new Vec3d(20.5, 2, 2.5);
  private static final BlockPos DOG_START = new BlockPos(2, 2, 2);
  private static final double NEAR_OWNER = 4.0;

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 20)
  public void summonBringsTheDogAndStartsTheWhistleCooldown(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity husky = spawnRegisteredDog(context, owner);

    PetManagerSummon.summon(owner, husky.getUuid());

    context.assertTrue(
        liveDog(context, husky).squaredDistanceTo(owner) <= NEAR_OWNER * NEAR_OWNER,
        "A Pet Manager summon should bring the dog to its owner");
    context.assertTrue(
        owner.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE),
        "A Pet Manager summon should start the whistle's cooldown");
    context.complete();
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 20)
  public void secondSummonWithinTheCooldownIsIgnored(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity husky = spawnRegisteredDog(context, owner);
    PetManagerSummon.summon(owner, husky.getUuid());
    final Vec3d start = context.getAbsolute(Vec3d.ofBottomCenter(DOG_START));
    liveDog(context, husky).refreshPositionAndAngles(start.x, start.y, start.z, 0.0F, 0.0F);

    PetManagerSummon.summon(owner, husky.getUuid());

    context.assertTrue(
        liveDog(context, husky).getPos().squaredDistanceTo(start) < 0.01,
        "A second Pet Manager summon within the cooldown should leave the dog where it is");
    context.complete();
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 20)
  public void summonRightAfterAWhistleBlowIsIgnored(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity husky = spawnRegisteredDog(context, owner);
    final Vec3d start = husky.getPos();
    owner.getItemCooldownManager().set(ModItems.DOG_WHISTLE, DogWhistleItem.BLOW_COOLDOWN_TICKS);

    PetManagerSummon.summon(owner, husky.getUuid());

    context.assertTrue(
        liveDog(context, husky).getPos().squaredDistanceTo(start) < 0.01,
        "A Pet Manager summon during the whistle's cooldown should leave the dog where it is");
    context.complete();
  }

  private static Entity liveDog(TestContext context, UnleashedDogEntity husky) {
    return context.getWorld().getEntity(husky.getUuid());
  }

  private static ServerPlayerEntity placeOwner(TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d absolutePos = context.getAbsolute(OWNER_POS);
    player.refreshPositionAndAngles(absolutePos.x, absolutePos.y, absolutePos.z, 0.0F, 0.0F);
    return player;
  }

  private static UnleashedDogEntity spawnRegisteredDog(
      TestContext context, ServerPlayerEntity owner) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    husky.setAiDisabled(true);
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());
    PetManager.get(context.getWorld().getServer())
        .registerPet(
            new PetData(
                husky.getUuid(),
                owner.getUuid(),
                husky.getBreed(),
                "Rex",
                husky.getHealth(),
                husky.getMaxHealth(),
                husky.getBlockPos(),
                context.getWorld().getRegistryKey().getValue().toString(),
                PetLifeState.LIVING));
    return husky;
  }
}
