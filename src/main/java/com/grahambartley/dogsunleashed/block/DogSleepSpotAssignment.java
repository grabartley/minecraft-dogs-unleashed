package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
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

/**
 * The assign, wake, and sleep-command flow shared by every block a dog can be assigned to. The dog
 * bed and the dog house differ only in their own extra interactions, so this owns the pending
 * assignment handshake started from the pet manager and the click handling that follows it.
 */
public final class DogSleepSpotAssignment {

  private static final Map<UUID, UUID> PENDING_ASSIGNMENTS = new HashMap<>();

  private DogSleepSpotAssignment() {}

  public static void setPendingAssignment(UUID playerUuid, UUID dogUuid) {
    PENDING_ASSIGNMENTS.put(playerUuid, dogUuid);
  }

  public static UUID consumePendingAssignment(UUID playerUuid) {
    return PENDING_ASSIGNMENTS.remove(playerUuid);
  }

  /**
   * Clears the JVM-global pending-assignment map. Lives for the lifetime of the JVM; survives world
   * reloads in singleplayer and leaks state between gametest batches. Called by test batch setup
   * hooks and by {@code SERVER_STOPPED}.
   */
  public static void clearPendingAssignments() {
    PENDING_ASSIGNMENTS.clear();
  }

  /**
   * Handles a bare-handed or assignment click on a sleep spot. {@code messageKeyRoot} is the
   * block's own translation key, so each block phrases its messages under its own namespace.
   */
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
        if (dog.isSleepingInBed()) {
          dog.getSleepController().markManuallyWoken();
          dog.wakeUp();
          player.sendMessage(Text.translatable(messageKeyRoot + ".wake_command", dogName), true);
        } else {
          dog.getSleepController().commandToSleep(pos);
          player.sendMessage(Text.translatable(messageKeyRoot + ".sleep_command", dogName), true);
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
    if (dog.isSleepingInBed()) {
      dog.wakeUp();
    }
    dog.getAssignedBedPos()
        .ifPresent(
            oldPos -> {
              if (world.getBlockEntity(oldPos) instanceof AssignedDogHolder oldHolder) {
                oldHolder.clearAssignedDog(null);
              }
            });
    holder.setAssignedDog(dog);
    dog.setAssignedBedPos(pos);
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
