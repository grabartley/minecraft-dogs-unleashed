package com.grahambartley.dogsunleashed.render.sled;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledFormation;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class HarnessLines {

  static final float WIDTH = 0.025f;
  static final float RED = 0.5f;
  static final float GREEN = 0.4f;
  static final float BLUE = 0.3f;
  static final float SHADE = 0.7f;

  private HarnessLines() {}

  public static void register() {
    WorldRenderEvents.AFTER_ENTITIES.register(HarnessLines::render);
    WorldRenderEvents.START.register(context -> HarnessRumps.startFrame());
  }

  public static List<UnleashedDogEntity> pullersOf(final SledEntity sled) {
    final List<UnleashedDogEntity> dogs = new ArrayList<>();
    for (final int id : sled.getTeam().pullerIds()) {
      if (sled.getWorld().getEntityById(id) instanceof UnleashedDogEntity dog) {
        dogs.add(dog);
      }
    }
    return dogs;
  }

  public static Vec3d eyeInWorld(final Vec3d sledPos, final float yaw, final Vector3f eye) {
    final Vec3d forward = SledFormation.forward(yaw);
    final Vec3d left = new Vec3d(forward.z, 0.0, -forward.x);
    return sledPos.add(left.multiply(eye.x)).add(0.0, eye.y, 0.0).add(forward.multiply(eye.z));
  }

  private static void render(final WorldRenderContext context) {
    final ClientWorld world = context.world();
    final MatrixStack matrices = context.matrixStack();
    final VertexConsumerProvider consumers = context.consumers();
    if (world == null || matrices == null || consumers == null) {
      return;
    }
    final float tickDelta = context.tickCounter().getTickDelta(false);
    final Vec3d camera = context.camera().getPos();
    for (final Entity entity : world.getEntities()) {
      if (entity instanceof SledEntity sled) {
        drawTeam(sled, world, matrices, consumers, tickDelta, camera);
      }
    }
  }

  private static void drawTeam(
      final SledEntity sled,
      final ClientWorld world,
      final MatrixStack matrices,
      final VertexConsumerProvider consumers,
      final float tickDelta,
      final Vec3d camera) {
    final List<UnleashedDogEntity> dogs = pullersOf(sled);
    if (dogs.isEmpty()) {
      return;
    }
    final double widest = dogs.stream().mapToDouble(Entity::getWidth).max().orElse(0.0);
    final List<Double> sides = new ArrayList<>();
    for (int index = 0; index < dogs.size(); index++) {
      sides.add(SledFormation.slot(index, dogs.size(), widest).left());
    }
    final List<Vector3f> eyes = HarnessEyes.assign(SledAnchors.shared().eyes(), sides);
    final Vec3d sledPos = sled.getLerpedPos(tickDelta);
    final float yaw = MathHelper.lerpAngleDegrees(tickDelta, sled.prevYaw, sled.getYaw());
    final VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLeash());
    for (int index = 0; index < dogs.size(); index++) {
      final Vec3d start = eyeInWorld(sledPos, yaw, eyes.get(index));
      final Vec3d end = HarnessRumps.of(dogs.get(index), tickDelta);
      matrices.push();
      matrices.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
      drawLine(
          buffer,
          matrices.peek().getPositionMatrix(),
          end.subtract(start).toVector3f(),
          lightAt(world, start),
          lightAt(world, end));
      matrices.pop();
    }
  }

  private static void drawLine(
      final VertexConsumer buffer,
      final Matrix4f matrix,
      final Vector3f span,
      final int startLight,
      final int endLight) {
    final float scale = MathHelper.inverseSqrt(span.x * span.x + span.z * span.z) * WIDTH / 2.0f;
    final float offsetX = span.z * scale;
    final float offsetZ = span.x * scale;
    for (int segment = 0; segment <= HarnessLineShape.SEGMENTS; segment++) {
      vertexPair(
          buffer,
          matrix,
          span,
          startLight,
          endLight,
          segment,
          offsetX,
          offsetZ,
          WIDTH,
          WIDTH,
          false);
    }
    for (int segment = HarnessLineShape.SEGMENTS; segment >= 0; segment--) {
      vertexPair(
          buffer, matrix, span, startLight, endLight, segment, offsetX, offsetZ, WIDTH, 0.0f, true);
    }
  }

  private static void vertexPair(
      final VertexConsumer buffer,
      final Matrix4f matrix,
      final Vector3f span,
      final int startLight,
      final int endLight,
      final int segment,
      final float offsetX,
      final float offsetZ,
      final float height,
      final float lift,
      final boolean back) {
    final float t = segment / (float) HarnessLineShape.SEGMENTS;
    final int block =
        (int)
            MathHelper.lerp(
                t,
                LightmapTextureManager.getBlockLightCoordinates(startLight),
                LightmapTextureManager.getBlockLightCoordinates(endLight));
    final int sky =
        (int)
            MathHelper.lerp(
                t,
                LightmapTextureManager.getSkyLightCoordinates(startLight),
                LightmapTextureManager.getSkyLightCoordinates(endLight));
    final int light = LightmapTextureManager.pack(block, sky);
    final float shade = segment % 2 == (back ? 1 : 0) ? SHADE : 1.0f;
    final Vector3f point = HarnessLineShape.pointAt(span, segment);
    buffer
        .vertex(matrix, point.x - offsetX, point.y + lift, point.z + offsetZ)
        .color(RED * shade, GREEN * shade, BLUE * shade, 1.0f)
        .light(light);
    buffer
        .vertex(matrix, point.x + offsetX, point.y + height - lift, point.z - offsetZ)
        .color(RED * shade, GREEN * shade, BLUE * shade, 1.0f)
        .light(light);
  }

  private static int lightAt(final ClientWorld world, final Vec3d point) {
    final BlockPos pos = BlockPos.ofFloored(point);
    return LightmapTextureManager.pack(
        world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos));
  }
}
