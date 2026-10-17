package com.grahambartley.dogsunleashed.render.armour;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class ArmourTextures {

  private static final Map<BakedGeoModel, Map<String, Identifier>> BY_MODEL =
      new MapMaker().weakKeys().makeMap();

  private ArmourTextures() {}

  public static Identifier layer(
      final BakedGeoModel model,
      final UnleashedDogBreed breed,
      final ArmourLook look,
      final ArmourPainter.Layer layer,
      final boolean trimDyed) {
    final String name =
        look.name().toLowerCase(Locale.ROOT)
            + "_"
            + layer.name().toLowerCase(Locale.ROOT)
            + (trimDyed ? "_trim" : "");
    return cached(
        model,
        breed,
        name,
        (cubes, width, height) -> ArmourPainter.paint(cubes, width, height, look, layer, trimDyed));
  }

  public static Identifier cracks(
      final BakedGeoModel model, final UnleashedDogBreed breed, final int level) {
    return cached(
        model,
        breed,
        "cracks_" + level,
        (cubes, width, height) -> ArmourPainter.cracks(cubes, width, height, level));
  }

  public static int crackLevel(final ItemStack worn) {
    return switch (Cracks.WOLF_ARMOR.getCrackLevel(worn)) {
      case NONE -> 0;
      case LOW -> 1;
      case MEDIUM -> 2;
      case HIGH -> 3;
    };
  }

  @FunctionalInterface
  private interface Painter {
    int[] paint(List<List<CubeGeometry.Face>> cubes, int width, int height);
  }

  private static Identifier cached(
      final BakedGeoModel model,
      final UnleashedDogBreed breed,
      final String name,
      final Painter painter) {
    return BY_MODEL
        .computeIfAbsent(model, key -> new HashMap<>())
        .computeIfAbsent(name, key -> register(model, breed, name, painter));
  }

  private static Identifier register(
      final BakedGeoModel model,
      final UnleashedDogBreed breed,
      final String name,
      final Painter painter) {
    final int width = (int) model.properties().textureWidth();
    final int height = (int) model.properties().textureHeight();
    return upload(
        Identifier.of(MOD_ID, "armour/" + breed.serializedId() + "/" + name),
        painter.paint(torsoCubes(model, breed), width, height),
        width,
        height);
  }

  static Identifier upload(
      final Identifier id, final int[] argb, final int width, final int height) {
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, true);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.setColor(x, y, abgr(argb[y * width + x]));
      }
    }
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    return id;
  }

  private static List<List<CubeGeometry.Face>> torsoCubes(
      final BakedGeoModel model, final UnleashedDogBreed breed) {
    final List<List<CubeGeometry.Face>> cubes = new ArrayList<>();
    collect(model.topLevelBones(), breed, cubes);
    return cubes;
  }

  private static void collect(
      final List<GeoBone> bones,
      final UnleashedDogBreed breed,
      final List<List<CubeGeometry.Face>> out) {
    for (final GeoBone bone : bones) {
      if (ArmourFit.torsoBones(breed).contains(bone.getName())) {
        final List<List<CubeGeometry.Face>> faces = CubeGeometry.facesByCube(bone);
        for (int i = 0; i < faces.size(); i++) {
          if (ArmourFit.covers(breed, bone.getName(), i)) {
            out.add(faces.get(i));
          }
        }
      }
      collect(bone.getChildBones(), breed, out);
    }
  }

  private static int abgr(final int argb) {
    return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
  }
}
