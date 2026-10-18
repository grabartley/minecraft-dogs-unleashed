package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.entity.DogSleepController;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DogSleepSpotAssignment {

  private static final Map<UUID, UUID> PENDING_ASSIGNMENTS = new HashMap<>();

  private DogSleepSpotAssignment() {}

  public static void setPendingAssignment(UUID playerUuid, UUID dogUuid) {
    PENDING_ASSIGNMENTS.put(playerUuid, dogUuid);
  }

  public static UUID consumePendingAssignment(UUID playerUuid) {
    return PENDING_ASSIGNMENTS.remove(playerUuid);
  }

  public static void clearPendingAssignments() {
    PENDING_ASSIGNMENTS.clear();
  }

  public static ActionResult handleUse(
      World world,
      BlockPos pos,
      PlayerEntity player,
      AssignedDogHolder holder,
      String messageKeyRoot) {

    if (player.isSneaking() && player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
      if (holder.hasAssignedDog()) {
        final String dogName = dogName(world, holder.getAssignedDog(world));
        holder.clearAssignedDog(world);
        player.sendMessage(Text.translatable(messageKeyRoot + ".unassigned", dogName), true);
        return ActionResult.SUCCESS;
      }
      return ActionResult.PASS;
    }

    final UUID pendingDogUuid = consumePendingAssignment(player.getUuid());
    if (pendingDogUuid != null && world instanceof ServerWorld serverWorld) {
      final Entity entity = serverWorld.getEntity(pendingDogUuid);
      if (entity instanceof UnleashedDogEntity dog && dog.isOwner(player)) {
        if (!dog.getBreed().nature().sleepsInBeds()) {
          player.sendMessage(
              Text.translatable("message.dogs-unleashed.bed_refused", dogName(world, dog)), true);
          return ActionResult.SUCCESS;
        }
        assign(serverWorld, pos, holder, dog);
        player.sendMessage(
            Text.translatable("message.dogs-unleashed.bed_assigned", dogName(world, dog)), true);
        return ActionResult.SUCCESS;
      }
    }

    if (holder.hasAssignedDog()) {
      final UnleashedDogEntity dog = holder.getAssignedDog(world);
      if (dog != null && dog.isOwner(player)) {
        final String dogName = dogName(world, dog);
        if (dog.getSleepController().isSleepingInBed()) {
          dog.getSleepController().markManuallyWoken();
          dog.wakeUp();
          player.sendMessage(Text.translatable(messageKeyRoot + ".wake_command", dogName), true);
        } else {
          final String replyKey =
              dog.getSleepController().commandToSleep(pos)
                  ? messageKeyRoot + ".sleep_command"
                  : DogSleepController.NOT_SLEEPY_MESSAGE_KEY;
          player.sendMessage(Text.translatable(replyKey, dogName), true);
        }
        return ActionResult.SUCCESS;
      }
      return ActionResult.PASS;
    }

    player.sendMessage(Text.translatable("message.dogs-unleashed.no_pending_assignment"), true);
    return ActionResult.SUCCESS;
  }

  private static void assign(
      ServerWorld world, BlockPos pos, AssignedDogHolder holder, UnleashedDogEntity dog) {
    if (dog.getSleepController().isSleepingInBed()) {
      dog.wakeUp();
    }
    dog.getSleepController()
        .getAssignedBedPos()
        .ifPresent(
            oldPos -> {
              if (world.getBlockEntity(oldPos) instanceof AssignedDogHolder oldHolder) {
                oldHolder.clearAssignedDog(null);
              }
            });
    holder.setAssignedDog(dog);
    dog.getSleepController().setAssignedBedPos(pos);
  }

  public static String dogName(World world, UnleashedDogEntity dog) {
    if (dog == null) {
      return "Dog";
    }
    if (world instanceof ServerWorld serverWorld) {
      final PetManager petManager = PetManager.get(serverWorld.getServer());
      final PetData petData = petManager.getPetByEntityId(dog.getUuid());
      if (petData != null) {
        return petData.getName();
      }
    }
    return Text.translatable(dog.getBreed().translationKey()).getString();
  }
}
