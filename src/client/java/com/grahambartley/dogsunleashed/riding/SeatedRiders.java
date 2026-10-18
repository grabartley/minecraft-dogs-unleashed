package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class SeatedRiders {

  private static @Nullable Entity drawnByWorld;
  private static @Nullable Entity drawnBySeat;
  private static final Map<Entity, Map<String, SeatTransform>> LAST_SEATS = new WeakHashMap<>();

  private SeatedRiders() {}

  public static @Nullable String seatOf(final Entity rider) {
    if (!(rider.getVehicle() instanceof UnleashedDogEntity mount)) {
      return null;
    }
    final List<String> seats = RigProfiles.of(mount.getRigSourceBreed()).seats();
    final int index = mount.getPassengerList().indexOf(rider);
    return index >= 0 && index < seats.size() ? seats.get(index) : null;
  }

  public static RiderPose poseOf(final UnleashedDogEntity mount, final float tickDelta) {
    return RigProfiles.of(mount.getRigSourceBreed())
        .riderPose()
        .at(mount.limbAnimator.getSpeed(tickDelta));
  }

  public static Vec3d eyeOf(final Entity rider, final Vec3d vanillaEye, final float tickDelta) {
    final String seatName = seatOf(rider);
    if (seatName == null || !(rider.getVehicle() instanceof UnleashedDogEntity mount)) {
      return vanillaEye;
    }
    final SeatTransform seat = LAST_SEATS.getOrDefault(mount, Map.of()).get(seatName);
    if (seat == null) {
      return vanillaEye;
    }
    final float lean = poseOf(mount, tickDelta).lean();
    final double eyeHeight = vanillaEye.y - lerpedPosition(rider, tickDelta).y;
    final Vector3f origin = seat.riderOrigin(rider.getVehicleAttachmentPos(mount), lean);
    final Vector3f eye =
        seat.riderRotation(lean).transform(new Vector3f(0.0f, (float) eyeHeight, 0.0f)).add(origin);
    return lerpedPosition(mount, tickDelta).add(eye.x, eye.y, eye.z);
  }

  public static boolean isSeated(final Entity rider) {
    return seatOf(rider) != null;
  }

  public static boolean isDrawnBySeat(final Entity entity) {
    return entity == drawnBySeat;
  }

  public static void beginWorldEntity(final Entity entity) {
    drawnByWorld = entity;
  }

  public static void endWorldEntity() {
    drawnByWorld = null;
  }

  public static void drawRiders(
      final UnleashedDogEntity mount,
      final Map<String, SeatTransform> seats,
      final MatrixStack matrices,
      final float tickDelta) {
    if (mount != drawnByWorld) {
      return;
    }
    LAST_SEATS.put(mount, seats);
    final MinecraftClient client = MinecraftClient.getInstance();
    final EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
    final Camera camera = dispatcher.camera;
    for (final Entity rider : mount.getPassengerList()) {
      final String seatName = seatOf(rider);
      if (seatName == null
          || (rider == camera.getFocusedEntity() && !camera.isThirdPerson())
          || rider.isRemoved()) {
        continue;
      }
      final SeatTransform seat = seats.get(seatName);
      matrices.push();
      if (seat != null) {
        seat.moveToRider(
            matrices, rider.getVehicleAttachmentPos(mount), poseOf(mount, tickDelta).lean());
        drawnBySeat = rider;
      } else {
        final Vec3d offset =
            lerpedPosition(rider, tickDelta).subtract(lerpedPosition(mount, tickDelta));
        matrices.translate(offset.x, offset.y, offset.z);
      }
      dispatcher.setRenderShadows(false);
      try {
        dispatcher.render(
            rider,
            0.0,
            0.0,
            0.0,
            MathHelper.lerp(tickDelta, rider.prevYaw, rider.getYaw()),
            tickDelta,
            matrices,
            consumersFor(client, rider),
            dispatcher.getLight(rider, tickDelta));
      } finally {
        dispatcher.setRenderShadows(true);
        drawnBySeat = null;
        matrices.pop();
      }
    }
  }

  private static Vec3d lerpedPosition(final Entity entity, final float tickDelta) {
    return new Vec3d(
        MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
        MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
        MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
  }

  private static VertexConsumerProvider consumersFor(
      final MinecraftClient client, final Entity rider) {
    if (!client.hasOutline(rider)) {
      return client.getBufferBuilders().getEntityVertexConsumers();
    }
    final OutlineVertexConsumerProvider outline =
        client.getBufferBuilders().getOutlineVertexConsumers();
    final int colour = rider.getTeamColorValue();
    outline.setColor(colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF, 0xFF);
    return outline;
  }
}
