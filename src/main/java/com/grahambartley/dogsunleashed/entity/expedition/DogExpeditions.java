package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogExpeditions {

  public static final int WHEEL_ACTION_ID = 0;
  public static final String WHEEL_LABEL_KEY = "command.dogs-unleashed.explore";
  public static final String AWAY_KEY = "message.dogs-unleashed.expedition.away";

  private DogExpeditions() {}

  public static void registerWheelAction() {
    DogWheelSubActions.REGISTERED.register(
        new DogWheelSubAction(
            WHEEL_ACTION_ID,
            WHEEL_LABEL_KEY,
            Identifier.ofVanilla("filled_map"),
            DogExpeditions::refusalKey,
            (dog, player) -> ExpeditionDeparture.send(dog, player)));
  }

  public static @Nullable String refusalKey(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    return ExpeditionEligibility.refusalKey(
            dog.isUndead(),
            dog.hasPassengers() || dog.hasVehicle(),
            dog.getSleepController().isAsleep() || dog.getSleepController().isCommandedToSleep(),
            dog.isSitting() || dog.isInSittingPose(),
            dog.isLeashed())
        .orElse(null);
  }

  public static boolean isAway(final MinecraftServer server, final PetData pet) {
    return Expeditions.get(server).isAway(pet.getPetId());
  }

  static void puff(final ServerWorld world, final Vec3d pos) {
    world.spawnParticles(ParticleTypes.POOF, pos.x, pos.y + 0.4, pos.z, 12, 0.3, 0.3, 0.3, 0.02);
  }
}
