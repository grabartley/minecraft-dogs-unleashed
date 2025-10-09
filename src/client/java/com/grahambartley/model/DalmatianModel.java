package com.grahambartley.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.WolfEntityModel;

public class DalmatianModel extends WolfEntityModel {

  public DalmatianModel(ModelPart root) {
    super(root);
  }

  public static TexturedModelData getTexturedModelData() {
    // TODO: Replace this with the actual model data exported from Blockbench
    // For now, using vanilla wolf model as placeholder
    ModelData modelData = WolfEntityModel.getTexturedModelData(Dilation.NONE);
    return TexturedModelData.of(modelData, 64, 32);
  }
}
