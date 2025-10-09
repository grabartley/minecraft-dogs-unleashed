package com.grahambartley.render;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.DalmatianEntity;
import com.grahambartley.model.DalmatianModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.util.Identifier;

public class DalmatianRenderer
    extends MobEntityRenderer<DalmatianEntity, WolfEntityRenderState, DalmatianModel> {

  public DalmatianRenderer(EntityRendererFactory.Context context) {
    super(context, new DalmatianModel(context.getPart(ModModelLayers.DALMATIAN)), 0.5f);
  }

  @Override
  public WolfEntityRenderState createRenderState() {
    return new WolfEntityRenderState();
  }

  @Override
  public Identifier getTexture(WolfEntityRenderState state) {
    return Identifier.of(MOD_ID, "textures/entity/dalmatian.png");
  }
}
