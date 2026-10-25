package com.grahambartley.dogsunleashed.render.rig;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.FileLoader;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public final class PrivateRigBones {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "private_rig_bones");

  private static final PrivateRigBones SHARED = new PrivateRigBones(PrivateRigBones::bake);

  private final Function<Identifier, BakedGeoModel> baker;
  private final Map<Identifier, BakedGeoModel> copies = new HashMap<>();

  public PrivateRigBones(final Function<Identifier, BakedGeoModel> baker) {
    this.baker = baker;
  }

  public static PrivateRigBones shared() {
    return SHARED;
  }

  public BakedGeoModel of(final Identifier rig) {
    return this.copies.computeIfAbsent(rig, this.baker);
  }

  public void clear() {
    this.copies.clear();
  }

  private static BakedGeoModel bake(final Identifier rig) {
    return BakedModelFactory.getForNamespace(rig.getNamespace())
        .constructGeoModel(
            GeometryTree.fromModel(
                FileLoader.loadModelFile(rig, MinecraftClient.getInstance().getResourceManager())));
  }
}
