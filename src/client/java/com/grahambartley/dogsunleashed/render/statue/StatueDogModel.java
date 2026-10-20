package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.PosedDogModel;
import net.minecraft.util.Identifier;

public final class StatueDogModel extends PosedDogModel {

  private StatueMaterial material = StatueMaterial.STONE;

  void frame(final StatueMaterial material, final double poseTick, final boolean starting) {
    this.material = material;
    this.frame(poseTick, starting);
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    return StatueCoatTextures.statueOf(super.getTextureResource(animatable), this.material);
  }
}
