package com.grahambartley.dogsunleashed.render.bend;

import net.minecraft.client.model.ModelPart;

public interface BendablePart {

  static BendablePart of(final ModelPart part) {
    return (BendablePart) (Object) part;
  }

  void dogsUnleashed$setBend(float joint, float angle);

  float dogsUnleashed$bendJoint();

  float dogsUnleashed$bendAngle();
}
