package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.render.bend.BendablePart;
import com.grahambartley.dogsunleashed.render.bend.BentCuboids;
import java.util.List;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements BendablePart {

  @Shadow @Final private List<ModelPart.Cuboid> cuboids;

  @Unique private float dogsUnleashed$joint;
  @Unique private float dogsUnleashed$angle;

  @Override
  public void dogsUnleashed$setBend(final float joint, final float angle) {
    this.dogsUnleashed$joint = joint;
    this.dogsUnleashed$angle = angle;
  }

  @Override
  public float dogsUnleashed$bendJoint() {
    return this.dogsUnleashed$joint;
  }

  @Override
  public float dogsUnleashed$bendAngle() {
    return this.dogsUnleashed$angle;
  }

  @Inject(method = "copyTransform", at = @At("TAIL"))
  private void dogsUnleashed$copyBend(final ModelPart part, final CallbackInfo info) {
    final BendablePart from = BendablePart.of(part);
    this.dogsUnleashed$setBend(from.dogsUnleashed$bendJoint(), from.dogsUnleashed$bendAngle());
  }

  @Inject(method = "renderCuboids", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$renderBent(
      final MatrixStack.Entry entry,
      final VertexConsumer vertices,
      final int light,
      final int overlay,
      final int color,
      final CallbackInfo info) {
    if (this.dogsUnleashed$angle != 0.0f) {
      BentCuboids.render(
          entry,
          this.cuboids,
          vertices,
          light,
          overlay,
          color,
          this.dogsUnleashed$joint,
          this.dogsUnleashed$angle);
      info.cancel();
    }
  }
}
